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

package com.moriafly.salt.ui.window

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.platform.linux.LinuxClientShadow
import com.moriafly.salt.ui.platform.windows.BasicWindowProc
import com.moriafly.salt.ui.platform.windows.ComposeWindowProc
import com.moriafly.salt.ui.platform.windows.HitTestResult
import com.moriafly.salt.ui.platform.windows.WinUserConst.WM_NCCALCSIZE
import com.moriafly.salt.ui.util.findSkiaLayer
import com.moriafly.salt.ui.util.hwnd
import com.sun.jna.platform.win32.WinDef
import java.awt.Color
import java.awt.Dimension
import java.awt.MouseInfo
import java.awt.Robot
import java.awt.event.InputEvent
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Assume.assumeTrue

@OptIn(
    ExperimentalTestApi::class,
    ExperimentalComposeUiApi::class,
    UnstableSaltUiApi::class
)
class SaltWindowTest {
    @Test
    fun saltWindow_displaysContent() = runDesktopComposeUiTest {
        lateinit var composeWindow: ComposeWindow
        setContent {
            SaltTheme {
                SaltWindow(
                    onCloseRequest = {},
                    state = rememberWindowState(),
                    title = "Test Window",
                    init = { composeWindow = it }
                ) {
                    Box(
                        modifier = Modifier
                            .testTag("saltWindowContent")
                    ) {
                        Text("Hello SaltWindow")
                    }
                }
            }
        }

        onNodeWithTag("saltWindowContent").assertIsDisplayed()

        // With the Linux client-drawn shadow the window background becomes fully transparent
        // once the window is undecorated
        val clientShadow = OS.isLinux() &&
            LinuxClientShadow.shouldUseClientShadow(WindowDecoration.SystemDefault)
        if (clientShadow) {
            waitUntil(timeoutMillis = 5_000) { composeWindow.background.alpha == 0 }
        } else {
            assertEquals(Color.BLACK, composeWindow.background)
        }
    }

    @Test
    fun saltWindow_undecoratedTransparent_displaysContent() = runDesktopComposeUiTest {
        setContent {
            SaltTheme {
                SaltWindow(
                    onCloseRequest = {},
                    state = rememberWindowState(),
                    title = "Transparent Window",
                    decoration = WindowDecoration.Undecorated(),
                    transparent = true
                ) {
                    Box(
                        modifier = Modifier
                            .testTag("transparentWindowContent")
                    ) {
                        Text("Hello Transparent SaltWindow")
                    }
                }
            }
        }

        onNodeWithTag("transparentWindowContent").assertIsDisplayed()
    }

    @Test
    fun saltWindow_windowsResizeKeepsNativeResizeChainActive() {
        if (OS.current !is OS.Windows) return

        runDesktopComposeUiTest {
            lateinit var composeWindow: ComposeWindow
            lateinit var resizeProbe: BasicWindowProc
            lateinit var testWindowProc: ComposeWindowProc
            val resizeMessageCount = AtomicInteger()
            setContent {
                SaltTheme {
                    SaltWindow(
                        onCloseRequest = {},
                        state = rememberWindowState(
                            position = WindowPosition.Absolute(120.dp, 120.dp),
                            size = DpSize(640.dp, 480.dp)
                        ),
                        title = "Native Resize Chain",
                        init = { window ->
                            composeWindow = window
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("nativeResizeChainContent")
                        )
                    }
                }
            }

            onNodeWithTag("nativeResizeChainContent").assertIsDisplayed()
            runOnUiThread {
                composeWindow.toFront()
                resizeProbe = object : BasicWindowProc(composeWindow.hwnd) {
                    override fun callback(
                        hwnd: WinDef.HWND,
                        uMsg: Int,
                        wParam: WinDef.WPARAM,
                        lParam: WinDef.LPARAM
                    ): WinDef.LRESULT {
                        if (uMsg == WM_NCCALCSIZE && wParam.toInt() != 0) {
                            resizeMessageCount.incrementAndGet()
                        }
                        return super.callback(hwnd, uMsg, wParam, lParam)
                    }
                }
                testWindowProc = ComposeWindowProc(
                    window = composeWindow,
                    hitTest = { _, _ -> HitTestResult.HTCLIENT },
                    onWindowInsetUpdate = {},
                    onResizeEdgeChange = {}
                )
            }
            waitForIdle()

            val robot = Robot().apply { autoDelay = 30 }
            assumeTrue(
                "java.awt.Robot cannot synthesize input events",
                robot.canControlPointer()
            )
            val initialSize = composeWindow.size
            robot.mouseMove(
                composeWindow.x + composeWindow.width / 2,
                composeWindow.y + composeWindow.height / 2
            )
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
            waitUntil(timeoutMillis = 5_000) { composeWindow.isFocused }

            resizeMessageCount.set(0)
            runOnUiThread {
                composeWindow.size = Dimension(
                    initialSize.width + 120,
                    initialSize.height + 80
                )
            }
            waitUntil(timeoutMillis = 5_000) {
                composeWindow.width >= initialSize.width + 100 &&
                    composeWindow.height >= initialSize.height + 60
            }

            assertEquals(
                Dimension(initialSize.width + 120, initialSize.height + 80),
                composeWindow.size
            )
            assertTrue(resizeMessageCount.get() > 0)
            assertTrue(composeWindow.isShowing)
            java.lang.ref.Reference.reachabilityFence(resizeProbe)
            java.lang.ref.Reference.reachabilityFence(testWindowProc)
        }
    }

    @Test
    fun saltWindow_macosVibrancy_displaysContent() {
        if (OS.current !is OS.MacOS) return

        runDesktopComposeUiTest {
            lateinit var composeWindow: ComposeWindow
            var backgroundType by mutableStateOf(SaltWindowBackgroundType.Vibrancy)
            setContent {
                SaltTheme {
                    SaltWindow(
                        onCloseRequest = {},
                        state = rememberWindowState(),
                        title = "Vibrancy Window",
                        properties = SaltWindowProperties.default(
                            backgroundType = backgroundType
                        ),
                        init = { composeWindow = it }
                    ) {
                        Box(
                            modifier = Modifier
                                .testTag("vibrancyWindowContent")
                        ) {
                            Text("Hello Vibrancy SaltWindow")
                        }
                    }
                }
            }

            onNodeWithTag("vibrancyWindowContent").assertIsDisplayed()
            assertEquals(0, composeWindow.findSkiaLayer()?.background?.alpha)

            runOnUiThread {
                composeWindow.size = Dimension(720, 540)
            }
            waitForIdle()
            assertEquals(Dimension(720, 540), composeWindow.size)
            onNodeWithTag("vibrancyWindowContent").assertIsDisplayed()

            runOnUiThread {
                backgroundType = SaltWindowBackgroundType.None
            }
            waitForIdle()
            onNodeWithTag("vibrancyWindowContent").assertIsDisplayed()
            assertEquals(Color.BLACK, composeWindow.background)
        }
    }

    @Test
    fun saltWindow_macosCaptionBar_doubleClickAtVisibleBottomMaximizes() {
        if (OS.current !is OS.MacOS) return

        runMacOSCaptionBarDoubleClickTest(
            relativeY = 50,
            extraDisplayScale = 1.0f,
            shouldMaximize = true
        )
    }

    @Test
    fun saltWindow_macosCaptionBar_doubleClickAtScaledVisibleBottomMaximizes() {
        if (OS.current !is OS.MacOS) return

        runMacOSCaptionBarDoubleClickTest(
            relativeY = 60,
            extraDisplayScale = 1.2f,
            shouldMaximize = true
        )
    }

    @Test
    fun saltWindow_macosCaptionBar_doubleClickBelowVisibleBottomDoesNotMaximize() {
        if (OS.current !is OS.MacOS) return

        runMacOSCaptionBarDoubleClickTest(
            relativeY = 54,
            extraDisplayScale = 1.0f,
            shouldMaximize = false
        )
    }

    @Test
    fun saltWindow_macosCaptionBar_doubleClickBelowScaledVisibleBottomDoesNotMaximize() {
        if (OS.current !is OS.MacOS) return

        runMacOSCaptionBarDoubleClickTest(
            relativeY = 66,
            extraDisplayScale = 1.2f,
            shouldMaximize = false
        )
    }

    private fun runMacOSCaptionBarDoubleClickTest(
        relativeY: Int,
        extraDisplayScale: Float,
        shouldMaximize: Boolean
    ) {
        runDesktopComposeUiTest {
            lateinit var composeWindow: ComposeWindow
            setContent {
                SaltTheme {
                    SaltWindow(
                        onCloseRequest = {},
                        state = rememberWindowState(
                            position = WindowPosition.Absolute(120.dp, 120.dp),
                            size = DpSize(640.dp, 480.dp)
                        ),
                        title = "Caption Bar Hit Test",
                        properties = SaltWindowProperties.default(
                            extraDisplayScale = extraDisplayScale
                        ),
                        init = { composeWindow = it }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            CaptionBarHitTest(
                                modifier = Modifier.testTag("captionBarHitTest")
                            )
                        }
                    }
                }
            }

            onNodeWithTag("captionBarHitTest").assertIsDisplayed()
            runOnUiThread {
                composeWindow.toFront()
                composeWindow.requestFocus()
            }
            waitForIdle()

            val robot = Robot().apply {
                autoDelay = 40
            }
            assumeTrue(
                "java.awt.Robot cannot synthesize input events; grant the host IDE or " +
                    "terminal Accessibility permission to run this real-input test",
                robot.canControlPointer()
            )
            val initialWidth = composeWindow.width
            robot.doubleClick(
                x = composeWindow.x + composeWindow.width / 2,
                y = composeWindow.y + relativeY
            )
            if (shouldMaximize) {
                waitUntil(timeoutMillis = 5_000) {
                    composeWindow.width > initialWidth + 100
                }
            } else {
                robot.delay(800)
                assertEquals(initialWidth, composeWindow.width)
            }
            assertTrue(composeWindow.isShowing)
        }
    }
}

private fun Robot.doubleClick(x: Int, y: Int) {
    mouseMove(x, y)
    delay(150)
    repeat(2) {
        mousePress(InputEvent.BUTTON1_DOWN_MASK)
        mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
        delay(60)
    }
}

/**
 * Returns whether this [Robot] can actually move the pointer. macOS silently drops synthesized
 * input until the user grants the host process Accessibility permission, in which case tests
 * driving real input events must skip instead of failing.
 */
private fun Robot.canControlPointer(): Boolean {
    val before = MouseInfo.getPointerInfo().location
    val probeX = if (before.x > 0) before.x - 1 else before.x + 1
    mouseMove(probeX, before.y)
    delay(50)
    val moved = MouseInfo.getPointerInfo().location.x == probeX
    mouseMove(before.x, before.y)
    return moved
}
