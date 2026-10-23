/*
 * SaltUI
 * Copyright (C) 2026 Moriafly
 * Licensed under the Apache License, Version 2.0
 */

@file:OptIn(
    androidx.compose.ui.test.ExperimentalTestApi::class,
    androidx.compose.ui.ExperimentalComposeUiApi::class,
    com.moriafly.salt.ui.UnstableSaltUiApi::class
)

package com.moriafly.salt.ui.platform.linux

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.window.Window
import com.moriafly.salt.ui.platform.linux.input.LinuxMouseInput
import com.moriafly.salt.ui.platform.linux.input.LinuxWindowGestures
import com.moriafly.salt.ui.platform.linux.input.LinuxWindowInput
import com.moriafly.salt.ui.platform.linux.window.LinuxCaptionBarHitTest
import com.moriafly.salt.ui.platform.linux.window.LinuxWindowGestureEffects
import com.moriafly.salt.ui.platform.linux.window.UndecoratedWindowResizer
import com.moriafly.salt.ui.platform.linux.x11.LinuxXInputSession
import com.moriafly.salt.ui.util.findSkiaLayer
import org.junit.Assume.assumeTrue
import java.awt.Dialog
import java.awt.EventQueue
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.awt.event.InputEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Exercises the real window delivery path with samples injected at the native callback / AWT
 * source boundary. Native connection negotiation and hardware delivery are outside these tests.
 */
class LinuxWindowInputTest {
    @Test
    fun applicationModalBlocksNativeOwnerHoverAndClosingRestoresIt() = withWindow { fixture ->
        var input: LinuxWindowInput? = null
        val dialog = runOnUiThread {
            Dialog(fixture.window, "Modal input regression", Dialog.ModalityType.APPLICATION_MODAL).apply {
                setBounds(fixture.window.x + fixture.window.width + 20, fixture.window.y, 200, 120)
            }
        }
        try {
            runOnUiThread {
                input = assertNotNull(LinuxWindowInput.create(fixture.window, fixture.gestures, null))
                fixture.received.clear()
                fixture.hover(assertNotNull(input))
                assertTrue(MouseEvent.MOUSE_ENTERED in fixture.received)
                assertTrue(MouseEvent.MOUSE_MOVED in fixture.received)
            }
            EventQueue.invokeLater { dialog.isVisible = true }
            waitUntil(timeoutMillis = 5_000) { dialog.isShowing }
            runOnUiThread {
                fixture.received.clear()
                fixture.hover(assertNotNull(input))
                assertTrue(fixture.received.isEmpty(), "Blocked owner received hover: ${fixture.received}")
                dialog.dispose()
                fixture.hover(assertNotNull(input))
                assertTrue(MouseEvent.MOUSE_ENTERED in fixture.received)
                assertTrue(MouseEvent.MOUSE_MOVED in fixture.received)
            }
        } finally {
            runOnUiThread {
                dialog.dispose()
                input?.close()
            }
        }
    }

    @Test
    fun unavailableXi2RestoresAwtHoverClickWheelAndDragDelivery() = withWindow { fixture ->
        runOnUiThread {
            // Window setup can deliver crossings before the native source is attached.
            fixture.canvas.dispatchEvent(fixture.mouse(MouseEvent.MOUSE_ENTERED))
            assertTrue(MouseEvent.MOUSE_ENTERED in fixture.received)
            LinuxMouseInput(fixture.window, fixture.canvas).use { source ->
                // Start measuring in this EDT turn so setup events cannot pollute the result.
                fixture.received.clear()
                val hover = fixture.mouse(MouseEvent.MOUSE_MOVED)
                source.dispatchAwt(hover, fixture.canvas::dispatchEvent)
                assertTrue(fixture.received.isEmpty(), "AWT hover waits for native negotiation: ${fixture.received}")

                // This is the capability result published by LinuxWindowInput.onClosed after
                // native initialization fails. Do not fake or replace a system libXi library.
                source.updateNativeCapabilities(LinuxXInputSession.Capabilities(touch = false, hover = false))
                val expected = listOf(
                    MouseEvent.MOUSE_ENTERED,
                    MouseEvent.MOUSE_MOVED,
                    MouseEvent.MOUSE_PRESSED,
                    MouseEvent.MOUSE_DRAGGED,
                    MouseEvent.MOUSE_RELEASED,
                    MouseEvent.MOUSE_CLICKED,
                    MouseEvent.MOUSE_WHEEL,
                    MouseEvent.MOUSE_EXITED
                )
                expected.forEach { id ->
                    source.dispatchAwt(fixture.mouse(id)) { event ->
                        if (event.id == MouseEvent.MOUSE_RELEASED) {
                            assertFalse(event.modifiersEx and InputEvent.BUTTON1_DOWN_MASK != 0)
                        }
                        fixture.canvas.dispatchEvent(event)
                    }
                }
                assertEquals(expected, fixture.received)
            }
        }
    }

    private fun withWindow(block: DesktopComposeUiTest.(InputFixture) -> Unit) {
        assumeTrue(System.getProperty("os.name").startsWith("Linux"))
        assumeTrue(!GraphicsEnvironment.isHeadless())
        assumeTrue(Toolkit.getDefaultToolkit().javaClass.name == "sun.awt.X11.XToolkit")
        runDesktopComposeUiTest {
            lateinit var window: ComposeWindow
            setContent {
                Window(onCloseRequest = {}, title = "SaltUI input delivery regression") {
                    window = this.window
                    Box(Modifier.fillMaxSize().testTag("input-window"))
                }
            }
            onNodeWithTag("input-window").assertExists()
            val fixture = runOnUiThread { InputFixture(window) }
            try {
                block(fixture)
            } finally {
                runOnUiThread { fixture.close() }
            }
        }
    }

    private class InputFixture(val window: ComposeWindow) : AutoCloseable {
        val canvas = checkNotNull(window.findSkiaLayer()).canvas
        val received = mutableListOf<Int>()
        private val listener = object : MouseAdapter() {
            override fun mouseEntered(event: MouseEvent) { received += event.id }
            override fun mouseExited(event: MouseEvent) { received += event.id }
            override fun mouseMoved(event: MouseEvent) { received += event.id }
            override fun mousePressed(event: MouseEvent) { received += event.id }
            override fun mouseDragged(event: MouseEvent) { received += event.id }
            override fun mouseReleased(event: MouseEvent) { received += event.id }
            override fun mouseClicked(event: MouseEvent) { received += event.id }
            override fun mouseWheelMoved(event: MouseWheelEvent) { received += event.id }
        }
        private val regions = UndecoratedWindowResizer {}
        val gestures = LinuxWindowGestures(
            window = window,
            caption = LinuxCaptionBarHitTest(),
            resizer = regions,
            captionHovered = mutableStateOf(false),
            effects = LinuxWindowGestureEffects(window, regions)
        )
        private val uninstall = gestures.install()

        init {
            gestures.configure(
                moveable = false,
                resizable = false,
                canDrag = false,
                canMaximize = false,
                viewConfiguration = object : ViewConfiguration {
                    override val longPressTimeoutMillis = 500L
                    override val doubleTapTimeoutMillis = 300L
                    override val doubleTapMinTimeMillis = 40L
                    override val touchSlop = 8f
                }
            )
            canvas.addMouseListener(listener)
            canvas.addMouseMotionListener(listener)
            canvas.addMouseWheelListener(listener)
        }

        fun hover(input: LinuxWindowInput) {
            // Select the callback's source explicitly; readiness of the host XI2 reader is not
            // part of this deterministic modality test. All calls happen in the same EDT turn.
            input.onReady(LinuxXInputSession.Capabilities(touch = false, hover = true))
            val origin = canvas.locationOnScreen
            val scale = canvas.graphicsConfiguration.defaultTransform
            for (phase in listOf(LinuxXInputSession.HoverPhase.Enter, LinuxXInputSession.HoverPhase.Move)) {
                input.onHover(
                    LinuxXInputSession.Hover(
                        phase = phase,
                        x = 20.0,
                        y = 20.0,
                        rootX = origin.x * scale.scaleX + 20,
                        rootY = origin.y * scale.scaleY + 20,
                        modifiers = 0,
                        directTouch = false,
                        emulated = false
                    )
                )
            }
        }

        fun mouse(id: Int): MouseEvent {
            if (id == MouseEvent.MOUSE_WHEEL) {
                return MouseWheelEvent(
                    canvas,
                    id,
                    0,
                    0,
                    20,
                    20,
                    0,
                    false,
                    MouseWheelEvent.WHEEL_UNIT_SCROLL,
                    3,
                    1
                )
            }
            val button = if (id in listOf(MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_RELEASED, MouseEvent.MOUSE_CLICKED)) {
                MouseEvent.BUTTON1
            } else {
                MouseEvent.NOBUTTON
            }
            return MouseEvent(
                canvas,
                id,
                0,
                InputEvent.BUTTON1_DOWN_MASK,
                20,
                20,
                0,
                false,
                button
            )
        }

        override fun close() {
            canvas.removeMouseListener(listener)
            canvas.removeMouseMotionListener(listener)
            canvas.removeMouseWheelListener(listener)
            uninstall()
        }
    }
}
