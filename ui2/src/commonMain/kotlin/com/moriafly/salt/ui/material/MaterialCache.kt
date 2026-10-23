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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import dev.chrisbanes.haze.LocalHazePerformanceMode
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.hazeSource

@Composable
private fun MaterialState.rememberSource(
    mode: MaterialSourceMode
): MaterialSourceState {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val isDarkTheme = SaltTheme.configs.isDarkTheme
    // Equal declarations retain the source; supported environment changes invalidate its pixels.
    // Content-specific dependencies (including palette values) belong in mode.Static.key.
    // Entry sizes are tracked separately by the positioning callback.
    return key(this, mode) {
        val source = remember(density, layoutDirection, isDarkTheme) {
            createSource(mode)
        }
        DisposableEffect(source) {
            register(source)
            onDispose {
                unregister(source)
            }
        }
        source
    }
}

@Composable
internal fun MaterialSourceState.rememberEntry(
    style: HazeBlurStyle
): MaterialCacheEntry? {
    val density = LocalDensity.current
    val performanceMode = LocalHazePerformanceMode.current
    return key(
        this,
        style,
        performanceMode,
        density,
        size
    ) {
        var entry by remember { mutableStateOf<MaterialCacheEntry?>(null) }
        DisposableEffect(this) {
            val acquired = acquire(style, performanceMode, density)
            entry = acquired
            onDispose {
                if (acquired != null) {
                    release(acquired)
                }
            }
        }
        entry
    }
}

@Composable
internal fun BoxScope.MaterialSourceContent(
    mode: MaterialSourceMode,
    content: @Composable BoxScope.() -> Unit
) {
    val state = LocalMaterialState.current
    if (state == null) {
        Box(content = content)
    } else {
        val source = state.rememberSource(mode)

        Box(
            modifier = Modifier
                .onGloballyPositioned(source.onPositioned)
                .hazeSource(state.hazeState),
            content = content
        )
        MaterialCacheContent(
            state = state,
            source = source
        )
    }
}
