/*
 * SaltUI
 * Copyright (C) 2023 Moriafly
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

@file:Suppress("UNUSED")

package com.moriafly.salt.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Dimens for Salt UI
 *
 *   ╭──────────────────────────────────────────────────╮
 *   │                   [padding] * 0.5f               │
 *   │           ╭──────────────────────────╮           │
 *   │           │       [subPadding]       │           │
 *   │ [padding] │ [padding] Text [padding] │ [padding] │
 *   │           │       [subPadding]       │           │
 *   │           ╰──────────────────────────╯           │
 *   │                   [padding] * 0.5f               │
 *   │                   [padding] * 0.5f               │
 *   │           ╭──────────────────────────╮           │
 *   │           │       [subPadding]       │           │
 *   │ [padding] │ [padding] Text [padding] │ [padding] │
 *   │           │       [subPadding]       │           │
 *   │           ╰──────────────────────────╯           │
 *   │                   [padding] * 0.5f               │
 *   ╰──────────────────────────────────────────────────╯
 *
 * @param item The minimum size of an [Item], [ItemSwitcher], etc
 * @param itemIcon size of icon in [Item], [ItemSwitcher], etc
 * @param corner corner
 * @param dialogCorner dialogCorner
 */
@Stable
class SaltDimens(
    item: Dp,
    itemIcon: Dp,
    corner: Dp,
    dialogCorner: Dp,
    padding: Dp,
    subPadding: Dp
) {
    val item by mutableStateOf(item, structuralEqualityPolicy())
    val itemIcon by mutableStateOf(itemIcon, structuralEqualityPolicy())
    val corner by mutableStateOf(corner, structuralEqualityPolicy())
    val dialogCorner by mutableStateOf(dialogCorner, structuralEqualityPolicy())
    val padding by mutableStateOf(padding, structuralEqualityPolicy())
    val subPadding by mutableStateOf(subPadding, structuralEqualityPolicy())
}

fun saltDimens(
    item: Dp = SaltDimensItem,
    itemIcon: Dp = SaltDimensItemIcon,
    corner: Dp = 12.dp,
    dialogCorner: Dp = 20.dp,
    padding: Dp = 16.dp,
    subPadding: Dp = 12.dp
): SaltDimens = SaltDimens(
    item = item,
    itemIcon = itemIcon,
    corner = corner,
    dialogCorner = dialogCorner,
    padding = padding,
    subPadding = subPadding
)

internal expect val SaltDimensItem: Dp

internal expect val SaltDimensItemIcon: Dp