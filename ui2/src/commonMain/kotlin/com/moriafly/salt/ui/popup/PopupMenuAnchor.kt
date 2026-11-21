/*
 * SaltUI
 * Copyright (C) 2023 Moriafly
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

@file:Suppress("unused")

package com.moriafly.salt.ui.popup

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isFinite
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * Captures a trigger's pointer position for one menu opening. Use a separate anchor for each
 * menu. Openings without a pointer position use the trigger's layout bounds.
 */
@Stable
@UnstableSaltUiApi
class PopupMenuAnchor internal constructor() {
    internal var coordinates: LayoutCoordinates? = null
    internal var pointerPosition: Offset? = null
    internal var pendingPositionInWindow: Offset? = null
        private set
    private var isMenuOpen = false

    /**
     * Captures the current pointer position and invokes [onOpen], which should expand the menu.
     * Call this from the trigger's activation callback.
     */
    fun open(onOpen: () -> Unit) {
        if (!isMenuOpen) {
            val position = pointerPosition
            val layout = coordinates?.takeIf { it.isAttached }
            pendingPositionInWindow = if (position != null && layout != null) {
                layout.localToWindow(position).takeIf { it.isFinite }
            } else {
                null
            }
        }
        pointerPosition = null
        onOpen()
    }

    internal fun onMenuOpened() {
        pendingPositionInWindow = null
        isMenuOpen = true
    }

    internal fun onMenuClosed() {
        isMenuOpen = false
    }
}

@UnstableSaltUiApi
@Composable
fun rememberPopupMenuAnchor(): PopupMenuAnchor = remember { PopupMenuAnchor() }

/**
 * Observes pointer input on the trigger without consuming it. Call [PopupMenuAnchor.open] from its click.
 */
@UnstableSaltUiApi
fun Modifier.popupMenuAnchor(
    anchor: PopupMenuAnchor
): Modifier = onPlaced { anchor.coordinates = it }
    .pointerInput(anchor) {
        try {
            awaitEachGesture {
                val down = awaitFirstDown(
                    requireUnconsumed = false,
                    pass = PointerEventPass.Initial
                )
                if (down.type == PointerType.Touch || down.type == PointerType.Mouse) {
                    anchor.pointerPosition = down.position
                    var pressed: Boolean
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Final)
                        pressed = event.changes.any { it.id == down.id && it.pressed }
                    } while (pressed)
                    anchor.pointerPosition = null
                }
            }
        } finally {
            anchor.pointerPosition = null
            anchor.coordinates = null
        }
    }
