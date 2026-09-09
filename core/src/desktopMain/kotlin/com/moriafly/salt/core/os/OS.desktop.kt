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

package com.moriafly.salt.core.os

import com.moriafly.salt.core.os.linux.LinuxDistributionInfo
import com.moriafly.salt.core.os.macos.MacOSVersionInfo
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.WinNT

actual fun os(): OS {
    val osName = System.getProperty("os.name")
    return when {
        // macOS product version and build via JNA
        osName == "Mac OS X" -> {
            OS.MacOS(
                version = System.getProperty("os.version"),
                build = MacOSVersionInfo.getBuildVersion()
            )
        }

        osName.startsWith("Win") -> {
            // https://learn.microsoft.com/en-us/windows-hardware/drivers/install/inf-manufacturer-section
            val osVersionInfoEx = WinNT.OSVERSIONINFOEX()
            Kernel32.INSTANCE.GetVersionEx(osVersionInfoEx)
            OS.Windows(osVersionInfoEx.buildNumber)
        }

        osName == "Linux" -> {
            OS.Linux(
                version = System.getProperty("os.version"),
                distro = LinuxDistributionInfo.getDistributionInfo()
            )
        }
        else -> throw Error("Unknown Desktop OS $osName")
    }
}
