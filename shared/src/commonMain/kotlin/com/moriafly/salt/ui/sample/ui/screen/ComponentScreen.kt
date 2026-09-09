/*
 * Salt UI
 * Copyright (C) 2026 Moriafly
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
