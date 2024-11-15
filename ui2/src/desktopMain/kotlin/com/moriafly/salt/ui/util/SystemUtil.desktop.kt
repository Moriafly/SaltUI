/*
 * Salt UI
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

package com.moriafly.salt.ui.util

import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.WinNT
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

internal actual fun versionCode(): Int {
    return when (hostOs) {
        OS.Windows -> {
            val osVersionInfoEx = WinNT.OSVERSIONINFOEX()
            Kernel32.INSTANCE.GetVersionEx(osVersionInfoEx)
            osVersionInfoEx.buildNumber
        }
        else -> throw NotImplementedError()
    }
}