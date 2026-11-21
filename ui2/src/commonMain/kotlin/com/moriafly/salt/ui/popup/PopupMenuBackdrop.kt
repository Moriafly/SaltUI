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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import com.moriafly.salt.ui.UnstableSaltUiApi
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/**
 * Captures the content behind descendant menus for the SaltUI glass effect.
 * Wrap the window or the part of the page that menus should sample.
 *
 * Set [revealWindowBackdrop] when the desktop host has a native material such as Acrylic or
 * Vibrancy. Menus in the host canvas reveal that material. Separate popup windows use the
 * regular glass fill when capture is available.
 */
@UnstableSaltUiApi
@Composable
fun PopupMenuBackdrop(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    revealWindowBackdrop: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    ProvidePopupBackdrop(
        enabled = enabled,
        revealWindowBackdrop = revealWindowBackdrop
    ) { backdropSource ->
        Box(
            modifier = modifier.then(backdropSource),
            content = content
        )
    }
}

@Composable
internal fun ProvidePopupBackdrop(
    enabled: Boolean = true,
    revealWindowBackdrop: Boolean = false,
    content: @Composable (backdropSource: Modifier) -> Unit
) {
    val hazeState = remember { HazeState() }
    val backdrop = remember(hazeState, enabled, revealWindowBackdrop) {
        if (enabled) {
            PopupBackdrop(
                input = HazeInput.Sources(hazeState),
                revealWindowBackdrop = revealWindowBackdrop
            )
        } else {
            null
        }
    }
    val backdropSource = if (backdrop != null) Modifier.hazeSource(hazeState) else Modifier
    CompositionLocalProvider(LocalPopupBackdrop provides backdrop) {
        content(backdropSource)
    }
}

internal class PopupBackdrop(
    val input: HazeInput.Sources,
    val revealWindowBackdrop: Boolean
)

internal val LocalPopupBackdrop = staticCompositionLocalOf<PopupBackdrop?> { null }

// Clear Compose's background before drawing the translucent menu over native material.
internal fun Modifier.revealPopupWindowBackdrop(shape: Shape): Modifier = drawWithCache {
    val outline = shape.createOutline(size, layoutDirection, this)
    onDrawBehind {
        drawOutline(outline, Color.Black, blendMode = BlendMode.DstOut)
    }
}

internal expect val popupSharesHostCanvas: Boolean
