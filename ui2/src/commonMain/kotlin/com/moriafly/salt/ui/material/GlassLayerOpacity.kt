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

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.moriafly.salt.ui.material

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.snapshots.ObserverHandle
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.node.NodeCoordinator
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

internal object GlassLayerOpacity {
    private val observerLock = SynchronizedObject()
    private var observer: ObserverHandle? = null
    private var placedSurfaces = 0

    private val snapshotRevision = mutableIntStateOf(0)

    val revision: Int get() = snapshotRevision.intValue

    fun observe(): AutoCloseable {
        synchronized(observerLock) {
            placedSurfaces++
            if (observer == null) {
                // Compose updates layer alpha without invalidating sibling draws.
                observer = Snapshot.registerApplyObserver { changes, _ ->
                    if (changes.any { it !== snapshotRevision }) {
                        synchronized(observerLock) {
                            if (placedSurfaces > 0) {
                                snapshotRevision.intValue++
                            }
                        }
                    }
                }
            }
        }
        return AutoCloseable {
            synchronized(observerLock) {
                placedSurfaces--
                if (placedSurfaces == 0) {
                    observer?.dispose()
                    observer = null
                }
            }
        }
    }

    fun findFadingLayers(
        coordinates: LayoutCoordinates
    ): List<FadingGlassLayer> {
        var fadingLayers: MutableList<FadingGlassLayer>? = null
        var opacity = 1f
        var ancestor: LayoutCoordinates? = coordinates
        while (ancestor?.isAttached == true) {
            val alpha = (ancestor as? NodeCoordinator)?.alpha ?: 1f
            if (alpha < 1f) {
                opacity *= alpha
                val coverage = ((opacity - CUTOUT_FADE_START) / (1f - CUTOUT_FADE_START))
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

    // A nearly opaque glass surface can gradually replace the source underneath it.
    private const val CUTOUT_FADE_START = 0.8f
}

internal data class FadingGlassLayer(
    val coordinates: LayoutCoordinates,
    val coverage: Float
)

internal fun List<FadingGlassLayer>.cutoutCoverageOver(source: LayoutCoordinates): Float {
    if (isEmpty() || !source.isAttached) return 1f

    // A shared fading layer applies to both the source and the glass.
    var ancestor: LayoutCoordinates? = source
    while (ancestor?.isAttached == true) {
        for (index in indices) {
            if (this[index].coordinates === ancestor) {
                return getOrNull(index - 1)?.coverage ?: 1f
            }
        }
        ancestor = ancestor.parentCoordinates
    }
    return last().coverage
}
