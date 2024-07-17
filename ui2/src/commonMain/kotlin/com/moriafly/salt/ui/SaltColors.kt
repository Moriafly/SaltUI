/**
 * SaltUI
 * Copyright (C) 2023 Moriafly
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

@file:Suppress("UNUSED")

package com.moriafly.salt.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color

/**
 * @param highlight highlight color
 * @param text main text color
 * @param subText sub text color
 * @param background main background color
 * @param subBackground sub background color
 */
@Stable
class SaltColors(
    highlight: Color,
    text: Color,
    subText: Color,
    background: Color,
    subBackground: Color
) {
    val highlight by mutableStateOf(highlight, structuralEqualityPolicy())
    val text by mutableStateOf(text, structuralEqualityPolicy())
    val subText by mutableStateOf(subText, structuralEqualityPolicy())
    val background by mutableStateOf(background, structuralEqualityPolicy())
    val subBackground by mutableStateOf(subBackground, structuralEqualityPolicy())
}

fun lightSaltColors(
    highlight: Color = Color(0xFF0470E6),
    text: Color = Color(0xFF1E1715),
    subText: Color = Color(0xFF8C8C8C),
    background: Color = Color(0xFFF7F9FA),
    subBackground: Color = Color(0xFFFFFFFF)
): SaltColors = SaltColors(
    highlight = highlight,
    text = text,
    subText = subText,
    background = background,
    subBackground = subBackground
)

fun darkSaltColors(
    highlight: Color = Color(0xFF1478C8),
    text: Color = Color(0xFFEBEEF1),
    subText: Color = Color(0xBFE1E6EB),
    background: Color = Color(0xFF0C0C0C),
    subBackground: Color = Color(0xFF191919)
): SaltColors = SaltColors(
    highlight = highlight,
    text = text,
    subText = subText,
    background = background,
    subBackground = subBackground
)