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

package com.moriafly.salt.ui.window

import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * Window background type.
 */
@UnstableSaltUiApi
enum class SaltWindowBackgroundType {
    None,

    /**
     * Windows 11 22000+
     */
    Mica,

    /**
     * Windows 11 22621+
     */
    Acrylic,

    /**
     * Windows 11 22621+
     */
    MicaAlt;

    /**
     * Whether the background type is supported on the current OS.
     */
    fun isSupported(): Boolean {
        val os = OS.current
        return when (this) {
            None -> true
            Mica if os is OS.Windows -> os.windowsBuild >= OS.Windows.WINDOWS_11_21H2
            Acrylic if os is OS.Windows -> return os.windowsBuild >= OS.Windows.WINDOWS_11_22H2
            MicaAlt if os is OS.Windows -> return os.windowsBuild >= OS.Windows.WINDOWS_11_22H2
            else -> false
        }
    }
}
