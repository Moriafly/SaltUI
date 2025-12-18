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

package com.moriafly.salt.ui.platform.windows

import androidx.compose.foundation.layout.WindowInsets
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.platform.windows.structure.WindowMargins
import com.moriafly.salt.ui.util.hwnd
import com.moriafly.salt.ui.util.isUndecorated
import com.moriafly.salt.ui.window.SaltWindowBackgroundType
import com.moriafly.salt.ui.window.internal.SaltWindowStyler
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference
import java.awt.Window

@UnstableSaltUiApi
internal class WindowsSaltWindowStyler(
    window: Window,
    private val hitTest: (Float, Float) -> HitTestResult,
    private val onWindowInsetUpdate: (WindowInsets) -> Unit
) : SaltWindowStyler {
    private val hwnd = window.hwnd

    @Suppress("unused")
    private val composeWindowProc = ComposeWindowProc(
        window = window,
        hitTest = hitTest,
        onWindowInsetUpdate = { windowClientInsets ->
            val left = windowClientInsets.leftVal
            val top = windowClientInsets.topVal
            val right = windowClientInsets.rightVal
            val bottom = windowClientInsets.bottomVal
            val windowInsets = if (left == 2 && top == 2 && right == 2 && bottom == 2) {
                // When the window is floating
                WindowInsets()
            } else {
                // Fix https://github.com/Moriafly/SaltPlayerSource/issues/1659
                WindowInsets(
                    left = left,
                    // TODO It seems that there is a 2px gap at the top when in full screen
                    //      on Windows 10/11, I'm not sure why
                    top = top - 2,
                    right = right,
                    bottom = bottom
                )
            }
            onWindowInsetUpdate(windowInsets)
        }
    )

    init {
        val isDecorated = !window.isUndecorated

        if (isDecorated) {
            User32Ex.INSTANCE.updateWindowStyle(hwnd) { oldStyle ->
                // Remove the system menu and the minimize/maximize/close buttons
                oldStyle and WinUser.WS_SYSMENU.inv()
            }
        }

        val os = OS.current
        if (os is OS.Windows) {
            if (os.windowsBuild >= OS.Windows.WINDOWS_11_21H2) {
                // Set the CaptionBar color to transparent
                // Since 0xFFFFFFFF represents DWMWA_COLOR_DEFAULT and uses the default theme color,
                // 0xFFFFFFFE is used here
                Dwmapi.INSTANCE.DwmSetWindowAttribute(
                    hwnd = hwnd,
                    attribute = DwmWindowAttribute.DWMWA_CAPTION_COLOR.value,
                    value = IntByReference((0xFFFFFFFE).toInt()),
                    valueSize = 4
                )
            }
        }
    }

    override fun updateIsResizable(value: Boolean) {
        composeWindowProc.isResizable = value
    }

    override fun updateBackground(type: SaltWindowBackgroundType, isDarkTheme: Boolean) {
        val os = OS.current
        if (os is OS.Windows) {
            // Set the light/dark mode first before setting the background to avoid a flicker
            if (os.windowsBuild >= OS.Windows.WINDOWS_11_21H2) {
                Dwmapi.INSTANCE.DwmSetWindowAttribute(
                    hwnd = hwnd,
                    attribute = DwmWindowAttribute.DWMWA_USE_IMMERSIVE_DARK_MODE.value,
                    value = IntByReference(
                        if (isDarkTheme) 1 else 0
                    ),
                    valueSize = 4
                )
            }

            // Set the background type
            when {
                os.windowsBuild >= OS.Windows.WINDOWS_11_22H2 ->
                    Dwmapi.INSTANCE.DwmSetWindowAttribute(
                        hwnd = hwnd,
                        attribute = DwmWindowAttribute.DWMWA_SYSTEMBACKDROP_TYPE.value,
                        value = IntByReference(
                            when (type) {
                                SaltWindowBackgroundType.None -> 1
                                SaltWindowBackgroundType.Mica -> 2
                                SaltWindowBackgroundType.Acrylic -> 3
                                SaltWindowBackgroundType.MicaAlt -> 4
                            }
                        ),
                        valueSize = 4
                    )

                os.windowsBuild >= OS.Windows.WINDOWS_11_21H2 ->
                    Dwmapi.INSTANCE.DwmSetWindowAttribute(
                        hwnd = hwnd,
                        attribute = DwmWindowAttribute.DWMWA_MICA_EFFECT.value,
                        value = IntByReference(
                            if (type == SaltWindowBackgroundType.Mica) 1 else 0
                        ),
                        valueSize = 4
                    )
            }
        }
    }

    override fun updateBorderAndShadow(value: Boolean) {
        val pMarInset =
            if (value) {
                // Enable window border and shadow, (-1, -1, -1, -1)
                WindowMargins.ByReference()
            } else {
                WindowMargins.ByReference()
                    .apply {
                        // To disable window border and shadow, pass (0, 0, 0, 0) as window margins
                        leftBorderWidth = 0
                        rightBorderWidth = 0
                        topBorderHeight = 0
                        bottomBorderHeight = 0
                    }
            }
        Dwmapi.INSTANCE.DwmExtendFrameIntoClientArea(hwnd, pMarInset)
    }

    /**
     * @see [WinUserConst.WS_EX_TOOLWINDOW]
     */
    override fun updateIsToolWindow(value: Boolean) {
        User32Ex.INSTANCE.updateWindowExStyle(hwnd) { oldStyle ->
            if (value) {
                oldStyle or WinUserConst.WS_EX_TOOLWINDOW
            } else {
                oldStyle and WinUserConst.WS_EX_TOOLWINDOW.inv()
            }
        }
    }
}
