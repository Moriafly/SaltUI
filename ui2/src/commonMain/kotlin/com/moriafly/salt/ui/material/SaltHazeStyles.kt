/*
 * Salt UI
 * Copyright (C) 2025 Moriafly
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package com.moriafly.salt.ui.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.materials.FluentMaterials

/**
 * Provides predefined [HazeBlurStyle]s for Salt UI's material components.
 *
 * This can also be conveniently used with the Haze library to achieve custom effects in certain
 * scenarios.
 */
@UnstableSaltUiApi
object SaltHazeStyles {
    /**
     * Creates a blurry glass effect using haze parameters.
     *
     * @param layer Material layer type (Background/SubBackground).
     * @param isDarkTheme Current theme configuration.
     */
    @Composable
    @ReadOnlyComposable
    fun blurryGlass(
        layer: MaterialLayer,
        isDarkTheme: Boolean = SaltTheme.configs.isDarkTheme
    ): HazeBlurStyle = when (layer) {
        MaterialLayer.Background ->
            HazeBlurStyle(
                backgroundColor = SaltTheme.colors.background,
                colorEffects = if (isDarkTheme) {
                    listOf(
                        HazeColorEffect.tint(
                            color = Color(0x80000000)
                        )
                    )
                } else {
                    listOf(
                        HazeColorEffect.tint(
                            color = Color(0x80FFFFFF)
                        )
                    )
                },
                blurRadius = 45.dp,
                noiseFactor = 0.01f
            )

        MaterialLayer.SubBackground ->
            HazeBlurStyle(
                backgroundColor = SaltTheme.colors.background,
                colorEffects = if (isDarkTheme) {
                    listOf(
                        HazeColorEffect.tint(
                            color = Color(0x60333333)
                        ),
                        HazeColorEffect.tint(
                            color = Color(0x80000000),
                            blendMode = BlendMode.Overlay
                        )
                    )
                } else {
                    listOf(
                        HazeColorEffect.tint(
                            color = Color(0x99585858),
                            blendMode = BlendMode.Luminosity
                        ),
                        HazeColorEffect.tint(
                            color = Color(0x60404040),
                            blendMode = BlendMode.Screen
                        ),
                        HazeColorEffect.tint(
                            color = Color(0xFF808080),
                            blendMode = BlendMode.ColorDodge
                        ),
                        HazeColorEffect.tint(
                            color = Color(0x8CFFFFFF),
                            blendMode = BlendMode.Luminosity
                        )
                    )
                },
                blurRadius = 45.dp,
                noiseFactor = 0.01f
            )
    }

    /**
     * Copy of [FluentMaterials].
     */
    @Composable
    @ReadOnlyComposable
    fun acrylic(
        layer: MaterialLayer,
        isDarkTheme: Boolean = SaltTheme.configs.isDarkTheme
    ): HazeBlurStyle = when (layer) {
        MaterialLayer.Background ->
            FluentMaterials.acrylicBase(isDarkTheme)
        MaterialLayer.SubBackground ->
            FluentMaterials.acrylicDefault(isDarkTheme)
    }

    /**
     * Copy of [FluentMaterials].
     */
    @Composable
    @ReadOnlyComposable
    fun mica(
        layer: MaterialLayer,
        isDarkTheme: Boolean = SaltTheme.configs.isDarkTheme
    ): HazeBlurStyle = when (layer) {
        MaterialLayer.Background ->
            FluentMaterials.micaAlt(isDarkTheme)
        MaterialLayer.SubBackground ->
            FluentMaterials.mica(isDarkTheme)
    }

    /**
     * Creates a premium layered haze effect with noise simulation.
     *
     * @param layer Material layer type (Background/SubBackground).
     * @param isDarkTheme Current theme configuration.
     */
    @Composable
    @ReadOnlyComposable
    fun premium(
        layer: MaterialLayer,
        isDarkTheme: Boolean = SaltTheme.configs.isDarkTheme
    ): HazeBlurStyle = when (layer) {
        MaterialLayer.Background ->
            HazeBlurStyle(
                backgroundColor = SaltTheme.colors.background,
                colorEffects = listOf(
                    HazeColorEffect.tint(
                        color = Color(0x10000000),
                        blendMode = BlendMode.Luminosity
                    )
                ),
                blurRadius = 90.dp
            )
        MaterialLayer.SubBackground ->
            HazeBlurStyle(
                backgroundColor = SaltTheme.colors.background,
                colorEffects = if (isDarkTheme) {
                    listOf(
                        HazeColorEffect.tint(
                            color = Color(0x35666666)
                        ),
                        HazeColorEffect.tint(
                            color = Color(0x15333333),
                            blendMode = BlendMode.Softlight
                        ),
                        HazeColorEffect.tint(
                            color = Color(0x99000000),
                            blendMode = BlendMode.Overlay
                        ),
                        HazeColorEffect.tint(
                            color = Color(0x18000000),
                            blendMode = BlendMode.Luminosity
                        )
                    )
                } else {
                    listOf(
                        HazeColorEffect.tint(
                            color = Color(0x65DBDBDB),
                            blendMode = BlendMode.Softlight
                        ),
                        HazeColorEffect.tint(
                            color = Color(0x38EFEFEF),
                            blendMode = BlendMode.Plus
                        )
                    )
                },
                blurRadius = if (isDarkTheme) {
                    110.dp
                } else {
                    90.dp
                },
                noiseFactor = 0.01f
            )
    }
}
