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

@UnstableSaltUiApi
object WinUserConst {
    /**
     * Calculate non client area size message.
     */
    const val WM_NCCALCSIZE = 0x0083

    /**
     * Non client area hit test message.
     */
    const val WM_NCHITTEST = 0x0084

    /**
     * Command message.
     */
    const val WM_COMMAND = 0x0111

    /**
     * Mouse move message.
     */
    const val WM_MOUSEMOVE = 0x0200

    /**
     * Left mouse button down message.
     */
    const val WM_LBUTTONDOWN = 0x0201

    /**
     * Left mouse button up message.
     */
    const val WM_LBUTTONUP = 0x0202

    /**
     * Non client area mouse move message.
     */
    const val WM_NCMOUSEMOVE = 0x00A0

    /**
     * Non client area left mouse down message.
     */
    const val WM_NCLBUTTONDOWN = 0x00A1

    /**
     * Non client area left mouse up message.
     */
    const val WM_NCLBUTTONUP = 0x00A2

    /**
     * Non client area right mouse up message.
     */
    const val WM_NCRBUTTONUP = 0x00A5

    /**
     * Setting changed message.
     */
    const val WM_SETTINGCHANGE = 0x001A

    /**
     * Window active event.
     */
    const val WM_ACTIVATE = 0x0006

    /**
     * Window is deactivated
     */
    const val WA_INACTIVE = 0x00000000

    const val SC_RESTORE = 0x0000f120

    const val SC_MOVE = 0xF010

    const val SC_SIZE = 0xF000

    const val SC_CLOSE = 0xF060

    const val WINT_MAX = 0xFFFF

    /**
     * The `fState` member is valid.
     */
    const val MIIM_STATE = 0x00000001

    /**
     * The item is a text string.
     */
    const val MFT_STRING = 0x00000000

    /**
     * Returns the menu item identifier of the user's selection instead of sending a message.
     */
    const val TPM_RETURNCMD = 0x0100

    /**
     * The item is enabled.
     */
    const val MFS_ENABLED = 0x00000000

    /**
     * The item is disabled.
     */
    const val MFS_DISABLED = 0x00000003

    /**
     * The OK button was selected.
     */
    const val IDOK = 1

    /**
     * The Cancel button was selected.
     */
    const val IDCANCEL = 2

    /**
     * The Abort button was selected.
     */
    const val IDABORT = 3

    /**
     * The Retry button was selected.
     */
    const val IDRETRY = 4

    /**
     * The Ignore button was selected.
     */
    const val IDIGNORE = 5

    /**
     * The Yes button was selected.
     */
    const val IDYES = 6

    /**
     * The No button was selected.
     */
    const val IDNO = 7

    /**
     * The Try Again button was selected.
     */
    const val IDTRYAGAIN = 10

    /**
     * The Continue button was selected.
     */
    const val IDCONTINUE = 11
}
