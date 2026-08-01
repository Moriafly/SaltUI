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

package com.moriafly.salt.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The relative size of interactive controls.
 *
 * A control size describes its surrounding layout density, not the importance of its action.
 * Platform components resolve each value to platform-appropriate metrics.
 */
enum class ControlSize {
    /** A compact control for space-constrained surfaces such as desktop toolbars and table rows. */
    Small,

    /** The default control size for dialogs, forms, settings, and general page actions. */
    Regular,

    /** A physically prominent control for standalone calls to action and immersive surfaces. */
    Large
}

/** The control size inherited by components in the current composition. */
val LocalControlSize = staticCompositionLocalOf { ControlSize.Regular }

/**
 * Provides [size] to controls in [content].
 *
 * Containers such as toolbars and dialogs should provide their preferred size so individual
 * controls do not need to repeat it.
 */
@Composable
fun ProvideControlSize(
    size: ControlSize,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalControlSize provides size,
        content = content
    )
}
