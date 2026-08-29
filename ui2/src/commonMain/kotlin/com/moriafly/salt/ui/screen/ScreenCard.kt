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
import com.moriafly.salt.ui.Card
import com.moriafly.salt.ui.CardDefaults
import com.moriafly.salt.ui.ItemOuterTip
import com.moriafly.salt.ui.ItemOuterTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * Displays a full-width screen section containing a [Card] with optional supporting text.
 *
 * This is the preferred replacement for the previous pattern of building page cards with
 * [RoundedColumn]. It combines the section title, card container, and supporting text in a single
 * screen-level component.
 *
 * The [header] is displayed above the card using [ItemOuterTitle], and the [footer] is displayed
 * below it using [ItemOuterTip]. The card is inset using the current [SaltTheme] spacing, while
 * [content] is arranged vertically without additional inner padding. The [shape], [color], and
 * [border] are passed directly to [Card].
 *
 * This component does not consume the padding values provided by [BasicScreen]. Apply those values
 * to the surrounding content container when necessary to avoid the title bar and window insets.
 *
 * @param modifier [Modifier] applied to the outer section container.
 * @param shape Shape used to clip the card and draw the [border].
 * @param color Fallback background color for the card's sub-material effect. Pass
 * [Color.Unspecified] to omit the material effect.
 * @param border Stroke drawn around the card, or `null` for no border.
 * @param header Optional section title displayed above the card; `null` omits it.
 * @param footer Optional supporting text displayed below the card; `null` omits it.
 * @param content Content arranged vertically inside the card, with a [ColumnScope] receiver.
 *
 * @see ItemOuterTitle
 * @see ItemOuterTip
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
    ) {
        header?.let {
            ItemOuterTitle(header)
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    PaddingValues(
                        horizontal = SaltTheme.dimens.padding,
                        vertical = SaltTheme.dimens.padding * 0.5f
                    )
                ),
            shape = shape,
            color = color,
            border = border,
            content = content
        )
        footer?.let {
            ItemOuterTip(footer)
        }
    }
}
