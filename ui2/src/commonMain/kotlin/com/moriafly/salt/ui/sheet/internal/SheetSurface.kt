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

@file:OptIn(
    com.moriafly.salt.ui.UnstableSaltUiApi::class,
    com.moriafly.salt.adaptive.window.UnstableSaltAdaptiveWindowApi::class
)

package com.moriafly.salt.ui.sheet.internal

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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moriafly.salt.adaptive.window.WindowSizeClass
import com.moriafly.salt.ui.sheet.SheetProperties
import com.moriafly.salt.ui.sheet.SheetScope
import com.moriafly.salt.ui.thenIf

@Composable
internal fun SheetSurface(
    state: SheetTransitionState,
    scrimAlpha: () -> Float,
    dimAlpha: () -> Float,
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    environment: SheetHostEnvironment,
    title: String?,
    properties: SheetProperties,
    actions: @Composable RowScope.() -> Unit,
    content: @Composable SheetScope.() -> Unit
) {
    val directInput = remember { SheetDirectInput() }
    val bottom = properties.displayMode.isBottom(
        adaptiveWindow = environment.adaptiveWindow,
        windowSize = environment.windowSize
    )
    val nestedScroll = rememberSheetNestedScroll(
        state = state,
        directInput = directInput,
        bottom = bottom,
        visible = visible,
        properties = properties,
        onDismissRequest = onDismissRequest
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .thenIf(!visible) {
                clearAndSetSemantics { hideFromAccessibility() }
            }
            .sheetModalInput(
                visible = visible,
                directInput = directInput
            )
    ) {
        SheetScrim(
            alpha = scrimAlpha,
            onDismissRequest = onDismissRequest
        )
        SheetViewport(
            state = state,
            dimAlpha = dimAlpha,
            visible = visible,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            environment = environment,
            title = title,
            properties = properties,
            bottom = bottom,
            nestedScroll = nestedScroll,
            actions = actions,
            content = content
        )
    }
}

@Composable
private fun rememberSheetNestedScroll(
    state: SheetTransitionState,
    directInput: SheetDirectInput,
    bottom: Boolean,
    visible: Boolean,
    properties: SheetProperties,
    onDismissRequest: () -> Unit
): SheetNestedScroll {
    val currentBottom by rememberUpdatedState(bottom)
    val currentVisible by rememberUpdatedState(visible)
    val currentProperties by rememberUpdatedState(properties)
    val currentDismissRequest by rememberUpdatedState(onDismissRequest)
    return remember(state, directInput) {
        SheetNestedScroll(
            state = state,
            enabled = {
                currentBottom && currentVisible && state.ready && currentProperties.dragEnabled &&
                    (currentProperties.dismissible || state.points.size > 1) &&
                    directInput.active
            },
            scrollBehavior = { currentProperties.scrollBehavior },
            dismissible = { currentProperties.dismissible },
            onDismissRequest = { currentDismissRequest() }
        )
    }
}

@Composable
private fun SheetViewport(
    state: SheetTransitionState,
    dimAlpha: () -> Float,
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    environment: SheetHostEnvironment,
    title: String?,
    properties: SheetProperties,
    bottom: Boolean,
    nestedScroll: SheetNestedScroll,
    actions: @Composable RowScope.() -> Unit,
    content: @Composable SheetScope.() -> Unit
) {
    val density = LocalDensity.current
    val viewportInsets = sheetViewportInsets(bottom = bottom, environment = environment)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(viewportInsets)
    ) {
        val panelWidth = sheetPanelWidth(
            availableWidth = maxWidth,
            bottom = bottom,
            sizeClass = environment.adaptiveWindow.sizeClass
        )
        val heightLimit = sheetPanelHeight(
            availableHeight = maxHeight,
            panelWidth = panelWidth,
            bottom = bottom
        )
        SheetPanel(
            state = state,
            dimAlpha = dimAlpha,
            visible = visible,
            onDismissRequest = onDismissRequest,
            title = title,
            properties = properties,
            bottom = bottom,
            bottomInsets = if (bottom) environment.contentInsets else WindowInsets(),
            imeInsets = if (bottom) environment.imeInsets else WindowInsets(),
            availableHeight = with(density) { heightLimit.roundToPx() },
            floatingHiddenBase = {
                maxHeight.toPx() / 2 + viewportInsets.getBottom(this)
            },
            modifier = Modifier
                .align(if (bottom) Alignment.BottomCenter else Alignment.Center)
                .width(panelWidth)
                .consumeWindowInsets(environment.contentInsets.only(WindowInsetsSides.Bottom))
                .nestedScroll(nestedScroll)
                .then(modifier),
            actions = actions,
            content = content
        )
    }
}

private fun sheetViewportInsets(
    bottom: Boolean,
    environment: SheetHostEnvironment
): WindowInsets = if (bottom) {
    environment.contentInsets.only(WindowInsetsSides.Top).union(
        SheetSymmetricInsets(
            insets = environment.contentInsets,
            minimum = 0.dp
        ).only(WindowInsetsSides.Horizontal)
    )
} else {
    SheetSymmetricInsets(
        insets = environment.safeInsets,
        minimum = SheetDefaults.WindowMargin
    ).union(environment.imeInsets)
}

private fun sheetPanelWidth(
    availableWidth: Dp,
    bottom: Boolean,
    sizeClass: WindowSizeClass
): Dp {
    val maximumWidth = if (bottom) SheetDefaults.BottomMaxWidth else SheetDefaults.FloatingMaxWidth
    val margin = if (bottom && sizeClass != WindowSizeClass.Compact) {
        SheetDefaults.WindowMargin
    } else {
        0.dp
    }
    return (availableWidth - margin * 2).coerceIn(0.dp, maximumWidth)
}

private fun sheetPanelHeight(
    availableHeight: Dp,
    panelWidth: Dp,
    bottom: Boolean
): Dp = if (bottom) {
    availableHeight
} else {
    availableHeight
        .coerceAtLeast(0.dp)
        .coerceAtMost(SheetDefaults.FloatingMaxHeight)
        .coerceAtMost(panelWidth * SheetDefaults.FLOATING_HEIGHT_TO_WIDTH_RATIO)
}
