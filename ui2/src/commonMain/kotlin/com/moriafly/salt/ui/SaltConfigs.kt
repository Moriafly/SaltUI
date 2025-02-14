/**
 * Salt UI
 * Copyright (C) 2024 Moriafly
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

import androidx.compose.foundation.Indication
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.structuralEqualityPolicy

/**
 * Holds the dynamic theme configuration states for the application.
 *
 * This stable configuration container uses observable state properties to ensure theme changes
 * automatically propagate through composition. All properties use structural equality comparison
 * to optimize recomposition efficiency.
 *
 * @property isDarkTheme Controls the dark/light theme mode state.
 * @property indication The visual interaction indicator for clickable elements.
 *
 * @see [ChangeSaltThemeIsDark] For more details on how to change [isDarkTheme].
 */
@Stable
class SaltConfigs(
    isDarkTheme: Boolean,
    indication: Indication
) {
    val isDarkTheme by mutableStateOf(isDarkTheme, structuralEqualityPolicy())
    val indication by mutableStateOf(indication, structuralEqualityPolicy())

    /**
     * Creates a copy of the configuration with optional overrides.
     *
     * @param isDarkTheme When specified, overrides the current dark theme state.
     * @param indication When specified, overrides the current indication style.
     */
    fun copy(
        isDarkTheme: Boolean = this.isDarkTheme,
        indication: Indication = this.indication
    ): SaltConfigs = SaltConfigs(
        isDarkTheme = isDarkTheme,
        indication = indication
    )
}

fun saltConfigs(
    isDarkTheme: Boolean = false,
    indication: Indication = AlphaIndication
): SaltConfigs = SaltConfigs(
    isDarkTheme = isDarkTheme,
    indication = indication
)
