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

package com.moriafly.salt.ui.platform.macos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowScope
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.CaptionButtonsAlign
import com.moriafly.salt.ui.window.LocalSaltWindowInfo
import com.moriafly.salt.ui.window.SaltWindowInfo
import com.moriafly.salt.ui.window.SaltWindowProperties

@UnstableSaltUiApi
@Composable
internal fun DialogWindowScope.MacOSSaltDialogWindowFrame(
    properties: SaltWindowProperties<ComposeDialog>,
    content: @Composable DialogWindowScope.() -> Unit
) {
    CompositionLocalProvider(
        LocalSaltWindowInfo provides SaltWindowInfo(
            captionBarHeight = properties.captionBarHeight,
            captionButtonsAlign = CaptionButtonsAlign.Start,
            captionButtonsFullWidth = MacOSSaltDialogWindowFrameDefaults.CaptionButtonsFullWidth
        )
    ) {
        val styler = remember(window) {
            MacOSSaltWindowStyler(window)
        }

        LaunchedEffect(properties.captionBarHeight) {
            styler.disableTitleBar(properties.captionBarHeight.value)
        }

        content()
    }
}

private object MacOSSaltDialogWindowFrameDefaults {
    val CaptionButtonsFullWidth = 80.dp
}
