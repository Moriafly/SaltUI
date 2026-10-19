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

package com.moriafly.salt.ui.platform.linux.input.gesture

import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.platform.linux.input.gesture.LinuxWindowGestureState.Pointer
import com.moriafly.salt.ui.platform.linux.input.gesture.LinuxWindowGestureState.Position
import com.moriafly.salt.ui.window.WindowResizeEdge

/**
 * Side effects injected into the platform dispatcher, never into the gesture state machine.
 * Coordinates use logical screen units. Implementations own native resources and queued work.
 */
@OptIn(UnstableSaltUiApi::class)
internal interface WindowGestureEffects {
    val mover: Mover
    val resizer: Resizer

    interface Mover {
        val isNativeMouseMove: Boolean
            get() = false

        fun prepareTouchMove(
            id: Long,
            position: Position
        ): Boolean

        fun commitTouchMove(
            id: Long
        ): Boolean

        fun install(): () -> Unit

        fun start(
            pointer: Pointer,
            start: Position,
            current: Position
        )

        fun update(
            position: Position
        )

        fun finish()

        fun toggleMaximize()
    }

    interface Resizer {
        fun setEnabled(
            enabled: Boolean
        )

        fun start(
            edge: WindowResizeEdge,
            position: Position
        )

        fun update(
            position: Position
        )

        fun finish(
            applyPending: Boolean
        )

        fun showEdge(
            edge: WindowResizeEdge
        )
    }

    /**
     * Activates the bound input target using native server time, independently of gesture capture.
     */
    fun interface Focus {
        fun onPress(
            serverTime: Long
        )
    }
}
