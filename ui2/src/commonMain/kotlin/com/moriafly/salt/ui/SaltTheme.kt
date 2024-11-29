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
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

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
    CompositionLocalProvider(
        LocalIndication provides configs.indication,
        LocalSaltConfigs provides configs,
        LocalSaltColors provides colors,
        LocalSaltTextStyles provides applyColorTextStyles,
        LocalSaltDimens provides dimens
    ) {
        content()
    }
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