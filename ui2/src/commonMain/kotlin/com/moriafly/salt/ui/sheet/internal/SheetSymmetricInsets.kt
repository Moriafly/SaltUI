/*
 * SaltUI
 * Copyright (C) 2026 Moriafly
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

package com.moriafly.salt.ui.sheet.internal

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/**
 * Reads the current system insets in the caller's layout phase, keeping floating margins symmetric.
 */
internal class SheetSymmetricInsets(
    private val insets: WindowInsets,
    private val minimum: Dp
) : WindowInsets {
    override fun getTop(density: Density): Int = vertical(density = density)

    override fun getBottom(density: Density): Int = vertical(density = density)

    override fun getLeft(density: Density, layoutDirection: LayoutDirection): Int = horizontal(
        density = density,
        layoutDirection = layoutDirection
    )

    override fun getRight(density: Density, layoutDirection: LayoutDirection): Int = horizontal(
        density = density,
        layoutDirection = layoutDirection
    )

    private fun vertical(density: Density): Int = maxOf(
        with(density) { minimum.roundToPx() },
        insets.getTop(density = density),
        insets.getBottom(density = density)
    )

    private fun horizontal(
        density: Density,
        layoutDirection: LayoutDirection
    ): Int = maxOf(
        with(density) { minimum.roundToPx() },
        insets.getLeft(density = density, layoutDirection = layoutDirection),
        insets.getRight(density = density, layoutDirection = layoutDirection)
    )
}
