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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import com.moriafly.salt.ui.BottomBar
import com.moriafly.salt.ui.BottomBarItem
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.ext.safeMainCompat
import com.moriafly.salt.ui.icons.SaltIcons
import com.moriafly.salt.ui.icons.Success
import com.moriafly.salt.ui.sample.ui.navigation.AppNavigation
import com.moriafly.salt.ui.sample.ui.navigation.LocalNavBackStack
import com.moriafly.salt.ui.sample.ui.navigation.ScreenRoute
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
        ) {
            AppNavigation(
                navBackStack = navBackStack
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                BottomBar {
                    BottomBarItem(
                        state = navBackStack[0] == ScreenRoute.Main,
                        onClick = {
                            if (navBackStack[0] != ScreenRoute.Main) {
                                navBackStack.subList(1, navBackStack.size).clear()
                                navBackStack[0] = ScreenRoute.Main
                            }
                        },
                        painter = rememberVectorPainter(SaltIcons.Success),
                        text = "Main"
                    )
                    BottomBarItem(
                        state = navBackStack[0] == ScreenRoute.About,
                        onClick = {
                            if (navBackStack[0] != ScreenRoute.About) {
                                navBackStack.subList(1, navBackStack.size).clear()
                                navBackStack[0] = ScreenRoute.About
                            }
                        },
                        painter = rememberVectorPainter(SaltIcons.Success),
                        text = "About"
                    )
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsBottomHeight(WindowInsets.safeMainCompat)
                        .background(SaltTheme.colors.subBackground)
                )
            }
        }
    }
}
