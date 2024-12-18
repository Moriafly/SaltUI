/*
 * Salt UI
 * Copyright (C) 2023 Moriafly
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

/**
 * Colors for SaltUI.
 *
 * @param highlight highlight color.
 * @param text main text color.
 * @param subText sub text color.
 * @param background main background color.
 * @param subBackground sub background color.
 * @param popup background color for UI elements like [com.moriafly.salt.ui.popup.PopupMenu].
 * @param stroke stroke color.
 */
@Stable
class SaltColors(
    highlight: Color,
    text: Color,
    subText: Color,
    background: Color,
    subBackground: Color,
    popup: Color,
    stroke: Color
) {
    val highlight by mutableStateOf(highlight, structuralEqualityPolicy())
    val text by mutableStateOf(text, structuralEqualityPolicy())
    val subText by mutableStateOf(subText, structuralEqualityPolicy())
    val background by mutableStateOf(background, structuralEqualityPolicy())
    val subBackground by mutableStateOf(subBackground, structuralEqualityPolicy())
    val popup by mutableStateOf(popup, structuralEqualityPolicy())
    val stroke by mutableStateOf(stroke, structuralEqualityPolicy())

    fun copy(
        highlight: Color = this.highlight,
        text: Color = this.text,
        subText: Color = this.subText,
        background: Color = this.background,
        subBackground: Color = this.subBackground,
        popup: Color = this.popup,
        stroke: Color = this.stroke
    ): SaltColors = SaltColors(
        highlight = highlight,
        text = text,
        subText = subText,
        background = background,
        subBackground = subBackground,
        popup = popup,
        stroke = stroke
    )
}

fun lightSaltColors(
    highlight: Color = Color(0xFF0470E6),
    text: Color = Color(0xFF1E1715),
    subText: Color = Color(0xFF8C8C8C),
    background: Color = Color(0xFFF7F9FA),
    subBackground: Color = Color(0xFFFFFFFF),
    popup: Color = subBackground.compositeOver(background),
    stroke: Color = subText.copy(alpha = 0.15f)
): SaltColors = SaltColors(
    highlight = highlight,
    text = text,
    subText = subText,
    background = background,
    subBackground = subBackground,
    popup = popup,
    stroke = stroke
)

fun darkSaltColors(
    highlight: Color = Color(0xFF1478C8),
    text: Color = Color(0xFFEBEEF1),
    subText: Color = Color(0xBFE1E6EB),
    background: Color = Color(0xFF0C0C0C),
    subBackground: Color = Color(0xFF191919),
    popup: Color = subBackground.compositeOver(background),
    stroke: Color = subText.copy(alpha = 0.1f)
): SaltColors = SaltColors(
    highlight = highlight,
    text = text,
    subText = subText,
    background = background,
    subBackground = subBackground,
    popup = popup,
    stroke = stroke
)
