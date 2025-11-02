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

@file:Suppress(
    "unused",
    "ktlint:standard:function-naming",
    "FunctionName",
    "SpellCheckingInspection"
)

package com.moriafly.salt.ui.platform.windows

import com.moriafly.salt.ui.platform.windows.structure.WindowMargins
import com.sun.jna.Native
import com.sun.jna.PointerType
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.platform.win32.WinNT.HRESULT
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions

internal interface Dwmapi : StdCallLibrary {
    /**
     * To disable window border and shadow, pass (0, 0, 0, 0) as window margins.
     */
    fun DwmExtendFrameIntoClientArea(
        hWnd: WinDef.HWND,
        pMarInset: WindowMargins.ByReference
    ): WinNT.HRESULT

    fun DwmSetWindowAttribute(
        hwnd: HWND,
        attribute: Int,
        value: PointerType?,
        valueSize: Int
    ): HRESULT

    companion object {
        val INSTANCE: Dwmapi = Native.load(
            "dwmapi",
            Dwmapi::class.java,
            W32APIOptions.DEFAULT_OPTIONS
        )
    }
}
