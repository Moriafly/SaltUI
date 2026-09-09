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

package com.moriafly.salt.ui.internal

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.rectangle

/** A rounded rectangle with curvature that eases smoothly into its straight edges. */
@Immutable
internal data class SmoothRoundedRectangleShape(
    val radius: Dp,
    val smoothing: Float
) : Shape {
    init {
        require(smoothing in 0f..1f) { "smoothing must be between 0 and 1" }
    }

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        if (size.isEmpty()) {
            return Outline.Rectangle(Rect(0f, 0f, size.width, size.height))
        }

        val radiusPx = with(density) { radius.toPx() }
            .coerceIn(0f, size.minDimension / 2f)
        if (radiusPx == 0f) {
            return Outline.Rectangle(Rect(0f, 0f, size.width, size.height))
        }

        val polygon = RoundedPolygon.rectangle(
            width = size.width,
            height = size.height,
            rounding = CornerRounding(
                radius = radiusPx,
                smoothing = smoothing
            ),
            centerX = size.width / 2f,
            centerY = size.height / 2f
        )
        val firstCubic = polygon.cubics.first()
        val path = Path().apply {
            moveTo(firstCubic.anchor0X, firstCubic.anchor0Y)
            polygon.cubics.forEach { cubic ->
                cubicTo(
                    cubic.control0X,
                    cubic.control0Y,
                    cubic.control1X,
                    cubic.control1Y,
                    cubic.anchor1X,
                    cubic.anchor1Y
                )
            }
            close()
        }
        return Outline.Generic(path)
    }
}
