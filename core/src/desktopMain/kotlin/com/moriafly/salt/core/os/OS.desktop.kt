/**
 * Salt UI
 * Copyright (C) 2025 Moriafly
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package com.moriafly.salt.core.os

import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.WinNT

actual fun os(): OS {
    // Copy from org.jetbrains.skiko.hostOs
    val osName = System.getProperty("os.name")
    return when {
        osName == "Mac OS X" -> OS.MacOS
        osName.startsWith("Win") -> OS.Windows
        "The Android Project" == System.getProperty("java.specification.vendor") -> OS.Android
        osName == "Linux" -> OS.Linux
        else -> throw Error("Unknown OS $osName")
    }
}

internal actual fun androidVersionSdk(): Int = throw UnsupportedOperationException()

internal actual fun windowsBuild(): Int {
    val osVersionInfoEx = WinNT.OSVERSIONINFOEX()
    Kernel32.INSTANCE.GetVersionEx(osVersionInfoEx)
    return osVersionInfoEx.buildNumber
}

internal actual fun macOSVersion(): String = System.getProperty("os.version")
