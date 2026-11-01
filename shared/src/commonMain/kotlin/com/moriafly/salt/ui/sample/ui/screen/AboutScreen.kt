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

import androidx.compose.runtime.Composable
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemOuterLargeTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.sample.ui.navigation.LocalSaltNavigator
import com.moriafly.salt.ui.sample.ui.navigation.ScreenRoute
import com.moriafly.salt.ui.sample.ui.screen.basic.BasicScreenColumn

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun AboutScreen() {
    BasicScreenColumn(
        title = "About"
    ) {
        ItemOuterLargeTitle(
            text = "SaltUI 3",
            sub = "UI Components for Compose Multiplatform (Android/Desktop/iOS)"
        )

        RoundedColumn {
            val navigator = LocalSaltNavigator.current
            Item(
                onClick = {
                    navigator.navigate(ScreenRoute.License)
                },
                text = "License"
            )
        }
    }
}
