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

package com.moriafly.salt.ui.popup

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalHapticFeedback

internal class MenuDragSelection<T : Any> {
    var coordinates: LayoutCoordinates? = null
    val rows = mutableMapOf<T, LayoutCoordinates>()
    var highlightedItem by mutableStateOf<T?>(null)
    var isSlideSelecting = false

    fun itemAt(position: Offset, isEnabled: (T) -> Boolean): T? {
        val menu = coordinates?.takeIf { it.isAttached } ?: return null
        return rows.entries.firstOrNull { (item, row) ->
            row.isAttached && isEnabled(item) && menu.localBoundingBoxOf(row).contains(position)
        }?.key
    }
}

/**
 * A short menu supports sliding from one action to another. A long menu leaves the same gesture to
 * the scroll container. A tap is always handled by the row's normal clickable.
 */
@Composable
internal fun <T : Any> Modifier.menuDragSelection(
    selection: MenuDragSelection<T>,
    enabled: Boolean,
    canSlide: () -> Boolean,
    entryEnabled: (T) -> Boolean,
    onSlideSelectingChange: (Boolean) -> Unit = {},
    onActivate: (T) -> Unit
): Modifier {
    val haptic = LocalHapticFeedback.current
    val currentCanSlide by rememberUpdatedState(canSlide)
    val currentEntryEnabled by rememberUpdatedState(entryEnabled)
    val slideSelectingChange by rememberUpdatedState(onSlideSelectingChange)
    val activate by rememberUpdatedState(onActivate)
    if (!enabled) {
        return this
    }

    return pointerInput(selection, haptic) {
        try {
            awaitEachGesture {
                val down = awaitFirstDown(pass = PointerEventPass.Initial)
                if (down.type != PointerType.Touch || !currentCanSlide()) {
                    return@awaitEachGesture
                }
                val firstItem =
                    selection.itemAt(down.position, currentEntryEnabled) ?: return@awaitEachGesture

                var lastHapticItem = firstItem
                try {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id }
                        if (change == null || change.isConsumed ||
                            event.changes.any { it.id != down.id && it.pressed }
                        ) {
                            break
                        }
                        if (!change.pressed) {
                            if (selection.isSlideSelecting) {
                                change.consume()
                                selection.itemAt(
                                    change.position,
                                    currentEntryEnabled
                                )?.let(activate)
                            }
                            break
                        }

                        if (!selection.isSlideSelecting &&
                            (change.position - down.position).getDistance() <
                            viewConfiguration.touchSlop
                        ) {
                            continue
                        }
                        if (!currentCanSlide()) {
                            break
                        }

                        if (!selection.isSlideSelecting) {
                            selection.isSlideSelecting = true
                            slideSelectingChange(true)
                        }
                        change.consume()
                        val item = selection.itemAt(change.position, currentEntryEnabled)
                        selection.highlightedItem = item
                        if (item != null && item != lastHapticItem) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            lastHapticItem = item
                        }
                    }
                } finally {
                    if (selection.isSlideSelecting) {
                        selection.isSlideSelecting = false
                        slideSelectingChange(false)
                    }
                    selection.highlightedItem = null
                }
            }
        } finally {
            selection.highlightedItem = null
        }
    }
}
