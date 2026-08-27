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

package com.moriafly.salt.ui.platform.linux

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.SaltTheme

/**
 * A 1 dp border drawn along the inner edge of the window, similar to the border of
 * JetBrains IDE windows on Linux, visually separating the undecorated window from
 * the desktop background.
 *
 * This composable only draws and never intercepts pointer events, so it does not
 * affect the resize edges of [UndecoratedWindowResizer].
 */
@Composable
internal fun LinuxWindowBorder(
    modifier: Modifier = Modifier
) {
    val isDarkTheme = SaltTheme.configs.isDarkTheme
    val color = if (isDarkTheme) {
        Color(0x1AFFFFFF)
    } else {
        Color(0x26000000)
    }
    Spacer(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val strokeWidth = 1f
                val halfStroke = strokeWidth / 2f
                drawRect(
                    color = color,
                    topLeft = Offset(halfStroke, halfStroke),
                    size = Size(
                        width = (size.width - strokeWidth).coerceAtLeast(0f),
                        height = (size.height - strokeWidth).coerceAtLeast(0f)
                    ),
                    style = Stroke(width = strokeWidth)
                )
            }
    )
}
