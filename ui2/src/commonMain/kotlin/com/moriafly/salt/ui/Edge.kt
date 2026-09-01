/*
 * Salt UI
 * Copyright (C) 2025 Moriafly
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
    val topEdgeOffset = top.toPx() / size.height
    val bottomEdgeOffset = 1f - bottom.toPx() / size.height

    val brush = Brush.verticalGradient(
        0f to Color.Transparent,
        topEdgeOffset to Color.Black,
        bottomEdgeOffset to Color.Black,
        1f to Color.Transparent
    )

    onDrawWithContent {
        drawContent()

        drawRect(
            brush = brush,
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
    val startEdgeOffset = start.toPx() / size.width
    val endEdgeOffset = 1f - end.toPx() / size.width

    val brush = Brush.horizontalGradient(
        0f to Color.Transparent,
        startEdgeOffset to Color.Black,
        endEdgeOffset to Color.Black,
        1f to Color.Transparent
    )

    onDrawWithContent {
        drawContent()

        drawRect(
            brush = brush,
            blendMode = BlendMode.DstIn
        )
    }
}
