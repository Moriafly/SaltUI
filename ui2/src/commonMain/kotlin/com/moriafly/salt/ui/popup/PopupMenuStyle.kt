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
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.takeOrElse
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.material.GlassStyles
import dev.chrisbanes.haze.glass.GlassDefaults

/**
 * Chooses how the menu surface uses its backdrop.
 */
@UnstableSaltUiApi
enum class PopupMenuMaterial {
    Glass,
    Blur,
    Solid
}

/**
 * Appearance shared by a menu and its submenus.
 *
 * [containerColor] fills solid menus and provides the fallback without a sampled backdrop.
 * [tintColor] colors the sampled backdrop. Use its alpha to control the tint strength.
 * [borderColor] draws the outline. Use [Color.Transparent] to hide it.
 * [backdropColor] is composited behind the sampled backdrop, including its alpha.
 */
@Immutable
@UnstableSaltUiApi
data class PopupMenuStyle(
    val material: PopupMenuMaterial,
    val containerColor: Color,
    val tintColor: Color,
    val borderColor: Color,
    val backdropColor: Color
)

@UnstableSaltUiApi
object PopupMenuDefaults {
    /**
     * Resolves a menu style from the current theme. The container defaults to the theme's popup
     * color. Dark Glass uses a more translucent tint calibrated to the theme background.
     * The outline uses the theme stroke in light mode and white at 16% in dark mode.
     * The fill behind Glass combines the tint with the light theme's white-point adjustment.
     * The fill behind Blur uses up to 26% opacity in light mode and 34% in dark mode.
     */
    @Composable
    @ReadOnlyComposable
    fun style(
        material: PopupMenuMaterial = PopupMenuMaterial.Glass,
        containerColor: Color = SaltTheme.colors.popup,
        tintColor: Color = defaultTintColor(
            material = material,
            containerColor = containerColor
        ),
        borderColor: Color = if (SaltTheme.configs.isDarkTheme) {
            Color.White.copy(alpha = 0.16f)
        } else {
            SaltTheme.colors.stroke
        },
        backdropColor: Color = defaultBackdropColor(
            material = material,
            containerColor = containerColor,
            tintColor = tintColor
        )
    ): PopupMenuStyle = PopupMenuStyle(
        material = material,
        containerColor = containerColor,
        tintColor = tintColor,
        borderColor = borderColor,
        backdropColor = backdropColor
    )

    @Composable
    @ReadOnlyComposable
    private fun defaultTintColor(
        material: PopupMenuMaterial,
        containerColor: Color
    ): Color {
        val isDarkTheme = SaltTheme.configs.isDarkTheme
        val tint = containerColor.withMaxAlpha(if (isDarkTheme) 0.78f else 0.60f)
        val preferredAlpha = 0.10f
        if (!isDarkTheme || material != PopupMenuMaterial.Glass || tint.alpha <= preferredAlpha) {
            return tint
        }

        val backgroundColor = SaltTheme.colors.background
        if (!backgroundColor.isSpecified) {
            return tint
        }

        val page = backgroundColor.convert(tint.colorSpace)
        val contrast = 1f + GlassStyles.DARK_CONTRAST

        fun shaded(channel: Float): Float =
            ((channel - 0.5f) * contrast + 0.5f).coerceIn(0f, 1f)

        // Use the previous composite on the theme page as the reference tone.
        val target = tint.compositeOver(
            page.copy(
                red = shaded(page.red),
                green = shaded(page.green),
                blue = shaded(page.blue),
                alpha = 1f
            )
        )

        // Use enough opacity to represent the target with an in-gamut tint.
        fun minimumAlpha(pageChannel: Float, targetChannel: Float): Float = when {
            targetChannel > pageChannel -> (targetChannel - pageChannel) / (1f - pageChannel)
            targetChannel < pageChannel -> (pageChannel - targetChannel) / pageChannel
            else -> 0f
        }

        val alpha = maxOf(
            preferredAlpha,
            minimumAlpha(page.red, target.red),
            minimumAlpha(page.green, target.green),
            minimumAlpha(page.blue, target.blue)
        ).coerceAtMost(1f)

        fun tintChannel(pageChannel: Float, targetChannel: Float): Float =
            (pageChannel + (targetChannel - pageChannel) / alpha).coerceIn(0f, 1f)

        return page.copy(
            red = tintChannel(page.red, target.red),
            green = tintChannel(page.green, target.green),
            blue = tintChannel(page.blue, target.blue),
            alpha = alpha
        )
    }

    @Composable
    @ReadOnlyComposable
    private fun defaultBackdropColor(
        material: PopupMenuMaterial,
        containerColor: Color,
        tintColor: Color
    ): Color {
        val isDarkTheme = SaltTheme.configs.isDarkTheme
        if (material != PopupMenuMaterial.Glass) {
            return containerColor.withMaxAlpha(if (isDarkTheme) 0.34f else 0.26f)
        }

        val tint = tintColor.takeOrElse { Color.Transparent }
        return if (isDarkTheme) {
            tint
        } else {
            tint.compositeOver(Color.White.copy(alpha = GlassDefaults.whitePoint))
        }
    }
}

private fun Color.withMaxAlpha(maxAlpha: Float): Color =
    if (isSpecified) copy(alpha = minOf(alpha, maxAlpha)) else Color.Transparent
