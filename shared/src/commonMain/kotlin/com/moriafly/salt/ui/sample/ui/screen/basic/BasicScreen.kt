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

package com.moriafly.salt.ui.sample.ui.screen.basic

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.rememberScrollState
import com.moriafly.salt.ui.sample.ui.navigation.LocalNavBackStack
import com.moriafly.salt.ui.screen.BasicScreen
import com.moriafly.salt.ui.screen.BasicScreenDefaults
import com.moriafly.salt.ui.screen.BasicScreenStyle
import com.moriafly.salt.ui.verticalScroll

@UnstableSaltUiApi
@Composable
fun BasicScreenBox(
    modifier: Modifier = Modifier,
    actionButton: (@Composable () -> Unit)? = {
        val navBackStack = LocalNavBackStack.current
        BasicScreenDefaults.BackButton(
            onBack = {
                navBackStack.removeLastOrNull()
            }
        )
    },
    title: String? = null,
    subtitle: String? = null,
    toolButtons: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = BasicScreenDefaults.ContentPadding,
    overlay: @Composable (BoxScope.(PaddingValues) -> Unit)? = null,
    style: BasicScreenStyle = SaltTheme.basicScreenStyle,
    content: @Composable (BoxScope.(PaddingValues) -> Unit)
) {
    BasicScreen(
        actionButton = actionButton,
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        toolButtons = toolButtons,
        contentPadding = contentPadding,
        overlay = overlay,
        style = style
    ) { contentPadding ->
        content(contentPadding)
    }
}

@UnstableSaltUiApi
@Composable
fun BasicScreenColumn(
    modifier: Modifier = Modifier,
    actionButton: (@Composable () -> Unit)? = {
        val navBackStack = LocalNavBackStack.current
        BasicScreenDefaults.BackButton(
            onBack = {
                navBackStack.removeLastOrNull()
            }
        )
    },
    title: String? = null,
    subtitle: String? = null,
    toolButtons: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = BasicScreenDefaults.ContentPadding,
    overlay: @Composable (BoxScope.(PaddingValues) -> Unit)? = null,
    style: BasicScreenStyle = SaltTheme.basicScreenStyle,
    content: @Composable ColumnScope.() -> Unit
) {
    BasicScreen(
        actionButton = actionButton,
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        toolButtons = toolButtons,
        contentPadding = contentPadding,
        overlay = overlay,
        style = style
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(contentPadding.calculateTopPadding()))
            content()
            Spacer(Modifier.height(contentPadding.calculateBottomPadding()))
        }
    }
}
