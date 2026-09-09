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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.Dp
import com.moriafly.salt.ui.material.subMaterial

/**
 * A card container that arranges [content] vertically.
 *
 * The card always clips its content to [shape]. When [color] is specified, the card applies the
 * sub-material effect and uses [color] as its fallback background. The [border] is configured
 * independently and follows the same [shape]. This component does not add content padding.
 *
 * @param modifier [Modifier] applied to the card.
 * @param shape Shape used to clip the card and draw its [border].
 * @param color Fallback background color for the sub-material effect. Pass [Color.Unspecified] to
 * omit the material effect.
 * @param border Stroke drawn around the card, or `null` for no border.
 * @param content Content arranged vertically inside the card, with a [ColumnScope] receiver.
 *
 * @see CardDefaults
 * @see Column
 */
@UnstableSaltUiApi
@Composable
fun Card(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    color: Color = CardDefaults.color,
    border: BorderStroke? = CardDefaults.border,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(shape)
            .thenIf(color.isSpecified) {
                subMaterial(fallback = color)
            }
            .thenIf(border != null) {
                border(
                    border = border,
                    shape = shape
                )
            },
        content = content
    )
}

/**
 * Default appearance values for [Card] and components built on it.
 */
@UnstableSaltUiApi
object CardDefaults {
    /**
     * Default card shape, resolved from the current [SaltTheme].
     */
    val shape: Shape
        @Composable get() = SaltTheme.shapes.medium

    /**
     * Default fallback background color, resolved from the current [SaltTheme].
     */
    val color: Color
        @Composable get() = SaltTheme.colors.subBackground

    /**
     * Default hairline border, using the stroke color from the current [SaltTheme].
     */
    val border: BorderStroke
        @Composable get() = BorderStroke(
            width = Dp.Hairline,
            color = SaltTheme.colors.stroke
        )
}
