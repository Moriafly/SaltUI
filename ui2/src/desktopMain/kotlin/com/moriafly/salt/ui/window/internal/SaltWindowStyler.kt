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
import androidx.compose.ui.awt.ComposeWindow
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.platform.windows.BasicWindowProc
import com.moriafly.salt.ui.platform.windows.ComposeWindowProc
import com.moriafly.salt.ui.platform.windows.HitTestResult
import com.moriafly.salt.ui.platform.windows.User32Ex
import com.moriafly.salt.ui.platform.windows.updateWindowStyle
import com.moriafly.salt.ui.util.hwnd
import com.sun.jna.platform.win32.WinUser.WS_SYSMENU

@OptIn(UnstableSaltUiApi::class)
internal class SaltWindowStyler(
    composeWindow: ComposeWindow,
    private val hitTest: (Float, Float) -> HitTestResult,
    private val onWindowInsetUpdate: (WindowInsets) -> Unit
) : BasicWindowProc(composeWindow.hwnd) {
    @Suppress("unused")
    private val composeWindowProc = ComposeWindowProc(
        composeWindow = composeWindow,
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
        val hwnd = composeWindow.hwnd
        val isDecorated = !composeWindow.isUndecorated

        if (isDecorated) {
            User32Ex.INSTANCE.updateWindowStyle(hwnd) { oldStyle ->
                // Remove the system menu and the minimize/maximize/close buttons
                oldStyle and WS_SYSMENU.inv()
            }
        }
    }
}
