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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import com.moriafly.salt.ui.UnstableSaltUiApi
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle

private const val MAX_CACHE_BYTES = 16L * 1024 * 1024

// UI-thread state only: composition owns registration/leases and the capture host owns GPU layers.
// Each source owns its entries and byte reservations; sources never share baked pixels.
internal class MaterialState(
    private val cacheBudgetBytes: Long = MAX_CACHE_BYTES,
    val readback: suspend (GraphicsLayer) -> ImageBitmap = { it.toImageBitmap() }
) {
    val hazeState = HazeState()
    private val sourceStates = mutableStateListOf<MaterialSourceState>()

    internal fun createSource(mode: MaterialSourceMode): MaterialSourceState =
        MaterialSourceState(mode, cacheBudgetBytes)

    internal fun register(source: MaterialSourceState) {
        sourceStates.add(source)
    }

    internal fun unregister(source: MaterialSourceState) {
        sourceStates.remove(source)
    }

    val sources: List<MaterialSourceState>
        get() = sourceStates.toList()
}

internal class MaterialSourceState(
    val mode: MaterialSourceMode,
    private val cacheBudgetBytes: Long
) {
    private val cachedEntries = mutableStateListOf<MaterialCacheEntry>()
    val entries: List<MaterialCacheEntry>
        get() = cachedEntries.toList()
    var size by mutableStateOf(IntSize.Zero)
        private set

    // Compose reuses coordinate objects; each actual positioning callback must notify consumers.
    var coordinates by mutableStateOf<LayoutCoordinates?>(null, neverEqualPolicy())
        private set

    val onPositioned: (LayoutCoordinates) -> Unit = {
        size = it.size
        coordinates = it
    }

    internal fun acquire(
        style: HazeBlurStyle,
        performanceMode: HazePerformanceMode,
        density: Density
    ): MaterialCacheEntry? {
        cachedEntries.removeAll { it.size != size }
        val existing = cachedEntries.firstOrNull {
            it.style == style && it.performanceMode == performanceMode && it.density == density
        }
        if (existing != null) {
            existing.consumers++
            return existing
        }
        if (size.width <= 0 || size.height <= 0) {
            return null
        }

        val imageSize = IntSize(maxOf(1, size.width / 2), maxOf(1, size.height / 2))
        val bytes = imageSize.width.toLong() * imageSize.height * 4
        if (bytes > cacheBudgetBytes) {
            return null
        }
        // Prefer active surfaces. An unused image is evicted before admitting another variant.
        if (cachedEntries.sumOf { it.bytes } + bytes > cacheBudgetBytes) {
            cachedEntries.removeAll { it.consumers == 0 }
        }
        if (cachedEntries.sumOf { it.bytes } + bytes > cacheBudgetBytes) {
            return null
        }

        return MaterialCacheEntry(
            style = style,
            performanceMode = performanceMode,
            density = density,
            size = size,
            imageSize = imageSize
        ).also {
            it.consumers = 1
            cachedEntries.add(it)
        }
    }

    internal fun release(
        entry: MaterialCacheEntry
    ) {
        entry.consumers--
        if (entry.consumers != 0 || entry !in cachedEntries) {
            return
        }
        if (entry.image == null) {
            cachedEntries.remove(entry)
            return
        }
        // Keep only the most recently released image, within the same total byte budget.
        cachedEntries.removeAll { it !== entry && it.consumers == 0 }
    }
}

// An entry has identity: replacing it must also replace its capture coroutine and graphics layer.
// Eviction drops our image reference. Do not recycle pixels that an in-flight draw may still use.
internal class MaterialCacheEntry(
    val style: HazeBlurStyle,
    val performanceMode: HazePerformanceMode,
    val density: Density,
    val size: IntSize,
    val imageSize: IntSize
) {
    val bytes = imageSize.width.toLong() * imageSize.height * 4
    var result by mutableStateOf<Result<ImageBitmap>?>(null)
    var consumers = 0
    var readbackNanos = 0L
    val image: ImageBitmap?
        get() = result?.getOrNull()
}
