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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.platform.macos.MacOSWindowMetrics
import java.awt.Window

/**
 * # Properties for SaltWindow
 *
 * @property minSize The minimum size of the window.
 * @property onVisibleChange The callback to be invoked when the visibility of the window changes.
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
 *
 * @property onResizeEdgeChange The callback to be invoked when the pointer moves to or away from
 * a resize edge of the window. The callback receives the [WindowResizeEdge] indicating which edge
 * the pointer is on, or [WindowResizeEdge.None] if not on any resize edge.
 * @property captionBarHeight The height of the caption bar. The platform default is 52.dp on macOS
 * and 40.dp on Windows and Linux.
 * @property captionButtonsVisible Whether the caption buttons are visible.
 * @property captionButtonHeight The height of the caption button, default is captionBarHeight.
 * You can also customize the height of the CaptionButton (window control buttons such as Minimize,
 * Maximize, and Close). Similar to how apps like Windows 11, [captionButtonHeight] <=
 * [captionBarHeight].
 * @property captionButtonIsDarkTheme Whether the caption button is dark theme.
 * @property backgroundType The platform window background type.
 * @property backgroundIsDarkTheme Whether the platform window background uses a dark appearance.
 * @property extraDisplayScale The extra display scale.
 * @property extraFontScale The extra font scale.
 * @property minimizeButtonEnabled Whether the minimize button is enabled, only used for
 * [SaltWindow]. TODO Support macOS.
 * @property maximizeOrRestoreButtonEnabled Whether the maximize/restore button is enabled, only
 * used for [SaltWindow]. TODO Support macOS.
 * @property moveable Whether the window is moveable.
 */
@UnstableSaltUiApi
data class SaltWindowProperties<T : Window>(
    val minSize: DpSize,
    val onVisibleChange: (T, Boolean) -> Unit,
    val onResizeEdgeChange: (T, WindowResizeEdge) -> Unit,
    val captionBarHeight: Dp,
    val captionButtonsVisible: Boolean,
    val captionButtonHeight: Dp,
    val captionButtonIsDarkTheme: Boolean,
    val backgroundType: SaltWindowBackgroundType,
    val backgroundIsDarkTheme: Boolean,
    val extraDisplayScale: Float,
    val extraFontScale: Float,
    val minimizeButtonEnabled: Boolean,
    val maximizeOrRestoreButtonEnabled: Boolean,
    val moveable: Boolean
) {
    companion object {
        @Composable
        fun <T : Window> default(
            minSize: DpSize = DpSize.Zero,
            onVisibleChange: (T, Boolean) -> Unit = { _, _ -> },
            onResizeEdgeChange: (T, WindowResizeEdge) -> Unit = { _, _ -> },
            captionBarHeight: Dp = defaultCaptionBarHeight(),
            captionButtonsVisible: Boolean = true,
            captionButtonHeight: Dp = captionBarHeight,
            captionButtonIsDarkTheme: Boolean = SaltTheme.configs.isDarkTheme,
            backgroundType: SaltWindowBackgroundType = SaltWindowBackgroundType.None,
            backgroundIsDarkTheme: Boolean = SaltTheme.configs.isDarkTheme,
            extraDisplayScale: Float = 1.0f,
            extraFontScale: Float = 1.0f,
            minimizeButtonEnabled: Boolean = true,
            maximizeOrRestoreButtonEnabled: Boolean = true,
            moveable: Boolean = true
        ): SaltWindowProperties<T> = SaltWindowProperties(
            minSize = minSize,
            onVisibleChange = onVisibleChange,
            onResizeEdgeChange = onResizeEdgeChange,
            captionBarHeight = captionBarHeight,
            captionButtonsVisible = captionButtonsVisible,
            captionButtonHeight = captionButtonHeight,
            captionButtonIsDarkTheme = captionButtonIsDarkTheme,
            backgroundType = backgroundType,
            backgroundIsDarkTheme = backgroundIsDarkTheme,
            extraDisplayScale = extraDisplayScale,
            extraFontScale = extraFontScale,
            minimizeButtonEnabled = minimizeButtonEnabled,
            maximizeOrRestoreButtonEnabled = maximizeOrRestoreButtonEnabled,
            moveable = moveable
        )
    }
}

internal fun defaultCaptionBarHeight(os: OS = OS.current): Dp = when (os) {
    is OS.MacOS -> MacOSWindowMetrics.defaultCaptionBarHeight
    else -> 40.dp
}

@UnstableSaltUiApi
// Caption appearance and interaction flags can change while the window stays open. Track readers
// so a caption update does not invalidate unrelated content throughout the window.
val LocalSaltWindowProperties = compositionLocalOf<SaltWindowProperties<Window>> {
    error("SaltWindowProperties is not provided")
}
