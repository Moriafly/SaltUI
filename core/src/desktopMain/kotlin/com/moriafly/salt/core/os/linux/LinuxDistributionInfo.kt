/*
 * Salt UI
 * Copyright (C) 2026 Moriafly
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
