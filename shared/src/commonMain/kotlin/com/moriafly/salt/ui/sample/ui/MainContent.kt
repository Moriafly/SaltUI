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

package com.moriafly.salt.ui.sample.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.moriafly.salt.adaptive.window.LocalSaltAdaptiveWindow
import com.moriafly.salt.adaptive.window.UnstableSaltAdaptiveWindowApi
import com.moriafly.salt.adaptive.window.WindowSizeClass
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.Layer
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemOuterTitle
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.navigation.rememberSaltNavigator
import com.moriafly.salt.ui.sample.ui.navigation.AppNavigation
import com.moriafly.salt.ui.sample.ui.navigation.LocalSaltNavigator
import com.moriafly.salt.ui.sample.ui.navigation.ScreenRoute
import com.moriafly.salt.ui.sidebar.SideBar
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private val navigatorConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(ScreenRoute.Main::class, ScreenRoute.Main.serializer())
            subclass(ScreenRoute.About::class, ScreenRoute.About.serializer())
            subclass(ScreenRoute.License::class, ScreenRoute.License.serializer())
            subclass(ScreenRoute.List::class, ScreenRoute.List.serializer())
            subclass(ScreenRoute.Button::class, ScreenRoute.Button.serializer())
            subclass(ScreenRoute.Component::class, ScreenRoute.Component.serializer())
            subclass(ScreenRoute.Items::class, ScreenRoute.Items.serializer())
            subclass(ScreenRoute.Layouts::class, ScreenRoute.Layouts.serializer())
            subclass(ScreenRoute.CollapsingHeader::class, ScreenRoute.CollapsingHeader.serializer())
            subclass(ScreenRoute.NestedCollapsed::class, ScreenRoute.NestedCollapsed.serializer())
            subclass(ScreenRoute.BottomSheet::class, ScreenRoute.BottomSheet.serializer())
            subclass(ScreenRoute.Dialog::class, ScreenRoute.Dialog.serializer())
            subclass(ScreenRoute.Sheet::class, ScreenRoute.Sheet.serializer())
            subclass(ScreenRoute.Material::class, ScreenRoute.Material.serializer())
        }
    }
}

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class, UnstableSaltAdaptiveWindowApi::class)
@Composable
fun MainContent(
    windowCaptionBarHeight: Dp = 0.dp
) {
    val navigator = rememberSaltNavigator(
        configuration = navigatorConfig,
        initRoute = ScreenRoute.Main,
        topLevelRoutes = setOf(ScreenRoute.Main)
    )
    val expandedLayout = LocalSaltAdaptiveWindow.current.sizeClass != WindowSizeClass.Compact

    CompositionLocalProvider(
        LocalSaltNavigator provides navigator
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row {
                if (expandedLayout) {
                    SideBar(
                        modifier = Modifier
                            .padding(top = windowCaptionBarHeight)
                    ) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            ItemOuterTitle("SaltUI")
                            listOf(
                                "Home" to ScreenRoute.Main,
                                "Switchers" to ScreenRoute.Component,
                                "Items" to ScreenRoute.Items,
                                "Buttons" to ScreenRoute.Button,
                                "List" to ScreenRoute.List,
                                "Layouts and scrolling" to ScreenRoute.Layouts,
                                "Collapsing header" to ScreenRoute.CollapsingHeader,
                                "Nested collapsed" to ScreenRoute.NestedCollapsed,
                                "Bottom sheet scaffold" to ScreenRoute.BottomSheet,
                                "Dialogs" to ScreenRoute.Dialog,
                                "Sheets" to ScreenRoute.Sheet,
                                "Theme and material" to ScreenRoute.Material,
                                "About" to ScreenRoute.About
                            ).forEach { (label, route) ->
                                Item(
                                    onClick = {
                                        if (navigator.navBackStack.lastOrNull() != route) {
                                            navigator.navigate(ScreenRoute.Main)
                                            navigator.navigate(route)
                                        }
                                    },
                                    text = label
                                )
                            }
                        }
                    }
                }

                Layer(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = windowCaptionBarHeight)
                        .testTag("mainContentLayer"),
                    decorationEnabled = OS.isDesktop()
                ) {
                    AppNavigation(
                        navigator = navigator
                    )
                }
            }
        }
    }
}
