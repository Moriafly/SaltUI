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
    "hwndFrom",
    "idFrom",
    "code"
)
internal open class NMHDR : Structure {
    @JvmField var hwndFrom: WinDef.HWND? = null

    @JvmField var idFrom: WinDef.UINT? = null

    @JvmField var code: WinDef.UINT? = null

    constructor() : super()
    constructor(p: Pointer) : super(p) {
        read()
    }

    class ByReference :
        NMHDR(),
        Structure.ByReference
}
