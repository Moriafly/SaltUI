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

package com.moriafly.salt.ui.button

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.material.subMaterial
import com.moriafly.salt.ui.thenIf

@UnstableSaltUiApi
@Composable
fun PillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: (@Composable () -> Unit)? = null,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .run {
                if (text != null) {
                    height(PillButtonDefaults.Height)
                } else {
                    size(PillButtonDefaults.Height)
                }
            }
            .clip(CircleShape)
            .subMaterial(fallback = SaltTheme.colors.subBackground)
            .border(Dp.Hairline, SaltTheme.colors.stroke, CircleShape)
            .clickable {
                onClick()
            }
            .thenIf(text != null) {
                padding(horizontal = 12.dp)
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()

        if (text != null) {
            Spacer(Modifier.width(4.dp))
            text()
        }
    }
}

@UnstableSaltUiApi
expect object PillButtonDefaults {
    /**
     * - Mobile: 48.dp
     * - Desktop: 32.dp
     */
    val Height: Dp

    /**
     * - Mobile: 20.dp
     * - Desktop: 16.dp
     */
    val IconSize: Dp
}
