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
    androidx.compose.ui.ExperimentalComposeUiApi::class,
    com.moriafly.salt.adaptive.window.UnstableSaltAdaptiveWindowApi::class,
    com.moriafly.salt.ui.UnstableSaltUiApi::class
)

package com.moriafly.salt.ui.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.adaptive.window.DeviceFormFactor
import com.moriafly.salt.adaptive.window.LocalSaltAdaptiveWindow
import com.moriafly.salt.adaptive.window.ProvideSaltAdaptiveWindow
import com.moriafly.salt.adaptive.window.currentSaltAdaptiveWindow
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.ControlSize
import com.moriafly.salt.ui.LocalControlSize
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.generated.resources.Res
import com.moriafly.salt.ui.generated.resources.sheet_close
import com.moriafly.salt.ui.util.findSkiaLayer
import com.moriafly.salt.ui.window.SaltWindow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Assume.assumeTrue
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Robot
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Every action below uses a visible SaltWindow and native AWT input, never performClick.
 */
class SheetWindowTest {
    @Test
    fun disablingDismissalRemovesTheCloseButtonAndItsHitRegion() = runDesktopComposeUiTest {
        val fixture = SheetFixture()
        setContent { fixture.Content() }
        val robot = fixture.activate()
        fixture.click(robot, "Open")
        val closeButton = sheetCloseButton()
        val region = onNode(closeButton).fetchSemanticsNode().boundsInWindow
        runOnIdle { fixture.dismissible = false }
        waitUntil(timeoutMillis = 5_000) {
            onAllNodes(closeButton).fetchSemanticsNodes().isEmpty()
        }
        onNode(closeButton).assertDoesNotExist()
        val pixelStep = fixture.window.graphicsConfiguration.defaultTransform.scaleX.toFloat()
        val left = ceil(region.left / pixelStep) * pixelStep
        val right = ceil(region.right / pixelStep) * pixelStep
        for (x in listOf(left - pixelStep, left, region.center.x, right, right + pixelStep)) {
            fixture.clickPoint(robot, x, region.center.y)
            waitForIdle()
            runOnIdle {
                assertEquals(0, fixture.requests)
                assertEquals(0, fixture.backgroundClicks)
                assertTrue(fixture.visible)
            }
        }
        fixture.click(robot, "Inside")
        runOnIdle { assertEquals(1, fixture.insideClicks) }
        runOnIdle { fixture.dismissible = true }
        waitUntil(timeoutMillis = 5_000) {
            onAllNodes(closeButton).fetchSemanticsNodes().size == 1
        }
        onNode(closeButton).assertIsDisplayed()
        fixture.clickPoint(robot, region.center.x, region.center.y)
        waitForIdle()
        runOnIdle {
            assertEquals(1, fixture.requests)
            assertFalse(fixture.visible)
        }
        fixture.restorePointer(robot)
    }

    @Test
    fun readingPaddingKeepsTheDefaultViewportAndListInputUsable() = runDesktopComposeUiTest {
        val fixture =
            SheetFixture(bottom = true, list = true, noActions = true, layoutControls = true)
        setContent { fixture.Content() }
        val robot = fixture.activate()
        fixture.click(robot, "Open")
        val before = fixture.bounds.getValue("list")
        fixture.click(robot, "Padding toggle")
        waitForIdle()
        onNodeWithText("Padding: 0.0.dp").assertIsDisplayed()
        assertEquals(before, fixture.bounds.getValue("list"))
        fixture.click(robot, "Inside")
        waitForIdle()
        runOnIdle { assertEquals(1, fixture.insideClicks) }
        fixture.click(robot, "Program close")
        fixture.click(robot, "Background")
        runOnIdle { assertEquals(1, fixture.backgroundClicks) }
        fixture.restorePointer(robot)
    }

    @Test
    fun fastDragAcrossMultipleHeightsSettlesAtTheReleasePosition() = runDesktopComposeUiTest {
        val fixture = SheetFixture(
            bottom = true,
            dismissible = false,
            configuredDetents = listOf(
                SheetDetent.Fraction(fraction = 0.35f),
                SheetDetent.Fraction(fraction = 0.65f),
                SheetDetent.Large
            )
        )
        setContent { fixture.Content() }
        val robot = fixture.activate()
        fixture.click(robot, "Open")
        val lowest = fixture.bounds.getValue("panel")
        robot.autoDelay = 2
        fixture.drag(
            robot = robot,
            x = lowest.center.x,
            y = lowest.top + 24f,
            delta = -lowest.height * 1.75f,
            stepDelay = 4
        )
        waitForIdle()
        val expanded = fixture.bounds.getValue("panel")
        assertTrue(expanded.height > lowest.height * 2.4f)
        fixture.click(robot, "Inside")
        runOnIdle {
            assertEquals(1, fixture.insideClicks)
            assertEquals(0, fixture.requests)
            assertTrue(fixture.visible)
        }
        fixture.click(robot, "Program close")
        fixture.restorePointer(robot)
    }

    @Test
    fun fontScaleChangePreservesAnExpandedSheetAndItsInput() = runDesktopComposeUiTest {
        val fixture = SheetFixture(bottom = true, list = true, layoutControls = true)
        setContent { fixture.Content() }
        val robot = fixture.activate()
        fixture.click(robot, "Open")
        val medium = fixture.bounds.getValue("panel")
        fixture.clickPoint(robot, medium.center.x, medium.top + 24f)
        waitForIdle()
        val expanded = fixture.bounds.getValue("panel")
        fixture.panelTrace.clear()
        fixture.click(robot, "Font scale")
        waitForIdle()
        assertEquals(expanded.height, fixture.bounds.getValue("panel").height, 2f)
        assertTrue(fixture.panelTrace.all { it.height >= expanded.height - 2f })
        fixture.click(robot, "Inside")
        runOnIdle { assertEquals(1, fixture.insideClicks) }
        fixture.click(robot, "Program close")
        fixture.restorePointer(robot)
    }

    @Test
    fun closingSheetReleasesHitBoundariesAndHidesSemantics() = runDesktopComposeUiTest {
        val fixture = SheetFixture()
        setContent { fixture.Content() }
        val robot = fixture.activate()
        var expectedClicks = 0
        for (position in listOf("inside", "boundary", "outside")) {
            fixture.click(robot, "Open")
            robot.autoDelay = 2
            val close = fixture.bounds.getValue("Program close")
            fixture.movePoint(robot, close.center.x, close.center.y)
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
            robot.delay(40)
            val background = fixture.bounds.getValue("Background")
            val pixelWidth = fixture.window.graphicsConfiguration.defaultTransform.scaleX.toFloat()
            val boundary = ceil(background.left / pixelWidth) * pixelWidth
            val x = when (position) {
                "boundary" -> boundary
                "outside" -> boundary - pixelWidth
                else -> background.center.x
            }
            fixture.movePoint(robot, x, background.center.y)
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
            robot.delay(60)
            if (position != "outside") expectedClicks++
            onNodeWithTag("sheet-panel").assertDoesNotExist()
            runOnIdle {
                assertEquals(expectedClicks, fixture.backgroundClicks)
                assertEquals(0, fixture.requests)
            }
            robot.delay(600)
        }
        fixture.restorePointer(robot)
    }

    @Test
    fun dismissalRequestsPreserveModalityUntilBusinessExit() = runDesktopComposeUiTest {
        val fixture = SheetFixture(retainRequest = true)
        setContent { fixture.Content() }
        val robot = fixture.activate()
        fixture.click(robot, "Open")
        waitForIdle()
        onNodeWithTag("sheet-panel").assertIsDisplayed()
        fixture.click(robot, "Inside")
        waitForIdle()
        runOnIdle { assertEquals(1, fixture.insideClicks) }
        fixture.click(robot, "Background")
        waitForIdle()
        runOnIdle {
            assertEquals(1, fixture.requests)
            assertEquals(0, fixture.backgroundClicks)
            assertTrue(fixture.visible)
        }
        robot.pressKey(KeyEvent.VK_ESCAPE)
        waitForIdle()
        runOnIdle {
            assertEquals(2, fixture.requests)
            assertTrue(fixture.visible)
        }
        fixture.click(robot, "Program close")
        waitForIdle()
        onNodeWithTag("sheet-panel").assertDoesNotExist()
        fixture.click(robot, "Background")
        waitForIdle()
        runOnIdle {
            assertEquals(1, fixture.backgroundClicks)
            assertEquals(2, fixture.requests)
        }
        fixture.restorePointer(robot)
    }

    @Test
    fun disabledDismissalAllowsBusinessExit() = runDesktopComposeUiTest {
        val fixture = SheetFixture(dismissible = false)
        setContent { fixture.Content() }
        val robot = fixture.activate()
        fixture.click(robot, "Open")
        waitForIdle()
        onNode(sheetCloseButton()).assertDoesNotExist()
        fixture.click(robot, "Background")
        robot.pressKey(KeyEvent.VK_ESCAPE)
        val panel = fixture.bounds.getValue("panel")
        fixture.clickPoint(robot, panel.right - 40f, panel.top + 32f)
        waitForIdle()
        runOnIdle {
            assertEquals(0, fixture.requests)
            assertEquals(0, fixture.backgroundClicks)
            assertTrue(fixture.visible)
        }
        fixture.click(robot, "Program close")
        waitForIdle()
        fixture.click(robot, "Background")
        waitForIdle()
        runOnIdle {
            assertFalse(fixture.visible)
            assertEquals(1, fixture.backgroundClicks)
        }
        fixture.restorePointer(robot)
    }

    @Test
    fun surfaceEdgesAndRoundedCornersSeparateInteriorFromScrim() = runDesktopComposeUiTest {
        val fixture = SheetFixture(retainRequest = true)
        setContent { fixture.Content() }
        val robot = fixture.activate()
        fixture.click(robot, "Open")
        waitForIdle()
        val panel = fixture.bounds.getValue("panel")
        // Interior, included left edge, excluded right edge, adjacent exterior, clipped corner.
        fixture.clickPoint(robot, panel.center.x, panel.center.y)
        fixture.clickPoint(robot, panel.left, panel.center.y)
        waitForIdle()
        runOnIdle { assertEquals(0, fixture.requests) }
        fixture.clickPoint(robot, panel.right, panel.center.y)
        fixture.clickPoint(robot, panel.left - 1f, panel.center.y)
        fixture.clickPoint(robot, panel.left + 1f, panel.top + 1f)
        waitForIdle()
        runOnIdle {
            assertEquals(3, fixture.requests)
            assertEquals(0, fixture.backgroundClicks)
            assertTrue(fixture.visible)
        }
        fixture.click(robot, "Program close")
        waitForIdle()
        fixture.restorePointer(robot)
    }

    @Test
    fun bottomHandleResizesAndRejectedSwipeRestoresTheVisiblePanel() = runDesktopComposeUiTest {
        val fixture = SheetFixture(bottom = true, retainRequest = true)
        setContent { fixture.Content() }
        val robot = fixture.activate()
        fixture.click(robot, "Open")
        waitForIdle()
        val medium = fixture.bounds.getValue("panel")
        fixture.drag(robot, medium.center.x, medium.top + 36f, -medium.height * 0.75f)
        waitForIdle()
        val large = fixture.bounds.getValue("panel")
        assertTrue(large.height > medium.height + 100f)
        fixture.drag(robot, large.center.x, large.top + 36f, medium.height * 0.75f)
        waitForIdle()
        val restored = fixture.bounds.getValue("panel")
        assertEquals(medium.height, restored.height, 2f)
        fixture.drag(robot, restored.center.x, restored.top + 36f, restored.height * 0.35f)
        waitForIdle()
        runOnIdle {
            assertEquals(1, fixture.requests)
            assertTrue(fixture.visible)
        }
        assertEquals(medium.height, fixture.bounds.getValue("panel").height, 2f)
        fixture.click(robot, "Program close")
        waitForIdle()
        fixture.restorePointer(robot)
    }

    @Test
    fun listWheelPreservesDetentAndReopeningPreservesScroll() = runDesktopComposeUiTest {
        val fixture = SheetFixture(bottom = true, list = true)
        setContent { fixture.Content() }
        val robot = fixture.activate()
        fixture.click(robot, "Open")
        waitForIdle()
        val medium = fixture.bounds.getValue("panel")
        val listBounds = fixture.bounds.getValue("list")
        fixture.capture(robot, "before-wheel")
        fixture.movePoint(robot, listBounds.center.x, listBounds.center.y)
        robot.mouseWheel(8)
        robot.delay(450)
        waitForIdle()
        fixture.capture(robot, "after-wheel")
        assertEquals(medium.height, fixture.bounds.getValue("panel").height, 2f)
        onNodeWithText("Row 0").assertDoesNotExist()
        fixture.click(robot, "Program close")
        waitForIdle()
        fixture.click(robot, "Open")
        waitForIdle()
        onNodeWithText("Row 0").assertDoesNotExist()
        runOnIdle { assertEquals(0, fixture.requests) }
        fixture.click(robot, "Program close")
        waitForIdle()
        fixture.restorePointer(robot)
    }

    @Test
    fun rapidReopeningCancelsOldDisposalAndTabStaysInsideTheModal() = runDesktopComposeUiTest {
        val fixture = SheetFixture()
        setContent { fixture.Content() }
        val robot = fixture.activate()
        fixture.click(robot, "Open")
        waitForIdle()
        fixture.click(robot, "Reopen")
        robot.delay(250)
        waitForIdle()
        onNodeWithTag("sheet-panel").assertIsDisplayed()
        fixture.click(robot, "Inside")
        fixture.focusTrace.clear()
        repeat(12) { robot.pressKey(KeyEvent.VK_TAB) }
        assertTrue(fixture.focusTrace.none { it == "Open" || it == "Background" })
        robot.pressKey(KeyEvent.VK_ENTER)
        robot.delay(650)
        waitForIdle()
        runOnIdle {
            assertEquals(0, fixture.backgroundClicks)
            assertEquals(0, fixture.requests)
        }
        if (fixture.visible) fixture.click(robot, "Program close")
        waitForIdle()
        fixture.click(robot, "Background")
        waitForIdle()
        runOnIdle { assertEquals(1, fixture.backgroundClicks) }
        fixture.restorePointer(robot)
    }

    @Test
    fun ownerResizeKeepsSheetUsableAndOtherWindowAcceptsInput() = runDesktopComposeUiTest {
        val owner = SheetFixture(windowX = 50.dp, resizeWindow = true)
        val other = SheetFixture(windowX = 850.dp)
        setContent {
            owner.Content()
            other.Content()
        }
        val robot = owner.activate()
        owner.click(robot, "Open")
        other.activate()
        other.click(robot, "Background")
        waitForIdle()
        runOnIdle {
            assertEquals(1, other.backgroundClicks)
            assertEquals(0, owner.backgroundClicks)
            assertTrue(owner.visible)
        }
        owner.activate()
        owner.click(robot, "Resize window")
        robot.delay(650)
        waitForIdle()
        assertEquals(580, owner.window.width)
        owner.capture(robot, "resized-owner")
        owner.click(robot, "Inside")
        waitForIdle()
        runOnIdle { assertEquals(1, owner.insideClicks) }
        owner.click(robot, "Program close")
        waitForIdle()
        owner.restorePointer(robot)
    }

    @Test
    fun handleHitRegionHonorsItsBoundaryAndOutsideRequestsDismissal() = runDesktopComposeUiTest {
        val fixture = SheetFixture(bottom = true, retainRequest = true)
        setContent { fixture.Content() }
        val robot = fixture.activate()
        fixture.click(robot, "Open")
        val medium = fixture.bounds.getValue("panel").height
        val pixelSize = fixture.window.graphicsConfiguration.defaultTransform.scaleX.toFloat()

        fun boundary(value: Float) = ceil(value / pixelSize) * pixelSize

        fun handle() = onNode(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.StateDescription)
        ).fetchSemanticsNode().boundsInWindow
        var region = handle()
        fixture.clickPoint(robot, region.center.x, boundary(region.top))
        waitForIdle()
        val large = fixture.bounds.getValue("panel").height
        assertTrue(large > medium)
        region = handle()
        fixture.clickPoint(robot, region.center.x, boundary(region.bottom))
        waitForIdle()
        assertEquals(large, fixture.bounds.getValue("panel").height, 2f)
        fixture.clickPoint(robot, boundary(region.left), boundary(region.bottom) - pixelSize)
        waitForIdle()
        assertEquals(medium, fixture.bounds.getValue("panel").height, 2f)
        region = handle()
        fixture.clickPoint(robot, boundary(region.right), boundary(region.bottom) - pixelSize)
        fixture.clickPoint(
            robot,
            boundary(region.right) + pixelSize,
            boundary(region.bottom) - pixelSize
        )
        waitForIdle()
        assertEquals(medium, fixture.bounds.getValue("panel").height, 2f)
        runOnIdle { assertEquals(0, fixture.requests) }
        fixture.clickPoint(robot, region.center.x, boundary(region.top) - pixelSize)
        waitForIdle()
        runOnIdle {
            assertEquals(1, fixture.requests)
            assertEquals(0, fixture.backgroundClicks)
        }
        fixture.click(robot, "Inside")
        runOnIdle { assertEquals(1, fixture.insideClicks) }
        fixture.click(robot, "Program close")
        fixture.click(robot, "Background")
        runOnIdle { assertEquals(1, fixture.backgroundClicks) }
        fixture.restorePointer(robot)
    }

    @Test
    fun switchingSheetWaitsForExitAndDoesNotRestoreBackgroundInput() = runDesktopComposeUiTest {
        val fixture = SheetFixture(handoff = true)
        setContent { fixture.Content() }
        val robot = fixture.activate()
        fixture.click(robot, "Open")
        fixture.click(robot, "Switch sheet")
        robot.delay(700)
        waitForIdle()
        onNodeWithText("Next sheet").assertIsDisplayed()
        fixture.click(robot, "Next inside")
        waitForIdle()
        runOnIdle {
            assertEquals(1, fixture.insideClicks)
            assertEquals(0, fixture.requests)
            assertEquals(0, fixture.backgroundClicks)
        }
        fixture.click(robot, "Next close")
        fixture.click(robot, "Background")
        waitForIdle()
        runOnIdle { assertEquals(1, fixture.backgroundClicks) }
        fixture.restorePointer(robot)
    }

    @Test
    fun tinyOwnerStillOffersBodyAndBusinessCloseAction() = runDesktopComposeUiTest {
        val fixture = SheetFixture(resizeWindow = true, compact = true)
        setContent { fixture.Content() }
        val robot = fixture.activate()
        fixture.click(robot, "Open")
        fixture.click(robot, "Resize window")
        robot.delay(500)
        waitForIdle()
        fixture.capture(robot, "compact-owner")
        fixture.click(robot, "Inside")
        waitForIdle()
        runOnIdle { assertEquals(1, fixture.insideClicks) }
        fixture.click(robot, "Program close")
        waitForIdle()
        onNodeWithTag("sheet-panel").assertDoesNotExist()
        runOnIdle { assertEquals(0, fixture.requests) }
        fixture.restorePointer(robot)
    }
}

internal class SheetFixture(
    private val retainRequest: Boolean = false,
    dismissible: Boolean = true,
    private val bottom: Boolean = false,
    private val list: Boolean = false,
    private val resizeWindow: Boolean = false,
    private val compact: Boolean = false,
    private val handoff: Boolean = false,
    private val scrollBehavior: SheetScrollBehavior = SheetScrollBehavior.ExpandFirst,
    private val configuredDetents: List<SheetDetent>? = null,
    private val noActions: Boolean = false,
    private val layoutControls: Boolean = false,
    private val windowX: Dp = 100.dp,
    private val displayMode: SheetDisplayMode? = null,
    private val initialWindowSize: DpSize = DpSize(720.dp, 640.dp),
    private val adaptiveControls: Boolean = false,
    private val extraSheetContent: @Composable () -> Unit = {},
    private val formFactor: DeviceFormFactor? = null,
    private val adaptiveSizes: List<DpSize> = emptyList(),
    private val undecorated: Boolean = false
) {
    lateinit var window: ComposeWindow
    private val coordinates = ConcurrentHashMap<String, LayoutCoordinates>()
    // A graphics-layer animation changes these bounds without another positioning callback.
    // Read the live coordinates on AWT's UI thread after native input has settled.
    val bounds: Map<String, Rect>
        get() {
            var rectangles: Map<String, Rect> = emptyMap()
            val readBounds = {
                rectangles = coordinates
                    .filterValues { it.isAttached }
                    .mapValues { it.value.boundsInWindow() }
            }
            if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                readBounds()
            } else {
                javax.swing.SwingUtilities.invokeAndWait(readBounds)
            }
            return rectangles
        }
    var dismissible by mutableStateOf(dismissible)
    var visible by mutableStateOf(false)
    private var nextVisible by mutableStateOf(false)
    private var readPadding by mutableStateOf(false)
    private var fontScale by mutableFloatStateOf(1f)
    private var nextWindowSize = 0
    var requests by mutableIntStateOf(0)
    var insideClicks by mutableIntStateOf(0)
    var backgroundClicks by mutableIntStateOf(0)
    val focusTrace = CopyOnWriteArrayList<String>()
    val panelTrace = CopyOnWriteArrayList<Rect>()
    private val previousPointer: Point = run {
        assumeTrue(
            "Sheet interaction tests require a desktop display",
            !java.awt.GraphicsEnvironment.isHeadless()
        )
        MouseInfo.getPointerInfo().location
    }

    @Composable
    fun Content() {
        SaltTheme {
            val windowState = rememberWindowState(
                size = initialWindowSize,
                position = WindowPosition.Absolute(windowX, 100.dp)
            )
            SaltWindow(
                onCloseRequest = {},
                state = windowState,
                decoration = if (undecorated) {
                    WindowDecoration.Undecorated()
                } else {
                    WindowDecoration.SystemDefault
                },
                alwaysOnTop = true,
                title = "SaltUI Sheet real input test",
                init = { window = it }
            ) {
                if (formFactor == null) {
                    WindowContent(windowState)
                } else {
                    ProvideSaltAdaptiveWindow(
                        currentSaltAdaptiveWindow().copy(formFactor = formFactor)
                    ) {
                        WindowContent(windowState)
                    }
                }
            }
        }
    }

    @Composable
    private fun WindowContent(windowState: WindowState) {
        PrimaryContent(windowState)
        if (handoff) NextSheet()
    }

    @Composable
    private fun PrimaryContent(windowState: WindowState) {
        val density = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(density.density, density.fontScale * fontScale)
        ) {
            if (adaptiveControls) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(probe("owner"))
                )
            }
            BackgroundContent()
            PrimarySheet(windowState)
        }
    }

    @Composable
    private fun BackgroundContent() {
        Column(
            modifier = Modifier
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FixtureButton("Open") { visible = true }
            FixtureButton("Background") { backgroundClicks++ }
            Text("Background clicks: $backgroundClicks")
        }
    }

    @Composable
    private fun PrimarySheet(windowState: WindowState) {
        val scope = rememberCoroutineScope()
        Sheet(
            visible = visible,
            onDismissRequest = {
                requests++
                if (!retainRequest) visible = false
            },
            title = "Sheet test",
            modifier = probe("panel").testTag("sheet-panel"),
            properties = sheetProperties(),
            actions = {
                if (!noActions) FixtureButton("Program close") { visible = false }
            }
        ) {
            PrimarySheetContent(windowState) {
                scope.launch {
                    visible = false
                    delay(80)
                    visible = true
                }
            }
            extraSheetContent()
        }
    }

    private fun sheetProperties() = SheetProperties(
        displayMode = displayMode ?: if (bottom) {
            SheetDisplayMode.Bottom
        } else {
            SheetDisplayMode.Floating
        },
        dismissible = dismissible,
        scrollBehavior = scrollBehavior,
        detents = configuredDetents ?: if (bottom) {
            listOf(SheetDetent.Medium, SheetDetent.Large)
        } else {
            listOf(SheetDetent.Content)
        }
    )

    @Composable
    private fun SheetScope.PrimarySheetContent(
        windowState: WindowState,
        onReopen: () -> Unit
    ) {
        if (list) {
            ListContent(contentPadding)
        } else {
            ButtonContent(windowState = windowState, onReopen = onReopen)
        }
    }

    @Composable
    private fun ListContent(contentPadding: PaddingValues) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .then(probe("list")),
            state = rememberLazyListState()
        ) {
            if (layoutControls) {
                item { ListControls(contentPadding) }
            }
            items(100) { Text("Row $it", Modifier.padding(12.dp)) }
        }
    }

    @Composable
    private fun ListControls(contentPadding: PaddingValues) {
        val paddingText = if (readPadding) {
            "Padding: ${contentPadding.calculateBottomPadding()}"
        } else {
            "Read padding"
        }
        FixtureButton(text = paddingText, key = "Padding toggle") { readPadding = !readPadding }
        FixtureButton(text = "Change font scale", key = "Font scale") { fontScale += 0.2f }
        FixtureButton("Inside") { insideClicks++ }
        if (noActions) FixtureButton("Program close") { visible = false }
    }

    @Composable
    private fun ButtonContent(
        windowState: WindowState,
        onReopen: () -> Unit
    ) {
        FixtureButton("Inside") { insideClicks++ }
        if (!adaptiveControls) {
            FixtureButton(text = "Reopen", onClick = onReopen)
            Text("Inside clicks: $insideClicks")
        }
        if (adaptiveControls) AdaptiveWindowControls(windowState)
        if (resizeWindow) {
            FixtureButton("Resize window") {
                windowState.size = DpSize(580.dp, if (compact) 200.dp else 400.dp)
            }
        }
        if (handoff) {
            FixtureButton("Switch sheet") {
                visible = false
                nextVisible = true
            }
        }
    }

    @Composable
    private fun AdaptiveWindowControls(windowState: WindowState) {
        Text("Window class: ${LocalSaltAdaptiveWindow.current.sizeClass}")
        if (adaptiveSizes.isEmpty()) {
            FixtureButton("Portrait window") { windowState.size = DpSize(500.dp, 700.dp) }
            FixtureButton("Landscape window") { windowState.size = DpSize(580.dp, 400.dp) }
            FixtureButton("Wide landscape window") { windowState.size = DpSize(900.dp, 400.dp) }
        } else {
            FixtureButton("Next window size") {
                windowState.size = adaptiveSizes[nextWindowSize]
                nextWindowSize = (nextWindowSize + 1) % adaptiveSizes.size
            }
        }
    }

    @Composable
    private fun NextSheet() {
        Sheet(
            visible = nextVisible,
            onDismissRequest = { nextVisible = false },
            title = "Next sheet"
        ) {
            FixtureButton("Next inside") { insideClicks++ }
            FixtureButton("Next close") { nextVisible = false }
        }
    }

    @Composable
    private fun FixtureButton(
        text: String,
        key: String = text,
        onClick: () -> Unit
    ) {
        Button(
            onClick = onClick,
            text = text,
            modifier = probe(key),
            size = if (adaptiveControls) ControlSize.Small else LocalControlSize.current
        )
    }

    private fun probe(key: String) = Modifier
        .onGloballyPositioned {
            coordinates[key] = it
            val rectangle = it.boundsInWindow()
            if (key == "panel") panelTrace += rectangle
        }
        .onFocusChanged { if (it.isFocused) focusTrace += key }

    fun activate(): Robot = Robot().apply {
        autoDelay = 60
        javax.swing.SwingUtilities.invokeAndWait {
            window.isAlwaysOnTop = true
            window.toFront()
            window.requestFocus()
        }
        delay(200)
    }

    fun click(robot: Robot, key: String) {
        val rect = bounds.getValue(key)
        clickPoint(robot, rect.center.x, rect.center.y)
    }

    fun movePoint(robot: Robot, x: Float, y: Float) {
        val layer = window.findSkiaLayer()!!
        val origin = layer.locationOnScreen
        val transform = window.graphicsConfiguration.defaultTransform
        robot.mouseMove(
            origin.x + (x / transform.scaleX).roundToInt(),
            origin.y + (y / transform.scaleY).roundToInt()
        )
    }

    fun clickPoint(robot: Robot, x: Float, y: Float) {
        movePoint(robot, x, y)
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
        // These are real top-level windows with a real frame clock, outside the harness clock.
        robot.delay(650)
    }

    fun drag(
        robot: Robot,
        x: Float,
        y: Float,
        delta: Float,
        stepDelay: Int = 20,
        releaseDelay: Int = 0
    ) {
        movePoint(robot, x, y)
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
        repeat(16) {
            movePoint(robot, x, y + delta * (it + 1) / 16)
            robot.delay(stepDelay)
        }
        if (releaseDelay > 0) robot.delay(releaseDelay)
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
        robot.delay(650)
    }

    fun restorePointer(robot: Robot) {
        robot.mouseMove(previousPointer.x, previousPointer.y)
    }

    fun capture(robot: Robot, name: String) {
        val directory = java.io.File("build/reports/sheet").apply { mkdirs() }
        javax.imageio.ImageIO.write(
            robot.createScreenCapture(window.bounds),
            "png",
            java.io.File(directory, "$name.png")
        )
    }
}

private fun Robot.pressKey(key: Int) {
    keyPress(key)
    keyRelease(key)
}

private fun sheetCloseButton() = hasContentDescription(
    runBlocking { getString(Res.string.sheet_close) }
)
