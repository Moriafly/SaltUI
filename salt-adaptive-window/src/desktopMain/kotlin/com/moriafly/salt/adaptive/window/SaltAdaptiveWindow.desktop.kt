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

@Composable
actual fun currentSaltAdaptiveWindow(): SaltAdaptiveWindow = SaltAdaptiveWindow(
    // The desktop target only ever runs on a desktop OS, so the form factor is a constant and only
    // the window width can vary, when the user resizes the window.
    formFactor = DeviceFormFactor.Desktop,
    sizeClass = calculateWindowSizeClass(LocalWindowInfo.current.containerDpSize.width)
)
