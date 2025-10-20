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

@file:Suppress("unused", "SpellCheckingInspection")

package com.moriafly.salt.ui.platform.windows

import com.moriafly.salt.ui.UnstableSaltUiApi
import com.sun.jna.platform.win32.WinDef.LRESULT

/**
 * The return value of the DefWindowProc function is one of the following values, indicating the
 * position of the cursor hot spot.
 */
@UnstableSaltUiApi
internal enum class HitTestResult(
    val value: Int
) {
    /**
     * On the screen background or on a dividing line between windows (same as HTNOWHERE, except
     * that the DefWindowProc function produces a system beep to indicate an error).
     */
    HTERROR(-2),

    /**
     * In a window currently covered by another window in the same thread (the message will be sent
     * to underlying windows in the same thread until one of them returns a code that is not
     * HTTRANSPARENT).
     */
    HTTRANSPARENT(-1),

    /**
     * On the screen background or on a dividing line between windows.
     */
    HTNOWHERE(0),

    /**
     * In a client area.
     */
    HTCLIENT(1),

    /**
     * In a title bar.
     */
    HTCAPTION(2),

    /**
     * In a window menu or in a Close button in a child window.
     */
    HTSYSMENU(3),

    /**
     * n a size box (same as HTSIZE).
     */
    HTGROWBOX(4),

    /**
     * In a menu.
     */
    HTMENU(5),

    /**
     * In a horizontal scroll bar.
     */
    HTHSCROLL(6),

    /**
     * In the vertical scroll bar.
     */
    HTVSCROLL(7),

    /**
     * In a Minimize button.
     */
    HTREDUCE(8),

    /**
     * In a Maximize button.
     */
    HTMAXBUTTON(9),

    /**
     * In the left border of a resizable window (the user can click the mouse to resize the window
     * horizontally).
     */
    HTLEFT(10),

    /**
     * In the right border of a resizable window (the user can click the mouse to resize the window
     * horizontally).
     */
    HTRIGHT(11),

    /**
     * In the upper-horizontal border of a window.
     */
    HTTOP(12),

    /**
     * In the upper-left corner of a window border.
     */
    HTTOPLEFT(13),

    /**
     * In the upper-right corner of a window border.
     */
    HTTOPRIGHT(14),

    /**
     * In the lower-horizontal border of a resizable window (the user can click the mouse to resize
     * the window vertically).
     */
    HTBOTTOM(15),

    /**
     * In the lower-left corner of a border of a resizable window (the user can click the mouse to
     * resize the window diagonally).
     */
    HTBOTTOMLEFT(16),

    /**
     * In the lower-right corner of a border of a resizable window (the user can click the mouse to
     * resize the window diagonally).
     */
    HTBOTTOMRIGHT(17),

    /**
     * In the border of a window that does not have a sizing border.
     */
    HTBORDER(18),

    /**
     * In a Close button.
     */
    HTCLOSE(20),

    /**
     * In a Help button.
     */
    HTHELP(21);

    fun toLRESULT(): LRESULT = LRESULT(value.toLong())
}
