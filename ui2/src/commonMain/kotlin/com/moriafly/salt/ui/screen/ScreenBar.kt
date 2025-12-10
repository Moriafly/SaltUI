/*
 * Salt UI
 * Copyright (C) 2025 Moriafly
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

@file:Suppress("ktlint:standard:filename")

package com.moriafly.salt.ui.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.nested.CollapsedState

@UnstableSaltUiApi
@Composable
fun ScreenTopBarCollapsed(
    state: CollapsedState,
    modifier: Modifier = Modifier,
    collapsedHeight: Dp = ScreenBarDefaults.CollapsedHeight,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val collapsedHeightPx = with(density) { collapsedHeight.roundToPx() }

    val isCollapsed by remember(collapsedHeightPx) {
        derivedStateOf {
            state.collapsedOffset <= -collapsedHeightPx
        }
    }
    val alpha by animateFloatAsState(
        targetValue = if (isCollapsed) 1f else 0f,
        animationSpec = tween(durationMillis = 300)
    )
    val translationY by animateDpAsState(
        targetValue = if (isCollapsed) 0.dp else 8.dp,
        animationSpec = tween(durationMillis = 300)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(collapsedHeight)
            .clipToBounds()
            .graphicsLayer {
                this.alpha = alpha
                this.translationY = translationY.toPx()
            }
    ) {
        content()
    }
}

object ScreenBarDefaults {
    val CollapsedHeight = 56.dp
}
