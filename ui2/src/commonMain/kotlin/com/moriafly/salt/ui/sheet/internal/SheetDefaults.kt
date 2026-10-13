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

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.sheet.SheetPresentation

internal object SheetDefaults {
    val AdaptiveBreakpoint = 600.dp
    val BottomMaxWidth = 640.dp
    val CenteredMaxWidth = 540.dp
    val CenteredMaxHeight = 640.dp
    val WindowMargin = 16.dp
    val CornerRadius = 34.dp
    val ActionsHorizontalPadding = 16.dp
    val ActionsTopPadding = 16.dp
    val ActionsBottomPadding = 16.dp
    val NoActionsBottomPadding = 8.dp
    val CenteredShape = RoundedCornerShape(size = CornerRadius)
    val BottomShape = RoundedCornerShape(
        topStart = CornerRadius,
        topEnd = CornerRadius
    )
    val DragHandleTopInset = 8.dp
    val DragHandleWidth = 44.dp
    val DragHandleHeight = 3.dp
    val DragHandleTouchHeight = 48.dp
    val CloseButtonInset = 16.dp
    const val DRAG_HANDLE_OPACITY = 0.3f
    const val SCRIM_OPACITY = 0.32f
    const val CENTERED_HEIGHT_TO_WIDTH_RATIO = 1.2f
}

@OptIn(com.moriafly.salt.ui.UnstableSaltUiApi::class)
internal fun SheetPresentation.isBottom(
    windowSize: DpSize
): Boolean = when (this) {
    SheetPresentation.Adaptive ->
        windowSize.width < SheetDefaults.AdaptiveBreakpoint &&
            windowSize.height >= windowSize.width
    SheetPresentation.Bottom -> true
    SheetPresentation.Centered -> false
}
