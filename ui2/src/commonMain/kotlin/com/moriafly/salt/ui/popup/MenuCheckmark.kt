/*
 * Fluent UI System Icons, ic_fluent_checkmark_24_regular.svg
 * https://github.com/microsoft/fluentui-system-icons
 *
 * MIT License
 *
 * Copyright (c) 2020 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.moriafly.salt.ui.popup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi

private val MenuCheckmark = ImageVector.Builder(
    name = "MenuCheckmark",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).addPath(
    pathData = PathParser().parsePathString(
        "M4.53033 12.9697C4.23744 12.6768 3.76256 12.6768 3.46967 12.9697" +
            "C3.17678 13.2626 3.17678 13.7374 3.46967 14.0303L7.96967 18.5303" +
            "C8.26256 18.8232 8.73744 18.8232 9.03033 18.5303L20.0303 7.53033" +
            "C20.3232 7.23744 20.3232 6.76256 20.0303 6.46967" +
            "C19.7374 6.17678 19.2626 6.17678 18.9697 6.46967L8.5 16.9393L4.53033 12.9697Z"
    ).toNodes(),
    fill = SolidColor(Color.Black)
).build()

@OptIn(UnstableSaltUiApi::class)
@Composable
internal fun MenuSelectionMark(marked: Boolean, size: Dp) {
    if (marked) {
        Icon(
            imageVector = MenuCheckmark,
            contentDescription = null,
            modifier = Modifier.size(size),
            tint = SaltTheme.colors.text
        )
    } else {
        Spacer(Modifier.width(size))
    }
}
