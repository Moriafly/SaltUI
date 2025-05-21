/**
 * Salt UI
 * Copyright (C) 2025 Moriafly
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

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * Vertical edge.
 *
 * Call with：
 *
 * ```kotlin
 * .graphicsLayer {
 *     compositingStrategy = CompositingStrategy.Offscreen
 * }
 * ```
 *
 * @param top Edge height for the top.
 * @param bottom Edge height for the bottom.
 */
@Stable
fun Modifier.verticalEdge(
    top: Dp,
    bottom: Dp
): Modifier = drawWithCache {
    onDrawWithContent {
        drawContent()

        val topEdgeOffset = top.toPx() / size.height
        val bottomEdgeOffset = 1f - bottom.toPx() / size.height

        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Transparent,
                topEdgeOffset to Color.Black,
                bottomEdgeOffset to Color.Black,
                1f to Color.Transparent
            ),
            blendMode = BlendMode.DstIn
        )
    }
}

/**
 * Horizontal edge.
 *
 * Call with:
 *
 * ```kotlin
 * .graphicsLayer {
 *     compositingStrategy = CompositingStrategy.Offscreen
 * }
 * ```
 *
 * @param start Edge width for the start.
 * @param end Edge width for the end.
 */
@Stable
fun Modifier.horizontalEdge(
    start: Dp,
    end: Dp
): Modifier = drawWithCache {
    onDrawWithContent {
        drawContent()

        val startEdgeOffset = start.toPx() / size.width
        val endEdgeOffset = 1f - end.toPx() / size.width

        drawRect(
            brush = Brush.horizontalGradient(
                0f to Color.Transparent,
                startEdgeOffset to Color.Black,
                endEdgeOffset to Color.Black,
                1f to Color.Transparent
            ),
            blendMode = BlendMode.DstIn
        )
    }
}
