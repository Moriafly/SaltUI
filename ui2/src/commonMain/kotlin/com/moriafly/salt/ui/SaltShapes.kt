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
