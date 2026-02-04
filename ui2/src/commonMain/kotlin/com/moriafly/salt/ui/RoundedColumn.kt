/*
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
import com.moriafly.salt.ui.material.subMaterial

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
@OptIn(UnstableSaltUiApi::class)
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
            .thenIf(color != Color.Unspecified) {
                this
                    .subMaterial(fallback = color)
                    .border(
                        width = Dp.Hairline,
                        color = SaltTheme.colors.stroke,
                        shape = SaltTheme.shapes.medium
                    )
            },
        content = content
    )
}

/**
 * A specialized version of [RoundedColumn] that uses predefined padding configurations.
 * This overload simplifies usage by providing common padding presets based on the
 * layout context (e.g., standard grouping vs. nested list items).
 *
 * @param type The preset type that determines the [PaddingValues] to be applied.
 * @param modifier The modifier to be applied to the layout.
 * @param color The background color of the container.
 * @param content The composable content within the column.
 */
@UnstableSaltUiApi
@Composable
fun RoundedColumn(
    type: RoundedColumnType,
    modifier: Modifier = Modifier,
    color: Color = SaltTheme.colors.subBackground,
    content: @Composable ColumnScope.() -> Unit
) {
    RoundedColumn(
        modifier = modifier,
        paddingValues = when (type) {
            RoundedColumnType.Default -> PaddingValues(
                horizontal = SaltTheme.dimens.padding,
                vertical = SaltTheme.dimens.padding * 0.5f
            )

            RoundedColumnType.InList -> PaddingValues(
                horizontal = SaltTheme.dimens.padding,
                vertical = SaltDimens.RoundedColumnInListItemPadding
            )
        },
        color = color,
        content = content
    )
}

/**
 * Defines the layout behavior and spacing presets for a [RoundedColumn].
 */
@UnstableSaltUiApi
enum class RoundedColumnType {
    /**
     * Standard layout with balanced horizontal and vertical padding.
     * Suitable for independent card-like sections.
     * Mainly used for building standard settings screens.
     */
    Default,

    /**
     * Optimized for use within list containers (e.g., LazyColumn).
     * Applies specific vertical compensation for better alignment with list edges.
     * Mainly used for constructing items in lists without large rounded corner hierarchies inside,
     * such as folder lists.
     *
     * @see SaltDimens.RoundedColumnInListEdgePadding
     */
    InList
}
