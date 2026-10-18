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

package com.moriafly.salt.ui.platform.linux.window

import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.platform.linux.input.gesture.LinuxWindowGestureState.Pointer
import com.moriafly.salt.ui.platform.linux.input.gesture.LinuxWindowGestureState.Position
import com.moriafly.salt.ui.platform.linux.input.gesture.WindowGestureEffects
import com.moriafly.salt.ui.platform.linux.x11.LinuxX11WindowMover
import com.moriafly.salt.ui.window.WindowResizeEdge
import java.awt.Frame
import java.awt.Point
import java.awt.Window
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseEvent

/**
 * Executes gesture commands through the WM and AWT. Native restore notifications and queued resize
 * work stay here; neither can change contact ownership or interpret another pointer's input.
 */
@OptIn(UnstableSaltUiApi::class)
internal class LinuxWindowGestureEffects(
    window: Window,
    resizeRegions: UndecoratedWindowResizer
) : WindowGestureEffects {
    override val mover: WindowGestureEffects.Mover = WindowMover(window)
    override val resizer: WindowGestureEffects.Resizer = object : WindowGestureEffects.Resizer {
        private var operation: LinuxWindowResizeOperation? = null

        override fun setEnabled(
            enabled: Boolean
        ) {
            resizeRegions.enabled = enabled
        }

        override fun start(
            edge: WindowResizeEdge,
            position: Position
        ) {
            operation?.finish()
            operation = LinuxWindowResizeOperation(
                window = window,
                edge = edge,
                start = Point(position.x, position.y)
            )
        }

        override fun update(
            position: Position
        ) {
            operation?.update(Point(position.x, position.y))
        }

        override fun finish(
            applyPending: Boolean
        ) {
            val active = operation
            operation = null
            active?.finish(applyPending)
        }

        override fun showEdge(
            edge: WindowResizeEdge
        ) = resizeRegions.showEdge(edge)
    }

    private class WindowMover(
        private val window: Window
    ) : WindowGestureEffects.Mover {
        private val fallback = LinuxFallbackWindowMover(window)
        private var useNativeDrag = true
        private var restoreWidth = window.width
        private var drag: Drag? = null

        override fun install(): () -> Unit {
            val listener = object : ComponentAdapter() {
                override fun componentResized(
                    event: ComponentEvent
                ) = onWindowResized()
            }
            window.addComponentListener(listener)
            return {
                window.removeComponentListener(listener)
                finish()
            }
        }

        override fun start(
            pointer: Pointer,
            start: Position,
            current: Position
        ) {
            val active = Drag(pointer, current)
            drag = active
            val frame = window as? Frame
            if (frame?.extendedState == Frame.MAXIMIZED_BOTH) {
                val localX = start.x - window.x
                active.restoreOffset = Position(
                    (restoreWidth * localX.toFloat() / window.width).toInt(),
                    start.y - window.y
                )
                frame.extendedState = Frame.NORMAL
            } else {
                startMover(start, pointer)
                fallback.updateDrag(Point(current.x, current.y))
            }
        }

        override fun update(
            position: Position
        ) {
            val active = drag ?: return
            active.latestPosition = position
            if (active.restoreOffset == null) fallback.updateDrag(Point(position.x, position.y))
        }

        override fun finish() {
            drag = null
            fallback.endDrag()
        }

        override fun toggleMaximize() {
            val frame = window as? Frame ?: return
            frame.extendedState = if (frame.extendedState == Frame.MAXIMIZED_BOTH) {
                Frame.NORMAL
            } else {
                Frame.MAXIMIZED_BOTH
            }
        }

        private fun startMover(
            position: Position,
            pointer: Pointer
        ) {
            val point = Point(position.x, position.y)
            if (useNativeDrag) {
                val button =
                    if (pointer == Pointer.Mouse) MouseEvent.BUTTON1 else MouseEvent.NOBUTTON
                val started = LinuxX11WindowMover.tryMove(
                    window = window,
                    screenPosition = point,
                    button = button
                )
                if (started) return
                useNativeDrag = false
            }
            fallback.startDrag(point)
        }

        private fun onWindowResized() {
            if ((window as? Frame)?.extendedState == Frame.MAXIMIZED_BOTH) return
            restoreWidth = window.width
            val active = drag ?: return
            val offset = active.restoreOffset ?: return
            active.restoreOffset = null
            // Moving before native restore would overwrite the WM's saved geometry.
            window.setLocation(
                active.latestPosition.x - offset.x,
                active.latestPosition.y - offset.y
            )
            startMover(active.latestPosition, active.pointer)
        }

        private class Drag(
            val pointer: Pointer,
            var latestPosition: Position,
            var restoreOffset: Position? = null
        )
    }
}
