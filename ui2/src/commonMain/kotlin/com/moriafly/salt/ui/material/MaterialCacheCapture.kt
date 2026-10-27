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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import com.moriafly.salt.ui.UnstableSaltUiApi
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.LocalHazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import kotlin.time.TimeSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay

private const val CACHE_SETTLE_MILLIS = 80L

@Composable
internal fun BoxScope.MaterialCacheContent(
    state: MaterialState,
    source: MaterialSourceState
) {
    val size = source.size
    val hasSize = size.width > 0 && size.height > 0
    val canPrepare = source.mode is MaterialSourceMode.Static &&
        state.sources.singleOrNull() === source && hasSize
    if (canPrepare) {
        var settledSize by remember(source) { mutableStateOf(IntSize.Zero) }
        LaunchedEffect(source, size) {
            settledSize = IntSize.Zero
            // Leave the first visible frames live and coalesce resize bursts before readback.
            repeat(2) { withFrameNanos {} }
            delay(CACHE_SETTLE_MILLIS)
            settledSize = size
        }
        if (settledSize == size) {
            // Keep each capture alive while other styles enter or leave.
            for (entry in source.entries) {
                key(entry) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                    ) {
                        MaterialCacheCapture(
                            state = state,
                            entry = entry
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialCacheCapture(
    state: MaterialState,
    entry: MaterialCacheEntry
) {
    if (entry.result == null) {
        val layer = rememberGraphicsLayer()
        // Drawing signals the coroutine without writing back to composition state.
        val recorded = remember(layer) { CompletableDeferred<Unit>() }
        LaunchedEffect(state, entry, layer) {
            recorded.await()
            // Keep readback after the recording frame and its following render submission.
            repeat(2) { withFrameNanos {} }
            val started = TimeSource.Monotonic.markNow()
            try {
                val image = state.readback(layer)
                entry.readbackNanos = started.elapsedNow().inWholeNanoseconds
                entry.result = Result.success(image)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                // A failed readback uses the live pipeline until invalidated.
                entry.result = Result.failure(failure)
            } catch (failure: Error) {
                if (!isMaterialAllocationFailure(failure)) {
                    throw failure
                }
                // This optional allocation must not turn a working live surface into a crash.
                entry.result = Result.failure(failure)
            }
        }
        CompositionLocalProvider(
            LocalDensity provides entry.density,
            LocalHazeBlurStyle provides HazeBlurStyle
        ) {
            val width = with(entry.density) { entry.size.width.toDp() }
            val height = with(entry.density) { entry.size.height.toDp() }
            Box(
                modifier = Modifier
                    .requiredSize(width, height)
                    .drawWithContent {
                        if (!recorded.isCompleted) {
                            layer.record(size = entry.imageSize) {
                                scale(
                                    scaleX = entry.imageSize.width.toFloat() / entry.size.width,
                                    scaleY = entry.imageSize.height.toFloat() / entry.size.height,
                                    pivot = Offset.Zero
                                ) {
                                    this@drawWithContent.drawContent()
                                }
                            }
                            recorded.complete(Unit)
                        }
                    }
                    .hazeBlur(
                        input = HazeInput.Sources(state.hazeState),
                        style = entry.style,
                        performanceMode = entry.performanceMode
                    )
            )
        }
    }
}

internal expect fun isMaterialAllocationFailure(
    failure: Error
): Boolean
