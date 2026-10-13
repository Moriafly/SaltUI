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

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.sheet.SheetDetent
import com.moriafly.salt.ui.sheet.SheetProperties
import kotlin.math.abs
import kotlin.math.roundToInt

internal class SheetGeometry(
    val points: List<Pair<SheetDetent, Float>>,
    val selected: SheetDetent
)

/**
 * Resolves configuration into physical heights without changing visibility or starting animations.
 */
internal fun resolveSheetGeometry(
    density: Density,
    height: Int,
    chromeHeight: Int,
    bottomInset: Int,
    contentHeight: Float,
    preferred: SheetDetent,
    previousHeight: Float?,
    properties: SheetProperties
): SheetGeometry {
    val inset = bottomInset.coerceIn(0, height).toFloat()
    val usableHeight = height - inset
    val chromeMinimum = (chromeHeight - inset).coerceIn(0f, usableHeight)
    val minimum = (chromeMinimum + with(density) { 48.dp.toPx() }).coerceAtMost(usableHeight)

    fun SheetDetent.resolveHeight(): Float = when (this) {
        SheetDetent.Content -> contentHeight.takeIf { it > 0f } ?: usableHeight
        SheetDetent.Medium -> usableHeight * 0.5f
        SheetDetent.Large -> usableHeight
        is SheetDetent.Height -> with(density) { this@resolveHeight.height.toPx() }
        is SheetDetent.Fraction -> usableHeight * fraction
    }

    fun SheetDetent.constrainHeight(value: Float): Float = value.coerceIn(
        minimumValue = if (this == SheetDetent.Content) chromeMinimum else minimum,
        maximumValue = usableHeight
    ) + inset
    val points = properties.detents.mapNotNull { detent ->
        val target = detent.resolveHeight()
        if (detent == SheetDetent.Medium && target < minimum) {
            null
        } else {
            detent to detent.constrainHeight(value = target)
        }
    }.distinctBy { it.second.roundToInt() }
        .sortedBy { it.second }
        .ifEmpty { listOf(SheetDetent.Large to height.toFloat()) }
    val preferredHeight = preferred.constrainHeight(value = preferred.resolveHeight())
    val selected = points.firstOrNull { it.first == preferred }?.first
        ?: points.firstOrNull {
            preferred in properties.detents && abs(it.second - preferredHeight) < 1f
        }?.first
        ?: points.minWith(
            compareBy<Pair<SheetDetent, Float>> {
                abs(it.second - (previousHeight ?: preferredHeight))
            }.thenByDescending { it.second }
        ).first
    return SheetGeometry(points = points, selected = selected)
}
