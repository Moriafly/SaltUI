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

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalWindowInfo
import platform.UIKit.UIDevice
import platform.UIKit.UIUserInterfaceIdiomCarPlay
import platform.UIKit.UIUserInterfaceIdiomPad
import platform.UIKit.UIUserInterfaceIdiomPhone

@Composable
actual fun currentSaltAdaptiveWindow(): SaltAdaptiveWindow = SaltAdaptiveWindow(
    formFactor = UIDevice.currentDevice.deviceFormFactor(),
    // Reading the window rather than the screen keeps the size class correct under iPad
    // Split View and Stage Manager, where the app does not own the whole display.
    sizeClass = calculateWindowSizeClass(LocalWindowInfo.current.containerDpSize.width)
)

private fun UIDevice.deviceFormFactor(): DeviceFormFactor = when (userInterfaceIdiom) {
    UIUserInterfaceIdiomPhone -> DeviceFormFactor.Phone
    UIUserInterfaceIdiomPad -> DeviceFormFactor.Tablet
    UIUserInterfaceIdiomCarPlay -> DeviceFormFactor.Car
    else -> DeviceFormFactor.Unknown
}
