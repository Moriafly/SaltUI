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

@file:Suppress("UNUSED", "DEPRECATION")

package com.moriafly.salt.ui.util

import android.os.Build
import android.view.View
import android.view.Window
import androidx.annotation.RequiresApi
import com.moriafly.salt.ui.UnstableSaltApi

@UnstableSaltApi
object WindowUtil {

    /**
     * Set the status bar foreground color
     */
    fun setStatusBarForegroundColor(window: Window, color: BarColor) {
        val decorView = window.decorView
        var systemUiVisibility = decorView.systemUiVisibility
        systemUiVisibility = when (color) {
            BarColor.Black -> systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            BarColor.White -> systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        decorView.systemUiVisibility = systemUiVisibility
    }

    /**
     * Set the navigation bar foreground color
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun setNavigationBarForegroundColor(window: Window, color: BarColor) {
        val decorView = window.decorView
        var systemUiVisibility = decorView.systemUiVisibility
        systemUiVisibility = when (color) {
            BarColor.Black -> systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            BarColor.White -> systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
        decorView.systemUiVisibility = systemUiVisibility
    }

    /**
     * In development practice, the terms white and black are clearer than light and dark
     */
    enum class BarColor {
        White,
        Black
    }

}