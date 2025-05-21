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
