@file:Suppress("UNUSED")

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

package com.moriafly.salt.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Immutable
class SaltTextStyles (
    val main: TextStyle,
    val sub: TextStyle,
    val paragraph: TextStyle
)

fun saltTextStyles(
    main: TextStyle = DefaultTextStyle,
    sub: TextStyle = SubTextStyle,
    paragraph: TextStyle = ParagraphTextStyle
): SaltTextStyles = SaltTextStyles(
    main = main,
    sub = sub,
    paragraph = paragraph
)

private val DefaultTextStyle: TextStyle
    get() = TextStyle(
        fontSize = 16.sp
    )

private val SubTextStyle: TextStyle
    get() = TextStyle(
        fontSize = 12.sp
    )

private val ParagraphTextStyle: TextStyle
    get() = TextStyle(
        fontSize = 16.sp,
        lineHeight = 1.5f.em
    )