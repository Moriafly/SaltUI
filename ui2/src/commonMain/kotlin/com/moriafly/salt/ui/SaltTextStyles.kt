/*
 * Salt UI
 * Copyright (C) 2024 Moriafly
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

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Immutable
class SaltTextStyles(
    val main: TextStyle,
    val sub: TextStyle,
    val paragraph: TextStyle,
    val largeTitle: TextStyle
) {
    fun copy(
        main: TextStyle = this.main,
        sub: TextStyle = this.sub,
        paragraph: TextStyle = this.paragraph,
        largeTitle: TextStyle = this.largeTitle
    ): SaltTextStyles = SaltTextStyles(
        main = main,
        sub = sub,
        paragraph = paragraph,
        largeTitle = largeTitle
    )
}

fun saltTextStyles(
    main: TextStyle = DefaultTextStyle,
    sub: TextStyle = SubTextStyle,
    paragraph: TextStyle = ParagraphTextStyle,
    largeTitle: TextStyle = LargeTitleTextStyle
): SaltTextStyles = SaltTextStyles(
    main = main,
    sub = sub,
    paragraph = paragraph,
    largeTitle = largeTitle
)

internal expect val DefaultTextStyle: TextStyle

private val SubTextStyle: TextStyle
    get() = TextStyle(
        fontSize = 12.sp
    )

private val ParagraphTextStyle: TextStyle
    get() = TextStyle(
        fontSize = 16.sp,
        lineHeight = 1.5f.em
    )

private val LargeTitleTextStyle: TextStyle
    get() = TextStyle(
        fontSize = 32.sp
    )
