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

@file:Suppress("unused")

package com.moriafly.salt.core.os

/**
 * Operating System.
 */
sealed class OS {
    data class Android(
        val versionSdk: Int
    ) : OS() {
        companion object {
            const val ANDROID_6 = 23
            const val ANDROID_7 = 24
            const val ANDROID_7_1 = 25
            const val ANDROID_8 = 26
            const val ANDROID_8_1 = 27
            const val ANDROID_9 = 28
            const val ANDROID_10 = 29
            const val ANDROID_11 = 30
            const val ANDROID_12 = 31
            const val ANDROID_12_V2 = 32
            const val ANDROID_13 = 33
            const val ANDROID_14 = 34
            const val ANDROID_15 = 35
            const val ANDROID_16 = 36
        }
    }

    data class Windows(
        val windowsBuild: Int
    ) : OS() {
        fun isAtLeastWindows11(): Boolean = windowsBuild >= WINDOWS_11_21H2

        companion object {
            const val WINDOWS_10_1607 = 14393
            const val WINDOWS_SERVER_2016 = 14393
            const val WINDOWS_10_1703 = 15063
            const val WINDOWS_10_1709 = 16299
            const val WINDOWS_10_1803 = 17134
            const val WINDOWS_10_1809 = 17763
            const val WINDOWS_SERVER_2019 = 17763
            const val WINDOWS_10_1903 = 18362
            const val WINDOWS_10_1909 = 18363
            const val WINDOWS_10_2004 = 19041
            const val WINDOWS_10_20H2 = 19042
            const val WINDOWS_10_21H1 = 19043
            const val WINDOWS_10_21H2 = 19044
            const val WINDOWS_10_22H2 = 19045
            const val WINDOWS_SERVER_2022 = 20348
            const val WINDOWS_11_21H2 = 22000
            const val WINDOWS_11_22H2 = 22621
            const val WINDOWS_11_23H2 = 22631
            const val WINDOWS_11_24H2 = 26100
            const val WINDOWS_11_25H2 = 26200
        }
    }

    data class MacOS(
        val version: String
    ) : OS()

    object Linux : OS()

    object IOS : OS()

    object Unknown : OS()

    companion object {
        val current: OS by lazy { os() }

        @Deprecated(
            message = "Use current instead",
            replaceWith = ReplaceWith("current")
        )
        val os: OS = current

        fun isAndroid(): Boolean = current is Android

        fun isWindows(): Boolean = current is Windows

        fun isMacOS(): Boolean = current is MacOS

        fun isLinux(): Boolean = current is Linux

        fun isDesktop(): Boolean = isMacOS() || isLinux() || isWindows()

        fun isIOS(): Boolean = current is IOS

        fun isUnknown(): Boolean = current is Unknown

        fun ifAndroid(value: (Android) -> Boolean): Boolean =
            current is Android && value(current as Android)

        fun ifWindows(value: (Windows) -> Boolean): Boolean =
            current is Windows && value(current as Windows)

        fun ifMacOS(value: (MacOS) -> Boolean): Boolean =
            current is MacOS && value(current as MacOS)

        fun ifLinux(value: (Linux) -> Boolean): Boolean =
            current is Linux && value(current as Linux)

        fun ifIOS(value: (IOS) -> Boolean): Boolean =
            current is IOS && value(current as IOS)

        fun ifUnknown(value: (Unknown) -> Boolean): Boolean =
            current is Unknown && value(current as Unknown)
    }
}

internal expect fun os(): OS
