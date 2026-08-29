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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.Card
import com.moriafly.salt.ui.CardDefaults
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * A full-width [Card] section for grouping related content within a [BasicScreen].
 *
 * The optional [header] and [footer] are placed above and below the card, and [content] is arranged
 * vertically inside it. The outer section applies [ScreenCardDefaults.paddingValues], while the
 * card fills the remaining width. The [shape], [color], and [border] are forwarded to [Card].
 *
 * When used in a [BasicScreen], the surrounding content container remains responsible for applying
 * the padding values supplied by [BasicScreen].
 *
 * @param modifier [Modifier] applied to the outer section container.
 * @param shape Shape used to clip the card and draw its [border].
 * @param color Fallback background color for the card's sub-material effect. Pass
 * [Color.Unspecified] to omit the material effect.
 * @param border Stroke drawn around the card, or `null` for no border.
 * @param header Optional section heading displayed above the card; `null` omits it.
 * @param footer Optional supporting text displayed below the card; `null` omits it.
 * @param content Content arranged vertically inside the card, with a [ColumnScope] receiver.
 *
 * @see ScreenCardDefaults
 */
@UnstableSaltUiApi
@Composable
fun ScreenCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    color: Color = CardDefaults.color,
    border: BorderStroke? = CardDefaults.border,
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
                .fillMaxWidth(),
            shape = shape,
            color = color,
            border = border,
            content = content
        )
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

/**
 * Default layout values for [ScreenCard].
 */
@UnstableSaltUiApi
object ScreenCardDefaults {
    /**
     * Padding around the complete section, including its header, card, and footer.
     */
    val paddingValues: PaddingValues =
        PaddingValues(
            horizontal = 16.dp,
            vertical = 8.dp
        )
}
