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

package com.moriafly.salt.ui.gestures.cupertino

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals

class CupertinoOverscrollEffectTest {
    @Test
    fun releaseAfterPointerInputCancellationDoesNotFail() {
        val node = CupertinoOverscrollEffect(
            applyClip = false,
            allowTopOverscroll = true,
            allowBottomOverscroll = true,
            allowStartOverscroll = true,
            allowEndOverscroll = true
        ).node as PointerInputModifierNode

        node.onPointerEvent(
            pointerEvent = pointerEvent(pointerChange(id = 0, pressed = true)),
            pass = PointerEventPass.Initial,
            bounds = IntSize.Zero
        )
        node.onCancelPointerInput()
        node.onPointerEvent(
            pointerEvent = pointerEvent(pointerChange(id = 0, pressed = false)),
            pass = PointerEventPass.Initial,
            bounds = IntSize.Zero
        )
    }

    @Test
    fun partialPointerEventsPreserveOtherActivePointers() {
        val tracker = CupertinoPointerTracker()

        tracker.update(pointerEvent(pointerChange(id = 0, pressed = true)))
        tracker.update(pointerEvent(pointerChange(id = 1, pressed = true)))
        assertEquals(2, tracker.pointersDown)

        tracker.update(pointerEvent(pointerChange(id = 0, pressed = false)))
        assertEquals(1, tracker.pointersDown)

        tracker.update(pointerEvent(pointerChange(id = 1, pressed = false)))
        assertEquals(0, tracker.pointersDown)
    }

    private fun pointerEvent(vararg changes: PointerInputChange) =
        PointerEvent(changes.toList())

    private fun pointerChange(id: Long, pressed: Boolean) =
        PointerInputChange(
            id = PointerId(id),
            uptimeMillis = 1,
            position = Offset.Zero,
            pressed = pressed,
            previousUptimeMillis = 0,
            previousPosition = Offset.Zero,
            previousPressed = !pressed,
            isInitiallyConsumed = false
        )
}
