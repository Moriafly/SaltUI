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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
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
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private val navBackStackConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(ScreenRoute.Main::class, ScreenRoute.Main.serializer())
            subclass(ScreenRoute.About::class, ScreenRoute.About.serializer())
            subclass(ScreenRoute.License::class, ScreenRoute.License.serializer())
            subclass(ScreenRoute.Button::class, ScreenRoute.Button.serializer())
            subclass(
                ScreenRoute.Component::class,
                ScreenRoute.Component.serializer()
            )
        }
    }
}

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun MainContent(
    windowCaptionBarHeight: Dp = 0.dp
) {
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
                            .padding(top = windowCaptionBarHeight)
                            .width(220.dp)
                            .fillMaxHeight()
                    ) {
                    }
                }

                Layer(
                    modifier = Modifier
                        .padding(top = windowCaptionBarHeight)
                        .testTag("mainContentLayer"),
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
