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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import com.moriafly.salt.ui.LocalControlSize
import com.moriafly.salt.ui.ProvideContentColorTextStyle
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.button.PillButtonDefaults
import com.moriafly.salt.ui.internal.SmoothRoundedRectangleShape
import com.moriafly.salt.ui.sheet.SheetProperties
import com.moriafly.salt.ui.sheet.SheetScope
import com.moriafly.salt.ui.thenIf

@Composable
internal fun SheetPanel(
    state: SheetTransitionState,
    dimAlpha: () -> Float,
    visible: Boolean,
    onDismissRequest: () -> Unit,
    title: String?,
    properties: SheetProperties,
    bottom: Boolean,
    bottomInsets: WindowInsets,
    imeInsets: WindowInsets,
    availableHeight: Int,
    floatingHiddenBase: Density.() -> Float,
    modifier: Modifier,
    actions: @Composable RowScope.() -> Unit,
    content: @Composable SheetScope.() -> Unit
) {
    val canResize = if (state.ready) {
        state.points.size > 1
    } else {
        properties.detents.distinct().size > 1
    }
    val canDrag = bottom && properties.dragEnabled && (canResize || properties.dismissible)
    val closeButtonSize = PillButtonDefaults.containerHeight(LocalControlSize.current)
    val cornerRadius = closeButtonSize / 2 + SaltTheme.dimens.padding
    val shape = remember(bottom, cornerRadius) {
        SmoothRoundedRectangleShape(radius = cornerRadius, topOnly = bottom)
    }
    ProvideContentColorTextStyle(
        contentColor = SaltTheme.colors.text,
        textStyle = SaltTheme.textStyles.main
    ) {
        SheetLayout(
            state = state,
            properties = properties,
            bottom = bottom,
            bottomInsets = bottomInsets,
            imeInsets = imeInsets,
            availableHeight = availableHeight,
            modifier = Modifier
                .graphicsLayer {
                    alpha = 1f
                    scaleX = 1f
                    scaleY = 1f
                    if (bottom) {
                        translationY = state.surfaceHeight - state.visibleHeight
                    } else {
                        translationY = (floatingHiddenBase(this) + size.height / 2) *
                            (1f - state.presentationProgress)
                    }
                }
                .clip(shape = shape)
                .drawWithContent {
                    drawContent()
                    val alpha = dimAlpha()
                    if (alpha > 0f) drawRect(color = Color.Black.copy(alpha = alpha))
                }
                .background(color = SaltTheme.colors.background)
                .border(
                    width = 0.5.dp,
                    color = SaltTheme.colors.stroke,
                    shape = shape
                )
                .semantics {
                    dialog()
                    isTraversalGroup = true
                    paneTitle = title ?: "Sheet"
                }
                .sheetHeightActions(
                    state = state,
                    enabled = bottom && visible
                )
                // Blank interior space stays in the panel's hit path rather than the scrim's.
                .pointerInput(Unit) { }
                .then(modifier),
            header = { compactChrome ->
                SheetHeader(
                    state = state,
                    title = title,
                    visible = visible,
                    properties = properties,
                    showHandle = canDrag,
                    dragEnabled = canDrag && visible && state.ready,
                    compactChrome = compactChrome,
                    onDismissRequest = onDismissRequest
                )
            },
            actions = { compactChrome ->
                SheetActions(
                    compactChrome = compactChrome,
                    content = actions
                )
            },
            content = content
        )
    }
}

@Composable
private fun SheetActions(
    compactChrome: Boolean,
    content: @Composable RowScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .thenIf(condition = compactChrome) {
                verticalScroll(state = scrollState)
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .sheetActionsPadding(
                    top = SheetDefaults.ActionsTopPadding,
                    bottom = SheetDefaults.ActionsBottomPadding
                )
                .padding(horizontal = SheetDefaults.ActionsHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(
                space = 12.dp,
                alignment = Alignment.End
            ),
            verticalAlignment = Alignment.Bottom,
            content = content
        )
    }
}

/**
 * Pads visible actions without reserving space for an empty action slot.
 */
private fun Modifier.sheetActionsPadding(
    top: Dp,
    bottom: Dp
): Modifier = layout { measurable, constraints ->
    val topPixels = top.roundToPx()
    val bottomPixels = bottom.roundToPx()
    val actions = measurable.measure(
        constraints = constraints.offset(vertical = -topPixels - bottomPixels)
    )
    val topInset = if (actions.height > 0) topPixels else 0
    val bottomInset = if (actions.height > 0) bottomPixels else 0
    layout(
        width = actions.width,
        height = constraints.constrainHeight(actions.height + topInset + bottomInset)
    ) {
        actions.placeRelative(x = 0, y = topInset)
    }
}
