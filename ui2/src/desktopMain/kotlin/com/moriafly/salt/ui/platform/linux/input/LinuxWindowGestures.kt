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

package com.moriafly.salt.ui.platform.linux.input

import androidx.compose.runtime.State
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.platform.ViewConfiguration
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.platform.linux.window.LinuxCaptionBarHitTest
import com.moriafly.salt.ui.platform.linux.window.LinuxFallbackWindowMover
import com.moriafly.salt.ui.platform.linux.window.LinuxWindowResizeOperation
import com.moriafly.salt.ui.platform.linux.window.UndecoratedWindowResizer
import com.moriafly.salt.ui.platform.linux.x11.LinuxX11WindowMover
import com.moriafly.salt.ui.window.WindowResizeEdge
import java.awt.Frame
import java.awt.Point
import java.awt.Window
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseEvent

internal val LocalLinuxWindowGestures = staticCompositionLocalOf<LinuxWindowGestures?> { null }

/**
 * The window's single move/resize owner, confined to the AWT event thread.
 *
 * Mouse and touch use the same geometry and movement policy. Borders take precedence over the
 * caption. Modal content is checked before dispatch so its scrim cannot swallow a caption press.
 * Otherwise Compose resolves caption/control overlap during the first press dispatched here.
 * This owner cancels Compose before capturing that contact until release. Other fingers never
 * inherit a window gesture, even after its owner lifts.
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
    private var captionHitByCompose = false
    private val touchTapRecognizer = LinuxTouchTapRecognizer(window)

    fun configure(
        moveable: Boolean,
        resizable: Boolean,
        canDrag: Boolean,
        canMaximize: Boolean,
        viewConfiguration: ViewConfiguration
    ) {
        if (this.moveable != moveable || this.canDrag != canDrag ||
            this.canMaximize != canMaximize
        ) {
            touchTapRecognizer.cancel()
        }
        touchTapRecognizer.configure(viewConfiguration)
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
     * Records a Compose caption hit only while resolving the first contact's target.
     * Gesture eligibility, capture and movement belong to this owner.
     */
    fun onCaptionPress(event: LinuxTouchEvent): Boolean {
        if (dispatchingPress !== event) return false
        captionHitByCompose = true
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
            if (!firstContact) touchTapRecognizer.cancel()
        } else if (event.id !in contacts) {
            return
        }
        try {
            if (capturedTouches || gesture?.pointer == Pointer.Mouse) {
                capturedTouches = true
                if (gesture?.pointer == Pointer.Touch(event.id)) {
                    when (event.type) {
                        PointerEventType.Move -> updateGesture(event.screenPoint())
                        PointerEventType.Release -> {
                            val active = gesture
                            val doubleTap = active is Gesture.Move &&
                                touchTapRecognizer.finish(event.screenPoint(), event.timeMillis)
                            finishGesture(applyPendingResize = true)
                            if (doubleTap) toggleMaximizeOrRestore()
                        }
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
                val isCaption = when (captionHitAt(event.contentPosition)) {
                    LinuxCaptionBarHitTest.Result.Caption -> true
                    LinuxCaptionBarHitTest.Result.Excluded -> {
                        dispatchToCompose()
                        false
                    }

                    LinuxCaptionBarHitTest.Result.RequiresComposeHitTest ->
                        dispatchCaptionPress(event, dispatchToCompose)
                }
                if (isCaption) {
                    capturedTouches = true
                    cancelCompose()
                    touchTapRecognizer.begin(event.screenPoint(), event.timeMillis)
                    beginMove(Pointer.Touch(event.id), event.screenPoint())
                } else {
                    touchTapRecognizer.cancel()
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
        if (event.id == MouseEvent.MOUSE_PRESSED) touchTapRecognizer.cancel()
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
                val position = LinuxWindowMouseInput.positionInContent(event, window)
                val isCaption = when (captionHitAt(position)) {
                    LinuxCaptionBarHitTest.Result.Caption -> true
                    LinuxCaptionBarHitTest.Result.Excluded -> false
                    // Enter and press can arrive before recomposition.
                    LinuxCaptionBarHitTest.Result.RequiresComposeHitTest -> captionHovered.value
                }
                if (!isCaption) return false
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
                if (click == Click.Caption && event.clickCount == 2) {
                    toggleMaximizeOrRestore()
                }
                return true
            }
        }
        return false
    }

    override fun afterMouseEvent(event: MouseEvent) = resizer.afterMouseEvent(event)

    override fun cancel() {
        touchTapRecognizer.cancel()
        finishGesture()
        suppressedClick = null
        dispatchingPress = null
        captionHitByCompose = false
        // Keep known contacts quarantined until release or an explicit native input reset.
        capturedTouches = contacts.isNotEmpty()
    }

    fun resetTouchInput() {
        touchTapRecognizer.cancel()
        // XI2 device changes and touch-channel shutdown must not cancel an AWT mouse drag.
        if (gesture?.pointer is Pointer.Touch) finishGesture()
        dispatchingPress = null
        captionHitByCompose = false
        contacts.clear()
        capturedTouches = false
    }

    private fun captionHitAt(position: Offset): LinuxCaptionBarHitTest.Result =
        if (moveable && canDrag) {
            caption.hitTest(position, window)
        } else {
            LinuxCaptionBarHitTest.Result.Excluded
        }

    private fun dispatchCaptionPress(
        event: LinuxTouchEvent,
        dispatchToCompose: () -> Unit
    ): Boolean {
        dispatchingPress = event
        captionHitByCompose = false
        try {
            dispatchToCompose()
            return captionHitByCompose
        } finally {
            dispatchingPress = null
            captionHitByCompose = false
        }
    }

    private fun toggleMaximizeOrRestore() {
        if (!moveable || !canDrag || !canMaximize) return
        val frame = window as? Frame ?: return
        frame.extendedState = if (frame.extendedState == Frame.MAXIMIZED_BOTH) {
            Frame.NORMAL
        } else {
            Frame.MAXIMIZED_BOTH
        }
    }

    private fun beginMove(
        pointer: Pointer,
        position: Point
    ) {
        gesture = Gesture.Move(pointer, position)
    }

    private fun beginResize(
        pointer: Pointer,
        edge: WindowResizeEdge,
        position: Point
    ) {
        touchTapRecognizer.cancel()
        gesture = Gesture.Resize(
            pointer,
            LinuxWindowResizeOperation(
                window = window,
                edge = edge,
                start = position
            )
        )
        resizer.showEdge(edge)
    }

    private fun updateGesture(position: Point) {
        when (val active = gesture) {
            is Gesture.Move -> {
                active.latestPosition = position
                if (active.restoreOffset != null) return
                if (active.pending) {
                    // Small touch jitter remains a tap, including while maximized. Starting the
                    // native mover earlier would restore the window before a double tap resolves.
                    if (active.pointer is Pointer.Touch) {
                        if (touchTapRecognizer.isWithinSlop(active.start, position)) return
                        touchTapRecognizer.cancel()
                    }
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

            is Gesture.Resize -> active.operation.update(position)
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
        (gesture as? Gesture.Resize)?.operation?.finish(applyPendingResize)
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
        val pointer: Pointer
    ) {
        class Move(
            pointer: Pointer,
            val start: Point
        ) : Gesture(pointer) {
            var pending = true
            var latestPosition = start
            var restoreOffset: Point? = null
        }

        class Resize(
            pointer: Pointer,
            val operation: LinuxWindowResizeOperation
        ) : Gesture(pointer)
    }

    private enum class Click { Caption, Resize }
}
