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

@file:OptIn(com.moriafly.salt.ui.UnstableSaltUiApi::class)

package com.moriafly.salt.sample.ui

import android.graphics.Point
import android.os.Build
import android.os.SystemClock
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.ItemButton
import com.moriafly.salt.ui.ItemEdit
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.sheet.Sheet
import com.moriafly.salt.ui.sheet.SheetContentLayout
import com.moriafly.salt.ui.sheet.SheetDetent
import com.moriafly.salt.ui.sheet.SheetPresentation
import com.moriafly.salt.ui.sheet.SheetProperties
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

/**
 * Real Activity, dialog, IME, touchscreen swipes and system key events.
 */
@RunWith(AndroidJUnit4::class)
class SheetInteractionTest {
    private val device get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Test
    fun noActionListDrawsToTheBottomAndItsLastButtonScrollsIntoTheSafeArea() {
        val fixture = AndroidSheetFixture(list = true, noActions = true, edgeToEdge = true)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.setContent { fixture.Content() } }
            click(By.text("Open sheet"))
            val body = fixture.body.get()
            assertEquals(device.displayHeight.toFloat(), body.bottom, 2f)
            repeat(18) {
                if (!device.hasObject(By.text("Select last item"))) {
                    device.swipe(
                        body.center.x.toInt(),
                        (body.bottom - 100).toInt(),
                        body.center.x.toInt(),
                        (body.top + 20).toInt(),
                        10
                    )
                }
            }
            shown(By.text("Select last item"))
            device.swipe(
                body.center.x.toInt(),
                (body.bottom - 100).toInt(),
                body.center.x.toInt(),
                (body.top + 20).toInt(),
                20
            )
            device.waitForIdle()
            val last = device.findObject(By.text("Select last item")).visibleBounds
            assertTrue(last.bottom < device.displayHeight - fixture.navigationInset)
            click(By.text("Select last item"))
            shown(By.text("Last item selected"))
            scenario.onActivity {
                assertEquals(1, fixture.insideClicks)
                assertEquals(0, fixture.requests)
            }
            device.pressBack()
            shown(By.text("Open sheet"))
            click(By.text("Background"))
            shown(By.text("Background: 1"))
        }
    }

    @Test
    fun programmaticCloseWithTheImeOpenKeepsBackgroundInputAndRemovesSheetActions() {
        val fixture = AndroidSheetFixture(form = true)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.setContent { fixture.Content() } }
            click(By.text("Open sheet"))
            click(By.clazz("android.widget.EditText"))
            device.pressKeyCode(KeyEvent.KEYCODE_A)
            val close = device.findObject(By.text("Program close")).visibleBounds
            device.click(close.centerX(), close.centerY())
            SystemClock.sleep(60)
            if (Build.VERSION.SDK_INT >= 33) {
                InstrumentationRegistry.getInstrumentation().uiAutomation.clearCache()
            }
            assertTrue(!device.hasObject(By.text("Program close")))
            click(By.text("Background"))
            shown(By.text("Background: 1"))
            click(By.text("Open sheet"))
            shown(By.text("Fixture sheet"))
            scenario.onActivity { assertEquals(0, fixture.requests) }
            click(By.text("Program close"))
        }
    }

    @Test
    fun edgeToEdgeScrimDimsStatusBarAndKeepsActionsInTheSafeArea() {
        val fixture = AndroidSheetFixture()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.setContent { fixture.Content() } }
            shown(By.text("Open sheet"))
            val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
            val before = automation.takeScreenshot()
            click(By.text("Open sheet"))
            SystemClock.sleep(600)
            val after = automation.takeScreenshot()

            fun brightness(pixel: Int): Int = android.graphics.Color.red(pixel) +
                android.graphics.Color.green(pixel) + android.graphics.Color.blue(pixel)
            val x = before.width / 3
            for (y in listOf(12)) {
                assertTrue(
                    "Scrim covers the system bar at y=$y",
                    brightness(after.getPixel(x, y)) < brightness(before.getPixel(x, y)) * 0.8f
                )
            }
            val navigationPixel = after.getPixel(x, after.height - 12)
            for (channel in listOf<(Int) -> Int>(
                android.graphics.Color::red,
                android.graphics.Color::green,
                android.graphics.Color::blue
            )) {
                assertTrue(
                    "The Sheet background reaches behind the navigation bar",
                    kotlin.math.abs(channel(navigationPixel) - channel(fixture.sheetBackground)) <=
                        5
                )
            }
            scenario.onActivity {
                assertEquals(fixture.lightBackground, fixture.navigationDarkIcons?.invoke())
            }
            val close = device.findObject(By.text("Program close")).visibleBounds
            assertTrue(
                "Close action stays above the system gesture area",
                close.bottom < after.height - 30
            )
            click(By.text("Program close"))
            click(By.text("Background"))
            shown(By.text("Background: 1"))
            before.recycle()
            after.recycle()
        }
    }

    @Test
    fun roundedContentUsesOneHorizontalMarginAndItsButtonRemainsClickable() {
        val fixture = AndroidSheetFixture(retain = true, rounded = true)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.setContent { fixture.Content() } }
            click(By.text("Open sheet"))
            val panel = fixture.panel.get()
            val button = fixture.roundedButton.get()
            val left = (panel.left + fixture.contentMargin).toInt()
            device.click(left + 1, button.center.y.toInt())
            SystemClock.sleep(300)
            device.click(left, button.center.y.toInt())
            SystemClock.sleep(300)
            device.click(left - 1, button.center.y.toInt())
            SystemClock.sleep(300)
            scenario.onActivity {
                assertEquals(2, fixture.insideClicks)
                assertEquals(0, fixture.requests)
            }
            click(By.text("Program close"))
        }
    }

    @Test
    fun rejectedRequestsKeepModalInputAndProgrammaticExitRestoresBackground() {
        val fixture = AndroidSheetFixture(retain = true)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.setContent { fixture.Content() } }
            val background = device.wait(
                Until.findObject(By.text("Background")),
                5_000
            ).visibleBounds
            click(By.text("Open sheet"))
            click(By.text("Inside"))
            shown(By.text("Inside: 1"))
            device.click(background.centerX(), background.centerY())
            shown(By.text("Requests: 1"))
            device.pressBack()
            SystemClock.sleep(500)
            scenario.onActivity {
                assertEquals("One request for a system Back", 2, fixture.requests)
            }
            shown(By.text("Requests: 2"))
            scenario.onActivity { assertEquals(0, fixture.backgroundClicks) }
            click(By.text("Program close"))
            assertTrue(device.wait(Until.gone(By.text("Fixture sheet")), 5_000))
            click(By.text("Background"))
            shown(By.text("Background: 1"))
            scenario.onActivity { assertEquals(2, fixture.requests) }
        }
    }

    @Test
    fun closeIconRequestsOnceAndRoundedSurfaceBlocksBackgroundInput() {
        val fixture = AndroidSheetFixture(retain = true)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.setContent { fixture.Content() } }
            click(By.text("Open sheet"))
            click(By.desc(java.util.regex.Pattern.compile("Close sheet|关闭面板")))
            shown(By.text("Requests: 1"))
            click(By.text("Inside"))
            shown(By.text("Inside: 1"))
            val surface = fixture.panel.get()
            // Interior and included top edge, then the adjacent outside point and clipped corner.
            device.click(surface.center.x.toInt(), (surface.top + 3).toInt())
            device.click(surface.center.x.toInt(), surface.top.toInt())
            SystemClock.sleep(300)
            scenario.onActivity { assertEquals(1, fixture.requests) }
            device.click(surface.center.x.toInt(), surface.top.toInt() - 1)
            SystemClock.sleep(300)
            device.click(surface.left.toInt() + 1, surface.top.toInt() + 1)
            SystemClock.sleep(300)
            scenario.onActivity {
                assertEquals(3, fixture.requests)
                assertEquals(0, fixture.backgroundClicks)
                assertTrue(fixture.visible)
            }
            click(By.text("Program close"))
            assertTrue(device.wait(Until.gone(By.text("Fixture sheet")), 5_000))
            click(By.text("Background"))
            shown(By.text("Background: 1"))
        }
    }

    @Test
    fun disabledUserDismissalStillAllowsTheBusinessCloseAction() {
        val fixture = AndroidSheetFixture(dismissible = false)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.setContent { fixture.Content() } }
            val background = device.wait(
                Until.findObject(By.text("Background")),
                5_000
            ).visibleBounds
            click(By.text("Open sheet"))
            click(By.desc(java.util.regex.Pattern.compile("Close sheet|关闭面板")))
            device.click(background.centerX(), background.centerY())
            device.pressBack()
            SystemClock.sleep(300)
            shown(By.text("Fixture sheet"))
            scenario.onActivity {
                assertEquals(0, fixture.requests)
                assertEquals(0, fixture.backgroundClicks)
            }
            click(By.text("Program close"))
            assertTrue(device.wait(Until.gone(By.text("Fixture sheet")), 5_000))
            click(By.text("Background"))
            shown(By.text("Background: 1"))
        }
    }

    @Test
    fun firstBackHidesImeAndSecondBackRequestsSheetDismissal() {
        val fixture = AndroidSheetFixture(retain = true, form = true)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.setContent { fixture.Content() } }
            click(By.text("Open sheet"))
            click(By.clazz("android.widget.EditText"))
            device.pressKeyCode(KeyEvent.KEYCODE_A)
            device.pressKeyCode(KeyEvent.KEYCODE_B)
            SystemClock.sleep(500)
            device.pressBack()
            SystemClock.sleep(500)
            shown(By.text("Fixture sheet"))
            scenario.onActivity {
                assertEquals(0, fixture.requests)
                assertEquals("ab", fixture.draft)
            }
            device.pressBack()
            shown(By.text("Requests: 1"))
            click(By.text("Program close"))
        }
    }

    @Test
    fun handleAndNestedTouchScrollingReachTheConfiguredHeights() {
        val fixture = AndroidSheetFixture(list = true)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.setContent { fixture.Content() } }
            click(By.text("Open sheet"))
            SystemClock.sleep(500)
            val medium = fixture.panel.get()
            val centerX = medium.center.x.toInt()
            device.swipe(
                centerX,
                (medium.top + 50).toInt(),
                centerX,
                (
                    medium.top -
                        medium.height * 0.7f
                ).toInt(),
                35
            )
            SystemClock.sleep(700)
            assertTrue(
                "Handle expands the visible sheet",
                fixture.panel.get().height > medium.height * 1.5f
            )
            val large = fixture.panel.get()
            device.swipe(
                centerX,
                (large.bottom - 300).toInt(),
                centerX,
                (large.top + 250).toInt(),
                35
            )
            SystemClock.sleep(500)
            assertTrue("List scrolls after expansion", !device.hasObject(By.text("Row 0")))
            click(By.text("Program close"))
            scenario.onActivity { assertEquals(0, fixture.requests) }
        }
    }

    @Test
    fun predictiveBackCancelKeepsSheetAndCommitRequestsOnce() {
        assumeTrue(Build.VERSION.SDK_INT >= 34)
        val navigation = device.executeShellCommand("cmd overlay list")
            .lineSequence().firstOrNull {
                it.trim().startsWith("[x]") && "systemui.navbar." in it
            }?.substringAfter("] ")?.trim()
        val fixture = AndroidSheetFixture(retain = true)
        try {
            device.executeShellCommand(
                "cmd overlay enable-exclusive --category com.android.internal.systemui.navbar.gestural"
            )
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onActivity { it.setContent { fixture.Content() } }
                click(By.text("Open sheet"))
                val y = device.displayHeight / 2
                val distance = device.displayWidth / 3
                device.swipe(
                    arrayOf(Point(1, y), Point(distance, y), Point(1, y)),
                    30
                )
                SystemClock.sleep(700)
                shown(By.text("Fixture sheet"))
                scenario.onActivity { assertEquals(0, fixture.requests) }
                device.swipe(1, y, distance * 2, y, 35)
                SystemClock.sleep(700)
                scenario.onActivity { assertEquals(1, fixture.requests) }
                shown(By.text("Fixture sheet"))
                click(By.text("Program close"))
            }
        } finally {
            if (navigation != null) {
                device.executeShellCommand("cmd overlay enable-exclusive --category $navigation")
            }
        }
    }

    @Test
    fun sampleSheetScreenOpensAndItsFeatureSwitchesRemainUsable() {
        ActivityScenario.launch(MainActivity::class.java).use {
            repeat(4) {
                if (!device.hasObject(By.text("Sheet"))) device.swipe(500, 1900, 500, 600, 20)
            }
            click(By.text("Sheet"))
            shown(By.text("Open a sheet"))
            click(By.text("Presentation"))
            click(By.text("Bottom"))
            click(By.text("Open Short content"))
            click(By.text("Show more content"))
            shown(
                By.text(
                    java.util.regex.Pattern.compile(
                        "(?s).*Extra content to see how the Content detent.*"
                    )
                )
            )
            click(By.text("Close directly"))
            assertTrue(device.wait(Until.gone(By.text("Show less content")), 5_000))
            shown(By.text("Open a sheet"))
            click(By.text("Height"))
            click(By.text("Medium / Large"))
            click(By.text("Example content"))
            click(By.text("Playback queue"))
            shown(By.text("Playback queue"))
            click(By.text("Open Playback queue"))
            shown(By.text("Track 1"))
            click(By.text("Close directly"))
            assertTrue(device.wait(Until.gone(By.text("Track 1")), 5_000))
            click(By.text("Example content"))
            click(By.text("Draft form"))
            click(By.text("Open Draft form"))
            click(By.clazz("android.widget.EditText"))
            device.pressKeyCode(KeyEvent.KEYCODE_A)
            device.pressBack()
            SystemClock.sleep(400)
            device.pressBack()
            click(By.text("Keep editing"))
            shown(By.text("a"))
            click(By.text("Save"))
            shown(By.text("Open a sheet"))
            click(By.text("Example content"))
            click(By.text("Horizontal interactions"))
            click(By.text("Open Horizontal interactions"))
            val page = device.wait(Until.findObject(By.text("Page 1 / 5")), 5_000).visibleBounds
            device.swipe(page.right - 20, page.centerY(), page.left + 20, page.centerY(), 25)
            SystemClock.sleep(600)
            InstrumentationRegistry.getInstrumentation().uiAutomation.clearCache()
            shown(By.text("Current page: 2"))
            // Expand with a vertical gesture, then operate the horizontal Slider.
            device.swipe(540, page.bottom - 15, 540, 500, 30)
            SystemClock.sleep(650)
            InstrumentationRegistry.getInstrumentation().uiAutomation.clearCache()
            val slider = device.wait(
                Until.findObject(By.clazz("android.widget.SeekBar")),
                5_000
            ).visibleBounds
            device.swipe(
                slider.left + 20,
                slider.centerY(),
                slider.right - 40,
                slider.centerY(),
                30
            )
            SystemClock.sleep(600)
            InstrumentationRegistry.getInstrumentation().uiAutomation.clearCache()
            assertTrue("Slider's value changes", !device.hasObject(By.text("40%")))
            click(By.text("Button taps: 0"))
            shown(By.text("Button taps: 1"))
            click(By.text("Done"))
            shown(By.text("Open a sheet"))
        }
    }

    private fun click(selector: BySelector) {
        val node = device.wait(Until.findObject(selector), 5_000)
        if (node == null) {
            val directory = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
            device.takeScreenshot(java.io.File(directory, "sheet-test-failure.png"))
            device.dumpWindowHierarchy(java.io.File(directory, "sheet-test-failure.xml"))
        }
        assertTrue("Missing native UI node: $selector", node != null)
        val rect = node.visibleBounds
        device.click(rect.centerX(), rect.centerY())
        device.waitForIdle()
        SystemClock.sleep(350)
        if (Build.VERSION.SDK_INT >= 33) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.clearCache()
        }
    }

    private fun shown(selector: BySelector) {
        val found = device.wait(Until.hasObject(selector), 5_000)
        if (!found) {
            val directory = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
            device.takeScreenshot(java.io.File(directory, "sheet-test-failure.png"))
            device.dumpWindowHierarchy(java.io.File(directory, "sheet-test-failure.xml"))
        }
        assertTrue("Missing UI: $selector", found)
    }
}

private class AndroidSheetFixture(
    private val retain: Boolean = false,
    private val dismissible: Boolean = true,
    private val form: Boolean = false,
    private val list: Boolean = false,
    private val rounded: Boolean = false,
    private val noActions: Boolean = false,
    private val edgeToEdge: Boolean = false
) {
    var visible by mutableStateOf(false)
    var requests by mutableIntStateOf(0)
    var backgroundClicks by mutableIntStateOf(0)
    var insideClicks by mutableIntStateOf(0)
    var draft by mutableStateOf("")
    val panel = AtomicReference(Rect.Zero)
    val roundedButton = AtomicReference(Rect.Zero)
    val body = AtomicReference(Rect.Zero)
    var navigationInset = 0
    var sheetBackground = 0
    var lightBackground = false
    var navigationDarkIcons: (() -> Boolean)? = null
    var contentMargin = 0f

    @Composable
    fun Content() {
        SaltTheme {
            sheetBackground = SaltTheme.colors.background.toArgb()
            lightBackground = SaltTheme.colors.background.luminance() > 0.179f
            contentMargin = with(LocalDensity.current) { SaltTheme.dimens.padding.toPx() }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color(0xFFF8F8F8))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = { visible = true }, text = "Open sheet")
                Button(onClick = { backgroundClicks++ }, text = "Background")
                Text("Background: $backgroundClicks")
            }
            Sheet(
                visible = visible,
                onDismissRequest = {
                    requests++
                    if (!retain) visible = false
                },
                modifier = Modifier.onGloballyPositioned { panel.set(it.boundsInWindow()) },
                title = "Fixture sheet",
                properties = SheetProperties(
                    presentation = SheetPresentation.Bottom,
                    contentLayout = if (edgeToEdge) {
                        SheetContentLayout.EdgeToEdge
                    } else {
                        SheetContentLayout.Inset
                    },
                    dismissible = dismissible,
                    detents = if (list) {
                        listOf(
                            SheetDetent.Medium,
                            SheetDetent.Large
                        )
                    } else {
                        listOf(SheetDetent.Content)
                    }
                ),
                actions = {
                    if (!noActions) Button(onClick = { visible = false }, text = "Program close")
                }
            ) {
                val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
                navigationDarkIcons = dialogWindow?.let { window ->
                    {
                        WindowCompat.getInsetsController(
                            window,
                            window.decorView
                        ).isAppearanceLightNavigationBars
                    }
                }
                if (list) {
                    val padding = contentPadding
                    navigationInset = with(LocalDensity.current) {
                        WindowInsets.navigationBars.getBottom(this)
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { body.set(it.boundsInWindow()) },
                        contentPadding = if (edgeToEdge) padding else PaddingValues()
                    ) {
                        items(100) { Text("Row $it", Modifier.padding(12.dp)) }
                        if (noActions) {
                            item {
                                Button(
                                    onClick = { insideClicks++ },
                                    text = if (insideClicks >
                                        0
                                    ) {
                                        "Last item selected"
                                    } else {
                                        "Select last item"
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Button(onClick = { insideClicks++ }, text = "Inside")
                    Text("Inside: $insideClicks")
                    Text("Requests: $requests")
                    if (rounded) {
                        RoundedColumn {
                            ItemButton(
                                onClick = { insideClicks++ },
                                text = "Rounded content action",
                                modifier = Modifier.onGloballyPositioned {
                                    roundedButton.set(it.boundsInWindow())
                                }
                            )
                        }
                    }
                    if (form) {
                        ItemEdit(text = draft, onChange = {
                            draft = it
                        }, hint = "Draft input")
                    }
                }
            }
        }
    }
}
