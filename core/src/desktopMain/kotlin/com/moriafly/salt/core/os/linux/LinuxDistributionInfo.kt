package com.moriafly.salt.core.os.linux

import java.io.File

internal object LinuxDistributionInfo {
    fun getDistributionInfo(): String = readFromOsRelease().ifEmpty { readFromLsbRelease() }

    internal fun readFromOsRelease(): String = runCatching {
        val content = File("/etc/os-release").readText()
        val regex = "PRETTY_NAME=\"([^\"]+)\"".toRegex()
        regex.find(content)?.groupValues?.get(1).orEmpty()
    }
        .getOrNull()
        .orEmpty()

    internal fun readFromLsbRelease(): String = runCatching {
        val content = File("/etc/lsb-release").readText()
        val regex = "DISTRIB_DESCRIPTION=\"([^\"]+)\"".toRegex()
        regex.find(content)?.groupValues?.get(1).orEmpty()
    }
        .getOrNull()
        .orEmpty()
}
