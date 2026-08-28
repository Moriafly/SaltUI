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

package com.moriafly.salt.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.Card
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemArrowType
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.SaltConfigs
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * Displays a card section for grouping related controls within a [BasicScreen].
 *
 * An optional [header] and [footer] are placed above and below the card, while [content] is
 * arranged vertically inside the card. This component applies the standard horizontal screen
 * inset and trailing section spacing, allowing multiple card sections to be placed consecutively.
 *
 * This component should be used inside the content area of [BasicScreen]. Apply the padding values
 * supplied by [BasicScreen] to the surrounding content container so the card remains clear of the
 * title bar and window insets.
 *
 * @param modifier [Modifier] applied to the outer section container.
 * @param header Optional section heading displayed above the card.
 * @param footer Optional supporting text displayed below the card.
 * @param content Content displayed vertically inside the card, with a [ColumnScope] receiver.
 */
@UnstableSaltUiApi
@Composable
fun ScreenCard(
    modifier: Modifier = Modifier,
    header: String? = null,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp
            )
    ) {
        header?.let {
            Text(
                text = header,
                modifier = Modifier
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                color = SaltTheme.colors.subText,
                fontWeight = FontWeight.SemiBold,
                style = SaltTheme.textStyles.sub
            )
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column {
                content()
            }
        }
        footer?.let {
            Text(
                text = footer,
                modifier = Modifier
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                color = SaltTheme.colors.subText,
                style = SaltTheme.textStyles.sub
            )
        }
    }
}

@Preview
@Composable
@OptIn(UnstableSaltUiApi::class)
private fun ScreenCardPreview() {
    SaltTheme(
        configs = SaltConfigs.default(
            isDarkTheme = true
        )
    ) {
        Column(
            modifier = Modifier
                .background(SaltTheme.colors.background)
        ) {
            ScreenCard(
                header = "播放界面背景图片",
                footer = "禁用流光而使用自定义的播放界面背景"
            ) {
                Item(
                    onClick = {},
                    text = "选择图片",
                    arrowType = ItemArrowType.Link
                )
            }
            ScreenCard {
                Item(
                    onClick = {},
                    text = "Item"
                )
                ItemSwitcher(
                    state = true,
                    onChange = {},
                    text = "ItemSwitcher"
                )
            }
        }
    }
}
