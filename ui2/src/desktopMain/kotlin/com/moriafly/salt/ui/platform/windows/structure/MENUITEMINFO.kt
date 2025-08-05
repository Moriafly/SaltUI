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

@file:Suppress("SpellCheckingInspection", "ktlint:standard:filename", "unused")

package com.moriafly.salt.ui.platform.windows.structure

import com.moriafly.salt.ui.UnstableSaltUiApi
import com.sun.jna.Structure
import com.sun.jna.platform.win32.BaseTSD.ULONG_PTR
import com.sun.jna.platform.win32.WinDef.HBITMAP
import com.sun.jna.platform.win32.WinDef.HMENU

@UnstableSaltUiApi
class MENUITEMINFO :
    Structure(),
    Structure.ByReference {
    @JvmField
    var cbSize: Int = 0

    @JvmField
    var fMask: Int = 0

    @JvmField
    var fType: Int = 0

    @JvmField
    var fState: Int = 0

    @JvmField
    var wID: Int = 0

    @JvmField
    var hSubMenu: HMENU? = null

    @JvmField
    var hbmpChecked: HBITMAP? = null

    @JvmField
    var hbmpUnchecked: HBITMAP? = null

    @JvmField
    var dwItemData: ULONG_PTR = ULONG_PTR(0)

    @JvmField
    var dwTypeData: String? = null

    @JvmField
    var cch: Int = 0

    @JvmField
    var hbmpItem: HBITMAP? = null

    override fun getFieldOrder(): List<String> = listOf(
        "cbSize",
        "fMask",
        "fType",
        "fState",
        "wID",
        "hSubMenu",
        "hbmpChecked",
        "hbmpUnchecked",
        "dwItemData",
        "dwTypeData",
        "cch",
        "hbmpItem",
    )
}
