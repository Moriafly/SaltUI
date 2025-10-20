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

@file:Suppress("unused")

package com.moriafly.salt.ui.window

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * The CaptionBarHitTest is a crucial component. It should be placed between the content and the
 * clickable components in the title bar. This is particularly useful in certain scenarios, as it
 * allows for both correctly responding to the clickable components in the title bar and handling
 * the drag events of the title bar.
 */
@UnstableSaltUiApi
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CaptionBarHitTest(
    modifier: Modifier = Modifier
) {
    val saltWindowProperties = LocalSaltWindowProperties.current
    val isHitTestInCaptionBarState = LocalIsHitTestInCaptionBarState.current
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(saltWindowProperties.captionBarHeight)
            .onPointerEvent(PointerEventType.Enter) {
                isHitTestInCaptionBarState.value = true
            }
            .onPointerEvent(PointerEventType.Exit) {
                isHitTestInCaptionBarState.value = false
            }
    )
}
