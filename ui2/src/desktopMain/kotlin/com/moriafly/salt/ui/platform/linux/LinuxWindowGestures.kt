/*
 * SaltUI
 * Copyright (C) 2026 Moriafly
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moriafly.salt.ui.platform.linux

import androidx.compose.runtime.State
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.input.pointer.PointerEventType
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.WindowResizeEdge
import java.awt.EventQueue
import java.awt.Frame
import java.awt.Point
import java.awt.Rectangle
import java.awt.Window
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseEvent

internal val LocalLinuxWindowGestures = staticCompositionLocalOf<LinuxWindowGestures?> { null }

/**
 * The window's single move/resize owner, confined to the AWT event thread.
 *
 * Mouse and touch use the same geometry and movement policy. Borders take precedence over the
 * caption. Compose resolves caption/control overlap, but can only request a drag during the first
 * press dispatched here. This owner cancels Compose after dispatch returns, then captures that
 * contact until release. Other fingers never inherit a window gesture, even after its owner lifts.
 */
@OptIn(UnstableSaltUiApi::class)
internal class LinuxWindowGestures(
    private val window: Window,
    private val caption: LinuxCaptionBarHitTest,
    private val resizer: UndecoratedWindowResizer,
    private val captionHovered: State<Boolean>
) : LinuxWindowMouseInput.Handler {
    private var moveable = false
    private var canDrag = true
    private var canMaximize = false
    private var gesture: Gesture? = null
    private val fallbackMover = LinuxFallbackWindowMover(window)
    private var useNativeDrag = true
    private var restoreWidth = window.width
    private var suppressedClick: Click? = null
    private val contacts = mutableSetOf<Long>()
    private var capturedTouches = false
    private var dispatchingPress: LinuxTouchEvent? = null
    private var captionRequested = false

    fun configure(
        moveable: Boolean,
        resizable: Boolean,
        canDrag: Boolean,
        canMaximize: Boolean
    ) {
        resizer.enabled = resizable
        this.moveable = moveable
        this.canDrag = canDrag
        this.canMaximize = canMaximize
        if (
            (gesture is Gesture.Move && (!moveable || !canDrag)) ||
            (gesture is Gesture.Resize && !resizer.enabled)
        ) {
            finishGesture()
        }
        if (!resizer.enabled) resizer.showEdge(WindowResizeEdge.None)
    }

    fun install(): () -> Unit {
        val listener = object : ComponentAdapter() {
            override fun componentResized(event: ComponentEvent) = onWindowResized()
        }
        window.addComponentListener(listener)
        val unregister = LinuxWindowMouseInput.register(window, this, priority = 1)
        return {
            unregister()
            window.removeComponentListener(listener)
        }
    }

    /**
     * A request from a caption that passed Compose hit testing, not an executable event callback.
     * Requests outside this owner's first-press dispatch cannot take over an existing gesture.
     */
    fun requestCaptionDrag(event: LinuxTouchEvent): Boolean {
        if (dispatchingPress !== event || !moveable || !canDrag) return false
        captionRequested = true
        return true
    }

    fun dispatchTouch(
        event: LinuxTouchEvent,
        dispatchToCompose: () -> Unit,
        cancelCompose: () -> Unit
    ) {
        val firstContact = contacts.isEmpty()
        if (event.type == PointerEventType.Press) {
            if (!contacts.add(event.id)) return
        } else if (event.id !in contacts) {
            return
        }
        try {
            if (capturedTouches || gesture?.pointer == Pointer.Mouse) {
                capturedTouches = true
                if (gesture?.pointer == Pointer.Touch(event.id)) {
                    when (event.type) {
                        PointerEventType.Move -> updateGesture(event.screenPoint())
                        PointerEventType.Release -> finishGesture(applyPendingResize = true)
                    }
                }
                return
            }
            if (firstContact && event.type == PointerEventType.Press) {
                val edge = resizer.edgeAt(event.contentPosition)
                if (edge != WindowResizeEdge.None) {
                    capturedTouches = true
                    cancelCompose()
                    beginResize(Pointer.Touch(event.id), edge, event.screenPoint())
                    return
                }
                dispatchingPress = event
                captionRequested = false
                try {
                    dispatchToCompose()
                    if (captionRequested) {
                        capturedTouches = true
                        cancelCompose()
                        beginMove(Pointer.Touch(event.id), event.screenPoint())
                    }
                } finally {
                    dispatchingPress = null
                    captionRequested = false
                }
            } else {
                dispatchToCompose()
            }
        } catch (exception: Throwable) {
            cancel()
            cancelCompose()
            throw exception
        } finally {
            if (event.type == PointerEventType.Release) {
                contacts.remove(event.id)
                if (contacts.isEmpty()) capturedTouches = false
            }
        }
    }

    override fun onMouseEvent(event: MouseEvent): Boolean {
        // A real mouse cannot steal a touch sequence, and vice versa.
        if (contacts.isNotEmpty()) {
            return event.id == MouseEvent.MOUSE_PRESSED || event.id == MouseEvent.MOUSE_DRAGGED ||
                event.id == MouseEvent.MOUSE_RELEASED || event.id == MouseEvent.MOUSE_CLICKED
        }
        val edge = resizer.edgeAt(LinuxWindowMouseInput.positionInContent(event, window))
        when (event.id) {
            MouseEvent.MOUSE_MOVED, MouseEvent.MOUSE_ENTERED -> resizer.showEdge(edge)
            MouseEvent.MOUSE_EXITED -> if (gesture == null) resizer.showEdge(WindowResizeEdge.None)
            MouseEvent.MOUSE_PRESSED -> {
                if (event.button != MouseEvent.BUTTON1) return gesture != null
                finishGesture()
                suppressedClick = null
                if (edge != WindowResizeEdge.None) {
                    beginResize(Pointer.Mouse, edge, event.locationOnScreen)
                    return true
                }
                if (!moveable || !canDrag || !caption.contains(
                        position = LinuxWindowMouseInput.positionInContent(event, window),
                        window = window,
                        // Enter and press can arrive before recomposition.
                        isHovered = captionHovered.value
                    )
                ) {
                    return false
                }
                beginMove(Pointer.Mouse, event.locationOnScreen)
                return true
            }

            MouseEvent.MOUSE_DRAGGED -> {
                if (gesture?.pointer != Pointer.Mouse) return false
                updateGesture(event.locationOnScreen)
                return true
            }

            MouseEvent.MOUSE_RELEASED -> {
                val active = gesture ?: return false
                if (event.button == MouseEvent.BUTTON1) {
                    suppressedClick = if (active is Gesture.Move) Click.Caption else Click.Resize
                    finishGesture(applyPendingResize = true)
                    resizer.showEdge(edge)
                }
                return true
            }

            MouseEvent.MOUSE_CLICKED -> {
                val click = suppressedClick ?: return false
                if (event.button != MouseEvent.BUTTON1) return false
                suppressedClick = null
                if (click == Click.Caption && event.clickCount == 2 &&
                    moveable && canDrag && canMaximize
                ) {
                    (window as? Frame)?.let { frame ->
                        frame.extendedState = if (frame.extendedState == Frame.MAXIMIZED_BOTH) {
                            Frame.NORMAL
                        } else {
                            Frame.MAXIMIZED_BOTH
                        }
                    }
                }
                return true
            }
        }
        return false
    }

    override fun afterMouseEvent(event: MouseEvent) = resizer.afterMouseEvent(event)

    override fun cancel() {
        finishGesture()
        suppressedClick = null
        dispatchingPress = null
        captionRequested = false
        // Keep known contacts quarantined until release or an explicit native input reset.
        capturedTouches = contacts.isNotEmpty()
    }

    fun resetTouchInput() {
        // XI2 device changes and touch-channel shutdown must not cancel an AWT mouse drag.
        if (gesture?.pointer is Pointer.Touch) finishGesture()
        dispatchingPress = null
        captionRequested = false
        contacts.clear()
        capturedTouches = false
    }

    private fun beginMove(pointer: Pointer, position: Point) {
        gesture = Gesture.Move(pointer, position)
    }

    private fun beginResize(pointer: Pointer, edge: WindowResizeEdge, position: Point) {
        gesture = Gesture.Resize(pointer, position, edge, window.bounds)
        resizer.showEdge(edge)
    }

    private fun updateGesture(position: Point) {
        when (val active = gesture) {
            is Gesture.Move -> {
                active.latestPosition = position
                if (active.restoreOffset != null) return
                if (active.pending) {
                    active.pending = false
                    val frame = window as? Frame
                    if (frame?.extendedState == Frame.MAXIMIZED_BOTH) {
                        val localX = active.start.x - window.x
                        active.restoreOffset = Point(
                            (restoreWidth * localX.toFloat() / window.width).toInt(),
                            active.start.y - window.y
                        )
                        frame.extendedState = Frame.NORMAL
                        return
                    }
                    startMover(active.start, active.pointer)
                }
                fallbackMover.updateDrag(position)
            }

            is Gesture.Resize -> scheduleResize(active, position)
            null -> Unit
        }
    }

    private fun startMover(position: Point, pointer: Pointer) {
        if (useNativeDrag) {
            val button = if (pointer == Pointer.Mouse) MouseEvent.BUTTON1 else MouseEvent.NOBUTTON
            if (LinuxX11WindowMover.tryMove(window, position, button)) return
            useNativeDrag = false
        }
        fallbackMover.startDrag(position)
    }

    private fun onWindowResized() {
        if ((window as? Frame)?.extendedState == Frame.MAXIMIZED_BOTH) return
        restoreWidth = window.width
        val active = gesture as? Gesture.Move ?: return
        val offset = active.restoreOffset ?: return
        active.restoreOffset = null
        // Position only after native restore; moving earlier overwrites the WM's saved geometry.
        window.setLocation(active.latestPosition.x - offset.x, active.latestPosition.y - offset.y)
        startMover(active.latestPosition, active.pointer)
    }

    private fun scheduleResize(active: Gesture.Resize, position: Point) {
        val alreadyQueued = active.pendingPosition != null
        active.pendingPosition = position
        if (alreadyQueued) return
        EventQueue.invokeLater {
            val latest = active.pendingPosition ?: return@invokeLater
            active.pendingPosition = null
            // A queued update must not outlive its gesture or affect the next pointer's drag.
            if (gesture === active) resize(active, latest)
        }
    }

    private fun resize(active: Gesture.Resize, position: Point) {
        val dx = position.x - active.start.x
        val dy = position.y - active.start.y
        val initial = active.bounds
        var x = initial.x
        var y = initial.y
        var width = initial.width
        var height = initial.height
        when (active.edge) {
            WindowResizeEdge.Left, WindowResizeEdge.TopLeft, WindowResizeEdge.BottomLeft -> {
                width = (initial.width - dx).coerceAtLeast(window.minimumSize.width)
                x = initial.x + initial.width - width
            }

            WindowResizeEdge.Right, WindowResizeEdge.TopRight, WindowResizeEdge.BottomRight ->
                width = (initial.width + dx).coerceAtLeast(window.minimumSize.width)

            else -> Unit
        }
        when (active.edge) {
            WindowResizeEdge.Top, WindowResizeEdge.TopLeft, WindowResizeEdge.TopRight -> {
                height = (initial.height - dy).coerceAtLeast(window.minimumSize.height)
                y = initial.y + initial.height - height
            }

            WindowResizeEdge.Bottom, WindowResizeEdge.BottomLeft, WindowResizeEdge.BottomRight ->
                height = (initial.height + dy).coerceAtLeast(window.minimumSize.height)

            else -> Unit
        }
        window.setLocation(x, y)
        window.setSize(width, height)
    }

    /**
     * Ends the current window gesture and clears the drag state and resize-edge indicator.
     *
     * @param applyPendingResize Pass `true` on mouse or touch release to apply the last pending
     * resize before ending the gesture. Leave `false` on cancellation to discard the pending
     * update and prevent further window size changes from that gesture.
     */
    private fun finishGesture(
        applyPendingResize: Boolean = false
    ) {
        val active = gesture
        if (active is Gesture.Resize) {
            val latest = active.pendingPosition
            active.pendingPosition = null
            if (applyPendingResize && latest != null) resize(active, latest)
        }
        gesture = null
        fallbackMover.endDrag()
        resizer.showEdge(WindowResizeEdge.None)
    }

    private fun LinuxTouchEvent.screenPoint() = Point(screenPosition.x, screenPosition.y)

    private sealed interface Pointer {
        data object Mouse : Pointer

        data class Touch(
            val id: Long
        ) : Pointer
    }

    private sealed class Gesture(
        val pointer: Pointer,
        val start: Point
    ) {
        class Move(
            pointer: Pointer,
            start: Point
        ) : Gesture(pointer, start) {
            var pending = true
            var latestPosition = start
            var restoreOffset: Point? = null
        }

        class Resize(
            pointer: Pointer,
            start: Point,
            val edge: WindowResizeEdge,
            val bounds: Rectangle
        ) : Gesture(pointer, start) {
            var pendingPosition: Point? = null
        }
    }

    private enum class Click { Caption, Resize }
}
