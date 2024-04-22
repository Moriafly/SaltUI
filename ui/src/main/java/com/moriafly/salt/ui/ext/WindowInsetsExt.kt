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

package com.moriafly.salt.ui.ext

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import com.moriafly.salt.ui.UnstableSaltApi

/**
 * All system bars (status bars, caption bar as well as navigation bars) and display cutout. but not ime.
 */
@UnstableSaltApi
val WindowInsets.Companion.safeMain: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = WindowInsets.systemBars.union(WindowInsets.displayCutout)

/**
 * The insets that the [safeMain] will consume if shown. If it cannot be shown then this will be empty.
 * In other words, regardless of whether the system columns included in [safeMain] are displayed or not, it will have paddings.
 */
@OptIn(ExperimentalLayoutApi::class)
@UnstableSaltApi
val WindowInsets.Companion.safeMainIgnoringVisibility: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = WindowInsets.systemBarsIgnoringVisibility.union(WindowInsets.displayCutout)

@UnstableSaltApi
@Composable
fun Modifier.safeMainPadding() = windowInsetsPadding(WindowInsets.safeMain)

@UnstableSaltApi
@Composable
fun Modifier.safeMainIgnoringVisibilityPadding() = windowInsetsPadding(WindowInsets.safeMainIgnoringVisibility)