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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.Layer
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.sample.ui.navigation.AppNavigation
import com.moriafly.salt.ui.sample.ui.navigation.LocalNavBackStack
import com.moriafly.salt.ui.sample.ui.navigation.ScreenRoute
import com.moriafly.salt.ui.thenIf
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val maxWidth = maxWidth
            Row {
                val pad = maxWidth > 600.dp
                if (pad) {
                    Box(
                        modifier = Modifier
                            .padding(top = 40.dp)
                            .width(220.dp)
                            .fillMaxHeight()
                    ) {
                    }
                }

                Layer(
                    modifier = Modifier
                        .thenIf(OS.isDesktop()) {
                            padding(top = 40.dp)
                        },
                    decorationEnabled = OS.isDesktop()
                ) {
                    AppNavigation(
                        navBackStack = navBackStack
                    )
                }
            }
        }
    }
}
