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

@file:OptIn(com.moriafly.salt.ui.UnstableSaltUiApi::class)

package com.moriafly.salt.ui.sheet.internal

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalFocusManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun Modifier.sheetModalInput(
    visible: Boolean,
    directInput: SheetDirectInput
): Modifier {
    val focus = LocalFocusManager.current
    return this
        .focusGroup()
        .onPreviewKeyEvent {
            if (!visible) {
                true
            } else if (it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
                val direction = if (it.isShiftPressed) {
                    FocusDirection.Previous
                } else {
                    FocusDirection.Next
                }
                if (!focus.moveFocus(direction)) {
                    focus.clearFocus(force = true)
                    focus.moveFocus(direction)
                }
                true
            } else {
                false
            }
        }
        .pointerInput(directInput, visible) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    if (!visible) event.changes.forEach { it.consume() }
                    when (event.type) {
                        PointerEventType.Press -> directInput.active = true
                        PointerEventType.Scroll -> directInput.active = false
                    }
                }
            }
        }
}

internal fun Modifier.sheetDrag(
    state: SheetTransitionState,
    scope: CoroutineScope,
    visible: () -> Boolean,
    dismissible: () -> Boolean,
    onDismissRequest: () -> Unit
): Modifier = pointerInput(state) {
    val tracker = VelocityTracker()
    detectVerticalDragGestures(
        onDragStart = {
            tracker.resetTracking()
            state.beginGesture()
        },
        onVerticalDrag = { change, amount ->
            tracker.addPosition(
                timeMillis = change.uptimeMillis,
                position = change.position
            )
            state.drag(
                delta = amount,
                dismissible = dismissible()
            )
            change.consume()
        },
        onDragEnd = {
            scope.launch {
                state.settle(
                    velocity = tracker.calculateVelocity().y,
                    dismissible = dismissible(),
                    onDismissRequest = onDismissRequest
                )
            }
        },
        onDragCancel = {
            if (visible()) {
                scope.launch {
                    state.settle(
                        velocity = 0f,
                        dismissible = false,
                        onDismissRequest = {}
                    )
                }
            }
        }
    )
}
