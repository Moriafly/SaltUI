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

@file:Suppress("unused", "SpellCheckingInspection", "PropertyName", "ClassName")

package com.moriafly.salt.ui.platform.windows.structure

import com.sun.jna.Structure
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinDef.POINT
import com.sun.jna.platform.win32.WinNT.HANDLE

/**
 * https://learn.microsoft.com/en-us/windows/win32/api/winuser/ns-winuser-pointer_info
 */
@Structure.FieldOrder(
    "pointerType",
    "pointerId",
    "frameId",
    "pointerFlags",
    "sourceDevice",
    "hwndTarget",
    "ptPixelLocation",
    "ptHimetricLocation",
    "ptPixelLocationRaw",
    "ptHimetricLocationRaw",
    "dwTime",
    "historyCount",
    "InputData",
    "dwKeyStates",
    "PerformanceCount",
    "ButtonChangeType"
)
internal class POINTER_INFO : Structure() {
    @JvmField var pointerType: Int = 0

    @JvmField var pointerId: Int = 0

    @JvmField var frameId: Int = 0

    @JvmField var pointerFlags: Int = 0

    @JvmField var sourceDevice: HANDLE? = null

    @JvmField var hwndTarget: HWND? = null

    @JvmField var ptPixelLocation: POINT = POINT()

    @JvmField var ptHimetricLocation: POINT = POINT()

    @JvmField var ptPixelLocationRaw: POINT = POINT()

    @JvmField var ptHimetricLocationRaw: POINT = POINT()

    @JvmField var dwTime: Int = 0

    @JvmField var historyCount: Int = 0

    @JvmField var InputData: Int = 0

    @JvmField var dwKeyStates: Int = 0

    @JvmField var PerformanceCount: Long = 0

    @JvmField var ButtonChangeType: Int = 0
}
