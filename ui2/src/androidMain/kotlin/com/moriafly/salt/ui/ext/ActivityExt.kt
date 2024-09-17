/*
 * SaltUI
 * Copyright (C) 2024 Moriafly
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

package com.moriafly.salt.ui.ext

import android.app.Activity
import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.core.view.WindowCompat
import com.moriafly.salt.ui.UnstableSaltApi

/**
 * Call this method in the [Activity.onCreate]
 *
 * ```xml
 * <style name="Theme.SaltUI" parent="@android:style/Theme.Holo">
 *     <item name="android:windowBackground">@null</item>
 * </style>
 * ```
 */
@UnstableSaltApi
@Suppress("DEPRECATION")
fun Activity.edgeToEdge() {
    // The decor view should not fit root-level content views for WindowInsets
    WindowCompat.setDecorFitsSystemWindows(window, false)

    requestWindowFeature(Window.FEATURE_NO_TITLE)

    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        // In the XML, it only requires O_MR1 API 27, I don't know why
        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isStatusBarContrastEnforced = false
        window.isNavigationBarContrastEnforced = false

        // Disable Force Dark
        window.decorView.isForceDarkAllowed = false
    }
}