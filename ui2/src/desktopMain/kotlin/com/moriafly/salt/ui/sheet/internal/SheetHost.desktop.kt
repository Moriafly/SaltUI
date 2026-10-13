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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.moriafly.salt.adaptive.window.SaltAdaptiveWindow
import com.moriafly.salt.ui.sheet.SheetDisplayMode
import com.moriafly.salt.ui.thenIf
import com.moriafly.salt.ui.window.internal.LocalSaltWindowContentDecoration

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
    val popupLayout = sheetPopupLayout(nativeLayers)
    Popup(
        popupPositionProvider = popupLayout.positionProvider,
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
                windowSize = popupLayout.contentPixels
            ) {
                Box(modifier = popupLayout.contentModifier) {
                    content(
                        sheetHostEnvironment(
                            windowSize = popupLayout.contentSize,
                            adaptiveWindow = adaptiveWindow
                        )
                    )
                }
            }
        }
    )
}

private class SheetPopupLayout(
    val contentPixels: IntSize,
    val contentSize: DpSize,
    val positionProvider: PopupPositionProvider,
    val contentModifier: Modifier
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun sheetPopupLayout(nativeLayers: Boolean): SheetPopupLayout {
    val density = LocalDensity.current
    val decoration = LocalSaltWindowContentDecoration.current
    val ownerInfo = LocalWindowInfo.current
    val ownerPixels = ownerInfo.containerSize
    val inset = if (nativeLayers) {
        0
    } else {
        with(density) { decoration.inset.roundToPx() }
            .coerceIn(0, minOf(ownerPixels.width, ownerPixels.height) / 2)
    }
    val contentPixels = IntSize(
        width = (ownerPixels.width - inset * 2).coerceAtLeast(0),
        height = (ownerPixels.height - inset * 2).coerceAtLeast(0)
    )
    val contentSize = if (nativeLayers) {
        ownerInfo.containerDpSize
    } else {
        with(density) {
            DpSize(contentPixels.width.toDp(), contentPixels.height.toDp())
        }
    }
    val positionProvider = remember(inset) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset = IntOffset(inset, inset)
        }
    }
    val contentShape = remember(decoration.cornerRadius) {
        RoundedCornerShape(decoration.cornerRadius)
    }
    // Keep both the scrim and a bottom panel inside the client-drawn frame.
    val contentModifier = Modifier
        .fillMaxSize()
        .thenIf(!nativeLayers) {
            graphicsLayer {
                shape = contentShape
                clip = true
                // Dialog preserves the transparent owner's alpha with SrcAtop.
                // Include the panel so its antialiased edges preserve it too.
                blendMode = BlendMode.SrcAtop
            }
        }
    return SheetPopupLayout(
        contentPixels = contentPixels,
        contentSize = contentSize,
        positionProvider = positionProvider,
        contentModifier = contentModifier
    )
}

@Composable
private fun rememberNativeSheetLayers(): Boolean = remember {
    System.getProperty("compose.layers.type") in setOf("COMPONENT", "WINDOW")
}
