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

@file:Suppress("ktlint:standard:filename")
@file:OptIn(com.moriafly.salt.adaptive.window.UnstableSaltAdaptiveWindowApi::class)

package com.moriafly.salt.ui.sheet.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.moriafly.salt.adaptive.window.SaltAdaptiveWindow
import com.moriafly.salt.ui.sheet.SheetDisplayMode

// A focusable scene popup covers its owning window and leaves other windows usable.
// The platform layer also follows owner movement/resizing and restores the previous focus.
@OptIn(ExperimentalComposeUiApi::class, com.moriafly.salt.ui.UnstableSaltUiApi::class)
@Composable
internal actual fun SheetHost(
    inputEnabled: Boolean,
    displayMode: SheetDisplayMode,
    adaptiveWindow: SaltAdaptiveWindow,
    backgroundColor: Color,
    onDismissRequest: () -> Unit,
    content: @Composable (SheetHostEnvironment) -> Unit
) {
    val nativeLayers = rememberNativeSheetLayers()
    // Native popup backends still own their drawing region. Remove them immediately on dismissal
    // rather than retaining a full-window native component that intercepts background input.
    if (!inputEnabled && nativeLayers) return
    val ownerSize = LocalWindowInfo.current.containerDpSize
    val ownerPixels = LocalWindowInfo.current.containerSize
    Popup(
        popupPositionProvider = SheetPopupPositionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = inputEnabled,
            dismissOnBackPress = inputEnabled,
            dismissOnClickOutside = false,
            clippingEnabled = false,
            usePlatformDefaultWidth = false,
            usePlatformInsets = false,
            consumePointerInputOutside = inputEnabled
        ),
        content = {
            SheetHostLayout(
                inputEnabled = inputEnabled,
                windowSize = ownerPixels
            ) {
                content(
                    sheetHostEnvironment(
                        windowSize = ownerSize,
                        adaptiveWindow = adaptiveWindow
                    )
                )
            }
        }
    )
}

@Composable
private fun rememberNativeSheetLayers(): Boolean = remember {
    System.getProperty("compose.layers.type") in setOf("COMPONENT", "WINDOW")
}
