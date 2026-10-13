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

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import kotlin.math.ceil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Checks motion and pending presentation through native window input and final actions.
 */
class SheetMotionWindowTest {
    @Test
    fun slowAndFastHandleDragsToTheSamePositionChooseDifferentHeights() =
        runDesktopComposeUiTest {
            val fixture = SheetFixture(bottom = true)
            setContent { fixture.Content() }
            val robot = fixture.activate()
            try {
                fixture.click(robot, "Open")
                val medium = fixture.bounds.getValue("panel")
                robot.autoDelay = 2
                fixture.drag(
                    robot = robot,
                    x = medium.center.x,
                    y = medium.top + 24f,
                    delta = -medium.height * 0.3f,
                    stepDelay = 30
                )
                waitForIdle()
                assertEquals(medium.height, fixture.bounds.getValue("panel").height, 2f)
                fixture.drag(
                    robot = robot,
                    x = medium.center.x,
                    y = medium.top + 24f,
                    delta = -medium.height * 0.3f,
                    stepDelay = 1
                )
                waitForIdle()
                assertTrue(fixture.bounds.getValue("panel").height > medium.height * 1.5f)
                fixture.click(robot, "Inside")
                runOnIdle {
                    assertEquals(1, fixture.insideClicks)
                    assertEquals(0, fixture.requests)
                }
                fixture.click(robot, "Program close")
                fixture.click(robot, "Background")
                runOnIdle { assertEquals(1, fixture.backgroundClicks) }
            } finally {
                fixture.restorePointer(robot)
            }
        }

    @Test
    fun holdingAfterAFastDragDoesNotFlingWhenReleased() = runDesktopComposeUiTest {
        val fixture = SheetFixture(bottom = true)
        setContent { fixture.Content() }
        val robot = fixture.activate()
        try {
            fixture.click(robot, "Open")
            val medium = fixture.bounds.getValue("panel")
            robot.autoDelay = 2
            fixture.drag(
                robot = robot,
                x = medium.center.x,
                y = medium.top + 24f,
                delta = -medium.height * 0.3f,
                stepDelay = 1,
                releaseDelay = 200
            )
            waitForIdle()
            assertEquals(medium.height, fixture.bounds.getValue("panel").height, 2f)
            fixture.click(robot, "Inside")
            runOnIdle {
                assertEquals(1, fixture.insideClicks)
                assertEquals(0, fixture.requests)
            }
            fixture.click(robot, "Program close")
        } finally {
            fixture.restorePointer(robot)
        }
    }

    @Test
    fun pendingHandoffBlocksBackgroundBoundariesAndKeyboardInput() = runDesktopComposeUiTest {
        val fixture = SheetFixture(handoff = true)
        setContent { fixture.Content() }
        val robot = fixture.activate()
        try {
            for ((index, position) in listOf("inside", "boundary", "outside").withIndex()) {
                fixture.click(robot, "Open")
                val background = fixture.bounds.getValue("Background")
                val switch = fixture.bounds.getValue("Switch sheet")
                val pixelSize = fixture.window.graphicsConfiguration.defaultTransform.scaleX
                    .toFloat()
                val edge = ceil(background.left / pixelSize) * pixelSize
                val x = when (position) {
                    "boundary" -> edge
                    "outside" -> edge - pixelSize
                    else -> background.center.x
                }
                robot.autoDelay = 2
                fixture.movePoint(robot, switch.center.x, switch.center.y)
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                robot.delay(40)
                // Query the UI only after injection, so synchronization cannot skip the handoff.
                fixture.movePoint(robot, x, background.center.y)
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                robot.keyPress(KeyEvent.VK_ENTER)
                robot.keyRelease(KeyEvent.VK_ENTER)
                robot.delay(650)
                waitForIdle()
                onNodeWithText("Next sheet").assertIsDisplayed()
                fixture.click(robot, "Next inside")
                runOnIdle {
                    assertFalse(fixture.visible)
                    assertEquals(index + 1, fixture.insideClicks)
                    assertEquals(0, fixture.backgroundClicks)
                    assertEquals(0, fixture.requests)
                }
                fixture.click(robot, "Next close")
            }
            fixture.click(robot, "Background")
            runOnIdle { assertEquals(1, fixture.backgroundClicks) }
        } finally {
            fixture.restorePointer(robot)
        }
    }
}
