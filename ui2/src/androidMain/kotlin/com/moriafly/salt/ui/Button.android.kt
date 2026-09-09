/*
 * Salt UI
 * Copyright (C) 2025 Moriafly
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

@file:Suppress("ktlint:standard:filename")

package com.moriafly.salt.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

private val SmallButtonMetrics = ButtonMetrics(
    containerHeight = 40.dp,
    contentPadding = PaddingValues(horizontal = 12.dp),
    iconSize = 18.dp,
    iconSpacing = 6.dp
)

private val RegularButtonMetrics = ButtonMetrics(
    containerHeight = 48.dp,
    contentPadding = PaddingValues(horizontal = 16.dp),
    iconSize = 20.dp,
    iconSpacing = 8.dp
)

private val LargeButtonMetrics = ButtonMetrics(
    containerHeight = 56.dp,
    contentPadding = PaddingValues(horizontal = 20.dp),
    iconSize = 24.dp,
    iconSpacing = 8.dp
)

internal actual fun platformButtonMetrics(size: ControlSize): ButtonMetrics = when (size) {
    ControlSize.Small -> SmallButtonMetrics
    ControlSize.Regular -> RegularButtonMetrics
    ControlSize.Large -> LargeButtonMetrics
}
