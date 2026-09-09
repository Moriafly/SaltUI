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

package com.moriafly.salt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Displays a label-value pair in a horizontal row.
 *
 * The label is displayed with a fixed width on the left, and the value is displayed on the right.
 * The value text is selectable.
 *
 * @param label The label text displayed on the left.
 * @param value The value text displayed on the right.
 * @param modifier [Modifier] to apply to this layout node.
 */
@UnstableSaltUiApi
@Composable
fun LabelValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SaltTheme.dimens.subPadding)
    ) {
        Text(
            text = label,
            modifier = Modifier
                .width(96.dp)
        )
        SelectionContainer {
            Text(
                text = value,
                color = SaltTheme.colors.subText
            )
        }
    }
}
