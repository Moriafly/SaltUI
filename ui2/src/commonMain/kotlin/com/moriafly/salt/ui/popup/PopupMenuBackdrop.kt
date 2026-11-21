/*
 * SaltUI
 * Copyright (C) 2023 Moriafly
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

@file:Suppress("unused")

package com.moriafly.salt.ui.popup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.moriafly.salt.ui.UnstableSaltUiApi
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/**
 * Captures the content behind descendant menus for the SaltUI glass effect.
 * Wrap the window or the part of the page that menus should sample.
 *
 * Use [enabled] to turn sampling off and render menus with their solid container color.
 */
@UnstableSaltUiApi
@Composable
fun PopupMenuBackdrop(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val hazeState = remember { HazeState() }
    val backdrop = remember(hazeState, enabled) {
        if (enabled) PopupBackdrop(HazeInput.Sources(hazeState)) else null
    }
    CompositionLocalProvider(LocalPopupBackdrop provides backdrop) {
        Box(
            modifier = if (enabled) modifier.hazeSource(hazeState) else modifier,
            content = content
        )
    }
}

internal class PopupBackdrop(
    val input: HazeInput.Sources
)

internal val LocalPopupBackdrop = staticCompositionLocalOf<PopupBackdrop?> { null }
