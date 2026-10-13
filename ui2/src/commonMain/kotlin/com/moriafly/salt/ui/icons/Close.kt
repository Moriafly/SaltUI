/*
 * SaltUI
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

@file:Suppress("unused", "UnusedReceiverParameter")

package com.moriafly.salt.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val SaltIcons.Close: ImageVector
    get() {
        if (_Close != null) {
            return _Close!!
        }
        _Close = ImageVector.Builder(
            name = "Close",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 48f,
            viewportHeight = 48f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(39.486f, 6.979f)
                arcTo(
                    1.5f,
                    1.5f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    38.439f,
                    7.439f
                )
                lineTo(24f, 21.879f)
                lineTo(9.561f, 7.439f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8.484f, 6.984f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 7.439f, 9.561f)
                lineTo(21.879f, 24f)
                lineTo(7.439f, 38.439f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = true, isPositiveArc = false, 9.561f, 40.561f)
                lineTo(24f, 26.121f)
                lineTo(38.439f, 40.561f)
                arcTo(
                    1.5f,
                    1.5f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    40.561f,
                    38.439f
                )
                lineTo(26.121f, 24f)
                lineTo(40.561f, 9.561f)
                arcTo(
                    1.5f,
                    1.5f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    39.486f,
                    6.979f
                )
                close()
            }
        }.build()

        return _Close!!
    }

@Suppress("ObjectPropertyName")
private var _Close: ImageVector? = null
