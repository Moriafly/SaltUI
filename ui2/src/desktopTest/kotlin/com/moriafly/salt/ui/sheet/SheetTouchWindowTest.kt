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

@file:OptIn(
    androidx.compose.ui.test.ExperimentalTestApi::class,
    com.moriafly.salt.adaptive.window.UnstableSaltAdaptiveWindowApi::class,
    com.moriafly.salt.ui.UnstableSaltUiApi::class
)

package com.moriafly.salt.ui.sheet

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.platform.windows.InjectedTouchInfo
import com.moriafly.salt.ui.platform.windows.TouchInjectionUser32
import com.moriafly.salt.ui.util.findSkiaLayer
import com.sun.jna.Native
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinDef.POINT
import org.junit.Assume.assumeTrue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SheetTouchWindowTest {
    @Test
    fun nativeNestedTouchExpandsThenScrollsWithoutPrematureCollapse() =
        verifyNestedTouch(SheetContentInteraction.ResizeFirst)

    @Test
    fun nativeScrollFirstKeepsHeightUntilTheHandleIsDragged() =
        verifyNestedTouch(SheetContentInteraction.ScrollFirst)

    private fun verifyNestedTouch(interaction: SheetContentInteraction) {
        assumeTrue(OS.isWindows())
        runDesktopComposeUiTest {
            val fixture = SheetFixture(bottom = true, list = true, interaction = interaction)
            setContent { fixture.Content() }
            val robot = fixture.activate()
            fixture.click(robot, "Open")
            val medium = fixture.bounds.getValue("panel")
            val native = Native.load("user32", TouchInjectionUser32::class.java)
            val origin = POINT(0, 0)
            runOnUiThread {
                val canvas = fixture.window.findSkiaLayer()!!.canvas
                assertTrue(native.ClientToScreen(HWND(Native.getComponentPointer(canvas)), origin))
            }
            val executor = Executors.newSingleThreadExecutor()
            try {
                executor.submit {
                    check(native.InitializeTouchInjection(2, 3))
                }.get(5, TimeUnit.SECONDS)

                fun swipe(startX: Float, startY: Float, deltaY: Float, cancel: Boolean = false) {
                    executor.submit {
                        val contact = InjectedTouchInfo()

                        fun inject(y: Float, flags: Int) {
                            contact.pointerInfo.pointerType = 2
                            contact.pointerInfo.pointerId = 1
                            contact.pointerInfo.pointerFlags = flags
                            contact.pointerInfo.ptPixelLocation = POINT(
                                origin.x + startX.roundToInt(),
                                origin.y + y.roundToInt()
                            )
                            contact.write()
                            Thread.sleep(20)
                            check(native.InjectTouchInput(1, contact.pointer)) {
                                "Native touch injection failed: ${Native.getLastError()}"
                            }
                        }
                        inject(startY, 0x00010000 or 0x00000002 or 0x00000004)
                        repeat(20) {
                            inject(
                                startY + deltaY * (it + 1) / 20,
                                0x00020000 or 0x00000002 or 0x00000004
                            )
                        }
                        inject(startY + deltaY, 0x00040000 or if (cancel) 0x00008000 else 0)
                    }.get(10, TimeUnit.SECONDS)
                    robot.delay(700)
                    waitForIdle()
                }
                // Cancel beyond the dismissal threshold: the sheet must recover without a request.
                swipe(medium.center.x, medium.top + 36f, 140f, cancel = true)
                assertEquals(medium.height, fixture.bounds.getValue("panel").height, 2f)
                runOnIdle { assertEquals(0, fixture.requests) }
                val list = fixture.bounds.getValue("list")
                swipe(list.center.x, list.bottom - 30f, -medium.height * 0.65f)
                if (interaction == SheetContentInteraction.ScrollFirst) {
                    assertEquals(medium.height, fixture.bounds.getValue("panel").height, 2f)
                    onNodeWithText("Row 0").assertDoesNotExist()
                    swipe(medium.center.x, medium.top + 36f, -medium.height * 0.65f)
                }
                val large = fixture.bounds.getValue("panel")
                assertTrue(large.height > medium.height * 1.5f)
                val expandedList = fixture.bounds.getValue("list")
                swipe(expandedList.center.x, expandedList.bottom - 80f, -expandedList.height * 0.5f)
                onNodeWithText("Row 0").assertDoesNotExist()
                swipe(expandedList.center.x, expandedList.center.y, 80f)
                assertEquals(large.height, fixture.bounds.getValue("panel").height, 2f)
                runOnIdle { assertEquals(0, fixture.requests) }
                fixture.click(robot, "Program close")
                waitForIdle()
            } finally {
                executor.shutdownNow()
                fixture.restorePointer(robot)
            }
        }
    }
}
