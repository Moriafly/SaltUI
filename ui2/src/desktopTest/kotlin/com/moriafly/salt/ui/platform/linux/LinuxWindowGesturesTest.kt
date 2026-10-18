/*
 * SaltUI
 * Copyright (C) 2026 Moriafly
 * Licensed under the Apache License, Version 2.0
 */

@file:OptIn(
    androidx.compose.ui.InternalComposeUiApi::class,
    com.moriafly.salt.ui.UnstableSaltUiApi::class
)

package com.moriafly.salt.ui.platform.linux

import androidx.compose.ui.geometry.Offset
import com.moriafly.salt.ui.platform.input.DesktopTouchScene
import com.moriafly.salt.ui.platform.linux.input.LinuxTouchInput
import com.moriafly.salt.ui.platform.linux.x11.LinuxXInputSession
import com.moriafly.salt.ui.platform.linux.x11.LinuxXInputSession.TouchPhase
import com.moriafly.salt.ui.util.findSkiaLayer
import com.moriafly.salt.ui.window.WindowResizeEdge
import java.awt.Frame
import java.awt.event.InputEvent
import java.awt.event.MouseEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Standard desktopTest integration tests: inject samples at the platform adapter and assert real
 * SaltWindow bounds and Compose clicks. These do not emulate a native touchscreen or compositor.
 */
class LinuxWindowGesturesTest {
    @Test
    fun mouseReleaseDuringTouchHoldCommitsResizeAndRestoresTouchClicks() = withWindow { touch ->
        val before = onEdt { window.bounds }
        val edgePoint = onEdt { Offset(content.right - 2f, content.center.y) }
        onEdt {
            val scale = window.graphicsConfiguration.defaultTransform.scaleX.toFloat()
            assertTrue(gestures.onMouseEvent(mouse(MouseEvent.MOUSE_PRESSED, edgePoint)))
            assertTrue(gestures.onMouseEvent(mouse(MouseEvent.MOUSE_DRAGGED, edgePoint + Offset(40f * scale, 0f))))
            touch.send(TouchPhase.Down, content.center)
            assertTrue(gestures.onMouseEvent(mouse(MouseEvent.MOUSE_DRAGGED, edgePoint + Offset(100f * scale, 0f))))
            assertTrue(gestures.onMouseEvent(mouse(MouseEvent.MOUSE_RELEASED, edgePoint + Offset(100f * scale, 0f))))
            assertEquals(before.width + 100, window.width, "Owning mouse release must apply pending bounds")
            touch.send(TouchPhase.Up, content.center)
            assertEquals(WindowResizeEdge.None, edge)
        }
        await("committed resize layout") {
            window.width == before.width + 100 &&
                content.width == (window.width * window.graphicsConfiguration.defaultTransform.scaleX).toFloat()
        }
        val resized = onEdt { window.bounds }
        touch.tap(onEdt { content.center })
        await("fresh Compose touch click after mouse release") { clicks == 1 }
        onEdt { assertEquals(resized, window.bounds, "The old drag must not resume") }
    }

    @Test
    fun touchCaptionDoubleTapMaximizesAndRestoresWindow() = withWindow { touch ->
        val before = onEdt { window.bounds }
        val captionWidth = onEdt { caption.width }
        touch.doubleTap(onEdt { Offset(content.center.x, content.top + 2f) })
        onEdt { assertEquals(Frame.NORMAL, window.extendedState, "Content is not a caption target") }
        touch.doubleTap(onEdt { caption.center })
        await("caption maximize and layout") {
            window.extendedState == Frame.MAXIMIZED_BOTH && caption.width > captionWidth
        }
        touch.doubleTap(onEdt { caption.center })
        await("caption restore") { window.extendedState == Frame.NORMAL && window.bounds == before }
    }

    @Test
    fun touchResizeCancelDiscardsPendingBoundsWithoutAffectingNextResize() = withWindow { touch ->
        val before = onEdt { window.bounds }
        onEdt {
            val point = Offset(content.right - 2f, content.center.y)
            val scale = window.graphicsConfiguration.defaultTransform.scaleX.toFloat()
            // Keep update and cancel in one EDT turn so the queued bounds update cannot run first.
            touch.send(TouchPhase.Down, point)
            assertEquals(WindowResizeEdge.Right, edge)
            touch.send(TouchPhase.Move, point + Offset(100f * scale, 0f))
            gestures.cancel()
            touch.send(TouchPhase.Up, point + Offset(100f * scale, 0f))
            assertEquals(before, window.bounds)
            assertEquals(WindowResizeEdge.None, edge)

            // A normally released resize must still commit, and the old queued task must not
            // overwrite it after the current EDT turn ends.
            touch.send(TouchPhase.Down, point)
            touch.send(TouchPhase.Move, point + Offset(60f * scale, 0f))
            touch.send(TouchPhase.Up, point + Offset(60f * scale, 0f))
            assertEquals(before.width + 60, window.width)
        }
        await("released resize after cancelled queued work") {
            window.width == before.width + 60 &&
                content.width == (window.width * window.graphicsConfiguration.defaultTransform.scaleX).toFloat()
        }
        touch.tap(onEdt { content.center })
        await("Compose touch click after cancellation") { clicks == 1 }
    }

    private fun withWindow(block: LinuxInputWindow.(TouchDriver) -> Unit) {
        LinuxInputWindow().use { fixture ->
            fixture.open()
            fixture.block(TouchDriver(fixture))
        }
    }

    private fun LinuxInputWindow.mouse(id: Int, position: Offset): MouseEvent {
        val canvas = checkNotNull(window.findSkiaLayer()).canvas
        val scale = window.graphicsConfiguration.defaultTransform
        val screen = screen(position)
        val dragging = id == MouseEvent.MOUSE_DRAGGED
        return MouseEvent(
            canvas,
            id,
            System.currentTimeMillis(),
            if (dragging || id == MouseEvent.MOUSE_PRESSED) InputEvent.BUTTON1_DOWN_MASK else 0,
            (position.x / scale.scaleX).toInt(),
            (position.y / scale.scaleY).toInt(),
            (screen.x / scale.scaleX).toInt(),
            (screen.y / scale.scaleY).toInt(),
            1,
            false,
            if (dragging) MouseEvent.NOBUTTON else MouseEvent.BUTTON1
        )
    }

    private class TouchDriver(private val fixture: LinuxInputWindow) {
        private var id = 0L
        private var time = 0L
        private val input = fixture.onEdt {
            LinuxTouchInput(
                canvas = checkNotNull(fixture.window.findSkiaLayer()).canvas,
                target = checkNotNull(DesktopTouchScene.create(fixture.window)),
                gestures = fixture.gestures,
                focus = {}
            )
        }

        fun send(phase: TouchPhase, position: Offset) = fixture.onEdt {
            if (phase == TouchPhase.Down) id++
            time += 80
            val screen = fixture.screen(position)
            assertTrue(
                input.onTouch(
                    LinuxXInputSession.Touch(
                        id = id,
                        phase = phase,
                        x = position.x.toDouble(),
                        y = position.y.toDouble(),
                        rootX = screen.x.toDouble(),
                        rootY = screen.y.toDouble(),
                        modifiers = 0,
                        timeMillis = time,
                        x11Timestamp = 0,
                        directTouch = true
                    )
                )
            )
        }

        fun tap(position: Offset) {
            send(TouchPhase.Down, position)
            send(TouchPhase.Up, position)
        }

        fun doubleTap(position: Offset) {
            tap(position)
            tap(position)
        }
    }
}
