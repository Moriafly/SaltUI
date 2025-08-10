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

@file:Suppress("unused")

package com.moriafly.salt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * A customized Column composable with rounded corners and a border.
 *
 * This component applies a rounded corner clipping, a background color, and a thin border
 * to create a contained section in the UI. It is primarily used to wrap components from [Item]
 * as a visual enhancement layer, providing consistent styling for list items or grouped content.
 *
 * Typical use cases include:
 * - Creating card-like containers for [Item] components
 * - Adding elevation-like effects through background/border styling
 * - Grouping related UI elements with cohesive rounded corners
 *
 * @param modifier The modifier to be applied to the layout. Defaults to [Modifier] but extends
 * to fill the maximum available width with [Modifier.fillMaxWidth].
 * @param paddingValues The padding values to be applied to the layout.
 * @param color The background color of the container. Defaults to [SaltColors.subBackground].
 * When set to [Color.Unspecified], both background and border colors will be unspecified, allowing
 * full customization of the container's appearance.
 * @param content The composable content to be laid out in column format. Receives a [ColumnScope]
 * to enable use of column-specific layout modifiers. This should typically contain multiple [Item]
 * components from [Item] for consistent visual hierarchy.
 *
 * @see Column
 * @see Item
 */
@Composable
fun RoundedColumn(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(
        horizontal = SaltTheme.dimens.padding,
        vertical = SaltTheme.dimens.padding * 0.5f
    ),
    color: Color = SaltTheme.colors.subBackground,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(paddingValues)
            .clip(SaltTheme.shapes.medium)
            .background(color)
            .border(
                width = Dp.Hairline,
                color = if (color == Color.Unspecified) {
                    Color.Unspecified
                } else {
                    SaltTheme.colors.stroke
                },
                shape = SaltTheme.shapes.medium
            ),
        content = content
    )
}
