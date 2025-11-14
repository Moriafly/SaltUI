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

package com.moriafly.salt.ui.platform.windows

/**
 * Window attributes
 */
internal enum class DwmWindowAttribute(
    val value: Int
) {
    /**
     * Windows 11 22000+
     */
    DWMWA_USE_IMMERSIVE_DARK_MODE(20),

    /**
     * Windows 11 22000+
     */
    DWMWA_CAPTION_COLOR(35),

    /**
     * Windows 11 22621+
     */
    DWMWA_SYSTEMBACKDROP_TYPE(38),

    /**
     * Windows 11 22000
     *
     * For the Mica effect, the first value that you are using, 1029, was undocumented and
     * unsupported functionality prior to 22H2/22621. So [DWMWA_SYSTEMBACKDROP_TYPE] with the value
     * of 38, which is available in the 22621 version of the Windows SDK is the only supported
     * value.
     *
     * https://learn.microsoft.com/en-us/answers/questions/4375268/deprecated-functions
     */
    DWMWA_MICA_EFFECT(1029)
}
