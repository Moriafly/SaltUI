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

import androidx.compose.runtime.Composable
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemOuterLargeTitle
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.sample.ui.navigation.LocalNavBackStack
import com.moriafly.salt.ui.sample.ui.navigation.ScreenRoute
import com.moriafly.salt.ui.sample.util.AppConfig

@OptIn(UnstableSaltUiApi::class)
@Composable
fun MainScreen() {
    BasicScreenColumn(
        title = "",
        showBackBtn = false
    ) {
        ItemOuterLargeTitle(
            text = "Salt UI",
            sub = "UI Components for Compose Multiplatform (Android/Desktop/iOS)"
        )

        RoundedColumn {
            ItemSwitcher(
                state = AppConfig.isDarkTheme,
                onChange = {
                    AppConfig.updateIsDarkTheme(it)
                },
                text = "Dark Theme"
            )
        }

        RoundedColumn {
            val navBackStack = LocalNavBackStack.current
            Item(
                onClick = {
                    navBackStack.add(ScreenRoute.List)
                },
                text = "List"
            )
        }
    }
}
