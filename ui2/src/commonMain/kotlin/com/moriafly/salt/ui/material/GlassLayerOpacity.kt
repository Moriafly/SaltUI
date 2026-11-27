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

package com.moriafly.salt.ui.material

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.layout.LayoutCoordinates

internal abstract class GlassLayerOpacity {
    val revision = mutableIntStateOf(0)

    abstract fun observe(): AutoCloseable

    protected abstract fun alphaOf(coordinates: LayoutCoordinates): Float

    fun fadingLayers(
        coordinates: LayoutCoordinates
    ): List<FadingGlassLayer> {
        var fadingLayers: MutableList<FadingGlassLayer>? = null
        var opacity = 1f
        var ancestor: LayoutCoordinates? = coordinates
        while (ancestor?.isAttached == true) {
            val alpha = alphaOf(ancestor)
            if (alpha < 1f) {
                opacity *= alpha
                val coverage = ((opacity - CutoutFadeStartOpacity) / (1f - CutoutFadeStartOpacity))
                    .coerceAtLeast(0f)
                if (fadingLayers == null) {
                    fadingLayers = ArrayList()
                }
                fadingLayers.add(FadingGlassLayer(ancestor, coverage))
                // Outer layers cannot restore a cutout whose coverage has reached zero.
                if (coverage == 0f) {
                    break
                }
            }
            ancestor = ancestor.parentCoordinates
        }
        return fadingLayers.orEmpty()
    }

    private companion object {
        // A nearly opaque glass surface can gradually replace the source underneath it.
        @Suppress("ConstPropertyName")
        const val CutoutFadeStartOpacity = 0.8f
    }
}

internal expect val glassLayerOpacity: GlassLayerOpacity?

internal data class FadingGlassLayer(
    val coordinates: LayoutCoordinates,
    val coverage: Float
)

internal fun List<FadingGlassLayer>.cutoutCoverageOver(source: LayoutCoordinates): Float {
    if (isEmpty() || !source.isAttached) return 1f

    var coverage = 1f
    for (layer in this) {
        // A shared layer fades the source and the glass together.
        var ancestor: LayoutCoordinates? = source
        while (ancestor?.isAttached == true && ancestor !== layer.coordinates) {
            ancestor = ancestor.parentCoordinates
        }
        if (ancestor === layer.coordinates) break
        coverage = layer.coverage
    }
    return coverage
}
