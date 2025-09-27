/**
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
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.FluentMaterials

/**
 * Provides predefined [HazeStyle]s for Salt UI's material components.
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
        isDarkTheme: Boolean
    ): HazeStyle = when (layer) {
        MaterialLayer.Background ->
            HazeStyle(
                backgroundColor = SaltTheme.colors.background,
                tints = if (isDarkTheme) {
                    listOf(
                        HazeTint(
                            color = Color(0x80000000)
                        )
                    )
                } else {
                    listOf(
                        HazeTint(
                            color = Color(0x80FFFFFF)
                        )
                    )
                },
                blurRadius = 45.dp
            )

        MaterialLayer.SubBackground ->
            HazeStyle(
                backgroundColor = SaltTheme.colors.background,
                tints = if (isDarkTheme) {
                    listOf(
                        HazeTint(
                            color = Color(0x60333333)
                        ),
                        HazeTint(
                            color = Color(0x80000000),
                            blendMode = BlendMode.Overlay
                        )
                    )
                } else {
                    listOf(
                        HazeTint(
                            color = Color(0x99585858),
                            blendMode = BlendMode.Luminosity
                        ),
                        HazeTint(
                            color = Color(0x60404040),
                            blendMode = BlendMode.Screen
                        ),
                        HazeTint(
                            color = Color(0xFF808080),
                            blendMode = BlendMode.ColorDodge
                        ),
                        HazeTint(
                            color = Color(0x8CFFFFFF),
                            blendMode = BlendMode.Luminosity
                        )
                    )
                },
                blurRadius = 45.dp
            )
    }

    /**
     * Copy of [FluentMaterials].
     */
    @OptIn(ExperimentalHazeMaterialsApi::class)
    @Composable
    @ReadOnlyComposable
    fun acrylic(
        layer: MaterialLayer,
        isDarkTheme: Boolean
    ): HazeStyle = when (layer) {
        MaterialLayer.Background ->
            FluentMaterials.acrylicBase(isDarkTheme)
        MaterialLayer.SubBackground ->
            FluentMaterials.acrylicDefault(isDarkTheme)
    }

    /**
     * Copy of [FluentMaterials].
     */
    @OptIn(ExperimentalHazeMaterialsApi::class)
    @Composable
    @ReadOnlyComposable
    fun mica(
        layer: MaterialLayer,
        isDarkTheme: Boolean
    ): HazeStyle = when (layer) {
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
        isDarkTheme: Boolean
    ): HazeStyle = when (layer) {
        MaterialLayer.Background ->
            HazeStyle(
                backgroundColor = SaltTheme.colors.background,
                tints = listOf(
                    HazeTint(
                        color = Color(0x10000000),
                        blendMode = BlendMode.Luminosity
                    )
                ),
                blurRadius = 90.dp
            )
        MaterialLayer.SubBackground ->
            HazeStyle(
                backgroundColor = SaltTheme.colors.background,
                tints = if (isDarkTheme) {
                    listOf(
                        HazeTint(
                            color = Color(0x35666666)
                        ),
                        HazeTint(
                            color = Color(0x15333333),
                            blendMode = BlendMode.Softlight
                        ),
                        HazeTint(
                            color = Color(0x99000000),
                            blendMode = BlendMode.Overlay
                        ),
                        HazeTint(
                            color = Color(0x18000000),
                            blendMode = BlendMode.Luminosity
                        )
                    )
                } else {
                    listOf(
                        HazeTint(
                            color = Color(0x65DBDBDB),
                            blendMode = BlendMode.Softlight
                        ),
                        HazeTint(
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
