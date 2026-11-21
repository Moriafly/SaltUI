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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.material.GlassSource
import com.moriafly.salt.ui.material.glassSource
import dev.chrisbanes.haze.hazeSource

/**
 * Captures the content behind descendant menus for the SaltUI glass effect.
 * Include the Compose background fills that should be sampled in [content]. The menu blends
 * the captured colors and their alpha with its own translucent fill. Backgrounds outside
 * this capture can show through the resulting translucent pixels.
 */
@UnstableSaltUiApi
@Composable
fun PopupMenuBackdrop(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    ProvidePopupBackdrop(
        enabled = enabled
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
    content: @Composable (backdropSource: Modifier) -> Unit
) {
    val backdrop = if (enabled) {
        val windowInfo = LocalWindowInfo.current
        remember(windowInfo) { PopupBackdrop(windowInfo) }
    } else {
        null
    }
    CompositionLocalProvider(LocalPopupBackdrop provides backdrop) {
        content(backdrop?.sourceModifier ?: Modifier)
    }
}

@OptIn(UnstableSaltUiApi::class)
internal class PopupBackdrop(
    private val windowInfo: WindowInfo
) {
    private val source = GlassSource()
    private val sharesHostCanvas = popupSharesHostCanvas

    val input = source.hazeInput
    val sourceModifier = if (sharesHostCanvas) {
        Modifier.glassSource(source)
    } else {
        Modifier.hazeSource(source.hazeState)
    }

    @Composable
    fun cutout(shape: Shape): Modifier =
        if (sharesHostCanvas && LocalWindowInfo.current === windowInfo) {
            source.cutout(
                shape = shape,
                useWindowCoordinates = true
            )
        } else {
            Modifier
        }
}

internal val LocalPopupBackdrop = staticCompositionLocalOf<PopupBackdrop?> { null }

internal expect val popupSharesHostCanvas: Boolean
