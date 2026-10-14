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
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
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
        val metrics = SheetLayoutMetrics(
            density = this,
            constraints = constraints,
            availableHeight = availableHeight,
            bottomInsets = bottomInsets,
            imeInsets = imeInsets
        )
        val footer = measureSheetChrome(
            slot = SheetLayoutSlot.Actions,
            metrics = metrics,
            maxHeight = metrics.actionsHeightLimit,
            content = actions
        )
        val spacing = SheetContentSpacing(
            density = this,
            metrics = metrics,
            properties = properties,
            footerHeight = footer.height
        )
        val header = measureSheetChrome(
            slot = SheetLayoutSlot.Header,
            metrics = metrics,
            maxHeight = spacing.headerHeightLimit,
            content = header
        )
        val chrome = header.height + spacing.reservedHeight
        val bodyChrome = spacing.bodyChromeHeight(header.height)
        val measureContent = state.needsContentMeasurement(properties)
        val limit = state.bodyHeightLimit(
            viewportHeight = metrics.viewportHeight,
            bottom = bottom,
            measureContent = measureContent
        )
        val body = subcompose(slotId = SheetLayoutSlot.Body) {
            SheetBody(contentPadding = spacing.contentPadding, content = content)
        }.single().measure(
            constraints = metrics.loose.copy(
                maxHeight = (limit - bodyChrome).coerceIn(0, metrics.viewportHeight)
            )
        )
        val measuredHeight = bodyChrome + body.height
        // Geometry bookkeeping reads the live animation position; it must not make that
        // position a measurement dependency when the panel only needs a layer translation.
        Snapshot.withoutReadObservation {
            state.updateGeometry(
                density = this,
                height = metrics.viewportHeight,
                chromeHeight = chrome,
                measuredContent = if (measureContent) measuredHeight else null,
                properties = properties,
                bottomInset = metrics.bottomInset
            )
        }
        val height = state.renderedHeight(
            bottom = bottom,
            measuredHeight = measuredHeight,
            viewportHeight = metrics.viewportHeight
        )
        val inset = subcompose(slotId = SheetLayoutSlot.Inset) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(metrics.bottomInset.toDp())
            )
        }.single().measure(metrics.loose)
        layout(width = constraints.maxWidth, height = height) {
            header.place(x = 0, y = 0)
            body.place(x = 0, y = header.height)
            footer.place(
                x = 0,
                y = (height - metrics.bottomInset - footer.height).coerceAtLeast(header.height)
            )
            inset.place(x = 0, y = height - metrics.bottomInset)
        }
    }
}

private class SheetLayoutMetrics(
    density: Density,
    constraints: Constraints,
    availableHeight: Int,
    bottomInsets: WindowInsets,
    imeInsets: WindowInsets
) {
    val viewportHeight = availableHeight.coerceIn(0, constraints.maxHeight)
    val bottomInset = bottomInsets.getBottom(density).coerceIn(0, viewportHeight)
    val imeInset = imeInsets.getBottom(density).coerceIn(0, bottomInset)
    val usableHeight = viewportHeight - bottomInset
    val compactChrome = usableHeight < with(density) { 200.dp.roundToPx() }
    val actionsHeightLimit = if (compactChrome) usableHeight / 3 else usableHeight
    val bodyMinimum = if (compactChrome) {
        (usableHeight / 3).coerceAtMost(with(density) { 48.dp.roundToPx() })
    } else {
        0
    }
    val loose = Constraints(
        minWidth = constraints.maxWidth,
        maxWidth = constraints.maxWidth,
        maxHeight = viewportHeight
    )
}

private class SheetContentSpacing(
    density: Density,
    metrics: SheetLayoutMetrics,
    properties: SheetProperties,
    footerHeight: Int
) {
    private val noActions = footerHeight == 0
    private val ownsContentPadding =
        noActions && properties.contentLayout == SheetContentLayout.EdgeToEdge
    private val trailingSpace = if (noActions) {
        with(density) { SheetDefaults.NoActionsBottomPadding.roundToPx() }
    } else {
        0
    }
    private val protectedBottomInset = if (ownsContentPadding) {
        metrics.imeInset
    } else {
        metrics.bottomInset
    }
    val contentPadding = PaddingValues(
        bottom = if (ownsContentPadding) {
            with(density) { (trailingSpace + metrics.bottomInset - metrics.imeInset).toDp() }
        } else {
            0.dp
        }
    )
    val reservedHeight = footerHeight + metrics.bottomInset + trailingSpace
    val headerHeightLimit = (
        metrics.usableHeight - footerHeight - trailingSpace - metrics.bodyMinimum
    ).coerceAtLeast(0)

    fun bodyChromeHeight(headerHeight: Int): Int = headerHeight + if (ownsContentPadding) {
        protectedBottomInset
    } else {
        reservedHeight
    }
}

private fun SubcomposeMeasureScope.measureSheetChrome(
    slot: SheetLayoutSlot,
    metrics: SheetLayoutMetrics,
    maxHeight: Int,
    content: @Composable (compactChrome: Boolean) -> Unit
): Placeable = subcompose(slot) {
    content(metrics.compactChrome)
}.single().measure(metrics.loose.copy(maxHeight = maxHeight))

@Composable
private fun SheetBody(
    contentPadding: PaddingValues,
    content: @Composable SheetScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val scope = remember(this, contentPadding) {
            SheetScopeImpl(scope = this, contentPadding = contentPadding)
        }
        scope.content()
    }
}

private fun SheetTransitionState.needsContentMeasurement(properties: SheetProperties): Boolean =
    SheetDetent.Content in properties.detents && (!ready || selected == SheetDetent.Content)

private fun SheetTransitionState.bodyHeightLimit(
    viewportHeight: Int,
    bottom: Boolean,
    measureContent: Boolean
): Int = when {
    !ready || measureContent -> viewportHeight
    bottom -> surfaceHeight.roundToInt()
    else -> selectedHeight.roundToInt()
}

private fun SheetTransitionState.renderedHeight(
    bottom: Boolean,
    measuredHeight: Int,
    viewportHeight: Int
): Int = if (bottom) {
    surfaceHeight.roundToInt()
} else {
    measuredHeight.coerceAtMost(selectedHeight.roundToInt())
}.coerceIn(0, viewportHeight)
