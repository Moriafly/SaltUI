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

package com.moriafly.salt.ui.platform.linux.x11

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLong
import com.sun.jna.Pointer
import com.sun.jna.platform.unix.X11

@Suppress("ktlint:standard:function-naming", "FunctionName")
internal interface LinuxXcursor : Library {
    fun XcursorGetTheme(
        display: X11.Display
    ): Pointer?

    fun XcursorGetDefaultSize(
        display: X11.Display
    ): Int

    fun XcursorLibraryLoadImages(
        name: String,
        theme: String?,
        size: Int
    ): Pointer?

    fun XcursorImagesLoadCursor(
        display: X11.Display,
        images: Pointer
    ): NativeLong

    fun XcursorImagesDestroy(
        images: Pointer
    )

    companion object {
        val INSTANCE: LinuxXcursor = Native.load("Xcursor", LinuxXcursor::class.java)
    }
}
