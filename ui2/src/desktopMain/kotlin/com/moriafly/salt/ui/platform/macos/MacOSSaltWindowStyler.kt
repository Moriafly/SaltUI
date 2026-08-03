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

package com.moriafly.salt.ui.platform.macos

import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.awt.ComposeWindow
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.util.findSkiaLayer
import com.moriafly.salt.ui.window.SaltWindowBackgroundType
import com.moriafly.salt.ui.window.internal.SaltWindowStyler
import java.awt.Color
import java.awt.Window

// TODO macos 26 https://github.com/electron/electron/issues/47514
@UnstableSaltUiApi
internal class MacOSSaltWindowStyler(
    window: Window
) : SaltWindowStyler {
    private val skiaLayer = window.findSkiaLayer()
    private val vibrancy = MacOSWindowVibrancy(window)
    private val titleBar = MacOSWindowTitleBar(window)

    private val rootPane = when (window) {
        is ComposeWindow -> window.rootPane
        is ComposeDialog -> window.rootPane
        else -> error("Unsupported window, window must be ComposeWindow or ComposeDialog")
    }

    init {
        rootPane.apply {
            putClientProperty("apple.awt.fullWindowContent", true)
            putClientProperty("apple.awt.transparentTitleBar", true)
            putClientProperty("apple.awt.windowTitleVisible", false)
        }
    }

    override fun updateIsResizable(value: Boolean) {
        TODO("Not yet implemented")
    }

    override fun updateBackground(type: SaltWindowBackgroundType, isDarkTheme: Boolean) {
        val windowAppearance =
            if (isDarkTheme) {
                "NSAppearanceNameVibrantDark"
            } else {
                "NSAppearanceNameVibrantLight"
            }
        rootPane.putClientProperty("apple.awt.windowAppearance", windowAppearance)
        val isVibrancyActive = vibrancy.update(
            enabled = type == SaltWindowBackgroundType.Vibrancy,
            isDarkTheme = isDarkTheme
        )
        if (isVibrancyActive) {
            // Compose deliberately inherits the Window background for a transparent Metal
            // SkiaLayer on a regular opaque window. Override the clear color after the native
            // window and its host Metal layer have become non-opaque.
            skiaLayer?.apply {
                transparency = true
                background = Color(0, 0, 0, 0)
            }
        } else {
            // Restore Compose's inherited BLACK fallback when vibrancy is disabled or unavailable.
            skiaLayer?.background = null
        }
    }

    override fun updateBorderAndShadow(value: Boolean) {
        TODO("Not yet implemented")
    }

    override fun updateIsToolWindow(value: Boolean) {
        TODO("Not yet implemented")
    }

    fun updateTitleBar(customHeaderHeight: Float) {
        titleBar.update(customHeaderHeight)
    }

    fun performWindowDrag(): Boolean = titleBar.performWindowDrag()

    fun dispose() {
        titleBar.dispose()
        if (!vibrancy.dispose() && skiaLayer?.isDisplayable == true) {
            skiaLayer.background = null
        }
    }
}
