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

package com.moriafly.salt.ui.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.Dimens
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltApi

@UnstableSaltApi
@Composable
fun PopupMenuItem(
    onClick: () -> Unit,
    selected: Boolean,
    position: PopupMenuItemPosition = PopupMenuItemPosition.CENTER,
    text: String,
    sub: String? = null
) {
    Column(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .fillMaxWidth()
            .sizeIn(
                minWidth = DropdownMenuItemDefaultMinWidth,
                maxWidth = DropdownMenuItemDefaultMaxWidth,
                minHeight = 0.dp
            )
            .background(if (selected) SaltTheme.colors.subBackground else Color.Unspecified)
            .padding(Dimens.innerHorizontalPadding, Dimens.innerVerticalPadding)
    ) {
        if (position == PopupMenuItemPosition.TOP) {
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(
            text = text,
            color = if (selected) SaltTheme.colors.highlight else SaltTheme.colors.text,
            style = SaltTheme.textStyles.main
        )
        sub?.let {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sub,
                color = if (selected) SaltTheme.colors.highlight else SaltTheme.colors.subText,
                style = SaltTheme.textStyles.sub
            )
        }
        if (position == PopupMenuItemPosition.BOTTOM) {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

enum class PopupMenuItemPosition {
    TOP,
    CENTER,
    BOTTOM
}

private val DropdownMenuItemDefaultMinWidth = 112.dp
private val DropdownMenuItemDefaultMaxWidth = 280.dp
private val DropdownMenuItemDefaultMinHeight = 48.dp