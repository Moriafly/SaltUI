/*
 * Salt UI
 * Copyright (C) 2026 Moriafly
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

package com.moriafly.salt.ui.sample.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class ScreenRoute : NavKey {
    @Serializable
    data object Main : ScreenRoute()

    @Serializable
    data object About : ScreenRoute()

    @Serializable
    data object License : ScreenRoute()
}
