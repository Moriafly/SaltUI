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

import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.platform.windows.WinUserConst.WM_LBUTTONDOWN
import com.moriafly.salt.ui.platform.windows.WinUserConst.WM_LBUTTONUP
import com.moriafly.salt.ui.platform.windows.WinUserConst.WM_MOUSELEAVE
import com.moriafly.salt.ui.platform.windows.WinUserConst.WM_MOUSEMOVE
import com.moriafly.salt.ui.platform.windows.WinUserConst.WM_NCHITTEST
import com.moriafly.salt.ui.platform.windows.WinUserConst.WM_NCLBUTTONDOWN
import com.moriafly.salt.ui.platform.windows.WinUserConst.WM_NCLBUTTONUP
import com.moriafly.salt.ui.platform.windows.WinUserConst.WM_NCMOUSELEAVE
import com.moriafly.salt.ui.platform.windows.WinUserConst.WM_NCMOUSEMOVE
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinDef.LRESULT
import com.sun.jna.platform.win32.WinDef.POINT
import com.sun.jna.platform.win32.WinUser
import org.jetbrains.skiko.SkiaLayer

@UnstableSaltUiApi
internal class SkiaLayerWindowProc(
    private val skiaLayer: SkiaLayer,
    private val hitTest: (x: Float, y: Float) -> HitTestResult
) : BasicWindowProc(HWND(skiaLayer.canvas.let(Native::getComponentPointer))) {
    private val skiaLayerHwnd = HWND(Pointer(skiaLayer.windowHandle))
    private var hitResult = HitTestResult.HTCLIENT

    override fun callback(
        hwnd: HWND,
        uMsg: Int,
        wParam: WinDef.WPARAM,
        lParam: WinDef.LPARAM
    ): LRESULT = when (uMsg) {
        WM_NCHITTEST -> {
            val x = lParam.x
            val y = lParam.y

            val point = POINT(x, y)
            User32Ex.INSTANCE.ScreenToClient(skiaLayerHwnd, point)
            hitResult = hitTest(point.x.toFloat(), point.y.toFloat())

            val result = when (hitResult) {
                HitTestResult.HTCLIENT,
                HitTestResult.HTREDUCE,
                HitTestResult.HTMAXBUTTON,
                HitTestResult.HTCLOSE -> hitResult // .toLRESULT()
                // TODO HitTestResult.HTTRANSPARENT
                else -> HitTestResult.HTTRANSPARENT // HitTestResult.HTCLIENT // .toLRESULT()
            }

            println("SkiaLayerWindowProc WM_NCHITTEST = $result")
            result.toLRESULT()
        }

        WM_NCMOUSEMOVE -> {
            when (hitResult) {
                HitTestResult.HTREDUCE,
                HitTestResult.HTMAXBUTTON,
                HitTestResult.HTCLOSE -> {
                    User32Ex.INSTANCE.SendMessage(originalHwnd, WM_MOUSEMOVE, wParam, lParam)
                }
                else -> {
                    User32Ex.INSTANCE.SendMessage(originalHwnd, WM_MOUSELEAVE, wParam, lParam)
                }
            }
            HitTestResult.HTNOWHERE.toLRESULT()
        }

        WM_NCMOUSELEAVE -> {
            User32Ex.INSTANCE.SendMessage(originalHwnd, WM_MOUSELEAVE, wParam, lParam)
            HitTestResult.HTNOWHERE.toLRESULT()
        }

        WM_NCLBUTTONDOWN -> {
            User32Ex.INSTANCE.SendMessage(originalHwnd, WM_LBUTTONDOWN, wParam, lParam)
            HitTestResult.HTNOWHERE.toLRESULT()
        }

        WM_NCLBUTTONUP -> {
            User32Ex.INSTANCE.SendMessage(originalHwnd, WM_LBUTTONUP, wParam, lParam)
            HitTestResult.HTNOWHERE.toLRESULT()
        }

        else -> super.callback(hwnd, uMsg, wParam, lParam)
    }
}

/**
 * Retrieves the signed x-coordinate (-32768 to 32767) from the specified [WinDef.LPARAM] value.
 *
 * https://learn.microsoft.com/en-us/windows/win32/api/windowsx/nf-windowsx-get_x_lparam
 */
private val WinDef.LPARAM.x: Int
    get() = ((this.toInt() and 0xFFFF) shl 16) shr 16

/**
 * Retrieves the signed y-coordinate (-32768 to 32767) from the specified [WinDef.LPARAM] value.
 *
 * https://learn.microsoft.com/en-us/windows/win32/api/windowsx/nf-windowsx-get_y_lparam
 */
private val WinDef.LPARAM.y: Int
    get() = this.toInt() shr 16
