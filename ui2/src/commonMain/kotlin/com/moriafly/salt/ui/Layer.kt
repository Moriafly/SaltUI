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

package com.moriafly.salt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * A layered surface component with rounded corners, a subtle border, and an adaptive background.
 *
 * Layer is typically used for floating panels, overlays, or elevated content blocks.
 * The background color automatically adapts to the current theme (light/dark).
 *
 * @param modifier Modifier to be applied to the Layer.
 * @param content The content of the Layer.
 */
@UnstableSaltUiApi
@Composable
fun Layer(
    modifier: Modifier = Modifier,
    decorationEnabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val isDarkTheme = SaltTheme.configs.isDarkTheme
    val borderColor =
        if (isDarkTheme) {
            Color(0x12FFFFFF)
        } else {
            Color(0x0F000000)
        }
    val decorationModifier =
        if (decorationEnabled) {
            Modifier
                .clip(RoundedCornerShape(topStart = 12.dp))
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    val radius = 12.dp.toPx()
                    // Top border (excluding the arc area)
                    drawLine(
                        color = borderColor,
                        start = Offset(radius, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = strokeWidth
                    )
                    // Start (left) border (excluding the arc area)
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, radius),
                        end = Offset(0f, size.height),
                        strokeWidth = strokeWidth
                    )
                    // Top-start corner arc
                    drawArc(
                        color = borderColor,
                        startAngle = 180f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset.Zero,
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth)
                    )
                }
                .background(
                    if (isDarkTheme) {
                        Color(0x1E3A3A3A)
                    } else {
                        Color(0x64FFFFFF)
                    }
                )
        } else {
            Modifier
        }
    Surface(
        modifier = modifier
            .then(decorationModifier),
        content = content
    )
}
