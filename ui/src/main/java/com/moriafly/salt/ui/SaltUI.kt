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

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

@Composable
fun SaltUILogo() {
    RoundedColumn {
        ItemContainer {
            Column {
                Text(
                    text = buildAnnotatedString {
                        append("Based on ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Black)) {
                            append("SaltUI")
                        }
                    },
                    style = SaltTheme.textStyles.main
                )
                Text(
                    text = "Copyright © 2023 Moriafly",
                    style = SaltTheme.textStyles.sub
                )
            }
        }
    }
}