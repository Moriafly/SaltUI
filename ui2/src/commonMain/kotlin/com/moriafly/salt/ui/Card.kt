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

@file:Suppress("unused")

package com.moriafly.salt.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.Dp
import com.moriafly.salt.ui.material.subMaterial

/**
 * A card-like container that clips its content to [shape] and applies Salt UI styling.
 *
 * When [color] is specified, the card uses it as the fallback color for the sub-material
 * background and draws a hairline border with [SaltColors.stroke]. Passing [Color.Unspecified]
 * skips both the background material and the border, while preserving the shape clipping.
 *
 * @param modifier The modifier to apply to the card.
 * @param color The fallback background color used by the sub-material effect.
 * @param shape The shape used to clip the card and draw its border.
 * @param content The content of the card, with a [BoxScope] receiver for box-specific modifiers.
 *
 * @see Box
 */
@UnstableSaltUiApi
@Composable
fun Card(
    modifier: Modifier = Modifier,
    color: Color = SaltTheme.colors.subBackground,
    shape: Shape = SaltTheme.shapes.medium,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .thenIf(color.isSpecified) {
                this
                    .subMaterial(fallback = color)
                    .border(
                        width = Dp.Hairline,
                        color = SaltTheme.colors.stroke,
                        shape = shape
                    )
            },
        content = content
    )
}
