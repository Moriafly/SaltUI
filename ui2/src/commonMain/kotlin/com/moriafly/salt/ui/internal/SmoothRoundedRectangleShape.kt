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
