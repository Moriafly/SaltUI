/*
 * Salt UI
 * Copyright (C) 2025 Moriafly
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

package com.moriafly.salt.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.nested.NestedCollapsed
import com.moriafly.salt.ui.nested.rememberCollapsedState

@UnstableSaltUiApi
@Composable
fun BasicScreen(
    modifier: Modifier = Modifier,
    collapsedTopBar: @Composable () -> Unit = {},
    expandedTopBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        val state = rememberCollapsedState()
        ScreenTopBarCollapsed(
            state = state,
            content = collapsedTopBar
        )

        NestedCollapsed(
            collapsed = expandedTopBar,
            state = state,
            content = content
        )
    }
}
