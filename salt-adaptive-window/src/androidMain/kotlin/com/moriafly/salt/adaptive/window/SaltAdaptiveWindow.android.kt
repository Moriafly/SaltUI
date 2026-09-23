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

@file:OptIn(UnstableSaltAdaptiveWindowApi::class)

package com.moriafly.salt.adaptive.window

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
actual fun currentSaltAdaptiveWindow(): SaltAdaptiveWindow {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    return SaltAdaptiveWindow(
        formFactor = context.deviceFormFactor(configuration.smallestScreenWidthDp),
        sizeClass = calculateWindowSizeClass(configuration.screenWidthDp.dp)
    )
}

private fun Context.deviceFormFactor(smallestScreenWidthDp: Int): DeviceFormFactor = when {
    isAutomotive() -> DeviceFormFactor.Car
    isFoldable() -> DeviceFormFactor.Foldable
    // sw600dp is the platform's own tablet qualifier, so it agrees with the resources the
    // framework selects for this device.
    smallestScreenWidthDp >= 600 -> DeviceFormFactor.Tablet
    else -> DeviceFormFactor.Phone
}

// Android Automotive only exists from API 24 onwards, so the guard is also semantically correct.
private fun Context.isAutomotive(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
        packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)

// A hinge angle sensor is the only foldable signal the platform exposes without the Jetpack
// WindowManager dependency, so this is a heuristic rather than a guarantee.
private fun Context.isFoldable(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
        packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_HINGE_ANGLE)
