/**
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Shape

/**
 * # Shapes of Salt UI
 *
 * @param small small shape, commonly used within elements like [RoundedColumn].
 * @param medium medium shape.
 * @param large large shape.
 */
@Stable
class SaltShapes(
    small: Shape,
    medium: Shape,
    large: Shape
) {
    val small by mutableStateOf(small)
    val medium by mutableStateOf(medium)
    val large by mutableStateOf(large)

    companion object
}

fun SaltShapes.Companion.default(
    small: Shape = SaltShapesDefaults.small,
    medium: Shape = SaltShapesDefaults.medium,
    large: Shape = SaltShapesDefaults.large
): SaltShapes = SaltShapes(
    small = small,
    medium = medium,
    large = large
)

expect object SaltShapesDefaults {
    val small: Shape
    val medium: Shape
    val large: Shape
}
