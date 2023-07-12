@file:Suppress("UNUSED")

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

package com.moriafly.salt.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalSaltColors = staticCompositionLocalOf { lightSaltColors() }

@Composable
fun SaltTheme(
    colors: SaltColors = SaltTheme.colors,
    content: @Composable() () -> Unit
) {
    CompositionLocalProvider(
        LocalSaltColors provides colors,
    ) {
        content()
    }
}

object SaltTheme {

    val colors: SaltColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSaltColors.current

}
