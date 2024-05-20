/**
 * SaltUI
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

@file:Suppress("UNUSED")

package com.moriafly.salt.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.structuralEqualityPolicy

/**
 * @param isDarkTheme isDarkTheme
 */
@Stable
class SaltConfigs(
    isDarkTheme: Boolean,
) {
    val isDarkTheme by mutableStateOf(isDarkTheme, structuralEqualityPolicy())
}

fun saltConfigs(
    isDarkTheme: Boolean = false
): SaltConfigs = SaltConfigs(
    isDarkTheme = isDarkTheme,
)