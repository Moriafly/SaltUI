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

package com.moriafly.salt.ui.ext

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * Same as [safeMain]
 */
@UnstableSaltUiApi
actual val WindowInsets.Companion.safeMainIgnoringVisibility: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = systemBars.union(displayCutout)

/**
 * Same as [safeMainIgnoringVisibility]
 */
@UnstableSaltUiApi
actual val WindowInsets.Companion.safeMainCompat: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = safeMainIgnoringVisibility