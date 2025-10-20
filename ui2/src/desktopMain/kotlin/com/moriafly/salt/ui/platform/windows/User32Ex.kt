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

@file:Suppress("unused", "FunctionName", "SpellCheckingInspection", "LocalVariableName")

package com.moriafly.salt.ui.platform.windows

import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.platform.windows.structure.MENUITEMINFO
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.BaseTSD.LONG_PTR
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HMENU
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinDef.LPARAM
import com.sun.jna.platform.win32.WinDef.LRESULT
import com.sun.jna.platform.win32.WinDef.POINT
import com.sun.jna.platform.win32.WinDef.RECT
import com.sun.jna.platform.win32.WinDef.UINT
import com.sun.jna.platform.win32.WinDef.WPARAM
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.platform.win32.WinUser.WindowProc
import com.sun.jna.win32.W32APIOptions

/**
 * TODO Test 1
 * TODO internal
 */
@UnstableSaltUiApi
interface User32Ex : User32 {
    fun SetWindowLongPtr(hWnd: HWND, nIndex: Int, wndProc: WindowProc): LONG_PTR

    fun CallWindowProc(
        proc: LONG_PTR,
        hWnd: HWND,
        uMsg: Int,
        wParam: WPARAM,
        lParam: LPARAM
    ): LRESULT

    fun GetSystemMetricsForDpi(nIndex: Int, dpi: UINT): Int

    fun GetDpiForWindow(hWnd: HWND): UINT

    fun ScreenToClient(hWnd: HWND, lpPoint: POINT): Boolean

    fun GetSystemMenu(hWnd: HWND, bRevert: Boolean): HMENU?

    /**
     * https://learn.microsoft.com/zh-cn/windows/win32/api/winuser/nf-winuser-setmenuiteminfow
     */
    fun SetMenuItemInfo(
        hMenu: HMENU,
        item: Int,
        fByPosition: Boolean,
        lpmii: MENUITEMINFO.ByReference
    ): Boolean

    fun TrackPopupMenu(
        hMenu: HMENU,
        uFlags: Int,
        x: Int,
        y: Int,
        nReserved: Int,
        hWnd: HWND,
        prcRect: RECT?
    ): Int

    fun SetMenuDefaultItem(hMenu: HMENU, uItem: Int, fByPos: Boolean): Boolean

    companion object {
        val INSTANCE: User32Ex = Native.load(
            "user32",
            User32Ex::class.java,
            W32APIOptions.DEFAULT_OPTIONS
        )
    }
}

/**
 * Checks if a window is maximized.
 *
 * @param hWnd The handle to the window to check.
 * @return `true` if the window is maximized, `false` otherwise or on failure.
 */
internal fun User32.isWindowInMaximized(hWnd: HWND): Boolean {
    val placement = WinUser.WINDOWPLACEMENT()
    return GetWindowPlacement(hWnd, placement).booleanValue() &&
        placement.showCmd == WinUser.SW_SHOWMAXIMIZED
}

/**
 * Updates the window's basic style using a transform function on the old style.
 */
@UnstableSaltUiApi
internal fun User32.updateWindowStyle(hWnd: HWND, styleBlock: (oldStyle: Int) -> Int) {
    val oldStyle = GetWindowLong(hWnd, WinUser.GWL_STYLE)
    SetWindowLongPtr(hWnd, WinUser.GWL_STYLE, Pointer(styleBlock(oldStyle).toLong()))
}

/**
 * Updates the window's extended style using a transform function on the old extended style.
 */
@UnstableSaltUiApi
internal fun User32.updateWindowExStyle(hWnd: HWND, exStyleBlock: (oldExStyle: Int) -> Int) {
    val oldExStyle = GetWindowLong(hWnd, WinUser.GWL_EXSTYLE)
    SetWindowLongPtr(hWnd, WinUser.GWL_EXSTYLE, Pointer(exStyleBlock(oldExStyle).toLong()))
}
