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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.WindowDecoration
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.NativeLong
import com.sun.jna.platform.unix.X11
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.NativeLongByReference
import com.sun.jna.ptr.PointerByReference
import java.awt.Window
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val MAX_WAIT_FOR_SHOWING_MS = 10_000L
private const val SHOWING_POLL_INTERVAL_MS = 50L

/**
 * Suspends until window is showing or a timeout is reached.
 *
 * X11 window properties such as `_MOTIF_WM_HINTS` must only be written after the window is
 * mapped, because AWT rewrites them when the window is mapped and would override earlier values.
 *
 * @return `true` if the window is showing, `false` on timeout.
 */
internal suspend fun Window.awaitShowing(): Boolean {
    var waited = 0L
    while (!isShowing && waited < MAX_WAIT_FOR_SHOWING_MS) {
        delay(SHOWING_POLL_INTERVAL_MS.milliseconds)
        waited += SHOWING_POLL_INTERVAL_MS
    }
    return isShowing
}

/**
 * Applies the native border-only frame (see [LinuxWindowDecoration]) to [window] once it is
 * showing.
 *
 * Does nothing when the current window manager does not support border-only frames.
 */
@UnstableSaltUiApi
@Composable
internal fun LinuxNativeBorderOnlyFrameEffect(
    window: Window,
    resizable: Boolean
) {
    val currentResizable by rememberUpdatedState(resizable)
    LaunchedEffect(window) {
        if (!LinuxWindowDecoration.isBorderOnlyFrameSupported) return@LaunchedEffect

        withContext(Dispatchers.IO) {
            if (window.awaitShowing()) {
                LinuxWindowDecoration.applyBorderOnlyFrame(window, currentResizable)
            }
        }
    }
}

/**
 * Native border-only window decoration for Linux via the X11 `_MOTIF_WM_HINTS` property.
 *
 * Salt windows are always created undecorated at the AWT level so that the client area extends
 * to the whole window. On window managers that honor granular Motif decoration hints (e.g. KWin,
 * Xfwm4, Openbox), requesting [MWM_DECOR_BORDER] gives the window a native frame without a title
 * bar: the window manager draws its native shadow and border around the window while the client
 * area still covers the whole window.
 *
 * Mutter (GNOME Shell) does not honor granular hints — any non-zero decoration value results in
 * a full native title bar above the window — so the hints are only applied on window managers
 * that are known to support border-only frames.
 */
@UnstableSaltUiApi
internal object LinuxWindowDecoration {
    private const val MWM_HINTS_DECORATIONS = 2L
    private const val MWM_DECOR_BORDER = 2L
    private const val MWM_DECOR_RESIZEH = 4L

    /**
     * Window managers known to honor border-only Motif decoration hints (lowercase substrings
     * matched against the `_NET_WM_NAME` of the window manager).
     */
    private val supportedWindowManagers = listOf(
        "kwin",
        "xfwm",
        "openbox",
        "compiz",
        "muffin",
        "marco",
        "metacity",
        "icewm"
    )

    private val windowManagerName: String? by lazy {
        readWindowManagerName()
    }

    /**
     * Whether the current window manager supports border-only Motif decorations.
     */
    val isBorderOnlyFrameSupported: Boolean
        get() = isSupportedWindowManager(windowManagerName)

    /**
     * Returns whether a window with [decoration] should use the client-drawn shadow decoration
     * (see [LinuxClientShadow]): the native border-only frame is preferred and used when the
     * window manager supports it; the client-drawn shadow is the fallback for window managers
     * like GNOME Shell/Mutter where native decoration without a title bar is impossible.
     */
    @OptIn(ExperimentalComposeUiApi::class)
    fun shouldUseClientShadow(decoration: WindowDecoration): Boolean =
        decoration == WindowDecoration.SystemDefault &&
            !isBorderOnlyFrameSupported &&
            LinuxClientShadow.isTranslucencySupported

    /**
     * Returns whether the window manager identified by [name] is known to honor border-only
     * Motif decoration hints without adding a title bar.
     */
    fun isSupportedWindowManager(name: String?): Boolean {
        name ?: return false
        val normalized = name.lowercase()
        return supportedWindowManagers.any { normalized.contains(it) }
    }

    /**
     * Applies border-only Motif decoration hints to [window].
     *
     * Must be called after the window is mapped, because AWT rewrites `_MOTIF_WM_HINTS` when the
     * window is mapped and would override earlier values.
     *
     * @param resizable Whether the frame should include the native resize handle.
     * @return `true` if the hints were applied, `false` if the current window manager does not
     * support border-only frames or the hints could not be set.
     */
    fun applyBorderOnlyFrame(window: Window, resizable: Boolean): Boolean {
        if (!isBorderOnlyFrameSupported) return false
        if (!window.isDisplayable) return false

        val x11 = try {
            X11Ext.INSTANCE
        } catch (_: UnsatisfiedLinkError) {
            return false
        }
        val display = x11.XOpenDisplay(null) ?: return false

        try {
            val windowId = Native.getComponentID(window)
            if (windowId == 0L) return false

            val motifHints = x11.XInternAtom(display, "_MOTIF_WM_HINTS", false)
            if (motifHints == null || motifHints.toLong() == 0L) return false

            var decorations = MWM_DECOR_BORDER
            if (resizable) {
                decorations = decorations or MWM_DECOR_RESIZEH
            }

            // XChangeProperty expects a long array for 32-bit format data
            val data = Memory(5L * NativeLong.SIZE)
            longArrayOf(MWM_HINTS_DECORATIONS, 0L, decorations, 0L, 0L)
                .forEachIndexed { index, value ->
                    data.setNativeLong(index.toLong() * NativeLong.SIZE, NativeLong(value))
                }

            x11.XChangeProperty(
                display,
                X11.Window(windowId),
                motifHints,
                motifHints,
                32,
                X11.PropModeReplace,
                data,
                5
            )
            x11.XFlush(display)

            return true
        } catch (_: Exception) {
            return false
        } finally {
            x11.XCloseDisplay(display)
        }
    }

    /**
     * Reads the window manager name from `_NET_SUPPORTING_WM_CHECK` / `_NET_WM_NAME`.
     */
    private fun readWindowManagerName(): String? {
        val x11 = try {
            X11Ext.INSTANCE
        } catch (_: UnsatisfiedLinkError) {
            return null
        }
        val display = x11.XOpenDisplay(null) ?: return null

        try {
            val root = x11.XRootWindow(display, x11.XDefaultScreen(display))
            val supportingWmCheck = x11.XInternAtom(display, "_NET_SUPPORTING_WM_CHECK", false)
            val wmWindowId = readWindowProperty(display, root, supportingWmCheck) ?: return null

            val netWmName = x11.XInternAtom(display, "_NET_WM_NAME", false)
            return readStringProperty(display, X11.Window(wmWindowId), netWmName)
        } catch (_: Exception) {
            return null
        } finally {
            x11.XCloseDisplay(display)
        }
    }

    /**
     * Reads a 32-bit window property containing a single X window id.
     */
    private fun readWindowProperty(
        display: X11.Display,
        window: X11.Window,
        property: X11.Atom
    ): Long? {
        val actualType = X11.AtomByReference()
        val actualFormat = IntByReference()
        val itemCount = NativeLongByReference()
        val bytesAfter = NativeLongByReference()
        val value = PointerByReference()

        val result = x11GetWindowProperty(
            display, window, property, 0, 1, false,
            actualType, actualFormat, itemCount, bytesAfter, value
        )
        if (result != 0 || value.value == null) return null

        try {
            if (actualFormat.value != 32 || itemCount.value.toLong() < 1L) return null
            // 32-bit format properties are returned as a long array
            return value.value.getNativeLong(0).toLong()
        } finally {
            X11Ext.INSTANCE.XFree(value.value)
        }
    }

    /**
     * Reads an 8-bit string property.
     */
    private fun readStringProperty(
        display: X11.Display,
        window: X11.Window,
        property: X11.Atom
    ): String? {
        val actualType = X11.AtomByReference()
        val actualFormat = IntByReference()
        val itemCount = NativeLongByReference()
        val bytesAfter = NativeLongByReference()
        val value = PointerByReference()

        val result = x11GetWindowProperty(
            display, window, property, 0, 256, false,
            actualType, actualFormat, itemCount, bytesAfter, value
        )
        if (result != 0 || value.value == null) return null

        try {
            if (actualFormat.value != 8) return null
            val length = itemCount.value.toInt()
            if (length <= 0) return null
            return String(value.value.getByteArray(0, length), Charsets.UTF_8)
        } finally {
            X11Ext.INSTANCE.XFree(value.value)
        }
    }

    private fun x11GetWindowProperty(
        display: X11.Display,
        window: X11.Window,
        property: X11.Atom,
        offset: Long,
        length: Long,
        delete: Boolean,
        actualType: X11.AtomByReference,
        actualFormat: IntByReference,
        itemCount: NativeLongByReference,
        bytesAfter: NativeLongByReference,
        value: PointerByReference
    ): Int = X11Ext.INSTANCE.XGetWindowProperty(
        display,
        window,
        property,
        NativeLong(offset),
        NativeLong(length),
        delete,
        X11.Atom(0), // AnyPropertyType
        actualType,
        actualFormat,
        itemCount,
        bytesAfter,
        value
    )
}
