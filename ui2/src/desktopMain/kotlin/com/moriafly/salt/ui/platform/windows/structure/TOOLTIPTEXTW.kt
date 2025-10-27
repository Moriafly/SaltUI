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

@file:Suppress("SpellCheckingInspection")

package com.moriafly.salt.ui.platform.windows.structure

import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.platform.win32.WinDef

@Structure.FieldOrder(
    "hdr",
    "lpszText",
    "szText",
    "hinst",
    "uFlags",
    "lParam"
)
internal open class TOOLTIPTEXTW : Structure() {
    @JvmField var hdr: NMHDR = NMHDR()

    @JvmField var lpszText: Pointer? = null

    // LPWSTR
    @JvmField var szText: CharArray = CharArray(80)

    // WCHAR[80]
    @JvmField var hinst: WinDef.HINSTANCE? = null

    @JvmField var uFlags: WinDef.UINT? = null

    @JvmField var lParam: WinDef.LPARAM? = null

    class ByReference :
        TOOLTIPTEXTW(),
        Structure.ByReference
}
