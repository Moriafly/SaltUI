/*
 * SaltUI
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

package com.moriafly.salt.ui.platform.linux.x11

import com.sun.jna.Native
import com.sun.jna.NativeLong
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.platform.unix.X11

/**
 * Shared Xlib binding, extending JNA with the functions used by Linux window and input support.
 */
@Suppress("ktlint:standard:function-naming", "FunctionName")
internal interface LinuxX11 : X11 {
    fun XUngrabPointer(
        display: X11.Display,
        time: NativeLong
    ): Int

    fun XFreeCursor(
        display: X11.Display,
        cursor: NativeLong
    ): Int

    fun XConnectionNumber(
        display: X11.Display
    ): Int

    fun XGetEventData(
        display: X11.Display,
        cookie: EventCookie
    ): Int

    fun XFreeEventData(
        display: X11.Display,
        cookie: EventCookie
    )

    @Structure.FieldOrder(
        "type",
        "serial",
        "sendEvent",
        "display",
        "extension",
        "eventType",
        "cookie",
        "data"
    )
    class EventCookie(
        pointer: Pointer
    ) : Structure(pointer) {
        @JvmField
        var type: Int = 0

        @JvmField
        var serial: NativeLong = NativeLong()

        @JvmField
        var sendEvent: Int = 0

        @JvmField
        var display: Pointer? = null

        @JvmField
        var extension: Int = 0

        @JvmField
        var eventType: Int = 0

        @JvmField
        var cookie: Int = 0

        @JvmField
        var data: Pointer? = null

        init {
            read()
        }
    }

    companion object {
        val INSTANCE: LinuxX11 = Native.load("X11", LinuxX11::class.java)
    }
}
