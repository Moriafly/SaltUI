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
import androidx.compose.runtime.CompositionLocalAccessorScope
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
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
 * [currentSaltAdaptiveWindow]. No window-root provider is required.
 *
 * @property formFactor The physical form factor of the device.
 * @property sizeClass The width size class of the window.
 * @property windowSize The owning window's size in dp, before component-specific inset handling.
 * Legacy values created without dimensions use [DpSize.Unspecified].
 */
@UnstableSaltAdaptiveWindowApi
@Immutable
data class SaltAdaptiveWindow(
    val formFactor: DeviceFormFactor,
    val sizeClass: WindowSizeClass,
    val windowSize: DpSize = DpSize.Unspecified
) {
    /**
     * Preserves the original constructor for previously compiled callers.
     */
    @Deprecated("Retained for binary compatibility", level = DeprecationLevel.HIDDEN)
    constructor(
        formFactor: DeviceFormFactor,
        sizeClass: WindowSizeClass
    ) : this(formFactor, sizeClass, DpSize.Unspecified)

    /**
     * Preserves the original copy and default-argument entry points for compiled callers.
     */
    @Deprecated("Retained for binary compatibility", level = DeprecationLevel.HIDDEN)
    fun copy(
        formFactor: DeviceFormFactor = this.formFactor,
        sizeClass: WindowSizeClass = this.sizeClass
    ): SaltAdaptiveWindow = copy(
        formFactor = formFactor,
        sizeClass = sizeClass,
        windowSize = windowSize
    )
}

private val LocalPlatformAdaptiveWindow = compositionLocalWithComputedDefaultOf {
    platformSaltAdaptiveWindow()
}

private val LocalProvidedAdaptiveWindow = compositionLocalWithComputedDefaultOf {
    LocalPlatformAdaptiveWindow.currentValue
}

/**
 * Provides [SaltAdaptiveWindow] to the composition so that deeply nested code can read the
 * adaptive window state without having it threaded through every parameter list.
 *
 * Without a provider, the value is computed from the current platform window. Its dimensions
 * remain reactive when the window is resized or rotated. Each platform window supplies its own
 * metrics, including when the app opens multiple windows. No explicit provider is required.
 */
@UnstableSaltAdaptiveWindowApi
val LocalSaltAdaptiveWindow: CompositionLocal<SaltAdaptiveWindow> = LocalProvidedAdaptiveWindow

/**
 * Overrides adaptive information for a subtree.
 *
 * Normal application content reads [LocalSaltAdaptiveWindow] directly. Use this only when a
 * controlled value is needed, such as a preview or a test fixture.
 */
@UnstableSaltAdaptiveWindowApi
@Composable
fun ProvideSaltAdaptiveWindow(
    adaptiveWindow: SaltAdaptiveWindow,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalProvidedAdaptiveWindow provides adaptiveWindow,
        content = content
    )
}

/**
 * Reads the current adaptive window state from the platform.
 *
 * The underlying window metrics and configuration invalidate their readers when the window is
 * resized, rotated, or moved between displays. The value follows the current platform window
 * independently of explicit [ProvideSaltAdaptiveWindow] overrides.
 * The `current` prefix follows the naming of `currentWindowAdaptiveInfo` in
 * `androidx.compose.material3.adaptive`.
 *
 * Most components can read the automatic local directly:
 * ```
 * val adaptiveWindow = LocalSaltAdaptiveWindow.current
 * ```
 */
@Composable
expect fun currentSaltAdaptiveWindow(): SaltAdaptiveWindow

@Composable
internal fun currentPlatformSaltAdaptiveWindow(): SaltAdaptiveWindow =
    LocalPlatformAdaptiveWindow.current

internal expect fun CompositionLocalAccessorScope.platformSaltAdaptiveWindow(): SaltAdaptiveWindow

internal fun saltAdaptiveWindow(
    formFactor: DeviceFormFactor,
    windowSize: DpSize
): SaltAdaptiveWindow = SaltAdaptiveWindow(
    formFactor = formFactor,
    sizeClass = calculateWindowSizeClass(windowSize.width),
    windowSize = windowSize
)

internal fun calculateWindowSizeClass(width: Dp): WindowSizeClass = when {
    width < 600.dp -> WindowSizeClass.Compact
    width < 840.dp -> WindowSizeClass.Medium
    else -> WindowSizeClass.Expanded
}
