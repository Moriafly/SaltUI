/*
 * Copyright 2024 The Android Open Source Project
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
 *
 * Source: https://dl.google.com/dl/android/maven2/androidx/compose/material3/material3-android/1.4.0/material3-android-1.4.0-sources.jar
 * Modified for SaltUI: use SaltUI theme colors and the bundled ripple nodes.
 */

package com.moriafly.salt.ui

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.unit.Dp
import com.moriafly.salt.ui.indication.ripple.createPlatformRippleNode

/**
 * Creates a ripple indication. [SaltTheme] uses it by default through [SaltConfigs.default].
 *
 * Presses animate a ripple, while hover, focus, and drag draw a state overlay.
 *
 * @param bounded If true, the ripple starts at the press position and is clipped to the
 *   layout bounds. Otherwise, it starts at the center and may extend outside those bounds.
 * @param radius The final ripple radius. [Dp.Unspecified] derives it from the layout size.
 * @param color The ripple color. [Color.Unspecified] uses SaltUI's [LocalContentColor].
 */
@Stable
fun ripple(
    bounded: Boolean = true,
    radius: Dp = Dp.Unspecified,
    color: Color = Color.Unspecified
): IndicationNodeFactory {
    if (radius == Dp.Unspecified && color == Color.Unspecified) {
        return if (bounded) DefaultBoundedRipple else DefaultUnboundedRipple
    }
    return RippleIndication(bounded = bounded, radius = radius, color = color)
}

@Stable
private data class RippleIndication(
    val bounded: Boolean,
    val radius: Dp,
    val color: Color
) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        createPlatformRippleNode(
            interactionSource = interactionSource,
            bounded = bounded,
            radius = radius,
            color = color
        )
}

private val DefaultBoundedRipple = RippleIndication(
    bounded = true,
    radius = Dp.Unspecified,
    color = Color.Unspecified
)

private val DefaultUnboundedRipple = DefaultBoundedRipple.copy(bounded = false)
