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

    companion object {
        fun default(
            isDarkTheme: Boolean = false,
            indication: Indication = AlphaIndication
        ): SaltConfigs = SaltConfigs(
            isDarkTheme = isDarkTheme,
            indication = indication
        )
    }
}

@Deprecated(
    "Use SaltConfigs.default() instead",
    ReplaceWith("SaltConfigs.default(isDarkTheme, indication)")
)
fun saltConfigs(
    isDarkTheme: Boolean = false,
    indication: Indication = AlphaIndication,
): SaltConfigs = SaltConfigs(
    isDarkTheme = isDarkTheme,
    indication = indication
)
