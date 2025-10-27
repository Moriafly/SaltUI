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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.platform.windows.WinUserConst.MFS_DISABLED
import com.moriafly.salt.ui.platform.windows.WinUserConst.MFS_ENABLED
import com.moriafly.salt.ui.platform.windows.WinUserConst.MFT_STRING
import com.moriafly.salt.ui.platform.windows.WinUserConst.MIIM_STATE
import com.moriafly.salt.ui.platform.windows.WinUserConst.SC_CLOSE
import com.moriafly.salt.ui.platform.windows.WinUserConst.SC_MOVE
import com.moriafly.salt.ui.platform.windows.WinUserConst.SC_RESTORE
import com.moriafly.salt.ui.platform.windows.WinUserConst.SC_SIZE
import com.moriafly.salt.ui.platform.windows.WinUserConst.TPM_RETURNCMD
import com.moriafly.salt.ui.platform.windows.WinUserConst.WA_INACTIVE
import com.moriafly.salt.ui.platform.windows.WinUserConst.WINT_MAX
import com.moriafly.salt.ui.platform.windows.WinUserConst.WM_ACTIVATE
import com.moriafly.salt.ui.platform.windows.WinUserConst.WM_NCCALCSIZE
import com.moriafly.salt.ui.platform.windows.WinUserConst.WM_NCHITTEST
import com.moriafly.salt.ui.platform.windows.WinUserConst.WM_NCMOUSEMOVE
import com.moriafly.salt.ui.platform.windows.WinUserConst.WM_NCRBUTTONUP
import com.moriafly.salt.ui.platform.windows.WinUserConst.WM_SETTINGCHANGE
import com.moriafly.salt.ui.platform.windows.structure.MENUITEMINFO
import com.moriafly.salt.ui.util.findSkiaLayer
import com.moriafly.salt.ui.util.hwnd
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.HMENU
import com.sun.jna.platform.win32.WinDef.LPARAM
import com.sun.jna.platform.win32.WinDef.LRESULT
import com.sun.jna.platform.win32.WinDef.UINT
import com.sun.jna.platform.win32.WinDef.WPARAM
import com.sun.jna.platform.win32.WinReg
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.platform.win32.WinUser.WM_SIZE
import com.sun.jna.platform.win32.WinUser.WS_SYSMENU
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.currentSystemTheme
import java.awt.Window

@UnstableSaltUiApi
internal class ComposeWindowProc(
    window: Window,
    private val hitTest: (x: Float, y: Float) -> HitTestResult,
    private val onWindowInsetUpdate: (WindowClientInsets) -> Unit
) : BasicWindowProc(window.hwnd) {
    private val skiaLayer: SkiaLayer = window.findSkiaLayer()!!

    private var hitResult = HitTestResult.HTCLIENT

    private var dpi = UINT(0)
    private var width = 0
    private var height = 0
    private var frameX = 0
    private var frameY = 0
    private var edgeX = 0
    private var edgeY = 0
    private var padding = 0

    private var isMaximized = User32Ex.INSTANCE.isWindowInMaximized(originalHwnd)

    /**
     * Whether the window is resizable.
     */
    var isResizable = true

    var isWindowFrameAccentColorEnabled by mutableStateOf(isAccentColorWindowFrame())

    var windowFrameColor by mutableStateOf(currentAccentColor())

    var windowTheme by mutableStateOf(currentSystemTheme)

    var isWindowActive by mutableStateOf(true)

    val skiaLayerProc = SkiaLayerWindowProc(
        skiaLayer = skiaLayer,
        hitTest = { x, y ->
            updateWindowInfo()
            val horizontalPadding = frameX
            val verticalPadding = frameY

            // Hit test for resizer border
            hitResult = when {
                // Skip resizer border hit test
                isMaximized ||
                    !isResizable ||
                    skiaLayer.fullscreen -> hitTest(x, y)

                x <= horizontalPadding &&
                    y > verticalPadding &&
                    y < height - verticalPadding -> HitTestResult.HTLEFT
                x <= horizontalPadding && y <= verticalPadding -> HitTestResult.HTTOPLEFT
                x <= horizontalPadding -> HitTestResult.HTBOTTOMLEFT
                y <= verticalPadding &&
                    x > horizontalPadding &&
                    x < width - horizontalPadding -> HitTestResult.HTTOP
                y <= verticalPadding && x <= horizontalPadding -> HitTestResult.HTTOPLEFT
                y <= verticalPadding -> HitTestResult.HTTOPRIGHT
                x >= width - horizontalPadding &&
                    y > verticalPadding &&
                    y < height - verticalPadding -> HitTestResult.HTRIGHT
                x >= width - horizontalPadding && y <= verticalPadding ->
                    HitTestResult.HTTOPRIGHT
                x >= width - horizontalPadding -> HitTestResult.HTBOTTOMRIGHT
                y >= height - verticalPadding &&
                    x > horizontalPadding &&
                    x < width - horizontalPadding -> HitTestResult.HTBOTTOM
                y >= height - verticalPadding && x <= horizontalPadding ->
                    HitTestResult.HTBOTTOMLEFT
                y >= height - verticalPadding -> HitTestResult.HTBOTTOMRIGHT
                // Else hit test by user
                else -> hitTest(x, y)
            }
            hitResult
        }
    )

    @OptIn(UnstableSaltUiApi::class)
    override fun callback(
        hwnd: WinDef.HWND,
        uMsg: Int,
        wParam: WPARAM,
        lParam: LPARAM
    ): LRESULT = when (uMsg) {
        // Returns 0 to make the window not draw the non-client area (title bar and border)
        // thus effectively making all the window our client area
        WM_NCCALCSIZE -> {
            if (wParam.toInt() == 0) {
                super.callback(hwnd, uMsg, wParam, lParam)
            } else {
                dpi = User32Ex.INSTANCE.GetDpiForWindow(hwnd)
                frameX = User32Ex.INSTANCE.GetSystemMetricsForDpi(WinUser.SM_CXFRAME, dpi)
                frameY = User32Ex.INSTANCE.GetSystemMetricsForDpi(WinUser.SM_CYFRAME, dpi)
                edgeX = User32Ex.INSTANCE.GetSystemMetricsForDpi(WinUser.SM_CXEDGE, dpi)
                edgeY = User32Ex.INSTANCE.GetSystemMetricsForDpi(WinUser.SM_CYEDGE, dpi)
                padding =
                    User32Ex.INSTANCE.GetSystemMetricsForDpi(WinUser.SM_CXPADDEDBORDER, dpi)
                isMaximized = User32Ex.INSTANCE.isWindowInMaximized(hwnd)

                // Edge inset padding for non-client area
                onWindowInsetUpdate(
                    WindowClientInsets(
                        leftVal = if (isMaximized) {
                            frameX + padding
                        } else {
                            edgeX
                        },
                        rightVal = if (isMaximized) {
                            frameX + padding
                        } else {
                            edgeX
                        },
                        topVal = if (isMaximized) {
                            frameY + padding
                        } else {
                            edgeY
                        },
                        bottomVal = if (isMaximized) {
                            frameY + padding
                        } else {
                            edgeY
                        }
                    )
                )
                LRESULT(0)
            }
        }

        WM_NCHITTEST -> {
            hitResult.toLRESULT()
        }

        WM_SIZE -> {
            width = lParam.toInt() and 0xFFFF
            height = (lParam.toInt() shr 16) and 0xFFFF
            User32Ex.INSTANCE.CallWindowProc(
                originalWindowProc,
                hwnd,
                uMsg,
                wParam,
                lParam
            )
        }

        WM_NCRBUTTONUP -> {
            if (wParam.toInt() == HitTestResult.HTCAPTION.value) {
                val user32 = User32Ex.INSTANCE
                val oldStyle = user32.GetWindowLong(hwnd, WinUser.GWL_STYLE)
                user32.SetWindowLong(hwnd, WinUser.GWL_STYLE, oldStyle or WS_SYSMENU)
                val menu = user32.GetSystemMenu(hwnd, false)
                user32.SetWindowLong(hwnd, WinUser.GWL_STYLE, oldStyle)
                isMaximized = user32.isWindowInMaximized(hwnd)
                if (menu != null) {
                    // Update menu items
                    val menuItemInfo = MENUITEMINFO.ByReference().apply {
                        cbSize = this.size()
                        fMask = MIIM_STATE
                        fType = MFT_STRING
                    }
                    updateMenuItemInfo(menu, menuItemInfo, SC_RESTORE, isMaximized)
                    updateMenuItemInfo(menu, menuItemInfo, SC_MOVE, !isMaximized)
                    updateMenuItemInfo(menu, menuItemInfo, SC_SIZE, !isMaximized)
                    updateMenuItemInfo(menu, menuItemInfo, WinUser.SC_MINIMIZE, true)
                    updateMenuItemInfo(menu, menuItemInfo, WinUser.SC_MAXIMIZE, !isMaximized)
                    updateMenuItemInfo(menu, menuItemInfo, SC_CLOSE, true)
                    // Set the default menu item
                    user32.SetMenuDefaultItem(menu, WINT_MAX, false)
                    // Get the mouse position
                    val lParamValue = lParam.toInt()
                    val x = lParamValue and 0xFFFF
                    val y = (lParamValue shr 16) and 0xFFFF
                    // Show the menu and get user selection
                    val ret = user32.TrackPopupMenu(menu, TPM_RETURNCMD, x, y, 0, hwnd, null)
                    if (ret != 0) {
                        // Send the command
                        user32.PostMessage(
                            hwnd,
                            WinUser.WM_SYSCOMMAND,
                            WPARAM(ret.toLong()),
                            LPARAM(0),
                        )
                    }
                }
            }
            User32Ex.INSTANCE.CallWindowProc(
                originalWindowProc,
                hwnd,
                uMsg,
                wParam,
                lParam
            )
        }

        WM_SETTINGCHANGE -> {
            val changedKey = Pointer(lParam.toLong()).getWideString(0)
            // Theme changed for color and darkTheme
            if (changedKey == "ImmersiveColorSet") {
                windowTheme = currentSystemTheme
                windowFrameColor = currentAccentColor()
                isWindowFrameAccentColorEnabled = isAccentColorWindowFrame()
            }
            User32Ex.INSTANCE.CallWindowProc(
                originalWindowProc,
                hwnd,
                uMsg,
                wParam,
                lParam
            )
        }

        WM_ACTIVATE -> {
            isWindowActive = wParam.toInt() != WA_INACTIVE
            super.callback(hwnd, uMsg, wParam, lParam)
        }

        WM_NCMOUSEMOVE -> {
            User32Ex.INSTANCE.PostMessage(skiaLayerProc.originalHwnd, uMsg, wParam, lParam)
            super.callback(hwnd, uMsg, wParam, lParam)
        }

        else -> {
            super.callback(hwnd, uMsg, wParam, lParam)
        }
    }

    fun currentAccentColor(): Color =
        try {
            val value = Advapi32Util.registryGetIntValue(
                WinReg.HKEY_CURRENT_USER,
                "Software\\Microsoft\\Windows\\DWM",
                "AccentColor",
            ).toLong()
            val alpha = (value and 0xFF000000)
            val green = (value and 0xFF).shl(16)
            val blue = (value and 0xFF00)
            val red = (value and 0xFF0000).shr(16)
            Color((alpha or green or blue or red).toInt())
        } catch (_: Exception) {
            Color(DEFAULT_WINDOWS_11_ACCENT_COLOR_VALUE)
        }

    fun isAccentColorWindowFrame(): Boolean =
        try {
            Advapi32Util.registryGetIntValue(
                WinReg.HKEY_CURRENT_USER,
                "Software\\Microsoft\\Windows\\DWM",
                "ColorPrevalence"
            ) != 0
        } catch (_: Exception) {
            false
        }

    /**
     * Force update window info that resolve the hit test result is incorrect when user moving
     * window to another monitor.
     */
    private fun updateWindowInfo() {
        dpi = User32Ex.INSTANCE.GetDpiForWindow(originalHwnd)
        frameX = User32Ex.INSTANCE.GetSystemMetricsForDpi(WinUser.SM_CXFRAME, dpi)
        frameY = User32Ex.INSTANCE.GetSystemMetricsForDpi(WinUser.SM_CYFRAME, dpi)

        val rect = WinDef.RECT()
        if (User32Ex.INSTANCE.GetWindowRect(originalHwnd, rect)) {
            rect.read()
            width = rect.right - rect.left
            height = rect.bottom - rect.top
        }
        rect.clear()
    }

    private fun updateMenuItemInfo(
        menu: HMENU,
        menuItemInfo: MENUITEMINFO.ByReference,
        item: Int,
        enabled: Boolean
    ) {
        menuItemInfo.fState = if (enabled) MFS_ENABLED else MFS_DISABLED
        User32Ex.INSTANCE.SetMenuItemInfo(menu, item, false, menuItemInfo)
    }

    @Suppress("unused", "SpellCheckingInspection")
    companion object {
        /**
         * Windows 11 Build 22000
         */
        private const val DWMWA_CAPTION_COLOR = 35
        private const val DEFAULT_WINDOWS_11_ACCENT_COLOR_VALUE = 0xFFD47800
    }
}
