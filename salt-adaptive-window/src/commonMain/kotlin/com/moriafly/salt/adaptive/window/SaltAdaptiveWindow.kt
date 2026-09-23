/*
 * Salt UI
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

@file:Suppress("unused")
@file:OptIn(UnstableSaltAdaptiveWindowApi::class)

package com.moriafly.salt.adaptive.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The physical form factor of the device the app is running on.
 *
 * Describes hardware and platform characteristics, not the amount of space available to the app.
 * A [Tablet] running in split-screen can still have a [WindowSizeClass.Compact] window, so layout
 * breakpoints must be driven by [SaltAdaptiveWindow.sizeClass]. Use [DeviceFormFactor] only for
 * decisions that genuinely depend on the hardware, such as choosing between touch and pointer
 * interaction.
 */
@UnstableSaltAdaptiveWindowApi
enum class DeviceFormFactor {
    /**
     * Handheld phone.
     */
    Phone,

    /**
     * Tablet.
     */
    Tablet,

    /**
     * Device with a foldable display.
     */
    Foldable,

    /**
     * Desktop or laptop computer.
     */
    Desktop,

    /**
     * Vehicle infotainment system.
     */
    Car,

    /**
     * The form factor could not be determined.
     */
    Unknown
}

/**
 * The width size class of the window, following the Material 3 breakpoints.
 *
 * This is the value layout decisions should be based on, because it reflects the space actually
 * available to the app rather than the device it happens to run on.
 */
@UnstableSaltAdaptiveWindowApi
enum class WindowSizeClass {
    /**
     * Width below 600 dp, typically a phone in portrait.
     */
    Compact,

    /**
     * Width from 600 dp to 840 dp, typically a tablet in portrait or an unfolded foldable.
     */
    Medium,

    /**
     * Width of 840 dp or more, typically a tablet in landscape or a desktop window.
     */
    Expanded
}

/**
 * The adaptive window state of the running app.
 *
 * Read it from [LocalSaltAdaptiveWindow] in composition. The value is produced by
 * [currentSaltAdaptiveWindow] and is meant to be provided once near the root of the tree.
 *
 * @property formFactor The physical form factor of the device.
 * @property sizeClass The width size class of the window.
 */
@UnstableSaltAdaptiveWindowApi
data class SaltAdaptiveWindow(
    val formFactor: DeviceFormFactor,
    val sizeClass: WindowSizeClass
)

/**
 * Provides [SaltAdaptiveWindow] to the composition so that deeply nested code can read the
 * adaptive window state without having it threaded through every parameter list.
 *
 * No default is supplied: reading it without a provider is a wiring mistake and must fail loudly
 * rather than silently falling back to a wrong form factor. This follows the same convention as
 * `LocalSaltWindowInfo` in the ui2 module.
 */
@UnstableSaltAdaptiveWindowApi
val LocalSaltAdaptiveWindow: CompositionLocal<SaltAdaptiveWindow> = compositionLocalOf {
    error("LocalSaltAdaptiveWindow is not provided")
}

/**
 * Reads the current adaptive window state from the platform.
 *
 * The result is recomputed on every recomposition rather than cached, because the underlying
 * window metrics and configuration are themselves composition locals that invalidate their readers
 * when the window is resized, rotated, or moved between displays. Hence the `current` prefix,
 * matching `currentWindowAdaptiveInfo` in `androidx.compose.material3.adaptive`.
 *
 * Provide it near the root of the composition:
 * ```
 * CompositionLocalProvider(
 *     LocalSaltAdaptiveWindow provides currentSaltAdaptiveWindow()
 * ) {
 *     App()
 * }
 * ```
 */
@Composable
expect fun currentSaltAdaptiveWindow(): SaltAdaptiveWindow

internal fun calculateWindowSizeClass(width: Dp): WindowSizeClass = when {
    width < 600.dp -> WindowSizeClass.Compact
    width < 840.dp -> WindowSizeClass.Medium
    else -> WindowSizeClass.Expanded
}
