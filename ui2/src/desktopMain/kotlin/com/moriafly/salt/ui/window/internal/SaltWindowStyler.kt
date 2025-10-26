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

package com.moriafly.salt.ui.window.internal

import androidx.compose.foundation.layout.WindowInsets
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.platform.windows.ComposeWindowProc
import com.moriafly.salt.ui.platform.windows.Dwmapi
import com.moriafly.salt.ui.platform.windows.HitTestResult
import com.moriafly.salt.ui.platform.windows.User32Ex
import com.moriafly.salt.ui.platform.windows.structure.WindowMargins
import com.moriafly.salt.ui.platform.windows.updateWindowStyle
import com.moriafly.salt.ui.util.hwnd
import com.moriafly.salt.ui.util.isUndecorated
import com.sun.jna.platform.win32.WinUser.WS_SYSMENU
import java.awt.Window

@OptIn(UnstableSaltUiApi::class)
internal class SaltWindowStyler(
    window: Window,
    private val hitTest: (Float, Float) -> HitTestResult,
    private val onWindowInsetUpdate: (WindowInsets) -> Unit
) {
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

    fun updateIsResizable(value: Boolean) {
        composeWindowProc.isResizable = value
    }

    /**
     * To disable window border and shadow, pass (0, 0, 0, 0) as window margins
     * (or, simply, don't call this function).
     */
    @Suppress("SpellCheckingInspection")
    fun enableBorderAndShadow() {
        Dwmapi.INSTANCE.DwmExtendFrameIntoClientArea(hwnd, WindowMargins.ByReference())
//        if (OS.ifWindows { it.isAtLeastWindows11() }) {
//            dwmApi?.getFunction("DwmSetWindowAttribute")?.apply {
//                invoke(
//                    WinNT.HRESULT::class.java,
//                    arrayOf(originalHwnd, 35, IntByReference((0xFFFFFFFE).toInt()), 4)
//                )
//                invoke(
//                    WinNT.HRESULT::class.java,
//                    arrayOf(hwnd, 38, IntByReference(2), 4)
//                )
//            }
//        }
    }

    fun disableBorderAndShadow() {
        val pMarInset = WindowMargins.ByReference()
            .apply {
                leftBorderWidth = 0
                rightBorderWidth = 0
                topBorderHeight = 0
                bottomBorderHeight = 0
            }
        Dwmapi.INSTANCE.DwmExtendFrameIntoClientArea(hwnd, pMarInset)
    }

    init {
        val isDecorated = !window.isUndecorated

        if (isDecorated) {
            User32Ex.INSTANCE.updateWindowStyle(hwnd) { oldStyle ->
                // Remove the system menu and the minimize/maximize/close buttons
                oldStyle and WS_SYSMENU.inv()
            }
        }
    }
}
