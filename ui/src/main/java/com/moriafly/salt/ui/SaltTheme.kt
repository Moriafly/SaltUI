/**
 * SaltUI
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

@file:Suppress("UNUSED")

package com.moriafly.salt.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LocalSaltConfigs = staticCompositionLocalOf { saltConfigs() }

private val LocalSaltColors = staticCompositionLocalOf { lightSaltColors() }

private val LocalSaltTextStyles = staticCompositionLocalOf { saltTextStyles() }

private val LocalSaltDimens = staticCompositionLocalOf { saltDimens() }

@Composable
fun SaltTheme(
    configs: SaltConfigs,
    colors: SaltColors = SaltTheme.colors,
    textStyles: SaltTextStyles = SaltTheme.textStyles,
    dimens: SaltDimens = SaltTheme.dimens,
    content: @Composable () -> Unit
) {
    val applyColorTextStyles = remember(colors.text, colors.subText) {
        // copy text colors
        saltTextStyles(
            main = textStyles.main.copy(color = colors.text),
            sub = textStyles.sub.copy(color = colors.subText),
            paragraph = textStyles.paragraph.copy(color = colors.text.copy(alpha = 0.85f))
        )
    }
    val rippleIndication = ripple()
    CompositionLocalProvider(
        LocalIndication provides rippleIndication,
        LocalSaltConfigs provides configs,
        LocalSaltColors provides colors,
        LocalSaltTextStyles provides applyColorTextStyles,
        LocalSaltDimens provides dimens
    ) {
        content()
    }
}

/**
 * The dynamic color on Android S (12+) Material You
 * see: https://m3.material.io/styles/color
 */
@Deprecated(
    message = "Use the saltColorsByColorScheme (this function is under the SaltColors.kt) to replace."
)
@RequiresApi(Build.VERSION_CODES.S)
@UnstableSaltApi
@Composable
fun DynamicSaltTheme(
    isDark: Boolean = isSystemInDarkTheme(),
    textStyles: SaltTextStyles = SaltTheme.textStyles,
    dimens: SaltDimens = SaltTheme.dimens,
    content: @Composable () -> Unit
) {
    val configs = saltConfigs(
        isDarkTheme = isDark
    )

    val context = LocalContext.current
    val colorScheme = if (isDark) {
        dynamicDarkColorScheme(context)
    } else {
        dynamicLightColorScheme(context)
    }
    val colors = SaltColors(
        highlight = colorScheme.primary,
        text = colorScheme.onSurface,
        subText = colorScheme.onSurfaceVariant,
        background = colorScheme.surface,
        subBackground = colorScheme.surfaceColorAtElevation(3.dp)
    )
    SaltTheme(
        configs = configs,
        colors = colors,
        textStyles = textStyles,
        dimens = dimens,
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
        get() = LocalSaltColors.current

    val textStyles: SaltTextStyles
        @Composable
        @ReadOnlyComposable
        get() = LocalSaltTextStyles.current

    val dimens: SaltDimens
        @Composable
        @ReadOnlyComposable
        get() = LocalSaltDimens.current

}