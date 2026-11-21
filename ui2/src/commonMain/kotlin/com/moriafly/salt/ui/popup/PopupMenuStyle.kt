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

package com.moriafly.salt.ui.popup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * Chooses how the menu surface uses its backdrop.
 */
@UnstableSaltUiApi
internal enum class PopupMenuMaterial {
    Glass,
    Blur,
    Solid
}

/**
 * Appearance of a popup menu surface.
 *
 * [containerColor] fills the surface and is used on platforms without backdrop effects.
 * [tintColor] colors the sampled backdrop. Use its alpha to control the tint strength.
 * [borderColor] draws the outline. Use [Color.Transparent] to hide it.
 */
@Immutable
@UnstableSaltUiApi
internal data class PopupMenuStyle(
    val material: PopupMenuMaterial,
    val containerColor: Color,
    val tintColor: Color,
    val borderColor: Color
)

@UnstableSaltUiApi
internal object PopupMenuDefaults {
    /**
     * Resolves a menu style from the current theme. The container defaults to the theme's popup
     * color. The tint uses the container color at up to 60% opacity in light mode and 78% in dark.
     * The outline uses the theme stroke in light mode and white at 16% in dark mode.
     */
    @Composable
    @ReadOnlyComposable
    fun style(
        material: PopupMenuMaterial = PopupMenuMaterial.Glass,
        containerColor: Color = SaltTheme.colors.popup,
        tintColor: Color = containerColor.withMaxAlpha(
            if (SaltTheme.configs.isDarkTheme) 0.78f else 0.60f
        ),
        borderColor: Color = if (SaltTheme.configs.isDarkTheme) {
            Color.White.copy(alpha = 0.16f)
        } else {
            SaltTheme.colors.stroke
        }
    ): PopupMenuStyle = PopupMenuStyle(
        material = material,
        containerColor = containerColor,
        tintColor = tintColor,
        borderColor = borderColor
    )
}

private fun Color.withMaxAlpha(maxAlpha: Float): Color =
    if (isSpecified) copy(alpha = minOf(alpha, maxAlpha)) else Color.Transparent
