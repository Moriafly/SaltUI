/*
 * Salt UI
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

package com.moriafly.salt.ui.platform.windows

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput

internal class WindowsTouchEvent {
    // Written by Compose on the EDT and read by the next native pointer message.
    @Volatile
    var isCaptionDragRequested = false
}

/**
 * Only the caption hit-test node receives this gesture; interactive siblings remain clickable
 * Windows owns the touch move loop, including its window preview and snap behavior
 */
internal fun Modifier.windowsCaptionBarTouchInput(enabled: Boolean): Modifier =
    pointerInput(enabled) {
        if (!enabled) return@pointerInput
        awaitEachGesture {
            val down = awaitFirstDown()
            if (down.type != PointerType.Touch) return@awaitEachGesture
            val native = currentEvent.nativeEvent as? WindowsTouchEvent ?: return@awaitEachGesture
            down.consume()
            try {
                native.isCaptionDragRequested = true
                waitForUpOrCancellation()
            } finally {
                native.isCaptionDragRequested = false
            }
        }
    }
