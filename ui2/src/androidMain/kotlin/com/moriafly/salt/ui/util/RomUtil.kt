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

import android.annotation.SuppressLint
import android.os.Build
import com.moriafly.salt.ui.UnstableSaltApi
import org.lsposed.hiddenapibypass.HiddenApiBypass

/**
 * Utility class for getting information about the current ROM.
 */
@UnstableSaltApi
object RomUtil {
    /**
     * [Meizu FlymeOS](https://www.flyme.com).
     */
    val isMeizuFlymeOS: Boolean by lazy {
        getSystemProperty("ro.build.user").contains("flyme")
    }

    /**
     * OnePlus Hydrogen OS.
     */
    val isOnePlusHydrogenOS: Boolean by lazy {
        getSystemProperty("ro.rom.version").contains("Hydrogen OS")
    }

    /**
     * Vivo OriginOS.
     */
    val isVivoOriginOS: Boolean by lazy {
        getSystemProperty("ro.vivo.os.build.display.id").contains("OriginOS")
    }

    /**
     * Abandoned by Microsoft, recommended for compatibility only.
     */
    val isWindowsSubsystemForAndroid: Boolean by lazy {
        Build.MODEL.toString() == "Subsystem for Android(TM)"
    }

    /**
     * Xiaomi MIUI or HyperOS.
     */
    val isXiaomi: Boolean by lazy {
        getSystemProperty("ro.product.system.manufacturer") == "Xiaomi"
    }

    /**
     * [Xiaomi HyperOS](https://hyperos.mi.com).
     */
    val isXiaomiHyperOS: Boolean by lazy {
        getSystemProperty("ro.mi.os.version.name") != Build.UNKNOWN
    }

    /**
     * Get system property from class [Build].
     *
     * @param property The property name
     * @return The property value, or [Build.UNKNOWN] if failed
     */
    @SuppressLint("PrivateApi")
    fun getSystemProperty(property: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.invoke(Build::class.java, null, "getString", property) as String
        } else {
            try {
                val clazz = Class.forName("android.os.Build")
                val method = clazz.getDeclaredMethod("getString", String::class.java)
                method.isAccessible = true
                method.invoke(null, property) as String
            } catch (e: Exception) {
                Build.UNKNOWN
            }
        }
    }
}