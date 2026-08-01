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
import androidx.compose.ui.unit.dp

private val SmallButtonMetrics = ButtonMetrics(
    containerHeight = 24.dp,
    contentPadding = PaddingValues(horizontal = 8.dp),
    iconSize = 14.dp,
    iconSpacing = 4.dp
)

private val RegularButtonMetrics = ButtonMetrics(
    containerHeight = 32.dp,
    contentPadding = PaddingValues(horizontal = 12.dp),
    iconSize = 16.dp,
    iconSpacing = 6.dp
)

private val LargeButtonMetrics = ButtonMetrics(
    containerHeight = 40.dp,
    contentPadding = PaddingValues(horizontal = 16.dp),
    iconSize = 20.dp,
    iconSpacing = 8.dp
)

internal actual fun platformButtonMetrics(size: ControlSize): ButtonMetrics = when (size) {
    ControlSize.Small -> SmallButtonMetrics
    ControlSize.Regular -> RegularButtonMetrics
    ControlSize.Large -> LargeButtonMetrics
}
