/*
 * SaltUI
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

// The compatibility overload keeps its published parameter order.
@file:Suppress("unused", "ktlint:compose:param-order-check")

package com.moriafly.salt.ui.popup

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi

@get:Composable
internal expect val popupMenuItemPadding: PaddingValues

@Suppress("ktlint:compose:modifier-missing-check")
@UnstableSaltUiApi
@Composable
fun PopupMenuItem(
    onClick: () -> Unit,
    selected: Boolean? = null,
    text: String,
    sub: String? = null,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = SaltTheme.colors.text
) {
    val desktop = OS.isDesktop()
    Row(
        modifier = Modifier
            .semantics {
                this.role = Role.RadioButton

                if (selected != null) {
                    this.toggleableState = when (selected) {
                        true -> ToggleableState.On
                        false -> ToggleableState.Off
                    }
                }
            }
            .fillMaxWidth()
            .sizeIn(
                minWidth = DropdownMenuItemDefaultMinWidth,
                maxWidth = DropdownMenuItemDefaultMaxWidth
            )
            .clickable {
                onClick()
            }
            .padding(popupMenuItemPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (desktop && selected != null) {
            MenuSelectionMark(marked = selected, size = 16.dp)
            Spacer(Modifier.width(8.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Text(
                text = text,
                color = SaltTheme.colors.text
            )
            sub?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sub,
                    color = SaltTheme.colors.subText,
                    style = SaltTheme.textStyles.sub
                )
            }
        }
        iconPainter?.let {
            Spacer(modifier = Modifier.width(SaltTheme.dimens.padding * 2))
            Image(
                modifier = Modifier
                    .size(SaltTheme.dimens.itemIcon)
                    .padding(iconPaddingValues),
                painter = iconPainter,
                contentDescription = null,
                colorFilter = iconColor?.let { ColorFilter.tint(iconColor) }
            )
        }
        if (!desktop && selected != null) {
            Spacer(Modifier.width(8.dp))
            MenuSelectionMark(marked = selected, size = 20.dp)
        }
    }
}

private val DropdownMenuItemDefaultMinWidth = 180.dp
private val DropdownMenuItemDefaultMaxWidth = 280.dp
private val DropdownMenuItemDefaultMinHeight = 48.dp
