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

package com.moriafly.salt.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import kotlin.math.max

/**
 * A layout that arranges two children at the start and end of its container.
 *
 * It prioritizes measuring the children at their intrinsic widths, respecting the minimum
 * space between them. If space is insufficient, it intelligently allocates space, allowing
 * a smaller child (one taking less than half the available space) to maintain its preferred
 * size while the larger child fills the remainder. Only if this condition isn't met will
 * the space be divided.
 *
 * This composable fully supports LTR and RTL layout directions.
 *
 * @param startContent The composable to place at the start of the layout.
 * @param endContent The composable to place at the end of the layout.
 * @param modifier The [Modifier] to be applied to this layout.
 * @param verticalAlignment The vertical alignment of the children. Defaults to Top.
 * @param spaceBetween The minimum space between the start and end content when there is
 * sufficient room.
 */
@UnstableSaltUiApi
@Composable
fun JustifiedRow(
    startContent: @Composable () -> Unit,
    endContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    spaceBetween: Dp = SaltTheme.dimens.subPadding
) {
    val spaceBetweenPx = with(LocalDensity.current) { spaceBetween.roundToPx() }

    Layout(
        contents = listOf(startContent, endContent),
        modifier = modifier
            .fillMaxWidth()
    ) { measurables, constraints ->
        val (startMeasurables, endMeasurables) = measurables
        val parentMaxWidth = constraints.maxWidth
        val heightHint = if (constraints.hasBoundedHeight) constraints.maxHeight else 0

        val startIntrinsic = startMeasurables
            .fastMap { it.maxIntrinsicWidth(heightHint) }
            .maxOrNull() ?: 0

        val endIntrinsic = endMeasurables
            .fastMap { it.maxIntrinsicWidth(heightHint) }
            .maxOrNull() ?: 0

        // Check if both exist, only add spacing in that case
        val hasStartContent = startMeasurables.isNotEmpty()
        val hasEndContent = endMeasurables.isNotEmpty()

        val spaceToUse = if (hasStartContent && hasEndContent) spaceBetweenPx else 0

        // Available space for allocation calculation
        val availableSpace = (parentMaxWidth - spaceToUse).coerceAtLeast(0)
        // Total required width including the gap (if needed)
        val totalRequired = startIntrinsic + spaceToUse + endIntrinsic

        // Decide hard caps for measurement
        val (startCap, endCap) = if (totalRequired <= parentMaxWidth) {
            // Fits (or only one element exists and it fits)
            startIntrinsic to endIntrinsic
        } else {
            // Not enough space, apply smart allocation logic
            // This logic works correctly even with only one element (e.g., hasStartContent=true, hasEndContent=false)
            // because isEndSmall will be true, isStartSmall will be false (assuming start overflows)
            // This enters the !isStartSmall && isEndSmall branch, calculating startCap = availableSpace, endCap = 0, which is correct
            val isStartSmall = startIntrinsic < availableSpace / 2
            val isEndSmall = endIntrinsic < availableSpace / 2
            when {
                isStartSmall && !isEndSmall -> {
                    val endCapCalculated = (availableSpace - startIntrinsic).coerceAtLeast(0)
                    startIntrinsic to endCapCalculated
                }

                !isStartSmall && isEndSmall -> {
                    val startCapCalculated = (availableSpace - endIntrinsic).coerceAtLeast(0)
                    startCapCalculated to endIntrinsic
                }

                else -> {
                    // Both are large or both are small (but sum overflows), split evenly
                    val half = availableSpace / 2
                    val other = availableSpace - half
                    half to other
                }
            }
        }

        // Build child constraints
        fun capWidth(maxW: Int): Constraints = Constraints(
            minWidth = 0,
            maxWidth = maxW.coerceAtLeast(0),
            minHeight = 0,
            maxHeight = constraints.maxHeight
        )

        val startPlaceables = startMeasurables.fastMap { it.measure(capWidth(startCap)) }
        val endPlaceables = endMeasurables.fastMap { it.measure(capWidth(endCap)) }

        val maxStartPlaceablesHeight = startPlaceables.maxOfOrNull { it.height } ?: 0
        val maxEndPlaceablesHeight = endPlaceables.maxOfOrNull { it.height } ?: 0

        val measuredHeight = max(
            maxStartPlaceablesHeight,
            maxEndPlaceablesHeight
        )
        val layoutWidth = constraints.constrainWidth(parentMaxWidth)
        val layoutHeight = constraints.constrainHeight(measuredHeight)

        // Placement
        layout(width = layoutWidth, height = layoutHeight) {
            // Calculate Y alignment
            val startY = verticalAlignment.align(maxStartPlaceablesHeight, layoutHeight)
            val endY = verticalAlignment.align(maxEndPlaceablesHeight, layoutHeight)

            // Place all start children (stacked at 0, startY)
            startPlaceables.fastForEach { it.placeRelative(x = 0, y = startY) }

            // Place all end children (each individually right-aligned, forming a "right-aligned stack")
            endPlaceables.fastForEach { it.placeRelative(x = layoutWidth - it.width, y = endY) }
        }
    }
}
