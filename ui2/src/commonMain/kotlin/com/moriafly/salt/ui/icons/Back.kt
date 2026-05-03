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

package com.moriafly.salt.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val SaltIcons.Back: ImageVector
    get() {
        if (_Back != null) {
            return _Back!!
        }
        _Back = ImageVector.Builder(
            name = "Back",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 48f,
            viewportHeight = 48f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(21.471f, 8.986f)
                arcTo(
                    1.5f,
                    1.5f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    20.439f,
                    9.439f
                )
                lineTo(6.439f, 23.439f)
                arcTo(
                    1.5f,
                    1.5f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    6.439f,
                    25.561f
                )
                lineTo(20.439f, 39.561f)
                arcTo(
                    1.5f,
                    1.5f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    22.561f,
                    37.439f
                )
                lineTo(11.121f, 26f)
                lineTo(40.5f, 26f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = true, isPositiveArc = false, 40.5f, 23f)
                lineTo(11.121f, 23f)
                lineTo(22.561f, 11.561f)
                arcTo(
                    1.5f,
                    1.5f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    21.471f,
                    8.986f
                )
                close()
            }
        }.build()

        return _Back!!
    }

@Suppress("ObjectPropertyName")
private var _Back: ImageVector? = null
