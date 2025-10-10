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
import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * Base [Application] class for Salt UI-powered applications.
 */
@UnstableSaltUiApi
abstract class SaltApplication : Application() {
    /**
     * Disables the MEIZU Flyme's automatic color inversion algorithm in night mode for the app.
     */
    @Keep
    fun mzNightModeUseOf(): Int = MeizuFlymeNightMode.Disable.value

    /**
     * Controls how a view behaves in Meizu Flyme's night mode.
     *
     * Applicable to [Flyme 7](https://www.flyme.com/flyme7/index.html) and above versions.
     *
     * @property value Integer value used by the platform.
     */
    private enum class MeizuFlymeNightMode(
        val value: Int
    ) {
        /**
         * System handles night mode (default).
         */
        System(1),

        /**
         * Disables night mode for this view.
         */
        Disable(2),

        /**
         * Forces color inversion in night mode.
         */
        Invert(3),

        /**
         * Reduces brightness in night mode.
         */
        Dim(4)
    }
}
