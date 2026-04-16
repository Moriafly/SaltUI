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

package com.moriafly.salt.core.os.linux

import java.io.File

internal object LinuxDistributionInfo {
    fun getDistributionInfo(): String = readFromOsRelease().ifEmpty { readFromLsbRelease() }

    private fun readFromOsRelease(): String = runCatching {
        val content = File("/etc/os-release").readText()
        val regex = "PRETTY_NAME=\"([^\"]+)\"".toRegex()
        regex.find(content)?.groupValues?.get(1).orEmpty()
    }
        .getOrNull()
        .orEmpty()

    private fun readFromLsbRelease(): String = runCatching {
        val content = File("/etc/lsb-release").readText()
        val regex = "DISTRIB_DESCRIPTION=\"([^\"]+)\"".toRegex()
        regex.find(content)?.groupValues?.get(1).orEmpty()
    }
        .getOrNull()
        .orEmpty()
}
