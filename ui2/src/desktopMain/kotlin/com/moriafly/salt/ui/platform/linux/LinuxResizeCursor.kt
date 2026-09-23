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

package com.moriafly.salt.ui.platform.linux

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLong
import com.sun.jna.Pointer
import java.awt.Cursor
import java.awt.Toolkit
import java.lang.reflect.Method

/**
 * Loads themed resize cursors using the same directional names as Linux window managers.
 * Native resources belong to AWT's cursor disposer, including cursors replaced after a theme change.
 */
internal object LinuxResizeCursor {
    private val bridge: X11CursorBridge? by lazy {
        try {
            if (Toolkit.getDefaultToolkit().javaClass.name == "sun.awt.X11.XToolkit") {
                X11CursorBridge()
            } else {
                null
            }
        } catch (_: ReflectiveOperationException) {
            null
        } catch (_: RuntimeException) {
            null
        } catch (_: LinkageError) {
            null
        }
    }

    fun get(type: Int): Cursor {
        val name = when (type) {
            Cursor.W_RESIZE_CURSOR -> "w-resize"
            Cursor.E_RESIZE_CURSOR -> "e-resize"
            Cursor.N_RESIZE_CURSOR -> "n-resize"
            Cursor.S_RESIZE_CURSOR -> "s-resize"
            Cursor.NW_RESIZE_CURSOR -> "nw-resize"
            Cursor.NE_RESIZE_CURSOR -> "ne-resize"
            Cursor.SW_RESIZE_CURSOR -> "sw-resize"
            Cursor.SE_RESIZE_CURSOR -> "se-resize"
            else -> return Cursor.getPredefinedCursor(type)
        }
        return try {
            bridge?.get(name) ?: Cursor.getPredefinedCursor(type)
        } catch (_: ReflectiveOperationException) {
            Cursor.getPredefinedCursor(type)
        } catch (_: RuntimeException) {
            Cursor.getPredefinedCursor(type)
        } catch (_: LinkageError) {
            Cursor.getPredefinedCursor(type)
        }
    }

    private class NamedCursor(
        name: String
    ) : Cursor(name)

    private data class Theme(
        val name: String?,
        val size: Int
    )

    private class X11CursorBridge {
        private val toolkit = Toolkit.getDefaultToolkit()
        private val xToolkit = toolkit.javaClass
        private val getDisplay = xToolkit.getMethod("getDisplay").accessible()
        private val awtLock = xToolkit.getMethod("awtLock").accessible()
        private val awtUnlock = xToolkit.getMethod("awtUnlock").accessible()
        private val setPData = Cursor::class.java
            .getDeclaredMethod("setPData", Long::class.javaPrimitiveType)
            .accessible()
        private val x11 = Native.load("X11", Xlib::class.java)
        private val xcursor = Native.load("Xcursor", Xcursor::class.java)
        private var theme: Theme? = null
        private val cursors = mutableMapOf<String, Cursor?>()

        @Synchronized
        fun get(name: String): Cursor? {
            val currentTheme = readTheme() ?: return null
            if (currentTheme != theme) {
                // AWT may still use old cursors; its disposer releases them when no longer referenced
                cursors.clear()
                theme = currentTheme
            }
            if (!cursors.containsKey(name)) {
                cursors[name] = load(name, currentTheme)
            }
            return cursors[name]
        }

        private fun readTheme(): Theme? {
            // A new connection reads current X resources instead of Xcursor's cached Display settings
            val display = x11.XOpenDisplay(null) ?: return null
            return try {
                val desktopName = toolkit.getDesktopProperty("gnome.Gtk/CursorThemeName") as? String
                val desktopSize = toolkit.getDesktopProperty("gnome.Gtk/CursorThemeSize") as? Int
                Theme(
                    name = System.getenv("XCURSOR_THEME")?.takeIf { it.isNotEmpty() }
                        ?: desktopName?.takeIf { it.isNotEmpty() }
                        ?: xcursor.XcursorGetTheme(display)?.getString(0),
                    size = System.getenv("XCURSOR_SIZE")?.toIntOrNull()?.takeIf { it > 0 }
                        ?: desktopSize?.takeIf { it > 0 }
                        ?: xcursor.XcursorGetDefaultSize(display)
                )
            } finally {
                x11.XCloseDisplay(display)
            }
        }

        private fun load(name: String, theme: Theme): Cursor? {
            val images =
                xcursor.XcursorLibraryLoadImages(name, theme.name, theme.size) ?: return null
            try {
                awtLock.invoke(null)
                try {
                    val display = Pointer(getDisplay.invoke(null) as Long)
                    val handle = xcursor.XcursorImagesLoadCursor(display, images)
                    if (handle.toLong() == 0L) return null
                    try {
                        return NamedCursor(name).also { setPData.invoke(it, handle.toLong()) }
                    } catch (error: ReflectiveOperationException) {
                        x11.XFreeCursor(display, handle)
                        throw error
                    } catch (error: RuntimeException) {
                        x11.XFreeCursor(display, handle)
                        throw error
                    }
                } finally {
                    awtUnlock.invoke(null)
                }
            } finally {
                xcursor.XcursorImagesDestroy(images)
            }
        }
    }

    private fun Method.accessible(): Method = apply {
        check(trySetAccessible()) { "AWT cursor access requires java.desktop module opens" }
    }

    private interface Xlib : Library {
        fun XOpenDisplay(name: String?): Pointer?

        fun XCloseDisplay(display: Pointer): Int

        fun XFreeCursor(display: Pointer, cursor: NativeLong): Int
    }

    private interface Xcursor : Library {
        fun XcursorGetTheme(display: Pointer): Pointer?

        fun XcursorGetDefaultSize(display: Pointer): Int

        fun XcursorLibraryLoadImages(
            name: String,
            theme: String?,
            size: Int
        ): Pointer?

        fun XcursorImagesLoadCursor(display: Pointer, images: Pointer): NativeLong

        fun XcursorImagesDestroy(images: Pointer)
    }
}
