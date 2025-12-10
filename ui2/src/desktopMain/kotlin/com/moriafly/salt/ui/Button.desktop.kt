/*
 * Salt UI
 * Copyright (C) 2025 Moriafly
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

@file:Suppress("ktlint:standard:filename")

package com.moriafly.salt.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable

actual object ButtonDefaults {
    actual val ContentPadding: PaddingValues
        @Composable
        get() = PaddingValues(
            horizontal = SaltTheme.dimens.padding,
            vertical = SaltTheme.dimens.padding * 0.5f
        )
}
