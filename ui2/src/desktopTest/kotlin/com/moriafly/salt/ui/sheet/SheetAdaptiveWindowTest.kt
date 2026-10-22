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

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.moriafly.salt.adaptive.window.DeviceFormFactor
import com.moriafly.salt.adaptive.window.ProvideSaltAdaptiveWindow
import com.moriafly.salt.adaptive.window.SaltAdaptiveWindow
import com.moriafly.salt.adaptive.window.WindowSizeClass
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Exercises automatic window scoping with real SaltWindow instances and native input.
 */
class SheetAdaptiveWindowTest {
    @Test
    fun autoDisplayModeFollowsWindowDimensions() = verifyDisplayModeAcrossNativeSizes(
        sizes = listOf(
            DpSize(500.dp, 700.dp) to true,
            DpSize(599.dp, 480.dp) to true,
            DpSize(600.dp, 479.dp) to true,
            DpSize(600.dp, 480.dp) to false,
            DpSize(900.dp, 400.dp) to true
        )
    )

    @Test
    fun explicitFloatingIgnoresWindowDimensions() = verifyDisplayModeAcrossNativeSizes(
        sizes = listOf(
            DpSize(500.dp, 700.dp) to false,
            DpSize(900.dp, 400.dp) to false
        ),
        displayMode = SheetDisplayMode.Floating
    )

    @Test
    fun explicitBottomIgnoresWindowDimensions() = verifyDisplayModeAcrossNativeSizes(
        sizes = listOf(
            DpSize(500.dp, 700.dp) to true,
            DpSize(900.dp, 400.dp) to true
        ),
        displayMode = SheetDisplayMode.Bottom
    )

    private fun verifyDisplayModeAcrossNativeSizes(
        sizes: List<Pair<DpSize, Boolean>>,
        displayMode: SheetDisplayMode = SheetDisplayMode.Auto
    ) = runDesktopComposeUiTest {
        val fixture = SheetFixture(
            displayMode = displayMode,
            initialWindowSize = sizes.first().first,
            adaptiveControls = true,
            adaptiveSizes = sizes.drop(1).map { it.first },
            undecorated = true
        )
        setContent { fixture.Content() }
        val robot = fixture.activate()
        try {
            fixture.click(robot, "Open")
            for ((index, size) in sizes.withIndex()) {
                if (index > 0) fixture.click(robot, "Next window size")
                waitUntil(timeoutMillis = 5_000) {
                    fixture.window.width == size.first.width.value.roundToInt() &&
                        fixture.window.height == size.first.height.value.roundToInt()
                }
                val bottom = when (displayMode) {
                    SheetDisplayMode.Auto -> size.second
                    SheetDisplayMode.Bottom -> true
                    SheetDisplayMode.Floating -> false
                }
                if (bottom) assertBottom(fixture) else assertCentered(fixture)
                fixture.click(robot, "Inside")
            }
            runOnIdle {
                assertEquals(sizes.size, fixture.insideClicks)
                assertEquals(0, fixture.requests)
                assertTrue(fixture.visible)
            }
            fixture.click(robot, "Program close")
            fixture.click(robot, "Background")
            runOnIdle { assertEquals(1, fixture.backgroundClicks) }
        } finally {
            fixture.restorePointer(robot)
        }
    }

    @Test
    fun anExplicitWindowSizeOverrideIsRetainedInsideTheModalHost() = runDesktopComposeUiTest {
        val fixture = adaptiveFixture()
        val adaptiveWindow = SaltAdaptiveWindow(
            formFactor = DeviceFormFactor.Phone,
            sizeClass = WindowSizeClass.Expanded,
            windowSize = DpSize(900.dp, 700.dp)
        )
        setContent {
            ProvideSaltAdaptiveWindow(adaptiveWindow) { fixture.Content() }
        }
        val robot = fixture.activate()
        fixture.click(robot, "Open")
        waitForIdle()
        onNodeWithText("Window class: Expanded").assertIsDisplayed()
        assertCentered(fixture)
        fixture.click(robot, "Inside")
        runOnIdle {
            assertEquals(1, fixture.insideClicks)
            assertEquals(0, fixture.requests)
        }
        fixture.click(robot, "Program close")
        fixture.click(robot, "Background")
        runOnIdle { assertEquals(1, fixture.backgroundClicks) }
        fixture.restorePointer(robot)
    }

    private fun adaptiveFixture() = SheetFixture(
        displayMode = SheetDisplayMode.Auto,
        initialWindowSize = DpSize(500.dp, 700.dp),
        adaptiveControls = true
    )

    private fun ComposeUiTest.assertBottom(fixture: SheetFixture) {
        val bounds = waitForPanelGeometry(fixture = fixture, bottom = true)
        val owner = bounds.getValue("owner")
        val panel = bounds.getValue("panel")
        assertEquals(owner.bottom, panel.bottom, 2f)
        assertEquals(owner.center.x, panel.center.x, 2f)
    }

    private fun ComposeUiTest.assertCentered(fixture: SheetFixture) {
        val bounds = waitForPanelGeometry(fixture = fixture, bottom = false)
        val owner = bounds.getValue("owner")
        val panel = bounds.getValue("panel")
        assertEquals(owner.center.x, panel.center.x, 2f)
        assertEquals(owner.center.y, panel.center.y, 2f)
        assertTrue(panel.bottom < owner.bottom)
    }

    private fun ComposeUiTest.waitForPanelGeometry(
        fixture: SheetFixture,
        bottom: Boolean
    ): Map<String, Rect> {
        var settledBounds = emptyMap<String, Rect>()
        // The real window has its own frame clock, independent of the test harness's idle state.
        waitUntil(timeoutMillis = 5_000) {
            val bounds = fixture.bounds
            val owner = bounds["owner"] ?: return@waitUntil false
            val panel = bounds["panel"] ?: return@waitUntil false
            val transform = fixture.window.graphicsConfiguration.defaultTransform
            val ownerSizeIsCurrent =
                abs(owner.right + owner.left - fixture.window.width * transform.scaleX) <= 2f &&
                    abs(owner.bottom + owner.top - fixture.window.height * transform.scaleY) <= 2f
            val alignedVertically = if (bottom) {
                abs(owner.bottom - panel.bottom) <= 2f
            } else {
                abs(owner.center.y - panel.center.y) <= 2f && panel.bottom < owner.bottom
            }
            val aligned = ownerSizeIsCurrent && panel.width > 0f &&
                abs(owner.center.x - panel.center.x) <= 2f && alignedVertically
            if (aligned) settledBounds = bounds
            aligned
        }
        return settledBounds
    }
}
