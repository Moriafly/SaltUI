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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package com.moriafly.salt.ui.sample.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.sample.ui.icons.SimpleIcons
import com.moriafly.salt.ui.sample.ui.icons.Star
import com.moriafly.salt.ui.sample.ui.screen.basic.BasicScreenColumn
import com.moriafly.salt.ui.screen.ScreenCard

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun ComponentScreen() {
    BasicScreenColumn(
        title = "Components"
    ) {
        ScreenCard(
            header = "ItemSwitcher Basic"
        ) {
            var initiallyOff by remember { mutableStateOf(false) }
            ItemSwitcher(
                state = initiallyOff,
                onChange = { initiallyOff = it },
                text = "Initially off"
            )
            var initiallyOn by remember { mutableStateOf(true) }
            ItemSwitcher(
                state = initiallyOn,
                onChange = { initiallyOn = it },
                text = "Initially on"
            )
        }

        ScreenCard(
            header = "ItemSwitcher Content"
        ) {
            var withDescription by remember { mutableStateOf(false) }
            ItemSwitcher(
                state = withDescription,
                onChange = { withDescription = it },
                text = "With description",
                sub = "Tap anywhere in the row to toggle the switch"
            )
            var favorites by remember { mutableStateOf(true) }
            ItemSwitcher(
                state = favorites,
                onChange = { favorites = it },
                text = "Favorites",
                iconPainter = rememberVectorPainter(SimpleIcons.Star),
                sub = "Show favorite items first"
            )
        }

        ScreenCard(
            header = "ItemSwitcher Disabled"
        ) {
            ItemSwitcher(
                state = false,
                onChange = {},
                text = "Disabled off",
                enabled = false
            )
            ItemSwitcher(
                state = true,
                onChange = {},
                text = "Disabled on",
                enabled = false
            )
        }
    }
}
