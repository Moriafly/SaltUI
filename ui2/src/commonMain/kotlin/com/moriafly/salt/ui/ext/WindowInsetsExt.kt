/*
 * Salt UI
 * Copyright (C) 2024 Moriafly
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

package com.moriafly.salt.ui.ext

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * All system bars (status bars, caption bar as well as navigation bars) and display cutout. but not
 * ime.
 */
val WindowInsets.Companion.safeMain: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = systemBars.union(displayCutout)

/**
 * The insets that the [safeMain] will consume if shown. If it cannot be shown then this will be
 * empty. In other words, regardless of whether the system columns included in [safeMain] are
 * displayed or not, it will have paddings.
 */
@UnstableSaltUiApi
expect val WindowInsets.Companion.safeMainIgnoringVisibility: WindowInsets

@UnstableSaltUiApi
expect val WindowInsets.Companion.safeMainCompat: WindowInsets

@Composable
fun Modifier.safeMainPadding() = windowInsetsPadding(WindowInsets.safeMain)

@UnstableSaltUiApi
@Composable
fun Modifier.safeMainIgnoringVisibilityPadding()
    = windowInsetsPadding(WindowInsets.safeMainIgnoringVisibility)

@UnstableSaltUiApi
@Composable
fun Modifier.safeMainCompatPadding() = windowInsetsPadding(WindowInsets.safeMainCompat)