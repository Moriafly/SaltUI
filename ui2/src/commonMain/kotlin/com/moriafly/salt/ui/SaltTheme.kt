/**
 * Salt UI
 * Copyright (C) 2023 Moriafly
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

@file:Suppress("unused")

package com.moriafly.salt.ui

import androidx.compose.foundation.LocalIndication
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalSaltConfigs = staticCompositionLocalOf { saltConfigs() }

private val LocalSaltDynamicColors = staticCompositionLocalOf { SaltDynamicColors.default() }

private val LocalSaltTextStyles = staticCompositionLocalOf { saltTextStyles() }

private val LocalSaltDimens = staticCompositionLocalOf { saltDimens() }

private val LocalSaltShapes = staticCompositionLocalOf { SaltShapes.default() }

@Composable
fun SaltTheme(
    configs: SaltConfigs,
    dynamicColors: SaltDynamicColors = SaltDynamicColors(
        light = lightSaltColors(),
        dark = darkSaltColors()
    ),
    textStyles: SaltTextStyles = SaltTheme.textStyles,
    dimens: SaltDimens = SaltTheme.dimens,
    shapes: SaltShapes = SaltTheme.shapes,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalIndication provides configs.indication,
        LocalSaltConfigs provides configs,
        LocalSaltDynamicColors provides dynamicColors,
        LocalSaltTextStyles provides textStyles,
        LocalSaltDimens provides dimens,
        LocalSaltShapes provides shapes
    ) {
        content()
    }
}

/**
 * Dynamically changes the theme's dark mode configuration.
 */
@UnstableSaltUiApi
@Composable
fun ChangeSaltThemeIsDark(
    isDarkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val configs = LocalSaltConfigs.current.copy(
        isDarkTheme = isDarkTheme
    )
    CompositionLocalProvider(
        LocalSaltConfigs provides configs,
        content = content
    )
}

object SaltTheme {
    val configs: SaltConfigs
        @Composable
        @ReadOnlyComposable
        get() = LocalSaltConfigs.current

    val colors: SaltColors
        @Composable
        @ReadOnlyComposable
        get() = if (configs.isDarkTheme) {
            LocalSaltDynamicColors.current.dark
        } else {
            LocalSaltDynamicColors.current.light
        }

    val textStyles: SaltTextStyles
        @Composable
        @ReadOnlyComposable
        get() = LocalSaltTextStyles.current

    val dimens: SaltDimens
        @Composable
        @ReadOnlyComposable
        get() = LocalSaltDimens.current

    val shapes: SaltShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalSaltShapes.current
}
