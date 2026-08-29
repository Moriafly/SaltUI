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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemInfo
import com.moriafly.salt.ui.ItemInfoType
import com.moriafly.salt.ui.ItemLabelValueContainer
import com.moriafly.salt.ui.ItemOuterLargeTitle
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.LabelValue
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.sample.ui.icons.Like
import com.moriafly.salt.ui.sample.ui.icons.Search
import com.moriafly.salt.ui.sample.ui.icons.SimpleIcons
import com.moriafly.salt.ui.sample.ui.navigation.LocalNavBackStack
import com.moriafly.salt.ui.sample.ui.navigation.ScreenRoute
import com.moriafly.salt.ui.sample.ui.screen.basic.BasicScreenColumn
import com.moriafly.salt.ui.sample.util.AppConfig
import com.moriafly.salt.ui.screen.ScreenCard
import com.moriafly.salt.ui.screen.TitleBarButton

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun MainScreen() {
    BasicScreenColumn(
        actionButton = null,
        title = "Main",
        subtitle = "Salt UI 3",
        toolButtons = {
            TitleBarButton(
                onClick = {}
            ) {
                Icon(
                    painter = rememberVectorPainter(SimpleIcons.Like),
                    contentDescription = null
                )
            }
            TitleBarButton(
                onClick = {}
            ) {
                Icon(
                    painter = rememberVectorPainter(SimpleIcons.Search),
                    contentDescription = null
                )
            }
        }
    ) {
        ItemOuterLargeTitle(
            text = "Salt UI 3",
            sub = "UI Components for Compose Multiplatform (Android/Desktop/iOS)"
        )

        ScreenCard {
            ItemInfo(
                text = "Coming!",
                infoType = ItemInfoType.Success
            )
        }

        ScreenCard(
            header = "Appearance",
            footer = "This is footer!"
        ) {
            ItemSwitcher(
                state = AppConfig.isDarkTheme,
                onChange = {
                    AppConfig.isDarkTheme = it
                },
                text = "Dark Theme"
            )
        }

        val navBackStack = LocalNavBackStack.current
        ScreenCard {
            Item(
                onClick = {
                    navBackStack.add(ScreenRoute.List)
                },
                text = "List"
            )
            Item(
                onClick = {
                    navBackStack.add(ScreenRoute.Button)
                },
                text = "Button",
                sub = "Appearance, intent, and control size"
            )
            Item(
                onClick = {
                },
                text = "Item"
            )
            Item(
                onClick = {
                    navBackStack.add(ScreenRoute.Dialog)
                },
                text = "Dialog"
            )
            Item(
                onClick = {
                    navBackStack.add(ScreenRoute.Material)
                },
                text = "Material"
            )
        }

        ScreenCard {
            Item(
                onClick = {
                    navBackStack.add(ScreenRoute.About)
                },
                text = "About"
            )
        }

        ScreenCard {
            ItemLabelValueContainer {
                LabelValue(
                    label = "Version",
                    value = "3.0"
                )
                LabelValue(
                    label = "Developer",
                    value = "Moriafly"
                )
                LabelValue(
                    label = "Year",
                    value = "2026"
                )
                LabelValue(
                    label = "设备名称",
                    value = "测试 DESKTOP"
                )
                LabelValue(
                    label = "处理器",
                    value = "测试 AMD"
                )
                LabelValue(
                    label = "产品 ID",
                    value = "测试 ABC"
                )
            }
        }
    }
}
