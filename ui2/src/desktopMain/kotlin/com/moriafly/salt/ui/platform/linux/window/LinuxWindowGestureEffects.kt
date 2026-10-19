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

/**
 * Executes gesture commands through native or fallback window operations. Each operation owns its
 * geometry and queued work; neither can change contact ownership or interpret another pointer's input.
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
        private var useNativeMouseDrag = true
        private var nativeDrag: NativeDrag? = null

        override val isNativeMouseMove: Boolean
            get() {
                val active = nativeDrag ?: return false
                return active.pointer == Pointer.Mouse && active.move.started
            }

        override fun prepareTouchMove(
            id: Long,
            start: Position,
            current: Position
        ): Boolean {
            finish()
            nativeDrag = prepareNativeMove(Pointer.Touch(id), start, current) ?: return false
            return true
        }

        override fun commitTouchMove(
            id: Long
        ): Boolean {
            val active = nativeDrag ?: return false
            if (active.pointer != Pointer.Touch(id)) return false
            if (active.move.start()) return true
            finish()
            return false
        }

        override fun install(): () -> Unit {
            val uninstallFallback = fallback.install()
            return {
                uninstallFallback()
                finish()
            }
        }

        override fun start(
            pointer: Pointer,
            start: Position,
            current: Position
        ) {
            finish()
            if (pointer == Pointer.Mouse && useNativeMouseDrag) {
                nativeDrag = prepareNativeMove(pointer, start, current)
                if (nativeDrag?.move?.start() == true) return
                finish()
                useNativeMouseDrag = false
            }
            fallback.startDrag(Point(start.x, start.y), Point(current.x, current.y))
        }

        override fun update(
            position: Position
        ) = fallback.updateDrag(Point(position.x, position.y))

        override fun finish() {
            val active = nativeDrag
            nativeDrag = null
            try {
                active?.move?.close()
            } finally {
                fallback.endDrag()
            }
        }

        override fun toggleMaximize() {
            val frame = window as? Frame ?: return
            frame.extendedState = if (frame.extendedState == Frame.MAXIMIZED_BOTH) {
                Frame.NORMAL
            } else {
                Frame.MAXIMIZED_BOTH
            }
        }

        private fun prepareNativeMove(
            pointer: Pointer,
            start: Position,
            current: Position
        ): NativeDrag? {
            val move = LinuxX11WindowMover.prepare(
                window = window,
                start = Point(start.x, start.y),
                current = Point(current.x, current.y),
                fromTouch = pointer is Pointer.Touch
            ) ?: return null
            return NativeDrag(pointer, move)
        }

        private class NativeDrag(
            val pointer: Pointer,
            val move: LinuxX11WindowMover.Move
        )
    }
}
