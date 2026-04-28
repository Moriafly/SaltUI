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

package com.moriafly.salt.ui.sample.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import com.moriafly.salt.ui.Island
import com.moriafly.salt.ui.IslandDefaults
import com.moriafly.salt.ui.IslandGroup
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.sample.ui.navigation.AppNavigation
import com.moriafly.salt.ui.sample.ui.navigation.LocalNavBackStack
import com.moriafly.salt.ui.sample.ui.navigation.ScreenRoute
import com.moriafly.salt.ui.sidebar.SideBar
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private val navBackStackConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(ScreenRoute.Main::class, ScreenRoute.Main.serializer())
            subclass(ScreenRoute.About::class, ScreenRoute.About.serializer())
            subclass(ScreenRoute.License::class, ScreenRoute.License.serializer())
        }
    }
}

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun MainContent() {
    val navBackStack = rememberNavBackStack(navBackStackConfig, ScreenRoute.Main)

    CompositionLocalProvider(
        LocalNavBackStack provides navBackStack
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SaltTheme.colors.subBackground)
        ) {
            IslandGroup(
                contentPadding = PaddingValues(
                    start = IslandDefaults.IslandGroupPadding,
                    end = IslandDefaults.IslandGroupPadding,
                    bottom = IslandDefaults.IslandGroupPadding
                )
            ) {
                Row {
                    Island(
                        modifier = Modifier
                            .padding(top = 40.dp)
                            .width(220.dp)
                            .fillMaxHeight(),
                        contentPadding = PaddingValues(
                            start = IslandDefaults.IslandPadding,
                            end = IslandDefaults.IslandPadding,
                            bottom = IslandDefaults.IslandPadding
                        )
                    ) {
                    }

                    Island(
                        modifier = Modifier
                            .padding(top = 40.dp),
                        contentPadding = PaddingValues(
                            start = IslandDefaults.IslandPadding,
                            end = IslandDefaults.IslandPadding,
                            bottom = IslandDefaults.IslandPadding
                        )
                    ) {
                        AppNavigation(
                            navBackStack = navBackStack
                        )
                    }
                }
            }

//            Column(
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .fillMaxWidth()
//            ) {
//                BottomBar {
//                    BottomBarItem(
//                        state = navBackStack[0] == ScreenRoute.Main,
//                        onClick = {
//                            if (navBackStack[0] != ScreenRoute.Main) {
//                                navBackStack.subList(1, navBackStack.size).clear()
//                                navBackStack[0] = ScreenRoute.Main
//                            }
//                        },
//                        painter = rememberVectorPainter(SaltIcons.Success),
//                        text = "Main"
//                    )
//                    BottomBarItem(
//                        state = navBackStack[0] == ScreenRoute.About,
//                        onClick = {
//                            if (navBackStack[0] != ScreenRoute.About) {
//                                navBackStack.subList(1, navBackStack.size).clear()
//                                navBackStack[0] = ScreenRoute.About
//                            }
//                        },
//                        painter = rememberVectorPainter(SaltIcons.Success),
//                        text = "About"
//                    )
//                }
//                Spacer(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .windowInsetsBottomHeight(WindowInsets.safeMainCompat)
//                        .background(SaltTheme.colors.subBackground)
//                )
//            }
        }
    }
}
