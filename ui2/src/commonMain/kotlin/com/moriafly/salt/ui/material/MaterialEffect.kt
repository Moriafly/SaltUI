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

@file:OptIn(UnstableSaltUiApi::class)

package com.moriafly.salt.ui.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import com.moriafly.salt.ui.UnstableSaltUiApi
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.LocalHazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import kotlin.math.abs

private const val COORDINATE_TOLERANCE = 0.01f

@Composable
internal fun Modifier.materialEffect(
    state: MaterialState,
    blurStyle: HazeBlurStyle
): Modifier {
    val source = state.sources.singleOrNull()
    // Multiple sources and opaque inherited styles cannot be baked independently without changing
    // blending or consumer-sized masks. Keep their existing live rendering.
    val hasCustomStyle = LocalHazeBlurStyle.current != HazeBlurStyle
    return if (source?.mode is MaterialSourceMode.Static && !hasCustomStyle) {
        var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null, neverEqualPolicy()) }
        val onPositioned = remember { { value: LayoutCoordinates -> coordinates = value } }
        val positioned = onGloballyPositioned(onPositioned)
        val eligible by remember(source) {
            derivedStateOf(structuralEqualityPolicy()) {
                val sourceCoordinates = source.coordinates
                val consumerCoordinates = coordinates
                sourceCoordinates != null && consumerCoordinates != null &&
                    cacheOffset(sourceCoordinates, consumerCoordinates) != null
            }
        }
        val entry = if (eligible) {
            source.rememberEntry(blurStyle)
        } else {
            null
        }
        val image = entry?.image
        if (entry != null && image != null) {
            positioned.drawWithContent {
                val sourceCoordinates = source.coordinates
                val consumerCoordinates = coordinates
                if (sourceCoordinates != null && consumerCoordinates != null) {
                    val offset = cacheOffset(sourceCoordinates, consumerCoordinates)
                    if (offset != null) {
                        clipRect {
                            translate(-offset.x, -offset.y) {
                                drawImage(
                                    image = image,
                                    dstSize = entry.size,
                                    filterQuality = FilterQuality.Low
                                )
                            }
                        }
                    }
                }
                drawContent()
            }
        } else {
            positioned.liveMaterial(state, blurStyle)
        }
    } else {
        liveMaterial(state, blurStyle)
    }
}

private fun Modifier.liveMaterial(
    state: MaterialState,
    style: HazeBlurStyle
): Modifier = hazeBlur(
    input = HazeInput.Sources(state.hazeState),
    style = style
)

private fun cacheOffset(
    source: LayoutCoordinates,
    consumer: LayoutCoordinates
): Offset? {
    if (!source.isAttached || !consumer.isAttached) {
        return null
    }
    if (source.findRootCoordinates() !== consumer.findRootCoordinates()) {
        return null
    }

    val width = consumer.size.width.toFloat()
    val height = consumer.size.height.toFloat()
    val start = source.localPositionOf(consumer, Offset.Zero)
    val right = source.localPositionOf(consumer, Offset(width, 0f))
    val bottom = source.localPositionOf(consumer, Offset(0f, height))
    val horizontal = right - start
    val vertical = bottom - start
    // A relative scale/rotation changes the blur radius; out-of-bounds consumers need uncached
    // edge pixels. Both cases retain the live pipeline.
    val tolerance = COORDINATE_TOLERANCE
    val sameHorizontalAxis = abs(horizontal.x - width) < tolerance && abs(horizontal.y) < tolerance
    val sameVerticalAxis = abs(vertical.x) < tolerance && abs(vertical.y - height) < tolerance
    if (!sameHorizontalAxis || !sameVerticalAxis) {
        return null
    }

    val startsInside = start.x >= -tolerance && start.y >= -tolerance
    val endsInside = right.x <= source.size.width + tolerance &&
        bottom.y <= source.size.height + tolerance
    if (!startsInside || !endsInside) {
        return null
    }
    return start
}
