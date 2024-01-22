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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @param corner corner
 * @param outerHorizontalPadding outerHorizontalPadding
 * @param innerHorizontalPadding innerHorizontalPadding
 */
@Stable
class SaltDimens(
    corner: Dp,
    outerHorizontalPadding: Dp,
    innerHorizontalPadding: Dp
) {
    val corner by mutableStateOf(corner, structuralEqualityPolicy())

    val outerHorizontalPadding by mutableStateOf(outerHorizontalPadding, structuralEqualityPolicy())

    val innerHorizontalPadding by mutableStateOf(innerHorizontalPadding, structuralEqualityPolicy())
}

fun saltDimens(
    corner: Dp = 12.dp,
    outerHorizontalPadding: Dp = 16.dp,
    innerHorizontalPadding: Dp = 16.dp
): SaltDimens = SaltDimens(
    corner = corner,
    outerHorizontalPadding = outerHorizontalPadding,
    innerHorizontalPadding = innerHorizontalPadding
)

