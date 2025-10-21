/*
 * Salt UI
 * Copyright (C) 2025 Moriafly
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

package com.moriafly.salt.ui.window

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.UnstableSaltUiApi
import java.awt.Window

/**
 * # Properties for SaltWindow
 *
 * @property minSize The minimum size of the window.
 * @property onVisibleChanged The callback to be invoked when the visibility of the window changes.
 * To replace obtaining the window isVisible state in Composable.
 *
 * Do **not** use this:
 * ```
 * SaltWindow(
 *     // ...
 * ) {
 *     LaunchEffect(window.isVisible) {
 *         // Do something
 *     }
 * }
 * ```
 *
 * Please use:
 * ```
 * SaltWindow(
 *     // ...,
 *     properties = SaltWindowProperties(
 *         onVisibleChanged = { window, visible ->
 *             // Do something
 *         }
 *     )
 * ) {
 *     // ...
 * }
 * ```
 * @property captionBarHeight The height of the caption bar, default is 40.dp, also usually 30.dp on
 * Windows.
 * @property captionButtonsVisible Whether the caption buttons are visible.
 * @property captionButtonHeight The height of the caption button, default is captionBarHeight.
 * You can also customize the height of the CaptionButton (window control buttons such as Minimize,
 * Maximize, and Close). Similar to how apps like Windows 11,
 * [captionButtonHeight] <= [captionBarHeight].
 * @property captionButtonIsDarkTheme Whether the caption button is dark theme.
 * @property extraDisplayScale The extra display scale.
 * @property extraFontScale The extra font scale.
 */
@UnstableSaltUiApi
data class SaltWindowProperties<T : Window>(
    val minSize: DpSize = DpSize.Zero,
    val onVisibleChanged: (T, Boolean) -> Unit = { _, _ -> },
    val captionBarHeight: Dp = 40.dp,
    val captionButtonsVisible: Boolean = true,
    val captionButtonHeight: Dp = captionBarHeight,
    val captionButtonIsDarkTheme: Boolean = false,
    val extraDisplayScale: Float = 1.0f,
    val extraFontScale: Float = 1.0f
)

@UnstableSaltUiApi
internal val LocalSaltWindowProperties = staticCompositionLocalOf<SaltWindowProperties<Window>> {
    error("SaltWindowProperties is not provided")
}
