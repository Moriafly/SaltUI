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

package com.moriafly.salt.ui.sample.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemOuterLargeTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.sample.ui.icons.Like
import com.moriafly.salt.ui.sample.ui.icons.SimpleIcons
import com.moriafly.salt.ui.sample.ui.icons.Star
import com.moriafly.salt.ui.sample.ui.navigation.LocalNavBackStack
import com.moriafly.salt.ui.sample.ui.navigation.ScreenRoute
import com.moriafly.salt.ui.sample.ui.screen.basic.BasicScreenColumn
import com.moriafly.salt.ui.screen.TitleBarButton

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun AboutScreen() {
    BasicScreenColumn(
        title = "About",
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
                    painter = rememberVectorPainter(SimpleIcons.Star),
                    contentDescription = null
                )
            }
        }
    ) {
        ItemOuterLargeTitle(
            text = "Salt UI 3",
            sub = "UI Components for Compose Multiplatform (Android/Desktop/iOS)"
        )

        RoundedColumn {
            val navBackStack = LocalNavBackStack.current
            Item(
                onClick = {
                    navBackStack.add(ScreenRoute.License)
                },
                text = "License"
            )
        }
    }
}
