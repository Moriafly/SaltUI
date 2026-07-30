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

package com.moriafly.salt.ui.window

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * A desktop caption bar container that automatically reserves space for the system window
 * caption buttons (minimize / maximize / close).
 *
 * The component reads the current [SaltWindowInfo] from [LocalSaltWindowInfo] and applies
 * a [PaddingValues] that offsets the content by [SaltWindowInfo.captionButtonsFullWidth]
 * on the side where the native buttons are aligned ([CaptionButtonsAlign.Start] or
 * [CaptionButtonsAlign.End]). This prevents custom content from overlapping the system
 * chrome.
 *
 * The container also fixes its height to [SaltWindowInfo.captionBarHeight] and exposes
 * a [BoxScope] receiver, so callers can use `Modifier.align()` and other BoxScope-only
 * modifiers inside [content].
 *
 * @param modifier The modifier to be applied to the caption bar.
 * @param content The content of the caption bar, scoped to [BoxScope].
 */
@UnstableSaltUiApi
@Composable
fun DesktopCaptionBar(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val saltWindowInfo = LocalSaltWindowInfo.current
    val captionButtonsFullWidth = saltWindowInfo.captionButtonsFullWidth
    val padding = when (saltWindowInfo.captionButtonsAlign) {
        CaptionButtonsAlign.Start -> PaddingValues(start = captionButtonsFullWidth)
        CaptionButtonsAlign.End -> PaddingValues(end = captionButtonsFullWidth)
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(saltWindowInfo.captionBarHeight)
            .padding(padding),
        content = content
    )
}
