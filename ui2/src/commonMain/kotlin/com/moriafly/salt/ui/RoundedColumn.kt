/**
 * Salt UI
 * Copyright (C) 2023 Moriafly
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

@file:Suppress("UNUSED")

package com.moriafly.salt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * Column with rounded radius
 *
 * @param modifier The modifier to be applied to the column
 * @param color The background color of the column
 * @param content The content of the column
 */
@Composable
fun RoundedColumn(
    modifier: Modifier = Modifier,
    color: Color = SaltTheme.colors.subBackground,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SaltTheme.dimens.padding, vertical = SaltTheme.dimens.padding * 0.5f)
            .clip(RoundedCornerShape(SaltTheme.dimens.corner))
            .background(color)
            .border(
                width = Dp.Hairline,
                color = if (color == Color.Unspecified) Color.Unspecified else SaltTheme.colors.stroke,
                shape = RoundedCornerShape(SaltTheme.dimens.corner)
            ),
        content = content
    )
}