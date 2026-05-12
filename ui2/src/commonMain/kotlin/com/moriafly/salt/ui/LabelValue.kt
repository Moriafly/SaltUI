/*
 * Salt UI
 * Copyright (C) 2026 Moriafly
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
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
