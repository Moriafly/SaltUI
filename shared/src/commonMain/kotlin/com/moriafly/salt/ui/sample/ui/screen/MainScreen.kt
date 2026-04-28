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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemOuterLargeTitle
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.button.PillButton
import com.moriafly.salt.ui.outerPadding
import com.moriafly.salt.ui.rememberScrollState
import com.moriafly.salt.ui.sample.ui.navigation.LocalNavBackStack
import com.moriafly.salt.ui.sample.ui.navigation.ScreenRoute
import com.moriafly.salt.ui.sample.util.AppConfig
import com.moriafly.salt.ui.screen.BasicScreen
import com.moriafly.salt.ui.verticalScroll

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun MainScreen() {
    BasicScreen(
        onBack = {
        },
        title = "Main",
        contentPadding = PaddingValues(
            top = 40.dp
        )
    ) { contentPaddingValues ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(contentPaddingValues.calculateTopPadding()))

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
                Item(
                    onClick = {
                        navBackStack.add(ScreenRoute.List)
                    },
                    text = "List"
                )
                Item(
                    onClick = {
                        navBackStack.add(ScreenRoute.List)
                    },
                    text = "List"
                )
                Item(
                    onClick = {
                        navBackStack.add(ScreenRoute.List)
                    },
                    text = "List"
                )
                Item(
                    onClick = {
                        navBackStack.add(ScreenRoute.List)
                    },
                    text = "List"
                )
                Item(
                    onClick = {
                        navBackStack.add(ScreenRoute.List)
                    },
                    text = "List"
                )
                Item(
                    onClick = {
                        navBackStack.add(ScreenRoute.List)
                    },
                    text = "List"
                )
                Item(
                    onClick = {
                        navBackStack.add(ScreenRoute.List)
                    },
                    text = "List"
                )
                Item(
                    onClick = {
                        navBackStack.add(ScreenRoute.List)
                    },
                    text = "List"
                )
                Item(
                    onClick = {
                        navBackStack.add(ScreenRoute.List)
                    },
                    text = "List"
                )
                Item(
                    onClick = {
                        navBackStack.add(ScreenRoute.List)
                    },
                    text = "List"
                )
            }

            FlowRow(
                modifier = Modifier
                    .outerPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PillButton(
                    onClick = {
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .border(1.dp, SaltTheme.colors.subText)
                    )
                }
                PillButton(
                    onClick = {
                    },
                    text = {
                        Text(
                            text = "你好"
                        )
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .border(1.dp, SaltTheme.colors.subText)
                    )
                }
                PillButton(
                    onClick = {
                    },
                    text = {
                        Text(
                            text = "禁用"
                        )
                    },
                    enabled = false
                ) {
                    Box(
                        modifier = Modifier
                            .border(1.dp, SaltTheme.colors.subText)
                    )
                }
            }
        }
    }
}
