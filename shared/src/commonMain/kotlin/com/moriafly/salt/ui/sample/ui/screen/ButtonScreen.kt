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

package com.moriafly.salt.ui.sample.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.ButtonAppearance
import com.moriafly.salt.ui.ButtonDefaults
import com.moriafly.salt.ui.ButtonIntent
import com.moriafly.salt.ui.ControlSize
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.ItemOuterTip
import com.moriafly.salt.ui.ItemOuterTitle
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.outerPadding
import com.moriafly.salt.ui.sample.ui.icons.SimpleIcons
import com.moriafly.salt.ui.sample.ui.icons.Star
import com.moriafly.salt.ui.sample.ui.screen.basic.BasicScreenColumn

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun ButtonScreen() {
    BasicScreenColumn(
        title = "Button"
    ) {
        ItemOuterTitle("Appearance")
        ButtonFlowRow {
            Button(
                onClick = {},
                text = "Filled"
            )
            Button(
                onClick = {},
                text = "Outlined",
                appearance = ButtonAppearance.Outlined
            )
            Button(
                onClick = {},
                text = "Plain",
                appearance = ButtonAppearance.Plain
            )
        }
        ItemOuterTip(
            "Use Filled for the prominent action, Outlined for supporting actions that need a " +
                "visible boundary, and Plain for lightweight highlighted actions"
        )

        ItemOuterTitle("Control size")
        ButtonFlowRow {
            ControlSize.entries.forEach { size ->
                Button(
                    onClick = {},
                    text = size.name,
                    size = size
                )
            }
        }
        ItemOuterTip(
            "ControlSize changes platform-resolved height and spacing, not action importance or " +
                "theme typography"
        )

        ItemOuterTitle("Intent")
        ButtonFlowRow {
            Button(
                onClick = {},
                text = "Delete",
                intent = ButtonIntent.Destructive
            )
            Button(
                onClick = {},
                text = "Remove",
                appearance = ButtonAppearance.Outlined,
                intent = ButtonIntent.Destructive
            )
        }

        ItemOuterTitle("Content")
        ButtonFlowRow {
            Button(
                onClick = {},
                text = "Favorite",
                leadingIcon = {
                    Icon(
                        painter = rememberVectorPainter(SimpleIcons.Star),
                        contentDescription = null
                    )
                }
            )
            Button(
                onClick = {},
                appearance = ButtonAppearance.Outlined,
                size = ControlSize.Regular
            ) {
                Text("Custom slot")
                Spacer(Modifier.width(ButtonDefaults.iconSpacing(ControlSize.Regular)))
                Icon(
                    painter = rememberVectorPainter(SimpleIcons.Star),
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.iconSize(ControlSize.Regular))
                )
            }
        }
        ItemOuterTip(
            "Prefer the text overload for standard labels and leading icons; use the content slot " +
                "for richer layouts such as a trailing icon"
        )

        ItemOuterTitle("State")
        ButtonFlowRow {
            Button(
                onClick = {},
                text = "Disabled",
                enabled = false
            )
            Button(
                onClick = {},
                text = "Disabled",
                enabled = false,
                appearance = ButtonAppearance.Outlined
            )
        }
    }
}

@Composable
private fun ButtonFlowRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    FlowRow(
        modifier = modifier.outerPadding(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}
