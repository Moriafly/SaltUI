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

package com.moriafly.salt.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

val Int.textDp: TextUnit
    @Composable get() = textDp(density = LocalDensity.current)

val Float.textDp: TextUnit
    @Composable get() = textDp(density = LocalDensity.current)

val Double.textDp: TextUnit
    @Composable get() = textDp(density = LocalDensity.current)

private fun Int.textDp(density: Density): TextUnit = with(density) {
    this@textDp.dp.toSp()
}

private fun Float.textDp(density: Density): TextUnit = with(density) {
    this@textDp.dp.toSp()
}

private fun Double.textDp(density: Density): TextUnit = with(density) {
    this@textDp.dp.toSp()
}