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
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalAccessorScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import kotlin.math.min

@Composable
actual fun currentSaltAdaptiveWindow(): SaltAdaptiveWindow = currentPlatformSaltAdaptiveWindow()

internal actual fun CompositionLocalAccessorScope.platformSaltAdaptiveWindow(): SaltAdaptiveWindow {
    val context = LocalContext.currentValue
    val configuration = LocalConfiguration.currentValue
    val density = configuration.densityDpi / DisplayMetrics.DENSITY_DEFAULT.toFloat()
    // On API <= 32, WindowInfo can cache stale bounds queried during configuration changes.
    // AndroidX fixes b/525259151, b/525284940 and b/508799456 by reading again after global layout:
    // https://android.googlesource.com/platform/frameworks/support/+/48413c3f127790e362bd05cc72dbc1ac22628a3b
    // The fix is present in Compose UI 1.13.0-alpha01, but absent from 1.12.1.
    // Also observe root geometry and the Resources used for WindowMetrics before drawing.
    // These signals can invalidate the cache after a callback has consumed the pending update.
    // Apply the same API < 33 condition as AndroidX; WindowMetricsCalculator supports older APIs.
    val windowSize =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            LocalView.currentValue.currentWindowMetricsSize(configuration)
        } else {
            LocalWindowInfo.currentValue.containerDpSize
        }
    return saltAdaptiveWindow(
        formFactor = context.deviceFormFactor(density),
        windowSize = windowSize
    )
}

private fun Context.deviceFormFactor(density: Float): DeviceFormFactor = when {
    isAutomotive() -> DeviceFormFactor.Car
    isFoldable() -> DeviceFormFactor.Foldable
    // Window-specific resource qualifiers shrink in split-screen; device adaptation must not.
    smallestDisplayWidthDp(density) >= 600f -> DeviceFormFactor.Tablet
    else -> DeviceFormFactor.Phone
}

private fun Context.smallestDisplayWidthDp(density: Float): Float {
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val bounds = windowManager.maximumWindowMetrics.bounds
        min(bounds.width(), bounds.height()) / density
    } else {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)
        min(metrics.widthPixels, metrics.heightPixels) / metrics.density
    }
}

// Android Automotive only exists from API 24 onwards, so the guard is also semantically correct.
private fun Context.isAutomotive(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
        packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)

// A hinge angle sensor is the platform signal used here for foldable detection.
// This is a heuristic rather than a guarantee.
// Some emulator images advertise the feature even when no hinge sensor is available.
private fun Context.isFoldable(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
        packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_HINGE_ANGLE) &&
        (getSystemService(Context.SENSOR_SERVICE) as? SensorManager)
            ?.getDefaultSensor(Sensor.TYPE_HINGE_ANGLE) != null
