/*
 * Salt UI
 * Copyright (C) 2025 Moriafly
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

package com.moriafly.salt.ui.app

import android.app.Application
import androidx.annotation.Keep

/**
 * Base [Application] class for Salt UI-powered applications.
 */
abstract class SaltApplication : Application() {
    /**
     * Disables the automatic color inversion of Flyme night mode.
     *
     * Flyme reads this method via reflection to decide how the application is
     * rendered in night mode, so it must keep its exact name and signature;
     * [Keep] prevents R8 from renaming or removing it.
     *
     * Returns [MeizuFlymeNightMode.Disable] to opt the application out of the
     * inversion, which suits applications that provide their own dark theme.
     * Applicable to [Flyme 7](https://www.flyme.com/flyme7/index.html) and above.
     */
    @Keep
    fun mzNightModeUseOf(): Int = MeizuFlymeNightMode.Disable.value

    /**
     * Night mode behavior recognized by the Flyme system.
     *
     * Applicable to [Flyme 7](https://www.flyme.com/flyme7/index.html) and above.
     *
     * @property value The integer value recognized by the Flyme system.
     */
    private enum class MeizuFlymeNightMode(
        val value: Int
    ) {
        /**
         * Follows the system night mode behavior (default).
         */
        System(1),

        /**
         * Uses the colors defined by the application instead of inverting them.
         */
        Disable(2),

        /**
         * Forces color inversion in night mode.
         */
        Invert(3),

        /**
         * Reduces brightness in night mode instead of inverting colors.
         */
        Dim(4)
    }
}
