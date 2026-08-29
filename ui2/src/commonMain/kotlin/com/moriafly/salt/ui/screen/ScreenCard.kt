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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.Card
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
            .padding(ScreenCardDefaults.paddingValues)
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
                fontWeight = FontWeight.Medium,
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

@UnstableSaltUiApi
object ScreenCardDefaults {
    val paddingValues: PaddingValues =
        PaddingValues(
            horizontal = 16.dp,
            vertical = 8.dp
        )
}
