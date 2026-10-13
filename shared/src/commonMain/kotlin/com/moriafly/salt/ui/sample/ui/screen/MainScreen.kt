/*
 * SaltUI
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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.ButtonAppearance
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
import com.moriafly.salt.ui.sheet.Sheet

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun MainScreen() {
    var welcomeSheetVisible by rememberSaveable { mutableStateOf(false) }
    BasicScreenColumn(
        actionButton = null,
        title = "Main",
        subtitle = "SaltUI 3",
        toolButtons = {
            TitleBarButton(
                onClick = { welcomeSheetVisible = true }
            ) {
                Icon(
                    painter = rememberVectorPainter(SimpleIcons.Like),
                    contentDescription = "About SaltUI"
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
            text = "SaltUI 3",
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
                    navBackStack.add(ScreenRoute.Component)
                },
                text = "Components",
                sub = "ItemSwitcher examples"
            )
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
                onClick = { navBackStack.add(ScreenRoute.Sheet) },
                text = "Sheet"
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
    MainWelcomeSheet(
        visible = welcomeSheetVisible,
        onDismissRequest = { welcomeSheetVisible = false }
    )
}

@Suppress("DEPRECATION")
@OptIn(UnstableSaltUiApi::class)
@Composable
private fun MainWelcomeSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var linkCopied by remember(visible) { mutableStateOf(false) }
    Sheet(
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = "Hi，SaltUI",
        actions = {
            Button(
                onClick = {
                    clipboard.setText(AnnotatedString("https://github.com/Moriafly/SaltUI"))
                    linkCopied = true
                },
                text = if (linkCopied) "Link copied" else "Share",
                appearance = ButtonAppearance.Subtle,
                modifier = Modifier
                    .weight(weight = 1f)
            )
            Button(
                onClick = onDismissRequest,
                text = "Got it",
                modifier = Modifier
                    .weight(weight = 1f)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(state = rememberScrollState())
        ) {
            ItemOuterLargeTitle(
                text = "Little details. A big difference.",
                sub = "Thoughtful components for Android, desktop, and iOS. " +
                    "Build something you love, and share it with the world."
            )
        }
    }
}
