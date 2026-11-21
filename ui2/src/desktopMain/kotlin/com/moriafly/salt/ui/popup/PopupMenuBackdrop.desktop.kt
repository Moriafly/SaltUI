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

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.PopupProperties

internal actual val popupSharesHostCanvas: Boolean
    get() {
        val layers = System.getProperty("compose.layers.type")
        return layers != "WINDOW" && layers != "COMPONENT"
    }

@Composable
internal actual fun PopupProperties.menuPositionOrigin(): IntOffset {
    if (!usePlatformInsets) return IntOffset.Zero
    val insets = WindowInsets.systemBars
    val density = LocalDensity.current
    return IntOffset(insets.getLeft(density, LocalLayoutDirection.current), insets.getTop(density))
}

@OptIn(ExperimentalComposeUiApi::class)
internal actual fun PopupProperties.forMenuExit(): PopupProperties = PopupProperties(
    focusable = false,
    dismissOnBackPress = false,
    dismissOnClickOutside = false,
    clippingEnabled = clippingEnabled,
    usePlatformDefaultWidth = usePlatformDefaultWidth,
    usePlatformInsets = usePlatformInsets,
    consumePointerInputOutside = false
)
