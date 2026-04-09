/*
 * Salt UI
 * Copyright (C) 2026 Moriafly
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

package com.moriafly.salt.core.os.macos

import java.io.File

internal object MacOSVersionInfo {
    fun getBuildVersion(): String {
        return readFromPlist().ifEmpty { readFromCommand() }
    }

    internal fun readFromPlist(): String = runCatching {
        File("/System/Library/CoreServices/SystemVersion.plist")
            .readText()
            .let { xml ->
                val regex = "<key>ProductBuildVersion</key>\\s*<string>([^<]+)</string>".toRegex()
                regex.find(xml)?.groupValues?.get(1)
            }
    }
        .getOrNull()
        .orEmpty()

    internal fun readFromCommand(): String = runCatching {
        ProcessBuilder("sw_vers", "-buildVersion")
            .start()
            .inputStream.bufferedReader()
            .readText()
            .trim()
    }
        .getOrDefault("")
}
