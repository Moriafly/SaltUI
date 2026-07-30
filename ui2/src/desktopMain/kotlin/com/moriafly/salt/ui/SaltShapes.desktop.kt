/*
 * Salt UI
 * Copyright (C) 2026 Moriafly
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

@file:Suppress("ktlint:standard:filename")

package com.moriafly.salt.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

actual object SaltShapesDefaults {
    actual val small: Shape
        get() = RoundedCornerShape(4.dp)
    actual val medium: Shape
        get() = RoundedCornerShape(6.dp)
    actual val large: Shape
        get() = RoundedCornerShape(10.dp)
}
