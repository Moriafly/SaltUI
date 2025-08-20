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

package com.moriafly.salt.ui.platform.windows

import com.moriafly.salt.ui.UnstableSaltUiApi
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.platform.win32.WinUser.WindowProc

/**
 * An abstract base class to intercept window messages via subclassing.
 *
 * TODO Test 1
 *
 * @property originalHwnd The handle of the window to be subclassed.
 */
@UnstableSaltUiApi
abstract class BasicWindowProc(
    val originalHwnd: WinDef.HWND
) : WindowProc {
    /**
     * The original window procedure. Subclasses must call this to process default messages.
     */
    val originalWindowProc =
        User32Ex.INSTANCE.SetWindowLongPtr(originalHwnd, WinUser.GWL_WNDPROC, this)

    override fun callback(
        hwnd: WinDef.HWND,
        uMsg: Int,
        wParam: WinDef.WPARAM,
        lParam: WinDef.LPARAM
    ): WinDef.LRESULT = User32Ex.INSTANCE.CallWindowProc(
        proc = originalWindowProc,
        hWnd = hwnd,
        uMsg = uMsg,
        wParam = wParam,
        lParam = lParam
    )
}
