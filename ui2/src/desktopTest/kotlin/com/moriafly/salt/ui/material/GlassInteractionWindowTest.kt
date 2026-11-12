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

package com.moriafly.salt.ui.material

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import java.awt.Robot
import java.awt.event.InputEvent
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class, UnstableSaltUiApi::class)
class GlassInteractionWindowTest {
    @Test
    fun repeatedPressesAnimateAndReturnToRest() = runDesktopComposeUiTest {
        lateinit var window: ComposeWindow
        lateinit var interaction: GlassInteractionState
        val clicks = AtomicInteger()
        setContent {
            Window(
                onCloseRequest = {},
                title = "Glass interaction regression",
                undecorated = true,
                alwaysOnTop = true,
                state = rememberWindowState(
                    position = WindowPosition.Absolute(100.dp, 100.dp),
                    size = DpSize(320.dp, 320.dp)
                )
            ) {
                val source = rememberGlassSource()
                val state = rememberGlassInteractionState()
                SideEffect {
                    window = this.window
                    interaction = state
                }
                SaltTheme {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Box(
                            Modifier.size(120.dp)
                                .testTag("glass")
                                .glass(
                                    source = source,
                                    effect = GlassEffect.Solid(),
                                    interactionState = state
                                )
                                .clickable { clicks.incrementAndGet() }
                        )
                    }
                }
            }
        }

        waitForIdle()
        runOnUiThread {
            window.toFront()
            window.requestFocus()
        }
        waitUntil(timeoutMillis = 5_000) { window.isFocused && window.isShowing }
        val center = onNodeWithTag("glass").fetchSemanticsNode().boundsInRoot.center
        val transform = window.graphicsConfiguration.defaultTransform
        val robot = Robot().apply { autoDelay = 30 }
        robot.mouseMove(
            window.x + (center.x / transform.scaleX).roundToInt(),
            window.y + (center.y / transform.scaleY).roundToInt()
        )

        try {
            repeat(3) { index ->
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                waitUntil(timeoutMillis = 5_000) { interaction.scale < 0.982f }
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                waitUntil(timeoutMillis = 5_000) {
                    interaction.scale == 1f && clicks.get() == index + 1
                }
            }

            // Interrupt the return animation with another real press.
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            waitUntil(timeoutMillis = 5_000) { interaction.scale < 0.982f }
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
            waitUntil(timeoutMillis = 5_000) { clicks.get() == 4 && interaction.scale > 0.986f }
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            waitUntil(timeoutMillis = 5_000) { interaction.scale < 0.982f }
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
            waitUntil(timeoutMillis = 5_000) { interaction.scale == 1f && clicks.get() == 5 }
            assertEquals(5, clicks.get())
        } catch (error: Throwable) {
            throw AssertionError(
                "Glass interaction failed: scale=${interaction.scale}, clicks=${clicks.get()}, " +
                    "focused=${window.isFocused}",
                error
            )
        } finally {
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
        }
    }

    @Test
    fun draggingFollowsThePointerUntilAnotherGestureClaimsIt() = runDesktopComposeUiTest {
        lateinit var window: ComposeWindow
        lateinit var interaction: GlassInteractionState
        val claimDrag = mutableStateOf(false)
        val drags = AtomicInteger()

        setContent {
            Window(
                onCloseRequest = {},
                title = "Glass drag",
                undecorated = true,
                alwaysOnTop = true,
                state = rememberWindowState(
                    position = WindowPosition.Absolute(500.dp, 100.dp),
                    size = DpSize(320.dp, 320.dp)
                )
            ) {
                val source = rememberGlassSource()
                val state = rememberGlassInteractionState()
                SideEffect {
                    window = this.window
                    interaction = state
                }
                SaltTheme {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                // Stands in for a scrollable that may claim the drag.
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        awaitFirstDown(requireUnconsumed = false)
                                        while (true) {
                                            val change = awaitPointerEvent()
                                                .changes
                                                .firstOrNull { it.pressed }
                                                ?: break
                                            drags.incrementAndGet()
                                            if (claimDrag.value) {
                                                change.consume()
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                Modifier
                                    .size(120.dp)
                                    .testTag("glass")
                                    .glass(
                                        source = source,
                                        effect = GlassEffect.Solid(),
                                        interactionState = state
                                    )
                            )
                        }
                    }
                }
            }
        }

        waitForIdle()
        runOnUiThread {
            window.toFront()
            window.requestFocus()
        }
        waitUntil(timeoutMillis = 5_000) { window.isFocused && window.isShowing }
        val center = onNodeWithTag("glass").fetchSemanticsNode().boundsInRoot.center
        val transform = window.graphicsConfiguration.defaultTransform
        val startX = window.x + (center.x / transform.scaleX).roundToInt()
        val startY = window.y + (center.y / transform.scaleY).roundToInt()

        val robot = Robot().apply { autoDelay = 30 }
        robot.mouseMove(startX, startY)

        try {
            // Uncontested drags keep following the pointer.
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            waitUntil(timeoutMillis = 5_000) { interaction.scale < 0.995f }
            robot.mouseMove(startX + 40, startY)
            robot.mouseMove(startX + 80, startY)
            waitUntil(timeoutMillis = 5_000) { interaction.translation.getDistance() > 1f }
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
            waitUntil(timeoutMillis = 5_000) { interaction.scale == 1f }

            // A claimed drag must end the press instead of dragging the surface along.
            claimDrag.value = true
            val dragsBefore = drags.get()
            robot.mouseMove(startX, startY)
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            waitUntil(timeoutMillis = 5_000) { interaction.scale < 0.995f }
            robot.mouseMove(startX + 40, startY)
            robot.mouseMove(startX + 80, startY)
            waitUntil(timeoutMillis = 5_000) { drags.get() > dragsBefore }
            waitUntil(timeoutMillis = 5_000) {
                interaction.scale == 1f && interaction.translation == Offset.Zero
            }
        } catch (error: Throwable) {
            throw AssertionError(
                "Glass drag handling changed: scale=${interaction.scale}, " +
                    "translation=${interaction.translation}, drags=${drags.get()}",
                error
            )
        } finally {
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
        }
    }
}
