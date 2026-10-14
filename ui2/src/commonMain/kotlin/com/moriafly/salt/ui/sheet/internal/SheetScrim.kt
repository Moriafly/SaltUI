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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.clearAndSetSemantics

@Composable
internal fun SheetScrim(
    alpha: () -> Float,
    onDismissRequest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind { drawSheetScrim(alpha()) }
            .focusProperties { canFocus = false }
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onDismissRequest
            )
            .clearAndSetSemantics { }
    )
}

/**
 * Shared drawing for the background scrim and dimmed outgoing surfaces on every platform.
 */
internal fun DrawScope.drawSheetScrim(alpha: Float) {
    if (alpha > 0f) drawRect(color = Color.Black.copy(alpha = alpha))
}
