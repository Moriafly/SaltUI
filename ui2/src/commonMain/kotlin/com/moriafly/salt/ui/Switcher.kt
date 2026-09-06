/*
 * Salt UI
 * Copyright (C) 2024 Moriafly
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

import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Displays an animated switch indicator using [SaltTheme] colors.
 *
 * The unchecked ring morphs into a vertical bar when checked. The containing component owns
 * input handling and accessibility semantics; this indicator only renders [state].
 *
 * Geometry and easing are adapted from Alexander Kolpakov's Apache-2.0-licensed
 * [SwitcherX](https://github.com/bitvale/Switcher/tree/568ea7a) implementation.
 *
 * @param state Whether the indicator is checked.
 * @param modifier Modifier applied to the indicator.
 */
@Composable
fun Switcher(
    state: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(
        targetState = state,
        label = "Switcher"
    )
    val expansion by transition.animateFloat(
        transitionSpec = {
            tween(
                durationMillis = SWITCHER_MORPH_DURATION_MILLIS,
                easing = if (targetState) SwitcherOnEasing else SwitcherOffEasing
            )
        },
        label = "Icon expansion"
    ) { checked ->
        if (checked) 0f else 1f
    }
    val position by transition.animateFloat(
        transitionSpec = {
            tween(
                durationMillis = SWITCHER_TRAVEL_DURATION_MILLIS,
                easing = SwitcherTravelEasing
            )
        },
        label = "Icon position"
    ) { checked ->
        if (checked) 1f else 0f
    }
    val onColor = SaltTheme.colors.highlight
    val offColor = SaltTheme.colors.subText.copy(alpha = 0.1f)
    val backgroundColor by transition.animateValue(
        typeConverter = SwitcherColorConverter,
        transitionSpec = {
            tween(
                durationMillis = SWITCHER_COLOR_DURATION_MILLIS,
                easing = SwitcherTravelEasing
            )
        },
        label = "Track color"
    ) { checked ->
        if (checked) onColor else offColor
    }
    val iconColor = SaltTheme.colors.onHighlight
    val direction = LocalLayoutDirection.current

    Box(
        modifier = modifier
            .size(46.dp, 26.dp)
            .drawWithCache {
                val iconPaint = Paint().apply { color = iconColor }
                val clearPaint = Paint().apply { blendMode = BlendMode.Clear }
                val layerPaint = Paint()
                val layerBounds = Rect(
                    Offset.Zero,
                    size
                )
                val radius = size.height / 2f
                val iconRadius = radius * 0.6f
                val clipRadius = iconRadius / 2.25f
                val collapsedWidth = iconRadius - clipRadius
                val iconHeight = iconRadius * 2f
                val iconCenterX = size.width - radius
                val iconTop = (size.height - iconHeight) / 2f
                val iconBottom = size.height - iconTop
                onDrawBehind {
                    if (size.width <= 0f || size.height <= 0f) return@onDrawBehind

                    // The track contracts during travel; icon deformation continues independently.
                    val targetPosition = if (state) 1f else 0f
                    val inset = if (position != targetPosition) SWITCHER_TRACK_INSET_PX else 0f
                    val offset = (iconRadius - collapsedWidth / 2f) * expansion
                    val left = iconCenterX - collapsedWidth / 2f - offset
                    val right = iconCenterX + collapsedWidth / 2f + offset
                    val clipOffset = clipRadius * expansion
                    drawRoundRect(
                        color = backgroundColor,
                        topLeft = Offset(inset, inset),
                        size = Size(
                            width = size.width - inset * 2f,
                            height = size.height - inset * 2f
                        ),
                        cornerRadius = CornerRadius(radius)
                    )
                    drawIntoCanvas { canvas ->
                        // Clearing an isolated icon layer preserves the translucent track below it.
                        canvas.withSaveLayer(
                            bounds = layerBounds,
                            paint = layerPaint
                        ) {
                            if (direction == LayoutDirection.Rtl) {
                                canvas.translate(size.width, 0f)
                                canvas.scale(-1f, 1f)
                            }
                            canvas.translate(
                                dx = -(size.width - size.height) * (1f - position),
                                dy = 0f
                            )
                            canvas.drawRoundRect(
                                left = left,
                                top = iconTop,
                                right = right,
                                bottom = iconBottom,
                                radiusX = radius,
                                radiusY = radius,
                                paint = iconPaint
                            )
                            if (clipOffset * 2f > collapsedWidth) {
                                canvas.drawRoundRect(
                                    left = iconCenterX - clipOffset,
                                    top = center.y - clipOffset,
                                    right = iconCenterX + clipOffset,
                                    bottom = center.y + clipOffset,
                                    radiusX = iconRadius,
                                    radiusY = iconRadius,
                                    paint = clearPaint
                                )
                            }
                        }
                    }
                }
            }
    )
}

private const val SWITCHER_MORPH_DURATION_MILLIS = 800
private const val SWITCHER_TRAVEL_DURATION_MILLIS = 200
private const val SWITCHER_COLOR_DURATION_MILLIS = 300
private const val SWITCHER_TRACK_INSET_PX = 2f

private val SwitcherTravelEasing = Easing { fraction ->
    (cos((fraction + 1f) * PI) / 2.0).toFloat() + 0.5f
}

private val SwitcherOnEasing = switcherBounceEasing(
    amplitude = 0.15,
    frequency = 12.0
)
private val SwitcherOffEasing = switcherBounceEasing(
    amplitude = 0.2,
    frequency = 14.5
)

private fun switcherBounceEasing(
    amplitude: Double,
    frequency: Double
): Easing =
    Easing { fraction ->
        (1.0 - exp(-fraction / amplitude) * cos(frequency * fraction)).toFloat()
    }

/**
 * Preserves SwitcherX's gamma-2.2 RGB interpolation and 8-bit color precision.
 */
private val SwitcherColorConverter = TwoWayConverter<Color, AnimationVector4D>(
    convertToVector = { color ->
        val argb = color.toArgb()

        fun channel(shift: Int): Float = ((argb ushr shift) and 255) / 255f

        AnimationVector4D(
            channel(16).toDouble().pow(2.2).toFloat(),
            channel(8).toDouble().pow(2.2).toFloat(),
            channel(0).toDouble().pow(2.2).toFloat(),
            channel(24)
        )
    },
    convertFromVector = { vector ->
        fun channel(value: Float): Int =
            (value.coerceIn(0f, 1f).toDouble().pow(1.0 / 2.2).toFloat() * 255f).roundToInt()

        Color(
            red = channel(vector.v1),
            green = channel(vector.v2),
            blue = channel(vector.v3),
            alpha = (vector.v4.coerceIn(0f, 1f) * 255f).roundToInt()
        )
    }
)
