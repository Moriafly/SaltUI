/*
 * Salt UI
 * Copyright (C) 2024 Moriafly
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

package com.moriafly.salt.sample.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.ext.edgeToEdge
import com.moriafly.salt.ui.gestures.cupertino.CupertinoOverscrollEffectFactory
import com.moriafly.salt.ui.sample.ui.MainContent
import com.moriafly.salt.ui.sample.ui.theme.AppTheme
import com.moriafly.salt.ui.sample.util.AppConfig
import com.moriafly.salt.ui.util.WindowUtil

class MainActivity : ComponentActivity() {
    @OptIn(UnstableSaltUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        edgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                CompositionLocalProvider(
                    LocalOverscrollFactory provides CupertinoOverscrollEffectFactory()
                ) {
                    Box(
                        modifier = Modifier
                            .background(SaltTheme.colors.background)
                    ) {
                        MainContent()
                    }
                }
            }

            val isDarkTheme = AppConfig.isDarkTheme
            LaunchedEffect(isDarkTheme) {
                val barColor = if (isDarkTheme) {
                    WindowUtil.BarColor.White
                } else {
                    WindowUtil.BarColor.Black
                }
                WindowUtil.setStatusBarForegroundColor(window, barColor)
                WindowUtil.setNavigationBarForegroundColor(window, barColor)
            }
        }
    }
}
