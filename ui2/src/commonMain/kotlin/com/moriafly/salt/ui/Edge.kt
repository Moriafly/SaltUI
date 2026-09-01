/*
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
import androidx.compose.ui.unit.dp

/**
 * Fades the content to transparent at its top and bottom edges.
 *
 * At each edge, the content transitions between complete transparency at the boundary and its
 * original opacity over the distance specified by [top] or [bottom]. A distance of zero disables
 * the fade for that edge.
 *
 * This modifier uses an alpha mask and must be placed after an offscreen graphics layer:
 *
 * ```kotlin
 * Modifier
 *     .graphicsLayer {
 *         compositingStrategy = CompositingStrategy.Offscreen
 *     }
 *     .verticalEdge(
 *         top = 16.dp,
 *         bottom = 16.dp
 *     )
 * ```
 *
 * @param top height of the fade at the top edge.
 * @param bottom height of the fade at the bottom edge.
 */
@Stable
fun Modifier.verticalEdge(
    top: Dp = 0.dp,
    bottom: Dp = 0.dp
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
 * Fades the content to transparent at its start and end edges.
 *
 * At each edge, the content transitions between complete transparency at the boundary and its
 * original opacity over the distance specified by [start] or [end]. A distance of zero disables
 * the fade for that edge.
 *
 * This modifier uses an alpha mask and must be placed after an offscreen graphics layer:
 *
 * ```kotlin
 * Modifier
 *     .graphicsLayer {
 *         compositingStrategy = CompositingStrategy.Offscreen
 *     }
 *     .horizontalEdge(
 *         start = 16.dp,
 *         end = 16.dp
 *     )
 * ```
 *
 * @param start width of the fade at the start edge.
 * @param end width of the fade at the end edge.
 */
@Stable
fun Modifier.horizontalEdge(
    start: Dp = 0.dp,
    end: Dp = 0.dp
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
