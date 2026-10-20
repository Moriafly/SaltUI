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

package com.moriafly.salt.ui.window

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogModalityType
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.util.findSkiaLayer
import org.junit.Assume.assumeTrue
import java.awt.Point
import java.awt.Robot
import java.awt.Window
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.SwingUtilities
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(
    ExperimentalTestApi::class,
    ExperimentalComposeUiApi::class,
    UnstableSaltUiApi::class
)
class CaptionBarHitTestTest {
    @Test
    fun secondRegionInWindowIsRejectedAfterRealClick() {
        assertDuplicateIsRejected(inDialog = false)
    }

    @Test
    fun secondRegionInChildDialogIsRejectedAfterRealClick() {
        assertDuplicateIsRejected(inDialog = true)
    }

    private fun assertDuplicateIsRejected(inDialog: Boolean) {
        val error = assertFailsWith<IllegalStateException> {
            runDesktopComposeUiTest {
                lateinit var target: Window
                var duplicate by mutableStateOf(false)
                setContent {
                    SaltTheme {
                        SaltWindow(
                            onCloseRequest = {},
                            title = "Caption registration parent",
                            alwaysOnTop = true,
                            init = { if (!inDialog) target = it }
                        ) {
                            if (inDialog) {
                                CaptionBarHitTest()
                                SaltDialogWindow(
                                    onCloseRequest = {},
                                    title = "Caption registration dialog",
                                    alwaysOnTop = true,
                                    init = { target = it }
                                ) {
                                    DuplicateCaptionContent(duplicate) { duplicate = true }
                                }
                            } else {
                                DuplicateCaptionContent(duplicate) { duplicate = true }
                            }
                        }
                    }
                }

                onNodeWithTag("add-region").assertIsDisplayed()
                val robot = Robot().apply { autoDelay = 30 }
                robot.bringToFront(target)
                robot.click(onNodeWithTag("add-region").screenPoint(target))
                waitUntil(timeoutMillis = 5_000) { duplicate }
                waitForIdle()
            }
        }
        assertTrue(error.message.orEmpty().contains("Only one CaptionBarHitTest"))
    }

    @Test
    fun parentChildWindowAndChildDialogHaveIndependentRegions() {
        assumeTrue(OS.isWindows())
        runDesktopComposeUiTest {
            lateinit var parent: ComposeWindow
            lateinit var child: ComposeWindow
            lateinit var dialog: ComposeDialog
            setContent {
                SaltTheme {
                    SaltWindow(
                        onCloseRequest = {},
                        state = rememberWindowState(
                            position = WindowPosition.Absolute(80.dp, 80.dp),
                            size = DpSize(640.dp, 480.dp)
                        ),
                        title = "Independent parent caption",
                        alwaysOnTop = true,
                        init = { parent = it }
                    ) {
                        CaptionBarHitTest(Modifier.testTag("parent-caption"))
                        SaltWindow(
                            onCloseRequest = {},
                            state = rememberWindowState(
                                position = WindowPosition.Absolute(160.dp, 160.dp),
                                size = DpSize(560.dp, 400.dp)
                            ),
                            title = "Independent child caption",
                            alwaysOnTop = true,
                            init = { child = it }
                        ) {
                            CaptionBarHitTest(Modifier.testTag("child-caption"))
                            SaltDialogWindow(
                                onCloseRequest = {},
                                state = rememberDialogState(
                                    position = WindowPosition.Absolute(240.dp, 240.dp),
                                    size = DpSize(480.dp, 320.dp)
                                ),
                                title = "Independent dialog caption",
                                alwaysOnTop = true,
                                modalityType = DialogModalityType.Modeless,
                                init = { dialog = it }
                            ) {
                                CaptionBarHitTest(Modifier.testTag("dialog-caption"))
                            }
                        }
                    }
                }
            }

            val windows = listOf(
                parent to "parent-caption",
                child to "child-caption",
                dialog to "dialog-caption"
            )
            assertEquals(child, dialog.owner)
            val robot = Robot().apply { autoDelay = 30 }
            for ((targetIndex, entry) in windows.withIndex()) {
                val (target, tag) = entry
                onNodeWithTag(tag).assertIsDisplayed()
                robot.bringToFront(target)
                waitForIdle()
                val initialLocations = windows.map { it.first.location }
                val targetLocation = initialLocations[targetIndex]
                robot.drag(onNodeWithTag(tag).screenPoint(target))
                waitUntil(timeoutMillis = 5_000) {
                    target.location.x >= targetLocation.x + 40
                }
                for ((index, other) in windows.withIndex()) {
                    if (other.first != target) {
                        assertEquals(initialLocations[index], other.first.location)
                    }
                }
            }
        }
    }

    @Test
    fun regionCanBeReplacedRemovedWhileHoveredAndRegisteredAgain() {
        assumeTrue(OS.isWindows())
        runDesktopComposeUiTest {
            lateinit var window: ComposeWindow
            var enabled by mutableStateOf(false)
            var alternate by mutableStateOf(false)
            setContent {
                SaltTheme {
                    SaltWindow(
                        onCloseRequest = {},
                        state = rememberWindowState(
                            position = WindowPosition.Absolute(120.dp, 120.dp),
                            size = DpSize(640.dp, 480.dp)
                        ),
                        title = "Caption registration lifecycle",
                        alwaysOnTop = true,
                        properties = SaltWindowProperties.default(extraDisplayScale = 1.25f),
                        onPreviewKeyEvent = {
                            when {
                                it.type != KeyEventType.KeyDown -> false
                                it.key == Key.Spacebar -> {
                                    enabled = !enabled
                                    true
                                }
                                it.key == Key.R -> {
                                    alternate = !alternate
                                    true
                                }
                                else -> false
                            }
                        },
                        init = { window = it }
                    ) {
                        Box(Modifier.fillMaxSize().testTag("content")) {
                            if (enabled) {
                                if (alternate) {
                                    Box {
                                        CaptionBarHitTest(Modifier.testTag("caption"))
                                    }
                                } else {
                                    CaptionBarHitTest(Modifier.testTag("caption"))
                                }
                            }
                        }
                    }
                }
            }

            onNodeWithTag("content").assertIsDisplayed()
            val robot = Robot().apply { autoDelay = 30 }
            robot.bringToFront(window)
            robot.click(onNodeWithTag("content").screenPoint(window))
            waitUntil(timeoutMillis = 5_000) { window.isFocused }
            val noRegionLocation = window.location
            val captionPoint = Point(
                onNodeWithTag("content").screenPoint(window).x,
                window.findSkiaLayer()!!.canvas.locationOnScreen.y + 25
            )
            robot.drag(captionPoint)
            waitForIdle()
            assertEquals(noRegionLocation, window.location)

            robot.pressKey(KeyEvent.VK_SPACE)
            onNodeWithTag("caption").assertIsDisplayed()
            val withRegionLocation = window.location
            robot.drag(onNodeWithTag("caption").screenPoint(window))
            waitUntil(timeoutMillis = 5_000) { window.x >= withRegionLocation.x + 40 }

            // Replacing the call site in one composition must release the previous registration.
            robot.pressKey(KeyEvent.VK_R)
            waitForIdle()
            val replacementLocation = window.location
            robot.drag(onNodeWithTag("caption").screenPoint(window, bottomOffset = -1))
            waitUntil(timeoutMillis = 5_000) { window.x >= replacementLocation.x + 40 }

            // The lower boundary and the point below it must remain client input.
            for (offset in listOf(0, 1)) {
                val initialLocation = window.location
                robot.drag(onNodeWithTag("caption").screenPoint(window, bottomOffset = offset))
                waitForIdle()
                assertEquals(initialLocation, window.location)
            }

            // Remove using the keyboard while the pointer is still inside the caption region.
            val removedPoint = onNodeWithTag("caption").screenPoint(window)
            robot.mouseMove(removedPoint.x, removedPoint.y)
            robot.delay(150)
            robot.pressKey(KeyEvent.VK_SPACE)
            onNodeWithTag("caption").assertDoesNotExist()
            val removedLocation = window.location
            robot.drag(removedPoint)
            waitForIdle()
            assertEquals(removedLocation, window.location)

            robot.pressKey(KeyEvent.VK_SPACE)
            onNodeWithTag("caption").assertIsDisplayed()
            val registeredAgainLocation = window.location
            robot.drag(onNodeWithTag("caption").screenPoint(window))
            waitUntil(timeoutMillis = 5_000) { window.x >= registeredAgainLocation.x + 40 }
        }
    }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun DuplicateCaptionContent(duplicate: Boolean, onAddRegion: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        CaptionBarHitTest()
        if (duplicate) {
            // A separate layout branch still belongs to the same native window.
            Box { CaptionBarHitTest() }
        }
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .size(100.dp)
                .testTag("add-region")
                .clickable(onClick = onAddRegion)
        )
    }
}

@OptIn(ExperimentalTestApi::class, UnstableSaltUiApi::class)
private fun SemanticsNodeInteraction.screenPoint(
    window: Window,
    bottomOffset: Int? = null
): Point {
    val bounds = fetchSemanticsNode().boundsInRoot
    val canvas = window.findSkiaLayer()!!.canvas
    val origin = canvas.locationOnScreen
    val transform = canvas.graphicsConfiguration.defaultTransform
    val y = if (bottomOffset == null) {
        (bounds.center.y / transform.scaleY).roundToInt()
    } else {
        (bounds.bottom / transform.scaleY).roundToInt() + bottomOffset
    }
    return Point(origin.x + (bounds.center.x / transform.scaleX).roundToInt(), origin.y + y)
}

private fun Robot.bringToFront(window: Window) {
    SwingUtilities.invokeAndWait {
        window.toFront()
        window.requestFocus()
    }
    delay(200)
}

private fun Robot.click(point: Point) {
    mouseMove(point.x, point.y)
    mousePress(InputEvent.BUTTON1_DOWN_MASK)
    mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
}

private fun Robot.drag(point: Point) {
    mouseMove(point.x, point.y)
    delay(150)
    mousePress(InputEvent.BUTTON1_DOWN_MASK)
    try {
        for (step in 1..8) {
            mouseMove(point.x + step * 8, point.y + step * 4)
        }
    } finally {
        mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
    }
}

private fun Robot.pressKey(code: Int) {
    keyPress(code)
    keyRelease(code)
}
