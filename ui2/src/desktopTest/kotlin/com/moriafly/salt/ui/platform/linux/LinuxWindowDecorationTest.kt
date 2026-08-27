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

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.SaltWindow
import com.sun.jna.Native
import com.sun.jna.NativeLong
import com.sun.jna.platform.unix.X11
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.NativeLongByReference
import com.sun.jna.ptr.PointerByReference
import java.awt.Window
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue

@OptIn(
    ExperimentalTestApi::class,
    ExperimentalComposeUiApi::class,
    UnstableSaltUiApi::class
)
class LinuxWindowDecorationTest {
    @Test
    fun windowManagersSupportingBorderOnlyFrames() {
        assertTrue(LinuxWindowDecoration.isSupportedWindowManager("KWin"))
        assertTrue(LinuxWindowDecoration.isSupportedWindowManager("KWin_wayland"))
        assertTrue(LinuxWindowDecoration.isSupportedWindowManager("Xfwm4"))
        assertTrue(LinuxWindowDecoration.isSupportedWindowManager("Openbox"))
        assertTrue(LinuxWindowDecoration.isSupportedWindowManager("Compiz"))
        assertTrue(LinuxWindowDecoration.isSupportedWindowManager("Muffin"))
    }

    @Test
    fun windowManagersAddingTitleBarAreNotSupported() {
        // Mutter (GNOME Shell) replaces any non-zero Motif decoration with a full title bar,
        // so border-only frames must not be requested there
        assertFalse(LinuxWindowDecoration.isSupportedWindowManager("GNOME Shell"))
        assertFalse(LinuxWindowDecoration.isSupportedWindowManager("Mutter"))
        assertFalse(LinuxWindowDecoration.isSupportedWindowManager("Mutter (Wayland)"))
    }

    @Test
    fun unknownOrMissingWindowManagerIsNotSupported() {
        assertFalse(LinuxWindowDecoration.isSupportedWindowManager("SomeExoticWm"))
        assertFalse(LinuxWindowDecoration.isSupportedWindowManager(null))
    }

    /**
     * On window managers without border-only frame support, a [SaltWindow] with the default
     * [androidx.compose.ui.window.WindowDecoration.SystemDefault] decoration must stay fully
     * frameless — no native title bar or frame may appear.
     */
    @Test
    fun saltWindow_defaultDecoration_staysFramelessOnUnsupportedWindowManager() {
        assumeTrue(OS.isLinux())
        assumeFalse(LinuxWindowDecoration.isBorderOnlyFrameSupported)

        runDesktopComposeUiTest {
            lateinit var composeWindow: ComposeWindow
            setContent {
                SaltTheme {
                    SaltWindow(
                        onCloseRequest = {},
                        state = rememberWindowState(),
                        title = "Linux Frameless Window",
                        init = { composeWindow = it }
                    ) {
                        Box(
                            modifier = Modifier.testTag("linuxFramelessContent")
                        ) {
                            Text("Hello Linux")
                        }
                    }
                }
            }

            onNodeWithTag("linuxFramelessContent").assertIsDisplayed()
            waitForIdle()

            // Give the decoration effect a chance to (not) apply
            Thread.sleep(500)

            val extents = frameExtents(composeWindow)
            assertTrue(
                extents == null || extents.all { it == 0L },
                "Expected no native frame extents, but was ${extents?.toList()}"
            )
        }
    }

    /**
     * Reads the `_NET_FRAME_EXTENTS` of [window], or `null` if the property or the X11
     * connection is unavailable.
     */
    private fun frameExtents(window: Window): LongArray? {
        if (!window.isDisplayable) return null

        val x11 = try {
            X11Ext.INSTANCE
        } catch (_: UnsatisfiedLinkError) {
            return null
        }
        val display = x11.XOpenDisplay(null) ?: return null

        try {
            val windowId = Native.getComponentID(window)
            if (windowId == 0L) return null

            val netFrameExtents = x11.XInternAtom(display, "_NET_FRAME_EXTENTS", false)
            if (netFrameExtents == null || netFrameExtents.toLong() == 0L) return null

            val actualType = X11.AtomByReference()
            val actualFormat = IntByReference()
            val itemCount = NativeLongByReference()
            val bytesAfter = NativeLongByReference()
            val value = PointerByReference()

            val result = x11.XGetWindowProperty(
                display,
                X11.Window(windowId),
                netFrameExtents,
                NativeLong(0),
                NativeLong(4),
                false,
                X11.Atom(0), // AnyPropertyType
                actualType,
                actualFormat,
                itemCount,
                bytesAfter,
                value
            )
            if (result != 0 || value.value == null) return null

            try {
                if (actualFormat.value != 32) return null
                val count = itemCount.value.toInt().coerceAtMost(4)
                if (count <= 0) return null
                // 32-bit format properties are returned as a long array
                return LongArray(count) { index ->
                    value.value.getNativeLong(index.toLong() * NativeLong.SIZE).toLong()
                }
            } finally {
                x11.XFree(value.value)
            }
        } catch (_: Exception) {
            return null
        } finally {
            x11.XCloseDisplay(display)
        }
    }
}
