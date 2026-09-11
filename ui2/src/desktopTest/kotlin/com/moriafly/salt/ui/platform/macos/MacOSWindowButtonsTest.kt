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

package com.moriafly.salt.ui.platform.macos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.SaltWindow
import java.awt.Color
import java.awt.Frame
import java.awt.MouseInfo
import java.awt.Rectangle
import java.awt.Robot
import java.awt.event.InputEvent
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertTrue
import org.junit.Assume.assumeTrue

@OptIn(ExperimentalTestApi::class, ExperimentalComposeUiApi::class, UnstableSaltUiApi::class)
class MacOSWindowButtonsTest {
    @Test
    fun buttonsKeepTheirVisibleSizeAcrossNativeWindowInteractions() {
        if (OS.current !is OS.MacOS) return

        runDesktopComposeUiTest {
            lateinit var window: ComposeWindow
            var visible by mutableStateOf(true)
            val windowState = WindowState(
                position = WindowPosition.Absolute(140.dp, 140.dp),
                size = DpSize(640.dp, 480.dp)
            )
            setContent {
                SaltTheme {
                    if (visible) {
                        SaltWindow(
                            onCloseRequest = { visible = false },
                            state = windowState,
                            title = "Salt UI Native Button Test",
                            init = { window = it }
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Button(
                                    onClick = { windowState.placement = WindowPlacement.Floating },
                                    text = "Exit full screen"
                                )
                            }
                        }
                    }
                }
            }
            val robot = Robot().apply { autoDelay = 60 }
            val previousPointer = MouseInfo.getPointerInfo().location
            try {
                runOnUiThread {
                    window.toFront()
                    window.requestFocus()
                }
                waitForIdle()
                robot.mouseMove(window.x + 300, window.y + 180)
                robot.delay(150)
                assumeTrue(
                    "Real native-button tests require Accessibility permission",
                    MouseInfo.getPointerInfo().location.x == window.x + 300
                )
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                waitUntil(timeoutMillis = 5_000) { window.isFocused }
                robot.delay(350)
                assertButtonArtwork(robot, window)

                val rightEdge = window.x + window.width
                val middleY = window.y + window.height / 2
                robot.mouseMove(rightEdge, middleY)
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                repeat(12) { step -> robot.mouseMove(rightEdge + (step + 1) * 10, middleY) }
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                waitUntil(timeoutMillis = 5_000) { window.width > 700 }
                waitForIdle()
                robot.delay(350)
                val buttons = assertButtonArtwork(robot, window)

                clickButton(robot, window, buttons[1])
                waitUntil(timeoutMillis = 5_000) { window.extendedState and Frame.ICONIFIED != 0 }
                runOnUiThread {
                    window.extendedState = Frame.NORMAL
                    window.toFront()
                    window.requestFocus()
                }
                waitUntil(timeoutMillis = 5_000) { window.isFocused }
                robot.delay(350)
                assertButtonArtwork(robot, window)

                val floatingBounds = window.bounds
                clickButton(robot, window, buttons[2])
                waitUntil(timeoutMillis = 8_000) { windowState.placement == WindowPlacement.Fullscreen }
                robot.delay(1_000)
                // The fixture provides an exit control because JavaExec has no application's
                // native Window menu or full-screen keyboard shortcut
                robot.mouseMove(
                    window.x + window.width / 2,
                    window.y + window.height / 2
                )
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                waitUntil(timeoutMillis = 8_000) { windowState.placement != WindowPlacement.Fullscreen }
                waitUntil(timeoutMillis = 8_000) { window.bounds == floatingBounds }
                robot.mouseMove(window.x + 300, window.y + 180)
                robot.delay(1_500)
                val restoredButtons = assertButtonArtwork(robot, window)

                clickButton(robot, window, restoredButtons.first())
                waitUntil(timeoutMillis = 5_000) { !visible && !window.isShowing }
            } finally {
                robot.mouseMove(previousPointer.x, previousPointer.y)
            }
        }
    }

    private fun clickButton(robot: Robot, window: ComposeWindow, button: Rectangle) {
        robot.mouseMove(window.x + button.x + button.width / 2, window.y + button.y + button.height / 2)
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
    }

    private fun assertButtonArtwork(robot: Robot, window: ComposeWindow): List<Rectangle> {
        val buttons = captureButtonArtwork(robot, Rectangle(window.x, window.y, 110, 52))
        val expectedDiameter = if (
            System.getProperty("os.version").substringBefore('.').toInt() >= 26
        ) 14 else 12
        buttons.forEachIndexed { index, bounds ->
            // Measure the rendered artwork, including antialiasing tolerance, not NSView bounds
            assertTrue(
                bounds.width in expectedDiameter - 1..expectedDiameter + 1 &&
                    bounds.height in expectedDiameter - 1..expectedDiameter + 1,
                "Button $index must retain its ${expectedDiameter}pt artwork, got $bounds"
            )
        }
        return buttons
    }

    private fun captureButtonArtwork(robot: Robot, region: Rectangle): List<Rectangle> {
        val image = robot.createScreenCapture(region)
        val artifact = Files.createTempFile("salt-native-buttons-", ".png").toFile()
        ImageIO.write(image, "png", artifact)
        val buttons = (0..2).map { index ->
            val pixels = buildList {
                for (y in 0 until image.height) {
                    for (x in 0 until image.width) {
                        val color = Color(image.getRGB(x, y))
                        val hsb = Color.RGBtoHSB(color.red, color.green, color.blue, null)
                        val matches = hsb[1] > 0.4f && hsb[2] > 0.45f && when (index) {
                            0 -> hsb[0] < 0.04f || hsb[0] > 0.95f
                            1 -> hsb[0] in 0.08f..0.18f
                            else -> hsb[0] in 0.23f..0.47f
                        }
                        if (matches) add(x to y)
                    }
                }
            }
            assertTrue(pixels.isNotEmpty(), "Button $index is not visible: $artifact")
            val left = pixels.minOf { it.first }
            val top = pixels.minOf { it.second }
            Rectangle(
                left,
                top,
                pixels.maxOf { it.first } - left + 1,
                pixels.maxOf { it.second } - top + 1
            )
        }
        println("Native button artwork: $buttons; screenshot: $artifact")
        return buttons
    }
}
