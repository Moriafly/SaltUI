/*
 * Salt UI
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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Immutable
internal class SaltPaddingValues(
    @Stable
    val start: Dp = 0.dp,
    @Stable
    val top: Dp = 0.dp,
    @Stable
    val end: Dp = 0.dp,
    @Stable
    val bottom: Dp = 0.dp
): PaddingValues {

    constructor(all: Dp) : this(all, all, all, all)
    constructor(horizontal: Dp, vertical: Dp) : this(horizontal, vertical, horizontal, vertical)

    init {
        require(start.value >= 0) { "Start padding must be non-negative" }
        require(top.value >= 0) { "Top padding must be non-negative" }
        require(end.value >= 0) { "End padding must be non-negative" }
        require(bottom.value >= 0) { "Bottom padding must be non-negative" }
    }

    override fun calculateLeftPadding(layoutDirection: LayoutDirection) =
        if (layoutDirection == LayoutDirection.Ltr) start else end

    override fun calculateTopPadding() = top

    override fun calculateRightPadding(layoutDirection: LayoutDirection) =
        if (layoutDirection == LayoutDirection.Ltr) end else start

    override fun calculateBottomPadding() = bottom

    override fun equals(other: Any?): Boolean {
        if (other !is SaltPaddingValues) return false
        return start == other.start &&
                top == other.top &&
                end == other.end &&
                bottom == other.bottom
    }

    override fun hashCode() =
        ((start.hashCode() * 31 + top.hashCode()) * 31 + end.hashCode()) * 31 + bottom.hashCode()

    override fun toString() = "PaddingValues(start=$start, top=$top, end=$end, bottom=$bottom)"

}

@Stable
@Composable
fun Modifier.innerPadding(
    start: Boolean = true,
    top: Boolean = true,
    end: Boolean = true,
    bottom: Boolean = true
) = padding(
    start = if (start) SaltTheme.dimens.padding else 0.dp,
    top = if (top) SaltTheme.dimens.subPadding else 0.dp,
    end = if (end) SaltTheme.dimens.padding else 0.dp,
    bottom = if (bottom) SaltTheme.dimens.subPadding else 0.dp
)

@Stable
@Composable
fun Modifier.innerPadding(
    horizontal: Boolean = true,
    vertical: Boolean = true
) = innerPadding(
    start = horizontal,
    top = vertical,
    end = horizontal,
    bottom = vertical
)

@Stable
@Composable
fun Modifier.innerPadding(
    all: Boolean = true
) = innerPadding(
    start = all,
    top = all,
    end = all,
    bottom = all
)

@Stable
@Composable
fun Modifier.outerPadding(
    start: Boolean = true,
    top: Boolean = true,
    end: Boolean = true,
    bottom: Boolean = true
) = padding(
    start = if (start) SaltTheme.dimens.padding else 0.dp,
    top = if (top) SaltTheme.dimens.padding * 0.5f else 0.dp,
    end = if (end) SaltTheme.dimens.padding else 0.dp,
    bottom = if (bottom) SaltTheme.dimens.padding * 0.5f else 0.dp
)

@Stable
@Composable
fun Modifier.outerPadding(
    horizontal: Boolean = true,
    vertical: Boolean = true
) = outerPadding(
    start = horizontal,
    top = vertical,
    end = horizontal,
    bottom = vertical
)

@Stable
@Composable
fun Modifier.outerPadding(
    all: Boolean = true
) = outerPadding(
    start = all,
    top = all,
    end = all,
    bottom = all
)