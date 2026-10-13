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

@file:OptIn(com.moriafly.salt.ui.UnstableSaltUiApi::class)

package com.moriafly.salt.ui.sheet.internal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.sheet.SheetProperties
import com.moriafly.salt.ui.sheet.SheetScope
import com.moriafly.salt.ui.thenIf

@Composable
internal fun SheetSurface(
    state: SheetTransitionState,
    progress: () -> Float,
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    environment: SheetHostEnvironment,
    title: String?,
    properties: SheetProperties,
    actions: @Composable RowScope.() -> Unit,
    content: @Composable SheetScope.() -> Unit
) {
    val density = LocalDensity.current
    val directInput = remember { SheetDirectInput() }
    val bottom = properties.presentation.isBottom(windowSize = environment.windowSize)
    val viewportInsets = if (bottom) {
        environment.contentInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    } else {
        SheetSymmetricInsets(
            insets = environment.safeInsets,
            minimum = SheetDefaults.WindowMargin
        ).union(environment.imeInsets)
    }
    val bottomInsets = if (bottom) environment.contentInsets else WindowInsets()
    val imeInsets = if (bottom) environment.imeInsets else WindowInsets()
    val currentBottom by rememberUpdatedState(bottom)
    val currentVisible by rememberUpdatedState(visible)
    val currentProperties by rememberUpdatedState(properties)
    val currentDismissRequest by rememberUpdatedState(onDismissRequest)
    val nestedScroll = remember(state, directInput) {
        SheetNestedScroll(
            state = state,
            enabled = {
                currentBottom && currentVisible && state.ready && currentProperties.dragEnabled &&
                    (currentProperties.dismissible || state.points.size > 1) &&
                    directInput.active
            },
            interaction = { currentProperties.contentInteraction },
            dismissible = { currentProperties.dismissible },
            onDismissRequest = { currentDismissRequest() }
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .thenIf(condition = !visible) {
                clearAndSetSemantics { hideFromAccessibility() }
            }
            .sheetModalInput(
                visible = visible,
                directInput = directInput
            )
    ) {
        SheetScrim(
            progress = progress,
            onDismissRequest = onDismissRequest
        )
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(insets = viewportInsets)
        ) {
            val maximumWidth = if (bottom) {
                SheetDefaults.BottomMaxWidth
            } else {
                SheetDefaults.CenteredMaxWidth
            }
            val horizontalMargin = if (bottom && maxWidth >= SheetDefaults.AdaptiveBreakpoint) {
                SheetDefaults.WindowMargin
            } else {
                0.dp
            }
            val panelWidth = (maxWidth - horizontalMargin * 2)
                .coerceAtLeast(0.dp)
                .coerceAtMost(maximumWidth)
            val heightLimit = if (bottom) {
                maxHeight
            } else {
                maxHeight
                    .coerceAtLeast(0.dp)
                    .coerceAtMost(SheetDefaults.CenteredMaxHeight)
                    .coerceAtMost(panelWidth * SheetDefaults.CENTERED_HEIGHT_TO_WIDTH_RATIO)
            }
            val availableHeight = with(density) { heightLimit.roundToPx() }
            SheetPanel(
                state = state,
                visible = visible,
                onDismissRequest = onDismissRequest,
                title = title,
                properties = properties,
                bottom = bottom,
                bottomInsets = bottomInsets,
                imeInsets = imeInsets,
                availableHeight = availableHeight,
                floatingHiddenBase = {
                    maxHeight.toPx() / 2 + viewportInsets.getBottom(this)
                },
                modifier = Modifier
                    .align(if (bottom) Alignment.BottomCenter else Alignment.Center)
                    .width(width = panelWidth)
                    .consumeWindowInsets(
                        insets = environment.contentInsets.only(WindowInsetsSides.Bottom)
                    )
                    .nestedScroll(nestedScroll)
                    .then(modifier),
                actions = actions,
                content = content
            )
        }
    }
}

@Composable
private fun SheetScrim(
    progress: () -> Float,
    onDismissRequest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(color = Color.Black.copy(alpha = SheetDefaults.SCRIM_OPACITY * progress()))
            }
            .focusProperties { canFocus = false }
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onDismissRequest
            )
            .clearAndSetSemantics { }
    )
}
