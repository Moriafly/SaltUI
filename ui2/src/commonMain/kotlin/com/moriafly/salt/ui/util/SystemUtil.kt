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

import com.moriafly.salt.ui.UnstableSaltUiApi

@Deprecated(
    message = "Use Core OS instead",
    replaceWith = ReplaceWith(
        expression = "OS",
        imports = arrayOf("com.moriafly.salt.core.os.OS")
    ),
    level = DeprecationLevel.WARNING
)
object SystemUtil {
    @Suppress("MemberVisibilityCanBePrivate")
    const val VERSION_CODE_UNKNOWN = -1

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

    val os: OS by lazy { os() }

    /**
     * android.os.Build.VERSION.SDK_INT.
     */
    @UnstableSaltUiApi
    val androidVersionSdk: Int by lazy { androidVersionSdk() }

    /**
     * [Build number](https://learn.microsoft.com/en-us/windows-hardware/drivers/install/inf-manufacturer-section).
     */
    @UnstableSaltUiApi
    val windowsBuild: Int by lazy { windowsBuild() }

    /**
     * macOS product version.
     */
    @UnstableSaltUiApi
    val macOSVersion: String by lazy { macOSVersion() }

    /**
     * Get the version code of the current system, or [VERSION_CODE_UNKNOWN] if the system is not
     * supported.
     *
     * - Android: android.os.Build.VERSION.SDK_INT.
     * - Windows: [Build number](https://learn.microsoft.com/en-us/windows-hardware/drivers/install/inf-manufacturer-section).
     *
     * @see [androidVersionSdk]
     * @see [windowsBuild]
     */
    @Deprecated(
        message = "Use androidVersionSdk or windowsBuild."
    )
    @UnstableSaltUiApi
    val versionCode by lazy {
        when (os) {
            OS.Android -> androidVersionSdk
            OS.Windows -> windowsBuild
            else -> VERSION_CODE_UNKNOWN
        }
    }

    /**
     * Sample:
     *
     * ```kotlin
     * if (SystemUtil.isAndroidAndVersionSdk { it >= SystemUtil.ANDROID_10 }) {
     *     // code.
     * }
     * ```
     */
    @UnstableSaltUiApi
    fun isAndroidAndVersionSdk(
        value: (Int) -> Boolean
    ): Boolean = if (os.isAndroid()) value(androidVersionSdk) else false

    /**
     * Sample:
     *
     * ```kotlin
     * if (SystemUtil.isWindowsAndBuild { it >= SystemUtil.WINDOWS_11_21H2 }) {
     *     // code.
     * }
     * ```
     */
    @UnstableSaltUiApi
    fun isWindowsAndBuild(
        value: (Int) -> Boolean
    ): Boolean = if (os.isWindows()) value(windowsBuild) else false

    @Deprecated(
        message = "Use Core OS instead"
    )
    enum class OS {
        Android,
        Windows,
        MacOS,
        Linux,
        IOS,
        Unknown;

        fun isAndroid(): Boolean = this == Android

        fun isWindows(): Boolean = this == Windows

        fun isMacOS(): Boolean = this == MacOS

        fun isLinux(): Boolean = this == Linux

        fun isIOS(): Boolean = this == IOS

        fun isUnknown(): Boolean = this == Unknown
    }
}

internal expect fun os(): SystemUtil.OS

internal expect fun androidVersionSdk(): Int

internal expect fun windowsBuild(): Int

internal expect fun macOSVersion(): String
