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

@file:Suppress("UnusedReceiverParameter")

package com.moriafly.salt.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val SaltIcons.Success: ImageVector
    get() {
        if (_Success != null) {
            return _Success!!
        }
        _Success = ImageVector.Builder(
            name = "Success",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 48f,
            viewportHeight = 48f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(24f, 4f)
                curveTo(12.972f, 4f, 4f, 12.972f, 4f, 24f)
                reflectiveCurveToRelative(8.972f, 20f, 20f, 20f)
                reflectiveCurveToRelative(20f, -8.972f, 20f, -20f)
                reflectiveCurveTo(35.028f, 4f, 24f, 4f)
                close()
                moveTo(32.561f, 20.561f)
                lineToRelative(-10f, 10f)
                curveTo(22.268f, 30.854f, 21.884f, 31f, 21.5f, 31f)
                reflectiveCurveToRelative(-0.768f, -0.146f, -1.061f, -0.439f)
                lineToRelative(-5f, -5f)
                curveToRelative(-0.586f, -0.586f, -0.586f, -1.535f, 0f, -2.121f)
                reflectiveCurveToRelative(1.535f, -0.586f, 2.121f, 0f)
                lineToRelative(3.939f, 3.939f)
                lineToRelative(8.939f, -8.939f)
                curveToRelative(0.586f, -0.586f, 1.535f, -0.586f, 2.121f, 0f)
                reflectiveCurveTo(33.146f, 19.975f, 32.561f, 20.561f)
                close()
            }
        }.build()

        return _Success!!
    }

@Suppress("ObjectPropertyName")
private var _Success: ImageVector? = null
