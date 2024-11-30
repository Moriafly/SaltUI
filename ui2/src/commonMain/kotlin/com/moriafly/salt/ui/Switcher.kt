/*
 * Salt UI
 * Copyright (C) 2024 Moriafly
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

@file:Suppress("unused")

package com.moriafly.salt.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Toggle Switch Component.
 *
 * This component is used to toggle between two states, indicating the current state by changing
 * the background color and the position of an internal ball.
 *
 * It uses animations to enhance the visual experience and supports layout direction switching.
 *
 * @param state the current state of the component.
 * @param modifier a modifier used to customize the layout of the component.
 */
@Composable
fun Switcher(
    state: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (state) {
            SaltTheme.colors.highlight
        } else {
            SaltTheme.colors.subText.copy(alpha = 0.1f)
        },
        animationSpec = tween(300)
    )

    Box(
        modifier = modifier
            .size(46.dp, 26.dp)
            .clip(CircleShape)
            .drawWithCache {
                onDrawBehind {
                    drawRect(color = backgroundColor)
                }
            }
            .padding(5.dp)
    ) {
        val layoutDirection = LocalLayoutDirection.current
        val translationX by animateDpAsState(
            targetValue = if (state) {
                when (layoutDirection) {
                    LayoutDirection.Ltr -> 20.dp
                    LayoutDirection.Rtl -> (-20).dp
                }
            } else {
                0.dp
            },
            animationSpec = tween(300)
        )
        Box(
            modifier = Modifier
                .graphicsLayer {
                    this.translationX = translationX.toPx()
                }
                .size(16.dp)
                .border(width = 4.dp, color = Color.White, shape = CircleShape)
        )
    }
}