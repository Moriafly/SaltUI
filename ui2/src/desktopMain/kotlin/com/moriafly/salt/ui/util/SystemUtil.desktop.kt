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

@file:Suppress("unused")

package com.moriafly.salt.ui.util

import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.WinNT
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

internal actual fun os(): SystemUtil.OS {
    return when (hostOs) {
        OS.Android -> SystemUtil.OS.Android
        OS.Windows -> SystemUtil.OS.Windows
        OS.MacOS -> SystemUtil.OS.MacOS
        OS.Linux -> SystemUtil.OS.Linux
        OS.Ios -> SystemUtil.OS.IOS
        else -> SystemUtil.OS.Unknown
    }
}

internal actual fun androidVersionSdk(): Int {
    throw UnsupportedOperationException()
}

internal actual fun windowsBuild(): Int {
    require(hostOs.isWindows)
    val osVersionInfoEx = WinNT.OSVERSIONINFOEX()
    Kernel32.INSTANCE.GetVersionEx(osVersionInfoEx)
    return osVersionInfoEx.buildNumber
}

internal actual fun macOSVersion(): String {
    require(hostOs.isMacOS)
    return System.getProperty("os.version")
}