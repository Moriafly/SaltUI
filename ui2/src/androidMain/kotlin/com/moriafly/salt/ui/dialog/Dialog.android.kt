/*
 * Salt UI
 * Copyright (C) 2024 Moriafly
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

package com.moriafly.salt.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * The basic bottom sheet dialog has default corner background and vertical padding
 */
@UnstableSaltUiApi
@Composable
fun BasicBottomSheetDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    BottomSheetDialog(
        onDismissRequest = onDismissRequest,
        properties = BottomSheetDialogProperties(
            enableEdgeToEdge = true,
            behaviorProperties = BottomSheetBehaviorProperties(
                state = BottomSheetBehaviorProperties.State.HalfExpanded,
                skipCollapsed = true,
                halfExpandedRatio = 0.66f
            )
        ),
        content = {
            Column(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(color = SaltTheme.colors.background)
            ) {
                content()
            }
        }
    )
}

@Deprecated("Use BasicBottomSheetDialog", replaceWith = ReplaceWith("BasicBottomSheetDialog"))
@UnstableSaltUiApi
@Composable
fun ColumnBottomSheetDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    BottomSheetDialog(
        onDismissRequest = onDismissRequest,
        properties = BottomSheetDialogProperties(
            enableEdgeToEdge = true,
            behaviorProperties = BottomSheetBehaviorProperties(
                state = BottomSheetBehaviorProperties.State.HalfExpanded,
                skipCollapsed = true,
                halfExpandedRatio = 0.66f
            )
        ),
        content = {
            Column(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(color = SaltTheme.colors.background)
            ) {
                content()
            }
        }
    )
}