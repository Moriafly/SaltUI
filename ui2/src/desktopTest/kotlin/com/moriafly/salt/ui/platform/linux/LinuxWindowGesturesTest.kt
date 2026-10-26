/*
 * SaltUI
 * Copyright (C) 2026 Moriafly
 * Licensed under the Apache License, Version 2.0
 */

@file:OptIn(
    androidx.compose.ui.InternalComposeUiApi::class,
    androidx.compose.ui.ExperimentalComposeUiApi::class,
    androidx.compose.runtime.tooling.ComposeToolingApi::class,
    com.moriafly.salt.ui.UnstableSaltUiApi::class
)

package com.moriafly.salt.ui.platform.linux

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.platform.input.DesktopTouchScene
import com.moriafly.salt.ui.platform.linux.input.LinuxTouchInput
import com.moriafly.salt.ui.platform.linux.input.LinuxWindowGestures
import com.moriafly.salt.ui.platform.linux.x11.LinuxX11
import com.moriafly.salt.ui.platform.linux.x11.LinuxXInputSession
import com.moriafly.salt.ui.platform.linux.x11.LinuxXInputSession.TouchPhase
import com.moriafly.salt.ui.util.findSkiaLayer
import com.moriafly.salt.ui.window.WindowResizeEdge
import com.sun.jna.NativeLong
import com.sun.jna.Pointer
import com.sun.jna.platform.unix.X11
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.NativeLongByReference
import com.sun.jna.ptr.PointerByReference
import java.awt.Frame
import java.awt.Point
import java.awt.Robot
import java.awt.Toolkit
import java.awt.event.InputEvent
import java.awt.event.MouseEvent
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.AfterClass
import org.junit.Assume.assumeTrue
import org.junit.BeforeClass

/**
 * Mouse tests use native pointer input through the complete window dispatch path. Touch tests
 * inject at the platform adapter and assert real SaltWindow bounds and Compose clicks; they do
 * not emulate a physical touchscreen. Both routes use the same production gesture implementation.
 */
class LinuxWindowGesturesTest {
    companion object {
        private var previousUnchangedMoveEvents = false

        @BeforeClass
        @JvmStatic
        fun enableUnchangedMoveEvents() {
            previousUnchangedMoveEvents =
                ComposeUiFlags.isTriggerMoveEventsWhenLocationHasNotChangedEnabled
            // Compose 1.12.x can retain a filtered stationary mouse Move in HitPathTracker,
            // causing the next touch Press to include stale mouse state and lose the click.
            // Process unchanged moves, as 1.13.0-beta01 does by default, until Compose is upgraded.
            ComposeUiFlags.isTriggerMoveEventsWhenLocationHasNotChangedEnabled = true
        }

        @AfterClass
        @JvmStatic
        fun restoreUnchangedMoveEvents() {
            ComposeUiFlags.isTriggerMoveEventsWhenLocationHasNotChangedEnabled =
                previousUnchangedMoveEvents
        }
    }

    @Test
    fun nativeMouseCaptionDragMovesWindowAndReleaseStopsIt() = withWindow {
        NativeMouse(this).use { mouse ->
            val before = onEdt { window.bounds }
            val start = mouse.screenPoint(onEdt { caption.center })
            mouse.move(start)
            mouse.press()
            mouse.move(Point(start.x + 30, start.y + 20))
            mouse.settle()
            mouse.move(Point(start.x + 110, start.y + 70))
            mouse.release()
            // Openbox reports a WM move to the client only when the move ends.
            await("window moved by the drag") {
                abs(window.x - before.x - 110) <= 1 && abs(window.y - before.y - 70) <= 1
            }
            val released = onEdt { window.bounds }
            mouse.move(Point(start.x + 160, start.y + 100))
            mouse.settle()
            onEdt { assertEquals(released, window.bounds, "Released drag must not follow the pointer") }
        }
    }

    @Test
    fun nativeDialogCaptionRespectsTopmostDialogAndRestoresAfterEachDismissal() {
        var depth by mutableStateOf(0)
        var lower = Rect.Zero
        var upper = Rect.Zero
        var lowerClicks = 0
        var upperClicks = 0
        val properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            usePlatformInsets = false,
            animateTransition = false
        )
        LinuxInputWindowTest(asDialog = true) {
            if (depth > 0) {
                Dialog(onDismissRequest = {}, properties = properties) {
                    Box(
                        Modifier
                            .width(260.dp)
                            .fillMaxHeight()
                            .onGloballyPositioned { lower = it.boundsInWindow() }
                    ) {
                        Text(
                            text = "Close lower dialog",
                            modifier = Modifier.align(Alignment.Center).clickable {
                                lowerClicks++
                                depth = 0
                            }
                        )
                    }
                    if (depth > 1) {
                        Dialog(onDismissRequest = {}, properties = properties) {
                            Box(
                                Modifier
                                    .width(120.dp)
                                    .fillMaxHeight()
                                    .onGloballyPositioned { upper = it.boundsInWindow() }
                            ) {
                                Text(
                                    text = "Close top dialog",
                                    modifier = Modifier.align(Alignment.Center).clickable {
                                        upperClicks++
                                        depth = 1
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }.use { fixture ->
            fixture.open()
            val dialog = fixture.window as ComposeDialog
            val baseOwners = fixture.onEdt { dialog.semanticsOwners.size }
            NativeMouse(fixture).use { mouse ->
                fun dragCaptionAt(
                    x: Float,
                    moves: Boolean,
                    description: String
                ) {
                    val before = fixture.onEdt { dialog.bounds }
                    val start = mouse.screenPoint(fixture.onEdt { Offset(x, fixture.caption.center.y) })
                    mouse.move(start)
                    mouse.press()
                    mouse.move(Point(start.x + 20, start.y))
                    mouse.settle()
                    mouse.move(Point(start.x + 60, start.y))
                    mouse.release()
                    mouse.settle()
                    fixture.onEdt {
                        assertEquals(before.x + if (moves) 60 else 0, dialog.x, description)
                        assertEquals(before.y, dialog.y, description)
                        assertEquals(before.size, dialog.size, description)
                    }
                }

                dragCaptionAt(fixture.onEdt { fixture.caption.center.x }, true, "Native dialog's own owner")
                fixture.onEdt { depth = 2 }
                fixture.await("two nested dialog owners and content") {
                    dialog.semanticsOwners.size >= baseOwners + 2 && lower != Rect.Zero && upper != Rect.Zero
                }
                fixture.onEdt {
                    assertTrue(lower.contains(Offset(lower.left + 4 * fixture.density, fixture.caption.center.y)))
                    assertTrue(upper.contains(Offset(upper.center.x, fixture.caption.center.y)))
                    assertTrue(lower.left < upper.left, "Lower dialog must extend outside the top dialog")
                }
                dragCaptionAt(upper.center.x, false, "Topmost dialog content blocks caption dragging")
                val lowerOnly = fixture.onEdt { lower.left + 4 * fixture.density }
                dragCaptionAt(lowerOnly, true, "Only the topmost dialog controls caption exclusion")
                mouse.move(mouse.screenPoint(fixture.onEdt { upper.center }))
                mouse.press()
                mouse.release()
                fixture.await("upper dialog dismissal restores lower owner") {
                    upperClicks == 1 && lowerClicks == 0 && dialog.semanticsOwners.size == baseOwners + 1
                }
                dragCaptionAt(lowerOnly, false, "Remaining dialog blocks its own content")
                mouse.move(mouse.screenPoint(fixture.onEdt { lower.center }))
                mouse.press()
                mouse.release()
                fixture.await("all overlay dialogs dismissed") {
                    lowerClicks == 1 && dialog.semanticsOwners.size == baseOwners
                }
                dragCaptionAt(lowerOnly, true, "Caption dragging resumes after both dialogs close")
            }
        }
    }

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
        onEdt { assertEquals(Frame.NORMAL, frame.extendedState, "Content is not a caption target") }
        touch.doubleTap(onEdt { caption.center })
        await("caption maximize and layout") {
            frame.extendedState == Frame.MAXIMIZED_BOTH && caption.width > captionWidth
        }
        touch.doubleTap(onEdt { caption.center })
        await("caption restore") { frame.extendedState == Frame.NORMAL && window.bounds == before }
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

    @Test
    fun nativeMouseCaptionBottomBoundaryExcludesContent() = withWindow {
        NativeMouse(this).use { mouse ->
            for (offset in listOf(-1, 0, 1)) {
                val before = onEdt { window.bounds }
                val point = mouse.screenPoint(onEdt {
                    Offset(caption.center.x, caption.bottom + offset * systemScale)
                })
                mouse.move(point)
                mouse.press()
                mouse.move(Point(point.x + 20, point.y))
                mouse.settle()
                mouse.move(Point(point.x + 60, point.y))
                mouse.release()
                mouse.settle()
                onEdt {
                    assertEquals(before.x + if (offset < 0) 60 else 0, window.x, "Caption boundary offset $offset")
                    assertEquals(before.y, window.y)
                    assertEquals(before.size, window.size)
                }
            }
        }
    }

    @Test
    fun nativeMouseDoubleClickOnlyMaximizesCaptionAndRestoresBounds() = withWindow {
        assumeTrue(Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH))
        NativeMouse(this).use { mouse ->
            val before = onEdt { window.bounds }
            mouse.doubleClick(mouse.screenPoint(onEdt { content.center }))
            onEdt { assertEquals(Frame.NORMAL, frame.extendedState, "Content double click must not maximize") }
            mouse.doubleClick(mouse.screenPoint(onEdt { caption.center }))
            await("mouse caption maximize") {
                frame.extendedState == Frame.MAXIMIZED_BOTH && window.width > before.width
            }
            mouse.doubleClick(mouse.screenPoint(onEdt { caption.center }))
            await("mouse caption restore") { frame.extendedState == Frame.NORMAL && window.bounds == before }
        }
    }

    @Test
    fun nativeMouseDragFromMaximizedCaptionKeepsPointerInsideRestoredCaption() = withWindow {
        assumeTrue(Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH))
        // Restoring a maximized window on move is WM policy; Openbox keeps it maximized.
        assumeTrue(
            "Requires a WM that restores maximized windows on move",
            windowManagerName().equals("KWin", ignoreCase = true)
        )
        NativeMouse(this).use { mouse ->
            val before = onEdt { window.bounds }
            mouse.doubleClick(mouse.screenPoint(onEdt { caption.center }))
            await("maximized caption layout") {
                frame.extendedState == Frame.MAXIMIZED_BOTH && caption.width > before.width * systemScale
            }
            val start = mouse.screenPoint(onEdt {
                Offset(caption.left + caption.width / 4, caption.center.y)
            })
            mouse.move(start)
            mouse.press()
            mouse.move(Point(start.x + 120, start.y + 120))
            mouse.move(Point(start.x + 240, start.y + 240))
            mouse.settle()
            val held = Point(start.x + 248, start.y + 248)
            mouse.move(held)
            await("restored window under pointer") {
                val topLeft = mouse.screenPoint(caption.topLeft)
                val bottomRight = mouse.screenPoint(caption.bottomRight)
                frame.extendedState == Frame.NORMAL && window.size == before.size &&
                    held.x in topLeft.x until bottomRight.x && held.y in topLeft.y until bottomRight.y
            }
            val restored = onEdt { window.location }
            mouse.move(Point(held.x + 60, held.y + 30))
            await("restored window following pointer") {
                abs(window.x - restored.x - 60) <= 2 && abs(window.y - restored.y - 30) <= 2
            }
            mouse.release()
            val released = onEdt { window.bounds }
            mouse.move(Point(held.x + 100, held.y + 60))
            mouse.settle()
            onEdt { assertEquals(released, window.bounds) }
        }
    }

    @Test
    fun nativeMouseResizeIncludesFourDpBoundaryButExcludesFiveDp() = withWindow {
        NativeMouse(this).use { mouse ->
            for (distance in listOf(3, 4, 5)) {
                val before = onEdt { window.bounds }
                val point = mouse.screenPoint(onEdt {
                    Offset(content.right - distance * density, content.center.y)
                })
                mouse.move(point)
                mouse.press()
                mouse.move(Point(point.x + 20, point.y))
                mouse.move(Point(point.x + 60, point.y))
                mouse.release()
                mouse.settle()
                onEdt {
                    assertEquals(before.width + if (distance <= 4) 60 else 0, window.width, "Resize at $distance dp")
                    assertEquals(before.location, window.location)
                }
                val expectedWidth = before.width + if (distance <= 4) 60 else 0
                await("resize boundary layout") {
                    window.width == expectedWidth && content.width == expectedWidth * systemScale
                }
            }
        }
    }

    @Test
    fun touchResizeIncludesFourDpBoundaryButExcludesFiveDp() = withWindow { touch ->
        for (distance in listOf(3, 4, 5)) {
            val before = onEdt { window.bounds }
            onEdt {
                val point = Offset(content.right - distance * density, content.center.y)
                touch.send(TouchPhase.Down, point)
                touch.send(TouchPhase.Move, point + Offset(60f * systemScale, 0f))
                touch.send(TouchPhase.Up, point + Offset(60f * systemScale, 0f))
                assertEquals(before.width + if (distance <= 4) 60 else 0, window.width, "Resize at $distance dp")
                assertEquals(before.location, window.location)
            }
            val expectedWidth = before.width + if (distance <= 4) 60 else 0
            await("touch resize boundary layout") {
                window.width == expectedWidth && content.width == expectedWidth * systemScale
            }
        }
    }

    @Test
    fun touchCaptionStartsMovingOnlyAfterExceedingSlop() = withWindow { touch ->
        val before = onEdt { window.bounds }
        onEdt {
            configureGestures(touchSlop = 8f * systemScale)
            val point = caption.center
            touch.send(TouchPhase.Down, point)
            touch.send(TouchPhase.Move, point + Offset(7f * systemScale, 0f))
            assertEquals(before, window.bounds, "Below slop")
            touch.send(TouchPhase.Move, point + Offset(8f * systemScale, 0f))
            assertEquals(before, window.bounds, "Exactly at slop")
            touch.send(TouchPhase.Move, point + Offset(9f * systemScale, 0f))
            assertEquals(before.x + 9, window.x, "The first sample beyond slop starts movement")
            touch.send(TouchPhase.Up, point + Offset(9f * systemScale, 0f))
        }
        val released = onEdt { window.bounds }
        touch.tap(onEdt { content.center })
        await("click after slop gesture") { clicks == 1 }
        onEdt { assertEquals(released, window.bounds) }
    }

    @Test
    fun resettingTouchCancelsMovementAndAllowsFreshClickWithoutOldRelease() = withWindow { touch ->
        onEdt {
            val point = caption.center
            touch.send(TouchPhase.Down, point)
            touch.send(TouchPhase.Move, point + Offset(60f * systemScale, 0f))
            touch.reset()
            val stopped = window.bounds
            touch.send(TouchPhase.Move, point + Offset(120f * systemScale, 0f))
            assertEquals(stopped, window.bounds, "Reset contact cannot keep moving the window")
        }
        touch.tap(onEdt { content.center })
        await("fresh click after session reset") { clicks == 1 }
    }

    @Test
    fun disablingDragDuringTouchHoldStopsMovementUntilFreshPress() = withWindow { touch ->
        val point = onEdt { caption.center }
        val origin = onEdt { checkNotNull(window.findSkiaLayer()).canvas.locationOnScreen }
        val stopped = onEdt {
            touch.send(TouchPhase.Down, point)
            touch.send(TouchPhase.Move, point + Offset(60f * systemScale, 0f))
            configureGestures(canDrag = false)
            val stopped = window.bounds
            touch.send(TouchPhase.Move, point + Offset(120f * systemScale, 0f))
            assertEquals(stopped, window.bounds)
            configureGestures()
            touch.send(TouchPhase.Move, point + Offset(180f * systemScale, 0f))
            assertEquals(stopped, window.bounds, "Reenable must not recapture the old contact")
            touch.send(TouchPhase.Up, point)
            stopped
        }
        // The WM applies and reports a move asynchronously. Starting the fresh contact before the
        // first move lands lets a late report or the shifting native origin leak into it.
        await("first move applied by the WM") {
            checkNotNull(window.findSkiaLayer()).canvas.locationOnScreen ==
                Point(origin.x + 60, origin.y)
        }
        onEdt {
            touch.send(TouchPhase.Down, point)
            touch.send(TouchPhase.Move, point + Offset(60f * systemScale, 0f))
            touch.send(TouchPhase.Up, point)
        }
        await("fresh caption press moving again") { window.x == stopped.x + 60 }
    }

    @Test
    fun disablingResizeDiscardsPendingBoundsAndNextResizeWorks() = withWindow { touch ->
        val before = onEdt { window.bounds }
        onEdt {
            val point = Offset(content.right - 3 * density, content.center.y)
            touch.send(TouchPhase.Down, point)
            touch.send(TouchPhase.Move, point + Offset(60f * systemScale, 0f))
            configureGestures(resizable = false)
            touch.send(TouchPhase.Up, point)
            assertEquals(before, window.bounds, "Disabling resize must discard its queued update")
            configureGestures()
        }
        // Let the disabled/enabled resizer layout settle before a fresh press.
        await("resizer reenabled") { content.width == window.width * systemScale }
        onEdt {
            val point = Offset(content.right - 3 * density, content.center.y)
            touch.send(TouchPhase.Down, point)
            touch.send(TouchPhase.Move, point + Offset(60f * systemScale, 0f))
            touch.send(TouchPhase.Up, point)
            assertEquals(before.width + 60, window.width)
        }
    }

    @Test
    fun secondaryTouchCannotFinishOrTakeOverResizeInEitherReleaseOrder() = withWindow { touch ->
        for (ownerFirst in listOf(true, false)) {
            val before = onEdt { window.bounds }
            onEdt {
                val point = Offset(content.right - 3 * density, content.center.y)
                touch.send(101, TouchPhase.Down, point)
                touch.send(102, TouchPhase.Down, content.center)
                touch.send(101, TouchPhase.Move, point + Offset(30f * systemScale, 0f))
                if (ownerFirst) {
                    touch.send(101, TouchPhase.Up, point)
                    assertEquals(before.width + 30, window.width)
                    touch.send(102, TouchPhase.Move, content.center + Offset(100f * systemScale, 0f))
                    touch.send(102, TouchPhase.Up, content.center)
                    assertEquals(before.width + 30, window.width, "Secondary contact must not take over")
                } else {
                    touch.send(102, TouchPhase.Up, content.center)
                    touch.send(101, TouchPhase.Move, point + Offset(60f * systemScale, 0f))
                    touch.send(101, TouchPhase.Up, point)
                    assertEquals(before.width + 60, window.width, "Secondary release must not stop the owner")
                }
            }
            val expectedWidth = before.width + if (ownerFirst) 30 else 60
            await("multi-touch resize layout") {
                window.width == expectedWidth && content.width == expectedWidth * systemScale
            }
            val previousClicks = onEdt { clicks }
            touch.tap(onEdt { content.center })
            await("click after all contacts release") { clicks == previousClicks + 1 }
        }
    }

    /** Reads the EWMH name of the running window manager, or `null` if none is advertised. */
    private fun windowManagerName(): String? {
        val x11 = LinuxX11.INSTANCE
        val display = x11.XOpenDisplay(null) ?: return null
        try {
            val root = x11.XDefaultRootWindow(display)
            val check = readProperty(display, root, "_NET_SUPPORTING_WM_CHECK") { data, _ ->
                X11.Window(data.getNativeLong(0).toLong())
            } ?: return null
            return readProperty(display, check, "_NET_WM_NAME") { data, count ->
                String(data.getByteArray(0, count), Charsets.UTF_8)
            }
        } finally {
            x11.XCloseDisplay(display)
        }
    }

    private fun <T> readProperty(
        display: X11.Display,
        window: X11.Window,
        name: String,
        read: (Pointer, Int) -> T
    ): T? {
        val x11 = LinuxX11.INSTANCE
        val count = NativeLongByReference()
        val data = PointerByReference()
        val result = x11.XGetWindowProperty(
            display,
            window,
            x11.XInternAtom(display, name, false),
            NativeLong(0),
            NativeLong(1024),
            false,
            X11.Atom(0), // AnyPropertyType
            X11.AtomByReference(),
            IntByReference(),
            count,
            NativeLongByReference(),
            data
        )
        try {
            if (result != X11.Success || count.value.toInt() == 0) return null
            return data.value?.let { read(it, count.value.toInt()) }
        } finally {
            data.value?.let { x11.XFree(it) }
        }
    }

    private fun withWindow(
        block: LinuxInputWindowTest.(TouchDriver) -> Unit
    ) {
        LinuxInputWindowTest().use { fixture ->
            fixture.open()
            fixture.block(TouchDriver(fixture))
        }
    }

    private fun LinuxInputWindowTest.mouse(
        id: Int,
        position: Offset
    ): MouseEvent {
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

    private class NativeMouse(
        private val fixture: LinuxInputWindowTest
    ) : AutoCloseable {
        private val robot = Robot().apply { autoDelay = 30 }
        private val helper = System.getenv("SALT_TEST_NATIVE_POINTER")?.let {
            ProcessBuilder(it).redirectError(ProcessBuilder.Redirect.INHERIT).start()
        }
        private val commands = helper?.outputStream?.bufferedWriter()
        private val replies = helper?.inputStream?.bufferedReader()

        init {
            if (helper != null) assertEquals("ready", replies!!.readLine())
        }

        private fun send(
            command: String
        ) {
            commands!!.apply {
                write(command)
                newLine()
                flush()
            }
            assertEquals("ok", replies!!.readLine(), "Native pointer command: $command")
            robot.delay(30)
        }

        fun screenPoint(
            position: Offset
        ): Point = fixture.onEdt {
            val point = fixture.screen(position)
            val transform = fixture.window.graphicsConfiguration.defaultTransform
            Point((point.x / transform.scaleX).toInt(), (point.y / transform.scaleY).toInt())
        }

        fun move(
            point: Point
        ) {
            if (helper == null) {
                robot.mouseMove(point.x, point.y)
            } else {
                val scale = fixture.onEdt { fixture.window.graphicsConfiguration.defaultTransform }
                send("move ${point.x * scale.scaleX} ${point.y * scale.scaleY}")
            }
            robot.waitForIdle()
        }

        fun press() {
            // Native crossings and Compose hover may arrive after the AWT queue barrier.
            settle()
            pressButton()
        }

        private fun pressButton() {
            if (helper == null) robot.mousePress(InputEvent.BUTTON1_DOWN_MASK) else send("press")
        }

        fun release() {
            if (helper == null) robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK) else send("release")
            robot.waitForIdle()
        }

        fun doubleClick(
            point: Point
        ) {
            val interval = Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval") as Int
            robot.delay(interval + 50)
            move(point)
            press()
            release()
            robot.delay(40)
            pressButton()
            release()
            settle()
        }

        fun settle() {
            robot.delay(150)
            robot.waitForIdle()
        }

        override fun close() {
            try {
                release()
            } finally {
                commands?.close()
                if (helper != null && !helper.waitFor(2, TimeUnit.SECONDS)) helper.destroyForcibly()
                replies?.close()
            }
        }
    }

    private class TouchDriver(
        private val fixture: LinuxInputWindowTest
    ) {
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

        fun send(
            phase: TouchPhase,
            position: Offset
        ) {
            if (phase == TouchPhase.Down) id++
            send(id, phase, position)
        }

        fun reset() = fixture.onEdt { input.cancel() }

        fun send(
            id: Long,
            phase: TouchPhase,
            position: Offset
        ) = fixture.onEdt {
            time += 80
            val screen = fixture.screen(position)
            assertEquals(
                LinuxWindowGestures.TouchResult.Handled,
                input.dispatch(
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
                    ),
                    canMoveNatively = false
                )
            )
        }

        fun tap(
            position: Offset
        ) = fixture.onEdt {
            // Keep the synthetic tap together instead of interleaving unrelated native events.
            send(TouchPhase.Down, position)
            send(TouchPhase.Up, position)
        }

        fun doubleTap(
            position: Offset
        ) {
            tap(position)
            tap(position)
        }
    }
}
