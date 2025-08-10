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
        content = {
            startContent()
            endContent()
        },
        modifier = modifier
            .fillMaxWidth()
    ) { measurables, constraints ->
        require(measurables.size == 2) { "JustifiedRow expects exactly two children" }

        val (startMeasurable, endMeasurable) = measurables
        val parentMaxWidth = constraints.maxWidth
        // Width available to children after reserving the minimum gap (never negative)
        val availableSpace = (parentMaxWidth - spaceBetweenPx).coerceAtLeast(0)

        // Height hint for intrinsics; use bounded maxHeight if available
        val heightHint = if (constraints.hasBoundedHeight) constraints.maxHeight else 0

        val startIntrinsic = startMeasurable.maxIntrinsicWidth(heightHint)
        val endIntrinsic = endMeasurable.maxIntrinsicWidth(heightHint)
        val totalRequired = startIntrinsic + spaceBetweenPx + endIntrinsic

        // Decide hard caps for measurement so min gap is enforced by construction
        val (startCap, endCap) = if (totalRequired <= parentMaxWidth) {
            // Fit case: cap at intrinsic to avoid uncontrolled expansion
            startIntrinsic to endIntrinsic
        } else {
            val isStartSmall = startIntrinsic < availableSpace / 2
            val isEndSmall = endIntrinsic < availableSpace / 2
            when {
                isStartSmall && !isEndSmall -> {
                    val endCap = (availableSpace - startIntrinsic).coerceAtLeast(0)
                    startIntrinsic to endCap
                }

                !isStartSmall && isEndSmall -> {
                    val startCap = (availableSpace - endIntrinsic).coerceAtLeast(0)
                    startCap to endIntrinsic
                }

                else -> {
                    // Even split; handle odd pixels to keep sum exact
                    val half = availableSpace / 2
                    val other = availableSpace - half
                    half to other
                }
            }
        }

        // Build child constraints with width caps aligned to parent height constraints
        fun capWidth(maxW: Int): Constraints = Constraints(
            minWidth = 0,
            maxWidth = maxW.coerceAtLeast(0),
            minHeight = 0,
            maxHeight = constraints.maxHeight
        )

        val startPlaceable = startMeasurable.measure(capWidth(startCap))
        val endPlaceable = endMeasurable.measure(capWidth(endCap))

        val measuredHeight = max(startPlaceable.height, endPlaceable.height)
        val layoutWidth = constraints.constrainWidth(parentMaxWidth)
        val layoutHeight = constraints.constrainHeight(measuredHeight)

        // Place at relative edges; remaining space forms the gap (≥ spaceBetweenPx)
        layout(width = layoutWidth, height = layoutHeight) {
            val startY = verticalAlignment.align(startPlaceable.height, layoutHeight)
            val endY = verticalAlignment.align(endPlaceable.height, layoutHeight)

            startPlaceable.placeRelative(x = 0, y = startY)
            endPlaceable.placeRelative(x = layoutWidth - endPlaceable.width, y = endY)
        }
    }
}
