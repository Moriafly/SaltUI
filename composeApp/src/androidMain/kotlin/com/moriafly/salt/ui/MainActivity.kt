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

package com.moriafly.salt.ui

import BasicScreenSample
import MainActivityContent
import MaterialScreen
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.moriafly.salt.ui.ext.edgeToEdge
import com.moriafly.salt.ui.gestures.cupertino.CupertinoOverscrollEffectFactory
import com.moriafly.salt.ui.gestures.cupertino.rememberCupertinoOverscrollEffect
import com.moriafly.salt.ui.lazy.LazyColumn
import com.moriafly.salt.ui.util.RomUtil

class MainActivity : ComponentActivity() {
    @OptIn(UnstableSaltUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        edgeToEdge()
        super.onCreate(savedInstanceState)

        Log.d(
            "MainActivity",
            """
            isXiaomiHyperOS: ${RomUtil.isXiaomiHyperOS}
            isMeizuFlymeOS: ${RomUtil.isMeizuFlymeOS}
            """.trimIndent()
        )

        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalOverscrollFactory provides CupertinoOverscrollEffectFactory()
            ) {
                // MaterialScreen()
                // MainActivityContent()
                // NestedScrollTopBarContainer()
                // BasicScreenSample()
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(SaltTheme.colors.background)
//                ) {
//                    item {
//                        Text(
//                            text = "Hello World",
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .outerPadding()
//                        )
//                    }
//                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SaltTheme.colors.background)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Hello World",
                        modifier = Modifier
                            .fillMaxWidth()
                            .outerPadding()
                    )
                }
            }
        }
    }
}
