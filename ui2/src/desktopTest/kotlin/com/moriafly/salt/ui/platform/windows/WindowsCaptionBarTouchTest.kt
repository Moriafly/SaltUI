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

package com.moriafly.salt.ui.platform.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.util.findSkiaLayer
import com.moriafly.salt.ui.util.hwnd
import com.moriafly.salt.ui.window.CaptionBarHitTest
import com.moriafly.salt.ui.window.SaltWindow
import com.moriafly.salt.ui.window.SaltWindowProperties
import com.sun.jna.Native
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinDef.POINT
import com.sun.jna.platform.win32.WinDef.RECT
import org.junit.Assume.assumeTrue
import java.awt.Rectangle
import java.awt.Robot
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(
    ExperimentalTestApi::class,
    ExperimentalComposeUiApi::class,
    UnstableSaltUiApi::class
)
class WindowsCaptionBarTouchTest {
    @Test
    fun nativeTouchOperatesCaptionWithoutMouseHover() {
        assumeTrue(OS.isWindows())
        val native = Native.load("user32", TouchInjectionUser32::class.java)
        val executor = Executors.newSingleThreadExecutor()
        try {
            runDesktopComposeUiTest {
                lateinit var window: ComposeWindow
                lateinit var state: WindowState
                var movable by mutableStateOf(true)
                var buttonsEnabled by mutableStateOf(true)
                var closeCount = 0
                var controlCount = 0
                setContent {
                    SaltTheme {
                        state = rememberWindowState(
                            position = WindowPosition.Absolute(200.dp, 200.dp),
                            size = DpSize(640.dp, 480.dp)
                        )
                        SaltWindow(
                            onCloseRequest = { closeCount++ },
                            state = state,
                            title = "SaltUI caption touch test",
                            alwaysOnTop = true,
                            properties = SaltWindowProperties.default(
                                moveable = movable,
                                minimizeButtonEnabled = buttonsEnabled,
                                maximizeOrRestoreButtonEnabled = buttonsEnabled,
                                extraDisplayScale = 1.25f
                            ),
                            init = { window = it }
                        ) {
                            Box(
                                Modifier.fillMaxSize().background(Color(0xFF1261AB))
                                    .testTag("caption-window")
                            ) {
                                CaptionBarHitTest()
                                Box(
                                    Modifier.size(80.dp, 40.dp)
                                        .clickable { controlCount++ }
                                )
                            }
                        }
                    }
                }
                onNodeWithTag("caption-window").assertExists()
                runOnUiThread {
                    window.toFront()
                    window.requestFocus()
                }
                waitForIdle()

                fun bounds(): RECT = RECT().also {
                    assertTrue(User32Ex.INSTANCE.GetWindowRect(window.hwnd, it))
                }

                val moveStarts = AtomicInteger()
                val moveEnds = AtomicInteger()
                val moveProbe = object : BasicWindowProc(window.hwnd) {
                    override fun callback(
                        hwnd: HWND,
                        uMsg: Int,
                        wParam: WinDef.WPARAM,
                        lParam: WinDef.LPARAM
                    ): WinDef.LRESULT {
                        if (uMsg == 0x0231) moveStarts.incrementAndGet()
                        if (uMsg == 0x0232) moveEnds.incrementAndGet()
                        return super.callback(hwnd, uMsg, wParam, lParam)
                    }
                }

                fun snapshot(name: String) {
                    val rect = bounds()
                    val image = Robot().createScreenCapture(
                        Rectangle(
                            (rect.left / window.graphicsConfiguration.defaultTransform.scaleX).roundToInt(),
                            (rect.top / window.graphicsConfiguration.defaultTransform.scaleY).roundToInt(),
                            (
                                (rect.right - rect.left) /
                                    window.graphicsConfiguration.defaultTransform.scaleX
                            ).roundToInt(),
                            (
                                (rect.bottom - rect.top) /
                                    window.graphicsConfiguration.defaultTransform.scaleY
                            ).roundToInt()
                        )
                    )
                    val output = File("build/reports/caption-touch/$name.png")
                    output.parentFile.mkdirs()
                    ImageIO.write(image, "png", output)
                }

                fun captionPoint(button: Int? = null, control: Boolean = false): POINT {
                    lateinit var point: POINT
                    runOnUiThread {
                        val canvas = window.findSkiaLayer()!!.canvas
                        val scale = canvas.graphicsConfiguration.defaultTransform.scaleX
                        val density = scale * 1.25
                        val x = when {
                            button != null ->
                                canvas.width * scale -
                                    WindowsCaptionButtonWidth.value * density * (button + 0.5)

                            control -> 40 * density
                            else -> 200 * density
                        }
                        point = POINT(x.roundToInt(), (20 * density).roundToInt())
                        assertTrue(
                            native.ClientToScreen(
                                HWND(Native.getComponentPointer(canvas)),
                                point
                            )
                        )
                    }
                    return point
                }

                fun touch(
                    point: POINT,
                    dx: Int = 0,
                    dy: Int = 0,
                    capturePreview: Boolean = false,
                    cancel: Boolean = false
                ) {
                    val injection = executor.submit {
                        check(native.InitializeTouchInjection(2, 3))
                        val contact = InjectedTouchInfo()

                        fun frame(
                            flags: Int,
                            x: Int,
                            y: Int
                        ) {
                            contact.pointerInfo.pointerType = 2
                            contact.pointerInfo.pointerId = 1
                            contact.pointerInfo.pointerFlags = flags
                            contact.pointerInfo.ptPixelLocation = POINT(x, y)
                            contact.write()
                            Thread.sleep(16)
                            check(native.InjectTouchInput(1, contact.pointer)) {
                                "InjectTouchInput failed: ${Native.getLastError()}"
                            }
                        }

                        var released = false
                        try {
                            frame(0x10006, point.x, point.y)
                            repeat(4) { frame(0x20006, point.x, point.y) }
                            repeat(12) { step ->
                                frame(
                                    0x20006,
                                    point.x + dx * (step + 1) / 12,
                                    point.y + dy * (step + 1) / 12
                                )
                            }
                            if (capturePreview) {
                                repeat(8) { frame(0x20006, point.x + dx, point.y + dy) }
                                snapshot("during")
                            }
                            frame(if (cancel) 0x48000 else 0x40000, point.x + dx, point.y + dy)
                            released = true
                        } finally {
                            if (!released) {
                                runCatching {
                                    frame(
                                        0x48000,
                                        point.x + dx,
                                        point.y + dy
                                    )
                                }
                            }
                        }
                    }
                    waitUntil(timeoutMillis = 10_000) { injection.isDone }
                    injection.get()
                    waitForIdle()
                }

                // Keep the real mouse away from the caption, so no hover prepares its hit test
                val initial = bounds()
                Robot().mouseMove(initial.left + 50, initial.bottom - 50)
                waitForIdle()
                touch(captionPoint(control = true))
                waitUntil(timeoutMillis = 5_000) { controlCount >= 1 }
                assertEquals(1, controlCount)
                assertEquals(initial.left, bounds().left)
                assertEquals(initial.top, bounds().top)

                assertEquals(0, moveStarts.get())
                snapshot("before")
                touch(captionPoint(), dx = 120, dy = 80, capturePreview = true)
                waitUntil(timeoutMillis = 5_000) { bounds().left != initial.left }
                assertEquals(initial.left + 120, bounds().left)
                assertEquals(initial.top + 80, bounds().top)
                waitUntil(timeoutMillis = 5_000) { moveEnds.get() > 0 }
                assertEquals(1, moveStarts.get())
                assertEquals(1, moveEnds.get())
                Robot().delay(300)
                snapshot("after")

                // A hovering mouse can route the touch through the top-level non-client procedure
                val hovered = captionPoint()
                Robot().apply {
                    mouseMove(hovered.x, hovered.y)
                    delay(150)
                }
                waitForIdle()
                val beforeHoverTouch = bounds()
                touch(captionPoint(control = true))
                waitUntil(timeoutMillis = 5_000) { controlCount >= 2 }
                assertEquals(2, controlCount)
                assertEquals(beforeHoverTouch.left, bounds().left)
                touch(captionPoint(), dx = 60, dy = 40)
                assertEquals(beforeHoverTouch.left + 60, bounds().left)
                assertEquals(beforeHoverTouch.top + 40, bounds().top)

                runOnUiThread { movable = false }
                waitForIdle()
                val fixed = bounds()
                touch(captionPoint(), dx = 100, dy = 50)
                assertEquals(fixed.left, bounds().left)
                assertEquals(fixed.top, bounds().top)
                runOnUiThread { movable = true }
                waitForIdle()

                touch(captionPoint(button = 1))
                waitUntil(timeoutMillis = 5_000) { state.placement == WindowPlacement.Maximized }
                touch(captionPoint(button = 1))
                waitUntil(timeoutMillis = 5_000) { state.placement == WindowPlacement.Floating }

                touch(captionPoint(button = 1))
                waitUntil(timeoutMillis = 5_000) { state.placement == WindowPlacement.Maximized }
                touch(captionPoint(), dx = 100, dy = 80)
                waitUntil(timeoutMillis = 5_000) { state.placement == WindowPlacement.Floating }

                touch(captionPoint(button = 2))
                waitUntil(timeoutMillis = 5_000) { state.isMinimized }
                runOnUiThread {
                    state.isMinimized = false
                    window.toFront()
                    window.requestFocus()
                }
                waitUntil(timeoutMillis = 5_000) { !state.isMinimized && window.isShowing }
                waitForIdle()

                val beforeCancel = moveEnds.get()
                touch(captionPoint(), dx = 40, dy = 20, cancel = true)
                waitUntil(timeoutMillis = 5_000) { moveEnds.get() > beforeCancel }
                touch(captionPoint(control = true))
                waitUntil(timeoutMillis = 5_000) { controlCount >= 3 }
                assertEquals(3, controlCount)

                runOnUiThread { buttonsEnabled = false }
                waitForIdle()
                touch(captionPoint(button = 1))
                touch(captionPoint(button = 2))
                assertEquals(WindowPlacement.Floating, state.placement)
                assertTrue(!state.isMinimized)

                touch(captionPoint(button = 0))
                waitUntil(timeoutMillis = 5_000) { closeCount == 1 }
                waitUntil(timeoutMillis = 5_000) { moveStarts.get() == moveEnds.get() }
                assertEquals(moveStarts.get(), moveEnds.get())
                assertTrue(moveProbe.originalWindowProc.toLong() != 0L)
                runOnUiThread { window.dispose() }
            }
        } finally {
            executor.shutdownNow()
        }
    }
}
