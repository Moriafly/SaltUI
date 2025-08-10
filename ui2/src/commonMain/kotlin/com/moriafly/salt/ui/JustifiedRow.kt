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
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import kotlin.math.max

/**
 * A layout that arranges two children at the start and end of its container.
 *
 * It prioritizes measuring the children at their intrinsic widths, respecting the minimum
 * space between them. If the total required width exceeds the available space, it splits
 * the available width equally between the two children, potentially causing them to wrap.
 * This composable fully supports LTR and RTL layout directions.
 *
 * @param startContent The composable to place at the start of the layout.
 * @param endContent The composable to place at the end of the layout.
 * @param modifier The [Modifier] to be applied to this layout.
 * @param spaceBetween The minimum space between the start and end content when there is
 * sufficient room.
 */
@Composable
fun JustifiedRow(
    startContent: @Composable () -> Unit,
    endContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    spaceBetween: Dp = SaltTheme.dimens.subPadding
) {
    val spaceBetweenPx = with(LocalDensity.current) { spaceBetween.roundToPx() }

    Layout(
        content = {
            startContent()
            endContent()
        },
        modifier = modifier.fillMaxWidth()
    ) { measurables, constraints ->

        if (measurables.size != 2) {
            error("JustifiedRow expects exactly two children")
        }

        val startMeasurable = measurables[0]
        val endMeasurable = measurables[1]
        val parentMaxWidth = constraints.maxWidth

        // Use intrinsic measurement to decide the layout strategy ahead of time
        val startIntrinsicWidth = startMeasurable.maxIntrinsicWidth(0)
        val endIntrinsicWidth = endMeasurable.maxIntrinsicWidth(0)

        val totalRequiredWidth = startIntrinsicWidth + spaceBetweenPx + endIntrinsicWidth

        val startPlaceable: Placeable
        val endPlaceable: Placeable

        if (totalRequiredWidth <= parentMaxWidth) {
            // Enough space: measure children with their preferred width
            startPlaceable = startMeasurable.measure(Constraints())
            endPlaceable = endMeasurable.measure(Constraints())
        } else {
            // Not enough space: divide width equally
            val halfWidthConstraints = constraints.copy(
                minWidth = 0,
                maxWidth = parentMaxWidth / 2
            )
            startPlaceable = startMeasurable.measure(halfWidthConstraints)
            endPlaceable = endMeasurable.measure(halfWidthConstraints)
        }

        val layoutHeight = max(startPlaceable.height, endPlaceable.height)

        layout(
            width = parentMaxWidth,
            height = layoutHeight
        ) {
            // placeRelative handles LTR and RTL automatically
            startPlaceable.placeRelative(x = 0, y = 0)
            endPlaceable.placeRelative(x = parentMaxWidth - endPlaceable.width, y = 0)
        }
    }
}
