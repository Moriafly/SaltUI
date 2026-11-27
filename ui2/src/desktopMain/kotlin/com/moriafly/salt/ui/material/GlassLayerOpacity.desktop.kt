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

import androidx.compose.runtime.snapshots.ObserverHandle
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.node.GraphicsLayerOwnerLayer
import androidx.compose.ui.node.NodeCoordinator

internal actual val glassLayerOpacity: GlassLayerOpacity? = DesktopGlassLayerOpacity

private object DesktopGlassLayerOpacity : GlassLayerOpacity() {
    private var observer: ObserverHandle? = null
    private var placedSurfaces = 0

    override fun alphaOf(coordinates: LayoutCoordinates): Float {
        val layer = (coordinates as? NodeCoordinator)?.layer as? GraphicsLayerOwnerLayer
        return layer?.graphicsLayer?.alpha ?: 1f
    }

    override fun observe(): AutoCloseable {
        synchronized(this) {
            placedSurfaces++
            if (observer == null) {
                // Layer alpha updates do not invalidate sibling draw modifiers.
                observer = Snapshot.registerApplyObserver { changes, _ ->
                    if (changes.any { it !== revision }) {
                        synchronized(this) {
                            revision.intValue++
                        }
                    }
                }
            }
        }
        return AutoCloseable {
            synchronized(this) {
                placedSurfaces--
                if (placedSurfaces == 0) {
                    observer?.dispose()
                    observer = null
                }
            }
        }
    }
}
