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

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.util.RomUtil

/**
 * The insets that the [safeMain] will consume if shown. If it cannot be shown then this will be empty
 * In other words, regardless of whether the system columns included in [safeMain] are displayed or not, it will have paddings
 */
@OptIn(ExperimentalLayoutApi::class)
@UnstableSaltApi
actual val WindowInsets.Companion.safeMainIgnoringVisibility: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = systemBarsIgnoringVisibility.union(displayCutout)

/**
 * This is the recommended alternative to [safeMainIgnoringVisibility]
 * On Xiaomi's HyperOS, adjusting the size of the small window may cause the navigation bar to hide and show intermittently
 * I don't understand why Xiaomi's HyperOS behaves this way, but there are cases where users hide the navigation bar on some car systems
 * So this parameter is added
 */
@UnstableSaltApi
actual val WindowInsets.Companion.safeMainCompat: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = if (RomUtil.isXiaomiHyperOS) {
        safeMainIgnoringVisibility
    } else {
        safeMain
    }