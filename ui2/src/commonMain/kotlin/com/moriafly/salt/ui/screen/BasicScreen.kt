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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi

@UnstableSaltUiApi
@Composable
fun BasicScreen(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    collapsedTopBar: @Composable () -> Unit = {},
    collapsedHeight: Dp = 56.dp,
    content: @Composable (PaddingValues) -> Unit
) {
    val scrollBehavior = TopScreenBarDefaults.exitUntilCollapsedScrollBehavior()

    Box {
        Column(
            modifier = modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            CollapsedTopBar(
                scrollBehavior = scrollBehavior,
                collapsedHeight = collapsedHeight,
                content = collapsedTopBar
            )
            BasicScreenLayout(
                topBar = {
                    TopScreenBar(
                        scrollBehavior = scrollBehavior,
                        content = topBar
                    )
                },
                content = content
            )
        }

        Column {
            String
            Text("heightOffset = ${scrollBehavior.state.heightOffset}")
            Text("contentOffset = ${scrollBehavior.state.contentOffset}")
        }
    }
}

@Composable
private fun BasicScreenLayout(
    topBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    // Create the backing value for the content padding
    // These values will be updated during measurement, but before subcomposing the body content
    // Remembering and updating a single PaddingValues avoids needing to recompose when the values
    // change
    val contentPadding = remember {
        object : PaddingValues {
            var paddingHolder by mutableStateOf(PaddingValues(0.dp))

            override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
                paddingHolder.calculateLeftPadding(layoutDirection)

            override fun calculateTopPadding(): Dp = paddingHolder.calculateTopPadding()

            override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
                paddingHolder.calculateRightPadding(layoutDirection)

            override fun calculateBottomPadding(): Dp = paddingHolder.calculateBottomPadding()
        }
    }

    val topBarContent: @Composable () -> Unit = remember(topBar) { { topBar() } }
    val bodyContent: @Composable () -> Unit =
        remember(content, contentPadding) { { Box { content(contentPadding) } } }

    SubcomposeLayout { constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight

        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        val topBarPlaceable =
            subcompose(BasicScreenContent.TopBar, topBarContent)
                .first()
                .measure(looseConstraints)

        contentPadding.paddingHolder =
            PaddingValues(
                top = topBarPlaceable.height.toDp()
            )

        val bodyContentPlaceable =
            subcompose(BasicScreenContent.Body, bodyContent)
                .first()
                .measure(looseConstraints)

        layout(layoutWidth, layoutHeight) {
            // Placing to control drawing order to match default elevation of each placeable
            bodyContentPlaceable.place(0, 0)
            topBarPlaceable.place(0, 0)
        }
    }
}

private enum class BasicScreenContent {
    TopBar,
    CollapsedTopBar,
    Body
}
