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

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.moriafly.salt.adaptive.window.DeviceFormFactor
import com.moriafly.salt.adaptive.window.ProvideSaltAdaptiveWindow
import com.moriafly.salt.adaptive.window.SaltAdaptiveWindow
import com.moriafly.salt.adaptive.window.WindowSizeClass
import com.moriafly.salt.ui.Button
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.math.abs

/**
 * Exercises automatic window scoping with real SaltWindow instances and native input.
 */
class SheetAdaptiveWindowTest {
    @Test
    fun desktopWindowsAdaptToTheirDimensionsWithoutAProvider() = verifyDisplayModeAcrossNativeSizes(
        formFactor = null
    )

    @Test
    fun phoneWindowsAdaptAcrossBothDimensionBreakpoints() = verifyDisplayModeAcrossNativeSizes(
        formFactor = DeviceFormFactor.Phone
    )

    @Test
    fun tabletWindowsAdaptAcrossBothDimensionBreakpoints() = verifyDisplayModeAcrossNativeSizes(
        formFactor = DeviceFormFactor.Tablet
    )

    @Test
    fun explicitFloatingOverridesSmallWindowDimensions() = verifyDisplayModeAcrossNativeSizes(
        formFactor = DeviceFormFactor.Phone,
        displayMode = SheetDisplayMode.Floating
    )

    @Test
    fun explicitBottomOverridesLargeWindowDimensions() = verifyDisplayModeAcrossNativeSizes(
        formFactor = DeviceFormFactor.Tablet,
        displayMode = SheetDisplayMode.Bottom
    )

    private fun verifyDisplayModeAcrossNativeSizes(
        formFactor: DeviceFormFactor?,
        displayMode: SheetDisplayMode = SheetDisplayMode.Auto
    ) = runDesktopComposeUiTest {
        val sizes = listOf(
            DpSize(500.dp, 700.dp) to true,
            DpSize(599.dp, 480.dp) to true,
            DpSize(600.dp, 479.dp) to true,
            DpSize(600.dp, 480.dp) to false,
            DpSize(601.dp, 481.dp) to false,
            DpSize(900.dp, 400.dp) to true,
            DpSize(500.dp, 700.dp) to true
        )
        val fixture = SheetFixture(
            displayMode = displayMode,
            initialWindowSize = sizes.first().first,
            adaptiveControls = true,
            formFactor = formFactor,
            adaptiveSizes = sizes.drop(1).map { it.first },
            undecorated = true
        )
        setContent { fixture.Content() }
        val robot = fixture.activate()
        try {
            fixture.click(robot, "Open")
            for ((index, size) in sizes.withIndex()) {
                if (index > 0) fixture.click(robot, "Next window size")
                waitForIdle()
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
    fun separateOwnersUseTheirOwnWindowMetricsWithoutProviders() = runDesktopComposeUiTest {
        val owner = adaptiveFixture(windowX = 50.dp)
        val other = SheetFixture(
            windowX = 650.dp,
            displayMode = SheetDisplayMode.Auto,
            initialWindowSize = DpSize(700.dp, 520.dp),
            adaptiveControls = true
        )
        setContent {
            owner.Content()
            other.Content()
        }
        val robot = owner.activate()
        owner.click(robot, "Open")
        other.activate()
        other.click(robot, "Open")
        waitForIdle()
        onNodeWithText("Window class: Compact").assertIsDisplayed()
        onNodeWithText("Window class: Medium").assertIsDisplayed()
        assertBottom(owner)
        assertCentered(other)
        val otherPanel = other.bounds.getValue("panel")
        owner.activate()
        owner.click(robot, "Landscape window")
        waitForIdle()
        assertBottom(owner)
        assertEquals(otherPanel, other.bounds.getValue("panel"))
        owner.click(robot, "Inside")
        other.activate()
        other.click(robot, "Inside")
        runOnIdle {
            assertEquals(1, owner.insideClicks)
            assertEquals(1, other.insideClicks)
            assertEquals(0, owner.requests)
            assertEquals(0, other.requests)
        }
        other.click(robot, "Program close")
        owner.activate()
        owner.click(robot, "Program close")
        owner.restorePointer(robot)
    }

    @Test
    fun aWindowOpenedInsideTheSheetUsesItsOwnMetricsAndInput() = runDesktopComposeUiTest {
        val nested = SheetFixture(
            windowX = 650.dp,
            displayMode = SheetDisplayMode.Auto,
            initialWindowSize = DpSize(700.dp, 520.dp),
            adaptiveControls = true
        )
        val owner = nestedWindowOwner(nested)
        setContent { owner.Content() }
        val robot = owner.activate()
        try {
            owner.click(robot, "Open")
            val open = onNodeWithText("Open nested window").fetchSemanticsNode().boundsInWindow
            owner.clickPoint(robot, open.center.x, open.center.y)
            waitForIdle()
            nested.activate()
            nested.click(robot, "Open")
            waitForIdle()
            onNodeWithText("Window class: Compact").assertIsDisplayed()
            onNodeWithText("Window class: Medium").assertIsDisplayed()
            assertBottom(owner)
            assertCentered(nested)
            val ownerPanel = owner.bounds.getValue("panel")
            nested.click(robot, "Landscape window")
            waitForIdle()
            assertBottom(nested)
            assertEquals(ownerPanel, owner.bounds.getValue("panel"))
            nested.click(robot, "Portrait window")
            waitForIdle()
            assertBottom(nested)
            assertBottom(owner)
            nested.click(robot, "Inside")
            nested.click(robot, "Program close")
            nested.click(robot, "Background")
            owner.activate()
            owner.click(robot, "Inside")
            owner.click(robot, "Program close")
            owner.click(robot, "Background")
            runOnIdle {
                assertEquals(1, owner.insideClicks)
                assertEquals(1, nested.insideClicks)
                assertEquals(1, owner.backgroundClicks)
                assertEquals(1, nested.backgroundClicks)
                assertEquals(0, owner.requests)
                assertEquals(0, nested.requests)
            }
        } finally {
            owner.restorePointer(robot)
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

    @Test
    fun adaptiveInformationWithoutDimensionsUsesTheOwnerWindowSize() = runDesktopComposeUiTest {
        val fixture = adaptiveFixture()
        val adaptiveWindow = SaltAdaptiveWindow(
            formFactor = DeviceFormFactor.Tablet,
            sizeClass = WindowSizeClass.Expanded
        )
        setContent {
            ProvideSaltAdaptiveWindow(adaptiveWindow) { fixture.Content() }
        }
        val robot = fixture.activate()
        try {
            fixture.click(robot, "Open")
            waitForIdle()
            onNodeWithText("Window class: Expanded").assertIsDisplayed()
            assertBottom(fixture)
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

    private fun adaptiveFixture(windowX: Dp = 100.dp) = SheetFixture(
        windowX = windowX,
        displayMode = SheetDisplayMode.Auto,
        initialWindowSize = DpSize(500.dp, 700.dp),
        adaptiveControls = true
    )

    private fun nestedWindowOwner(nested: SheetFixture): SheetFixture {
        val open = mutableStateOf(false)
        return SheetFixture(
            windowX = 50.dp,
            displayMode = SheetDisplayMode.Auto,
            initialWindowSize = DpSize(500.dp, 700.dp),
            adaptiveControls = true,
            extraSheetContent = {
                Button(onClick = { open.value = true }, text = "Open nested window")
                if (open.value) nested.Content()
            }
        )
    }

    private fun ComposeUiTest.assertBottom(fixture: SheetFixture) {
        waitForPanelGeometry(fixture = fixture, bottom = true)
        val owner = fixture.bounds.getValue("owner")
        val panel = fixture.bounds.getValue("panel")
        assertEquals(owner.bottom, panel.bottom, 2f)
        assertEquals(owner.center.x, panel.center.x, 2f)
    }

    private fun ComposeUiTest.assertCentered(fixture: SheetFixture) {
        waitForPanelGeometry(fixture = fixture, bottom = false)
        val owner = fixture.bounds.getValue("owner")
        val panel = fixture.bounds.getValue("panel")
        assertEquals(owner.center.x, panel.center.x, 2f)
        assertEquals(owner.center.y, panel.center.y, 2f)
        assertTrue(panel.bottom < owner.bottom)
    }

    private fun ComposeUiTest.waitForPanelGeometry(fixture: SheetFixture, bottom: Boolean) {
        // The real window has its own frame clock, independent of the test harness's idle state.
        waitUntil(timeoutMillis = 5_000) {
            val bounds = fixture.bounds
            val owner = bounds["owner"] ?: return@waitUntil false
            val panel = bounds["panel"] ?: return@waitUntil false
            val alignedVertically = if (bottom) {
                abs(owner.bottom - panel.bottom) <= 2f
            } else {
                abs(owner.center.y - panel.center.y) <= 2f && panel.bottom < owner.bottom
            }
            panel.width > 0f && abs(owner.center.x - panel.center.x) <= 2f && alignedVertically
        }
    }
}
