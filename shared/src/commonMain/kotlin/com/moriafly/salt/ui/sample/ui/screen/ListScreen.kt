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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.RoundedColumnType
import com.moriafly.salt.ui.SaltDimens
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.ext.safeMainCompat
import com.moriafly.salt.ui.innerPadding
import com.moriafly.salt.ui.lazy.LazyColumn

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun ListScreen() {
    BasicScreenBox(
        title = "List"
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                vertical = SaltDimens.RoundedColumnInListEdgePadding
            )
        ) {
            items(100) {
                RoundedColumn(
                    type = RoundedColumnType.InList
                ) {
                    Text(
                        text = "Item $it",
                        modifier = Modifier
                            .innerPadding()
                    )
                }
            }

            item {
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeMainCompat))
            }
        }
    }
}
