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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.sheet.SheetContentLayout
import com.moriafly.salt.ui.sheet.SheetDetent
import com.moriafly.salt.ui.sheet.SheetProperties
import com.moriafly.salt.ui.sheet.SheetScope
import kotlin.math.roundToInt

private enum class SheetLayoutSlot { Header, Body, Actions, Inset }

/**
 * Measures insets and actions before composing the body with immutable layout information.
 * Content spacing belongs to the caller; keyboard protection always belongs to the Sheet.
 */
@Composable
internal fun SheetLayout(
    state: SheetTransitionState,
    properties: SheetProperties,
    bottom: Boolean,
    bottomInsets: WindowInsets,
    imeInsets: WindowInsets,
    availableHeight: Int,
    modifier: Modifier,
    header: @Composable (compactChrome: Boolean) -> Unit,
    actions: @Composable (compactChrome: Boolean) -> Unit,
    content: @Composable SheetScope.() -> Unit
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val viewportHeight = availableHeight.coerceAtMost(constraints.maxHeight).coerceAtLeast(0)
        val width = constraints.maxWidth
        val bottomInset = bottomInsets.getBottom(density = this).coerceIn(0, viewportHeight)
        val imeInset = imeInsets.getBottom(density = this).coerceIn(0, bottomInset)
        val usableHeight = viewportHeight - bottomInset
        val compactChrome = usableHeight < 200.dp.roundToPx()
        val loose = Constraints(
            minWidth = width,
            maxWidth = width,
            maxHeight = viewportHeight
        )
        val footer = subcompose(slotId = SheetLayoutSlot.Actions) {
            actions(compactChrome)
        }.single().measure(
            constraints = loose.copy(
                maxHeight = if (compactChrome) usableHeight / 3 else usableHeight
            )
        )
        val noActions = footer.height == 0
        val ownsContentPadding =
            noActions && properties.contentLayout == SheetContentLayout.EdgeToEdge
        val trailingSpace = if (noActions) SheetDefaults.NoActionsBottomPadding.roundToPx() else 0
        val protectedBottomInset = if (ownsContentPadding) imeInset else bottomInset
        val contentPadding = PaddingValues(
            bottom = if (ownsContentPadding) {
                (trailingSpace + bottomInset - imeInset).toDp()
            } else {
                0.dp
            }
        )
        val bodyMinimum = if (compactChrome) {
            (usableHeight / 3).coerceAtMost(48.dp.roundToPx())
        } else {
            0
        }
        val header = subcompose(slotId = SheetLayoutSlot.Header) {
            header(compactChrome)
        }.single().measure(
            constraints = loose.copy(
                maxHeight = (usableHeight - footer.height - trailingSpace - bodyMinimum)
                    .coerceAtLeast(0)
            )
        )
        val chrome = header.height + footer.height + bottomInset + trailingSpace
        val measureContent = SheetDetent.Content in properties.detents &&
            (!state.ready || state.selected == SheetDetent.Content)
        val limit = if (!state.ready || measureContent) {
            viewportHeight
        } else if (bottom) {
            state.surfaceHeight.roundToInt()
        } else {
            state.selectedHeight.roundToInt()
        }
        val body = subcompose(slotId = SheetLayoutSlot.Body) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val scope = remember(this, contentPadding) {
                    SheetScopeImpl(
                        scope = this,
                        contentPadding = contentPadding
                    )
                }
                scope.content()
            }
        }.single().measure(
            constraints = loose.copy(
                maxHeight = (
                    limit - if (ownsContentPadding) {
                        header.height + protectedBottomInset
                    } else {
                        chrome
                    }
                ).coerceIn(0, viewportHeight)
            )
        )
        val measuredHeight = if (ownsContentPadding) {
            header.height + body.height + protectedBottomInset
        } else {
            chrome + body.height
        }
        state.updateGeometry(
            density = this,
            height = viewportHeight,
            chromeHeight = chrome,
            measuredContent = if (measureContent) measuredHeight else null,
            properties = properties,
            bottomInset = bottomInset
        )
        val height = if (bottom) {
            state.surfaceHeight.roundToInt()
        } else {
            measuredHeight.coerceAtMost(state.selectedHeight.roundToInt())
        }.coerceIn(0, viewportHeight)
        val inset = subcompose(slotId = SheetLayoutSlot.Inset) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = bottomInset.toDp())
            )
        }.single().measure(constraints = loose)
        layout(width = width, height = height) {
            header.place(x = 0, y = 0)
            body.place(x = 0, y = header.height)
            footer.place(
                x = 0,
                y = (height - bottomInset - footer.height).coerceAtLeast(header.height)
            )
            inset.place(x = 0, y = height - bottomInset)
        }
    }
}
