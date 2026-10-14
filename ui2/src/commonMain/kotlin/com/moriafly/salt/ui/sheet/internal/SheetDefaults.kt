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

package com.moriafly.salt.ui.sheet.internal

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import com.moriafly.salt.adaptive.window.SaltAdaptiveWindow
import com.moriafly.salt.adaptive.window.UnstableSaltAdaptiveWindowApi
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.sheet.SheetDisplayMode

internal object SheetDefaults {
    val BottomMaxWidth = 540.dp
    val FloatingMinWindowWidth = 600.dp
    val FloatingMinWindowHeight = 480.dp
    val FloatingMaxWidth = 540.dp
    val FloatingMaxHeight = 640.dp
    val WindowMargin = 16.dp
    val ActionsHorizontalPadding = 16.dp
    val ActionsTopPadding = 16.dp
    val ActionsBottomPadding = 16.dp
    val NoActionsBottomPadding = 8.dp
    val DragHandleTopInset = 8.dp
    val DragHandleWidth = 44.dp
    val DragHandleHeight = 3.dp
    val DragHandleTouchHeight = 48.dp
    const val DRAG_HANDLE_OPACITY = 0.3f
    const val SCRIM_OPACITY = 0.32f
    const val FLOATING_HEIGHT_TO_WIDTH_RATIO = 1.2f
}

@OptIn(UnstableSaltUiApi::class, UnstableSaltAdaptiveWindowApi::class)
internal fun SheetDisplayMode.isBottom(
    adaptiveWindow: SaltAdaptiveWindow,
    windowSize: DpSize
): Boolean = when (this) {
    SheetDisplayMode.Auto -> {
        val size = adaptiveWindow.windowSize.takeIf { it.isSpecified } ?: windowSize
        size.width < SheetDefaults.FloatingMinWindowWidth ||
            size.height < SheetDefaults.FloatingMinWindowHeight
    }
    SheetDisplayMode.Bottom -> true
    SheetDisplayMode.Floating -> false
}
