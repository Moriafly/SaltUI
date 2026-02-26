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

package com.moriafly.salt.ui.platform.linux

import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.util.findSkiaLayer
import com.moriafly.salt.ui.window.SaltWindowBackgroundType
import com.moriafly.salt.ui.window.internal.SaltWindowStyler
import org.jetbrains.skiko.disableTitleBar
import java.awt.Window

/**
 * Linux implementation of SaltWindowStyler. Most behaviors on Linux are best-effort/no-op
 * for now; this class exposes the same API as other platform stylers and delegates
 * title-bar disabling to Skia when available.
 */
@UnstableSaltUiApi
internal class LinuxSaltWindowStyler(
    window: Window
) : SaltWindowStyler {
    private val skiaLayer = try {
        window.findSkiaLayer()
    } catch (_: Throwable) {
        null
    }

    override fun updateIsResizable(value: Boolean) {
        // No-op on Linux for now.
    }

    override fun updateBackground(type: SaltWindowBackgroundType, isDarkTheme: Boolean) {
        // Best-effort: try to enable/disable transparency on Skia layer depending on background type.
        try {
            when (type) {
                SaltWindowBackgroundType.None -> skiaLayer?.transparency = true
                else -> skiaLayer?.transparency = false
            }
        } catch (_: Throwable) {
            // Ignore on failure.
        }
    }

    override fun updateBorderAndShadow(value: Boolean) {
        // No-op on Linux for now.
    }

    override fun updateIsToolWindow(value: Boolean) {
        // No-op on Linux for now.
    }

    fun disableTitleBar(customHeaderHeight: Float) {
        skiaLayer?.disableTitleBar(customHeaderHeight)
    }
}
