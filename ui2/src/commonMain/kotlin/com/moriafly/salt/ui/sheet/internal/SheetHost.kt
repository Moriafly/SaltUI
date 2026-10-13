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

@file:OptIn(com.moriafly.salt.adaptive.window.UnstableSaltAdaptiveWindowApi::class)

package com.moriafly.salt.ui.sheet.internal

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.waterfall
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider
import com.moriafly.salt.adaptive.window.SaltAdaptiveWindow
import com.moriafly.salt.ui.sheet.SheetDisplayMode

/**
 * Owner geometry stays independent of the dialog and its keyboard insets.
 */
internal class SheetHostEnvironment(
    val windowSize: DpSize,
    val safeInsets: WindowInsets,
    val imeInsets: WindowInsets,
    val adaptiveWindow: SaltAdaptiveWindow
) {
    val contentInsets = safeInsets.union(imeInsets)
}

@Composable
internal fun sheetHostEnvironment(
    windowSize: DpSize,
    adaptiveWindow: SaltAdaptiveWindow
): SheetHostEnvironment = SheetHostEnvironment(
    windowSize = windowSize,
    safeInsets = WindowInsets.systemBars
        .union(WindowInsets.displayCutout)
        .union(WindowInsets.waterfall),
    imeInsets = WindowInsets.ime,
    adaptiveWindow = adaptiveWindow
)

/**
 * The full-window scrim and the panel resolve their insets separately.
 * Owner adaptation is passed explicitly so nested native windows keep their own automatic locals.
 */
@Composable
@OptIn(com.moriafly.salt.ui.UnstableSaltUiApi::class)
internal expect fun SheetHost(
    inputEnabled: Boolean,
    displayMode: SheetDisplayMode,
    adaptiveWindow: SaltAdaptiveWindow,
    backgroundColor: Color,
    onDismissRequest: () -> Unit,
    content: @Composable (SheetHostEnvironment) -> Unit
)

internal object SheetPopupPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset = IntOffset.Zero
}

/**
 * Keeps the retained drawing full-sized while removing a closing popup's interaction bounds.
 */
@Composable
internal fun SheetHostLayout(
    inputEnabled: Boolean,
    windowSize: IntSize,
    content: @Composable () -> Unit
) {
    Layout(content = content) { measurables, _ ->
        val placeables = measurables.map {
            it.measure(
                constraints = Constraints.fixed(
                    width = windowSize.width,
                    height = windowSize.height
                )
            )
        }
        layout(
            width = if (inputEnabled) windowSize.width else 0,
            height = if (inputEnabled) windowSize.height else 0
        ) {
            placeables.forEach { it.place(x = 0, y = 0) }
        }
    }
}
