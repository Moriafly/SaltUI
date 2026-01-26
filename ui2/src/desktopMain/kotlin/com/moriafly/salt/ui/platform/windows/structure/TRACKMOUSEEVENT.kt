/*
 * Salt UI
 * Copyright (C) 2026 Moriafly
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

package com.moriafly.salt.ui.platform.windows.structure

import com.moriafly.salt.ui.UnstableSaltUiApi
import com.sun.jna.Structure
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.DWORD

/**
 * https://learn.microsoft.com/en-us/windows/win32/api/winuser/ns-winuser-trackmouseevent
 */
@UnstableSaltUiApi
@Structure.FieldOrder("cbSize", "dwFlags", "hwndTrack", "dwHoverTime")
internal open class TRACKMOUSEEVENT : Structure() {
    @JvmField var cbSize: DWORD = DWORD(0)

    @JvmField var dwFlags: DWORD = DWORD(0)

    @JvmField var hwndTrack: WinDef.HWND? = null

    @JvmField var dwHoverTime: DWORD = DWORD(0)

    class ByReference :
        TRACKMOUSEEVENT(),
        Structure.ByReference

    companion object {
        /** The caller wants to cancel a prior tracking request. */
        const val TME_CANCEL = 0x80000000.toInt()

        /** The caller wants hover notification. */
        const val TME_HOVER = 0x00000001

        /** The caller wants leave notification. */
        const val TME_LEAVE = 0x00000002

        /** The caller wants leave notification for the nonclient areas. */
        const val TME_NONCLIENT = 0x00000010
    }
}
