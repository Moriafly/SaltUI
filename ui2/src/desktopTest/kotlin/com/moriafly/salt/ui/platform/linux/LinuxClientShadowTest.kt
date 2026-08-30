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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.SaltWindow
import com.moriafly.salt.ui.window.SaltWindowProperties
import com.sun.jna.Native
import com.sun.jna.NativeLong
import com.sun.jna.platform.unix.X11
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.NativeLongByReference
import com.sun.jna.ptr.PointerByReference
import java.awt.Color
import java.awt.Frame
import java.awt.MouseInfo
import java.awt.Robot
import java.awt.Window
import java.awt.event.InputEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Assume.assumeTrue

/**
 * Tests for the client-drawn shadow used for Linux windows with
 * [WindowDecoration.SystemDefault].
 *
 * The window-level tests are skipped unless the client shadow is actually active for
 * [WindowDecoration.SystemDefault] on the host.
 */
@OptIn(
    ExperimentalTestApi::class,
    ExperimentalComposeUiApi::class,
    UnstableSaltUiApi::class
)
class LinuxClientShadowTest {
    @Test
    fun saltWindow_defaultDecoration_appliesGtkFrameExtents() {
        assumeClientShadowActive()

        runDesktopComposeUiTest {
            lateinit var composeWindow: ComposeWindow
            setContent {
                SaltTheme {
                    SaltWindow(
                        onCloseRequest = {},
                        state = rememberWindowState(),
                        title = "Linux Client Shadow",
                        properties = SaltWindowProperties.default(
                            extraDisplayScale = 1.5f
                        ),
                        init = { composeWindow = it }
                    ) {
                        Box(
                            modifier = Modifier.testTag("clientShadowContent")
                        ) {
                            Text("Hello Linux")
                        }
                    }
                }
            }

            onNodeWithTag("clientShadowContent").assertIsDisplayed()

            // The shadow area requires a transparent window background
            waitUntil(timeoutMillis = 5_000) { composeWindow.background.alpha == 0 }

            val expectedMargin = onNodeWithTag("clientShadowContent")
                .fetchSemanticsNode()
                .boundsInRoot
                .left
                .toLong()
            assertTrue(expectedMargin > LinuxClientShadow.margin.value)
            waitUntil(timeoutMillis = 5_000) {
                val extents = gtkFrameExtents(composeWindow)
                extents != null && extents.all { it == expectedMargin }
            }
        }
    }

    /**
     * The transparent shadow area must be click-through (XFixes input shape), while the content
     * area and its boundary must receive input.
     */
    @Test
    fun saltWindow_clientShadow_shadowAreaIsClickThrough() {
        assumeClientShadowActive()

        runDesktopComposeUiTest {
            lateinit var composeWindow: ComposeWindow
            setContent {
                SaltTheme {
                    SaltWindow(
                        onCloseRequest = {},
                        state = rememberWindowState(
                            position = WindowPosition.Absolute(300.dp, 300.dp),
                            size = DpSize(640.dp, 480.dp)
                        ),
                        title = "Linux Client Shadow Input",
                        alwaysOnTop = true,
                        init = { composeWindow = it }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("clientShadowInputContent")
                        )
                    }
                }
            }

            onNodeWithTag("clientShadowInputContent").assertIsDisplayed()
            runOnUiThread {
                composeWindow.toFront()
                composeWindow.requestFocus()
            }
            waitForIdle()

            val robot = Robot().apply { autoDelay = 40 }
            assumeTrue(
                "java.awt.Robot cannot synthesize input events",
                robot.canControlPointer()
            )

            val presses = AtomicInteger(0)
            runOnUiThread {
                composeWindow.addMouseListener(
                    object : MouseAdapter() {
                        override fun mousePressed(e: MouseEvent) {
                            presses.incrementAndGet()
                        }
                    }
                )
            }

            // Work in device pixels (the space of pointer events and X11 geometry)
            waitUntil(timeoutMillis = 10_000) { gtkFrameExtents(composeWindow) != null }
            val margin = gtkFrameExtents(composeWindow)!![0].toInt()
            assertTrue(margin > 4)

            // The window manager may still move the window after _GTK_FRAME_EXTENTS is
            // applied, so re-read the geometry before every click
            robot.delay(500)

            var geometry = x11Geometry(composeWindow)
            assumeTrue("Could not read the X11 window geometry", geometry != null)

            // Inside the content area. The first real click also raises the window, which a
            // programmatic toFront may not achieve under focus-stealing prevention
            var attempts = 0
            while (presses.get() == 0 && attempts < 3) {
                val g = x11Geometry(composeWindow)!!
                robot.click(g[0] + g[2] / 2, g[1] + g[3] / 2)
                robot.delay(1_000)
                attempts++
            }
            // Under Wayland, XTEST event delivery to XWayland windows is unreliable; skip when
            // even a plain content click never arrives
            assumeTrue(
                "java.awt.Robot clicks do not reach the window on this host",
                presses.get() >= 1
            )
            val baseline = presses.get()

            geometry = x11Geometry(composeWindow)!!
            val (windowX, windowY, windowWidth, windowHeight) = geometry
            assertTrue(windowWidth > 2 * margin)
            assertTrue(windowHeight > 2 * margin)
            val centerY = windowY + windowHeight / 2

            // Inside the shadow area (outside the content rect)
            robot.click(windowX + margin / 2, centerY)
            robot.delay(500)
            assertEquals(
                baseline,
                presses.get(),
                "Click in the shadow area must not reach the window"
            )

            // Boundary: just inside the content rect
            robot.click(windowX + margin + 2, centerY)
            waitUntil(timeoutMillis = 5_000) { presses.get() == baseline + 1 }

            // Boundary: just outside the content rect
            robot.click(windowX + margin - 2, centerY)
            robot.delay(500)
            assertEquals(
                baseline + 1,
                presses.get(),
                "Click just outside the content boundary must not reach the window"
            )
        }
    }

    @Test
    fun saltWindow_clientShadow_removedWhenMaximized() {
        assumeClientShadowActive()

        runDesktopComposeUiTest {
            lateinit var composeWindow: ComposeWindow
            lateinit var windowState: androidx.compose.ui.window.WindowState
            setContent {
                SaltTheme {
                    windowState = rememberWindowState()
                    SaltWindow(
                        onCloseRequest = {},
                        state = windowState,
                        title = "Linux Client Shadow Maximized",
                        init = { composeWindow = it }
                    ) {
                        Box(
                            modifier = Modifier.testTag("clientShadowMaximizedContent")
                        ) {
                            Text("Hello Linux")
                        }
                    }
                }
            }

            onNodeWithTag("clientShadowMaximizedContent").assertIsDisplayed()
            waitUntil(timeoutMillis = 10_000) { gtkFrameExtents(composeWindow) != null }

            runOnUiThread {
                windowState.placement = WindowPlacement.Maximized
            }

            // The shadow and its frame extents are dropped for maximized windows
            waitUntil(timeoutMillis = 10_000) {
                composeWindow.extendedState and Frame.MAXIMIZED_BOTH == Frame.MAXIMIZED_BOTH
            }
            waitUntil(timeoutMillis = 10_000) { gtkFrameExtents(composeWindow) == null }

            runOnUiThread {
                windowState.placement = WindowPlacement.Floating
            }
            waitUntil(timeoutMillis = 10_000) {
                composeWindow.extendedState and Frame.MAXIMIZED_BOTH == 0
            }
            waitUntil(timeoutMillis = 10_000) { gtkFrameExtents(composeWindow) != null }
        }
    }

    private fun assumeClientShadowActive() {
        assumeTrue(OS.isLinux())
        assumeTrue(
            "Client shadow is not active for this host",
            LinuxClientShadow.shouldUseClientShadow(WindowDecoration.SystemDefault)
        )
    }

    /**
     * A [WindowDecoration.Undecorated] window requests no decoration at all: the client-drawn
     * shadow must not be applied — the window stays opaque and gets no `_GTK_FRAME_EXTENTS`.
     */
    @Test
    fun saltWindow_undecoratedDecoration_noClientShadow() {
        assumeTrue(OS.isLinux())

        runDesktopComposeUiTest {
            lateinit var composeWindow: ComposeWindow
            setContent {
                SaltTheme {
                    SaltWindow(
                        onCloseRequest = {},
                        state = rememberWindowState(),
                        title = "Linux Undecorated Window",
                        decoration = WindowDecoration.Undecorated(),
                        init = { composeWindow = it }
                    ) {
                        Box(
                            modifier = Modifier.testTag("undecoratedContent")
                        ) {
                            Text("Hello Linux")
                        }
                    }
                }
            }

            onNodeWithTag("undecoratedContent").assertIsDisplayed()
            waitForIdle()

            // Give the shadow effect a chance to (not) apply
            Thread.sleep(500)

            assertEquals(Color.BLACK, composeWindow.background)
            val extents = gtkFrameExtents(composeWindow)
            assertTrue(
                extents == null || extents.all { it == 0L },
                "Expected no client shadow extents, but was ${extents?.toList()}"
            )
        }
    }

    /**
     * Salt windows are always undecorated at the AWT level; regardless of the window manager,
     * no native frame may appear around a [SaltWindow] with the default
     * [WindowDecoration.SystemDefault] decoration.
     */
    @Test
    fun saltWindow_defaultDecoration_hasNoNativeFrameExtents() {
        assumeTrue(OS.isLinux())

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

            // Give the window manager a chance to (not) apply a native frame
            Thread.sleep(500)

            val extents = netFrameExtents(composeWindow)
            assertTrue(
                extents == null || extents.all { it == 0L },
                "Expected no native frame extents, but was ${extents?.toList()}"
            )
        }
    }

    /**
     * Reads the `_GTK_FRAME_EXTENTS` (left, right, top, bottom) of [window], or `null` if the
     * property is absent or cannot be read.
     */
    private fun gtkFrameExtents(window: Window): LongArray? =
        readCardinalProperty(window, "_GTK_FRAME_EXTENTS")

    /**
     * Reads the `_NET_FRAME_EXTENTS` (left, right, top, bottom) of [window], or `null` if the
     * property is absent or cannot be read.
     */
    private fun netFrameExtents(window: Window): LongArray? =
        readCardinalProperty(window, "_NET_FRAME_EXTENTS")

    /**
     * Reads the 32-bit cardinal window property [propertyName] (left, right, top, bottom) of
     * [window], or `null` if the property is absent or cannot be read.
     */
    private fun readCardinalProperty(window: Window, propertyName: String): LongArray? {
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

            val property = x11.XInternAtom(display, propertyName, false)
            if (property == null || property.toLong() == 0L) return null

            val actualType = X11.AtomByReference()
            val actualFormat = IntByReference()
            val itemCount = NativeLongByReference()
            val bytesAfter = NativeLongByReference()
            val value = PointerByReference()

            val result = x11.XGetWindowProperty(
                display,
                X11.Window(windowId),
                property,
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

    /**
     * Reads the X11 geometry of [window] in device pixels: `[x, y, width, height]` relative to
     * the root window, or `null` if it cannot be read.
     */
    private fun x11Geometry(window: Window): IntArray? {
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
            val xWindow = X11.Window(windowId)

            val root = X11.WindowByReference()
            val x = IntByReference()
            val y = IntByReference()
            val width = IntByReference()
            val height = IntByReference()
            val borderWidth = IntByReference()
            val depth = IntByReference()

            val geometryResult = x11.XGetGeometry(
                display,
                X11.Drawable(windowId),
                root,
                x,
                y,
                width,
                height,
                borderWidth,
                depth
            )
            if (geometryResult == 0) return null

            // XGetGeometry reports coordinates relative to the parent; translate to the root
            val rootX = IntByReference()
            val rootY = IntByReference()
            val child = X11.WindowByReference()
            val translated = x11.XTranslateCoordinates(
                display,
                xWindow,
                root.value,
                0,
                0,
                rootX,
                rootY,
                child
            )
            if (!translated) return null

            return intArrayOf(rootX.value, rootY.value, width.value, height.value)
        } catch (_: Exception) {
            return null
        } finally {
            x11.XCloseDisplay(display)
        }
    }

    private fun Robot.click(x: Int, y: Int) {
        mouseMove(x, y)
        delay(100)
        mousePress(InputEvent.BUTTON1_DOWN_MASK)
        mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
    }

    private fun Robot.canControlPointer(): Boolean {
        val before = MouseInfo.getPointerInfo().location
        val probeX = if (before.x > 0) before.x - 1 else before.x + 1
        mouseMove(probeX, before.y)
        delay(50)
        val moved = MouseInfo.getPointerInfo().location.x == probeX
        mouseMove(before.x, before.y)
        return moved
    }
}
