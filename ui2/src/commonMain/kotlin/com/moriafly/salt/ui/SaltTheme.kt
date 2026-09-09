/*
 * Salt UI
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

package com.moriafly.salt.ui

import androidx.compose.foundation.LocalIndication
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.moriafly.salt.ui.material.LocalHazeState
import com.moriafly.salt.ui.material.MaterialType
import com.moriafly.salt.ui.screen.BasicScreenStyle
import dev.chrisbanes.haze.HazeState

/**
 * The main entry point for defining the theme.
 *
 * TODO Deprecate this API when SaltMaterial is stable.
 */
@Composable
fun SaltTheme(
    configs: SaltConfigs = SaltConfigs.default(),
    dynamicColors: SaltDynamicColors = SaltDynamicColors(
        light = SaltColors.defaultLight(),
        dark = SaltColors.defaultDark()
    ),
    textStyles: SaltTextStyles = SaltTheme.textStyles,
    dimens: SaltDimens = SaltTheme.dimens,
    shapes: SaltShapes = SaltTheme.shapes,
    content: @Composable () -> Unit
) {
    @OptIn(UnstableSaltUiApi::class)
    SaltTheme(
        configs = configs,
        dynamicColors = dynamicColors,
        textStyles = textStyles,
        dimens = dimens,
        shapes = shapes,
        material = SaltTheme.material,
        basicScreenStyle = SaltTheme.basicScreenStyle,
        content = content
    )
}

/**
 * The main entry point for defining the theme.
 */
@UnstableSaltUiApi
@Composable
fun SaltTheme(
    configs: SaltConfigs = SaltConfigs.default(),
    dynamicColors: SaltDynamicColors = SaltDynamicColors(
        light = SaltColors.defaultLight(),
        dark = SaltColors.defaultDark()
    ),
    textStyles: SaltTextStyles = SaltTheme.textStyles,
    dimens: SaltDimens = SaltTheme.dimens,
    shapes: SaltShapes = SaltTheme.shapes,
    material: SaltMaterial = SaltTheme.material,
    basicScreenStyle: BasicScreenStyle = SaltTheme.basicScreenStyle,
    content: @Composable () -> Unit
) {
    val materialType = material.type
    val hazeState = remember(materialType) {
        if (materialType != MaterialType.None) HazeState() else null
    }
    val colors = if (configs.isDarkTheme) dynamicColors.dark else dynamicColors.light

    CompositionLocalProvider(
        LocalIndication provides configs.indication,
        LocalContentColor provides colors.text,
        LocalSaltConfigs provides configs,
        LocalSaltDynamicColors provides dynamicColors,
        LocalSaltTextStyles provides textStyles,
        LocalSaltDimens provides dimens,
        LocalSaltShapes provides shapes,
        LocalSaltMaterial provides material,
        LocalBasicScreenStyle provides basicScreenStyle,
        LocalHazeState provides hazeState
    ) {
        ProvideTextStyle(
            value = textStyles.main,
            content = content
        )
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
    val dynamicColors = LocalSaltDynamicColors.current
    val colors = if (isDarkTheme) dynamicColors.dark else dynamicColors.light
    CompositionLocalProvider(
        LocalSaltConfigs provides configs,
        LocalContentColor provides colors.text,
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

    @UnstableSaltUiApi
    val material: SaltMaterial
        @Composable
        @ReadOnlyComposable
        get() = LocalSaltMaterial.current

    @UnstableSaltUiApi
    val basicScreenStyle: BasicScreenStyle
        @Composable
        @ReadOnlyComposable
        get() = LocalBasicScreenStyle.current
}

private val LocalSaltConfigs = staticCompositionLocalOf { SaltConfigs.default() }

private val LocalSaltDynamicColors = staticCompositionLocalOf { SaltDynamicColors.default() }

private val LocalSaltTextStyles = staticCompositionLocalOf { saltTextStyles() }

private val LocalSaltDimens = staticCompositionLocalOf { SaltDimens.default() }

private val LocalSaltShapes = staticCompositionLocalOf { SaltShapes.default() }

@UnstableSaltUiApi
private val LocalSaltMaterial = staticCompositionLocalOf { SaltMaterial.default() }

@UnstableSaltUiApi
private val LocalBasicScreenStyle = staticCompositionLocalOf { BasicScreenStyle.default() }
