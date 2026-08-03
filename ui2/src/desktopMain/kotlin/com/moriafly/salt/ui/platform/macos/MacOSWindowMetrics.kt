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

package com.moriafly.salt.ui.platform.macos

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.CaptionButtonsAlign
import com.moriafly.salt.ui.window.SaltWindowInfo
import com.moriafly.salt.ui.window.SaltWindowProperties
import java.awt.Window

/**
 * Native macOS window geometry expressed from the visible window-button artwork. The default
 * title bar is 14pt of artwork plus 19pt of padding on both sides; the 16pt AppKit control frame
 * remains centered inside the same 52pt height.
 */
internal object MacOSWindowMetrics {
    const val WINDOW_BUTTON_ARTWORK_DIAMETER = 14.0
    const val WINDOW_BUTTON_VERTICAL_PADDING = 19.0
    const val DEFAULT_CAPTION_BAR_HEIGHT =
        WINDOW_BUTTON_ARTWORK_DIAMETER + WINDOW_BUTTON_VERTICAL_PADDING * 2.0
    const val WINDOW_BUTTON_FRAME_WIDTH = 14.0
    const val WINDOW_BUTTON_FRAME_HEIGHT = 16.0
    const val WINDOW_BUTTON_BOUNDS_WIDTH = 12.0
    const val WINDOW_BUTTON_BOUNDS_WIDTH_RATIO =
        WINDOW_BUTTON_BOUNDS_WIDTH / WINDOW_BUTTON_FRAME_WIDTH
    const val WINDOW_BUTTON_BOUNDS_HEIGHT_RATIO =
        WINDOW_BUTTON_ARTWORK_DIAMETER / WINDOW_BUTTON_FRAME_HEIGHT
    const val WINDOW_BUTTON_LEADING = 19.0
    const val WINDOW_BUTTON_SPACING = 9.0
    const val LEGACY_HORIZONTAL_BUTTON_OFFSET = 20.0
    const val WINDOW_BUTTON_TRAILING_PADDING = 13.0
    const val CAPTION_BUTTONS_FULL_WIDTH =
        WINDOW_BUTTON_LEADING +
            WINDOW_BUTTON_FRAME_WIDTH * 3.0 +
            WINDOW_BUTTON_SPACING * 2.0 +
            WINDOW_BUTTON_TRAILING_PADDING

    val defaultCaptionBarHeight = DEFAULT_CAPTION_BAR_HEIGHT.dp
    val captionButtonsFullWidth = CAPTION_BUTTONS_FULL_WIDTH.dp
}

/**
 * Converts a Compose title-bar height to the logical coordinates shared by AWT and AppKit. Compose
 * density can include [SaltWindowProperties.extraDisplayScale], while the display transform only
 * describes the system scale, so passing the raw [Dp.value] to AppKit would create a different
 * native double-click region whenever an application scale other than 100% is used.
 */
@Composable
internal fun macOSCaptionBarHeightInWindowCoordinates(
    window: Window,
    captionBarHeight: Dp
): Float = captionBarHeightInWindowCoordinates(
    captionBarHeight = captionBarHeight,
    density = LocalDensity.current.density,
    displayScale = window.graphicsConfiguration?.defaultTransform?.scaleY ?: 1.0
)

internal fun captionBarHeightInWindowCoordinates(
    captionBarHeight: Dp,
    density: Float,
    displayScale: Double
): Float {
    val safeDisplayScale = displayScale.takeIf { it.isFinite() && it > 0.0 } ?: 1.0
    return captionBarHeight.value * density / safeDisplayScale.toFloat()
}

@OptIn(UnstableSaltUiApi::class)
internal fun macOSSaltWindowInfo(captionBarHeight: Dp) = SaltWindowInfo(
    captionBarHeight = captionBarHeight,
    captionButtonsAlign = CaptionButtonsAlign.Start,
    captionButtonsFullWidth = MacOSWindowMetrics.captionButtonsFullWidth
)
