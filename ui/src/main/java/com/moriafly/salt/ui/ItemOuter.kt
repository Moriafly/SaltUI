@file:Suppress("UNUSED")

/**
 * SaltUI
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

package com.moriafly.salt.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * For Item Outer
 */
@Composable
fun ItemOuterTextButton(
    onClick: () -> Unit,
    text: String,
    textColor: Color = Color.White,
    backgroundColor: Color = SaltTheme.colors.highlight
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = Dimens.outerHorizontalPadding + Dimens.innerHorizontalPadding, vertical = Dimens.innerVerticalPadding),
        text = text,
        textColor = textColor,
        backgroundColor = backgroundColor
    )
}