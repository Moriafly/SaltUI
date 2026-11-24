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

package com.moriafly.salt.sample.ui

import android.app.UiAutomation
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.ItemDropdown
import com.moriafly.salt.ui.SaltConfigs
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.popup.PopupMenu
import com.moriafly.salt.ui.popup.PopupMenuBackdrop
import com.moriafly.salt.ui.popup.PopupMenuDefaults
import com.moriafly.salt.ui.popup.PopupMenuItem
import com.moriafly.salt.ui.popup.PopupMenuMaterial
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs
import android.graphics.Color as AndroidColor

@OptIn(UnstableSaltUiApi::class)
@RunWith(AndroidJUnit4::class)
class PopupMenuGlassInteractionTest {
    @Before
    fun bringSampleToForeground() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        UiDevice.getInstance(instrumentation).executeShellCommand(
            "am start -W -n ${instrumentation.targetContext.packageName}/${MainActivity::class.java.name}"
        )
    }

    @Test
    fun qualityDropdownKeepsTheVolumeTrackAlignedWhileDragging() {
        assumeTrue(Build.VERSION.SDK_INT >= 33)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val automation = instrumentation.uiAutomation
        val device = UiDevice.getInstance(instrumentation)
        val density = instrumentation.targetContext.resources.displayMetrics.density

        ActivityScenario.launch(MainActivity::class.java).use {
            val items = device.wait(Until.findObject(By.text("Items")), 5_000)
                ?: throw AssertionError("Items page entry did not appear")
            items.click()
            val trigger = device.wait(Until.findObject(By.text("Quality")), 5_000)
                ?: throw AssertionError("Quality dropdown did not appear")
            val volume = device.findObject(By.text("Volume")).visibleBounds
            trigger.click()
            val first = device.wait(Until.findObject(By.text("Low")), 5_000)
                ?: throw AssertionError("Quality menu did not open")
            val last = device.findObject(By.text("High")).visibleBounds
            val insideX = first.visibleBounds.left - (8 * density).toInt()
            val outsideX = first.visibleBounds.left - (26 * density).toInt()
            val trackRows = volume.bottom..(volume.bottom + (56 * density).toInt())
            SystemClock.sleep(400)

            val rest = automation.volumeTrackOffset(insideX, outsideX, trackRows)
            val x = last.centerX()
            var y = last.centerY()
            val downTime = SystemClock.uptimeMillis()
            automation.touch(MotionEvent.ACTION_DOWN, x, y, downTime)
            val pressed: Float
            val draggedUp: Float
            try {
                SystemClock.sleep(250)
                pressed = automation.volumeTrackOffset(insideX, outsideX, trackRows)
                y -= (26 * density).toInt()
                automation.touch(MotionEvent.ACTION_MOVE, x, y, downTime)
                SystemClock.sleep(250)
                draggedUp = automation.volumeTrackOffset(insideX, outsideX, trackRows)
            } finally {
                automation.touch(MotionEvent.ACTION_CANCEL, x, y, downTime)
            }
            assertTrue("Volume track was misaligned at rest: $rest px", abs(rest) <= 2 * density)
            assertTrue(
                "Volume track slipped under the glass: rest=$rest, press=$pressed, up=$draggedUp px",
                abs(pressed - rest) <= 2 * density && abs(draggedUp - rest) <= 2 * density
            )
        }
    }

    @Test
    fun dropdownFollowsVerticalDragsAndSelectsOnRelease() {
        assumeTrue(Build.VERSION.SDK_INT >= 33)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val automation = instrumentation.uiAutomation
        val device = UiDevice.getInstance(instrumentation)
        val density = instrumentation.targetContext.resources.displayMetrics.density

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.setContent { GlassDropdown() } }
            val opener = device.wait(Until.findObject(By.text("Glass dropdown")), 5_000)
                ?: throw AssertionError("Dropdown opener did not appear")
            opener.click()
            val bottom = device.wait(Until.findObject(By.desc("Choose very high")), 5_000)
                ?: throw AssertionError("Dropdown did not open")
            val middleY = device.findObject(By.desc("Choose medium")).visibleBounds.centerY()
            val bottomBounds = bottom.visibleBounds
            SystemClock.sleep(400)

            val x = bottomBounds.centerX()
            val rowHeight = bottomBounds.height()
            val rest = automation.captureVerticalSpan(x, middleY, rowHeight)
            val downY = bottomBounds.centerY()
            val downTime = SystemClock.uptimeMillis()
            val smallDrag = (18 * density).toInt()
            automation.touch(MotionEvent.ACTION_DOWN, x, downY, downTime)
            var lastY = downY
            val pressed: VerticalSpan
            val draggedUp: VerticalSpan
            val draggedDown: VerticalSpan
            try {
                SystemClock.sleep(250)
                pressed = automation.captureVerticalSpan(x, middleY, rowHeight)
                lastY = downY - smallDrag
                automation.touch(MotionEvent.ACTION_MOVE, x, lastY, downTime)
                SystemClock.sleep(250)
                draggedUp = automation.captureVerticalSpan(x, middleY, rowHeight)
                lastY = downY + smallDrag
                automation.touch(MotionEvent.ACTION_MOVE, x, lastY, downTime)
                SystemClock.sleep(250)
                draggedDown = automation.captureVerticalSpan(x, middleY, rowHeight)
                lastY = middleY
                automation.touch(MotionEvent.ACTION_MOVE, x, lastY, downTime)
            } finally {
                automation.touch(MotionEvent.ACTION_UP, x, lastY, downTime)
            }

            assertTrue(
                "Expected visible press scale: rest=$rest, pressed=$pressed",
                pressed.height < rest.height
            )
            assertTrue(
                "Expected glass to follow upward drag: pressed=$pressed, up=$draggedUp",
                draggedUp.center < pressed.center
            )
            assertTrue(
                "Expected glass to follow downward drag: pressed=$pressed, down=$draggedDown",
                draggedDown.center > pressed.center
            )
            assertTrue(
                "Sliding should select Medium",
                device.wait(Until.hasObject(By.textContains("Medium selected")), 5_000)
            )
        }
    }

    @Test
    fun draggingMovesGlassOverItsLiveBackdrop() =
        verifyDraggingOverLiveBackdrop(PopupMenuMaterial.Glass)

    @Test
    fun draggingMovesBlurOverItsLiveBackdrop() =
        verifyDraggingOverLiveBackdrop(PopupMenuMaterial.Blur)

    private fun verifyDraggingOverLiveBackdrop(material: PopupMenuMaterial) {
        assumeTrue(Build.VERSION.SDK_INT >= 33)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val automation = instrumentation.uiAutomation
        val device = UiDevice.getInstance(instrumentation)
        val density = instrumentation.targetContext.resources.displayMetrics.density

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.setContent { BackdropMenu(material) } }
            val opener = device.wait(Until.findObject(By.text("Open glass menu")), 5_000)
                ?: throw AssertionError("Menu opener did not appear")
            opener.click()
            val menu = device.wait(Until.findObject(By.desc("Glass menu panel")), 5_000)
                ?: throw AssertionError("Glass menu did not open")
            SystemClock.sleep(400)
            val bounds = menu.visibleBounds
            val initial = automation.captureMenu(bounds)
            assertTrue(
                "Expected filtered backdrop in the menu: $initial",
                initial.blendedPixels >= 3
            )

            val center = bounds.centerX()
            val distance = bounds.width() * 2 / 5
            val downTime = SystemClock.uptimeMillis()
            var touchX = center

            automation.touch(MotionEvent.ACTION_DOWN, touchX, bounds.centerY(), downTime)
            try {
                for (direction in listOf(1, -1)) {
                    touchX = center + direction * distance
                    automation.touch(MotionEvent.ACTION_MOVE, touchX, bounds.centerY(), downTime)
                    val dragged = automation.awaitMenu(bounds) {
                        (it.markerX - initial.markerX) * direction > 1.5f * density
                    }
                    assertTrue(
                        "Expected filtered backdrop while dragging: $dragged",
                        dragged.blendedPixels >= 3
                    )
                    assertTrue(
                        "The backdrop slipped with the menu: $initial -> $dragged",
                        abs(dragged.backdropEdgeX - initial.backdropEdgeX) <= 2 * density
                    )
                }
            } finally {
                automation.touch(MotionEvent.ACTION_UP, touchX, bounds.centerY(), downTime)
            }
            automation.awaitMenu(bounds) {
                abs(it.markerX - initial.markerX) <= density &&
                    abs(it.backdropEdgeX - initial.backdropEdgeX) <= density
            }
        }
    }

    @Composable
    private fun GlassDropdown() {
        SaltTheme(configs = SaltConfigs.default(isDarkTheme = true)) {
            PopupMenuBackdrop(modifier = Modifier.fillMaxSize()) {
                var selection by remember { mutableStateOf("Low") }
                Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                ItemDropdown(
                    modifier = Modifier.align(Alignment.Center),
                    text = "Glass dropdown",
                    value = "$selection selected",
                    style = PopupMenuDefaults.style(tintColor = Color.White.copy(alpha = 0.45f))
                ) {
                    for (label in listOf("Very low", "Low", "Medium", "High", "Very high")) {
                        val description = "Choose ${label.lowercase()}"
                        PopupMenuItem(
                            modifier = Modifier.semantics { contentDescription = description },
                            text = { Text(label) },
                            onClick = {
                                selection = label
                                state.dismiss()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun UiAutomation.touch(action: Int, x: Int, y: Int, downTime: Long) {
        val pointer = MotionEvent.PointerProperties().apply {
            id = 0
            toolType = MotionEvent.TOOL_TYPE_FINGER
        }
        val position = MotionEvent.PointerCoords().apply {
            this.x = x.toFloat()
            this.y = y.toFloat()
            pressure = if (action == MotionEvent.ACTION_UP) 0f else 1f
            size = 1f
        }
        val event = MotionEvent.obtain(
            downTime,
            SystemClock.uptimeMillis(),
            action,
            1,
            arrayOf(pointer),
            arrayOf(position),
            0,
            0,
            1f,
            1f,
            0,
            0,
            InputDevice.SOURCE_TOUCHSCREEN,
            0
        )
        try {
            assertTrue(injectInputEvent(event, true))
        } finally {
            event.recycle()
        }
    }

    private fun UiAutomation.captureVerticalSpan(x: Int, y: Int, rowHeight: Int): VerticalSpan {
        val image = takeScreenshot()
        try {
            val start = (y - rowHeight * 3).coerceAtLeast(0)
            val end = (y + rowHeight * 3).coerceAtMost(image.height - 1)
            val glassPixels = (start..end).filter { AndroidColor.red(image.getPixel(x, it)) > 50 }
            assertTrue("Expected a visible glass panel", glassPixels.isNotEmpty())
            return VerticalSpan(glassPixels.first(), glassPixels.last() + 1)
        } finally {
            image.recycle()
        }
    }

    private fun UiAutomation.volumeTrackOffset(
        insideX: Int,
        outsideX: Int,
        rows: IntRange
    ): Float {
        val image = takeScreenshot()
        try {
            fun trackCenter(x: Int): Float {
                val blueRows = rows.filter { y ->
                    val color = image.getPixel(x, y)
                    AndroidColor.blue(color) - AndroidColor.red(color) > 14
                }
                assertTrue("Volume track should show at x=$x", blueRows.isNotEmpty())
                return (blueRows.first() + blueRows.last()) / 2f
            }
            return trackCenter(insideX) - trackCenter(outsideX)
        } finally {
            image.recycle()
        }
    }

    private data class VerticalSpan(
        val top: Int,
        val bottom: Int
    ) {
        val center get() = (top + bottom) / 2f
        val height get() = bottom - top
    }

    @Composable
    private fun BackdropMenu(material: PopupMenuMaterial) {
        SaltTheme(configs = SaltConfigs.default(isDarkTheme = true)) {
            PopupMenuBackdrop(modifier = Modifier.fillMaxSize()) {
                var expanded by remember { mutableStateOf(false) }
                Canvas(Modifier.fillMaxSize()) {
                    val halfWidth = size.width / 2f
                    val half = Size(halfWidth, size.height)
                    drawRect(Color.Red, size = half)
                    drawRect(Color.Blue, topLeft = Offset(halfWidth, 0f), size = half)
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 64.dp)
                ) {
                    Button(onClick = { expanded = true }, text = "Open glass menu")
                    PopupMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        style = PopupMenuDefaults.style(
                            material = material,
                            tintColor = Color.Transparent,
                            backdropColor = Color.Transparent
                        ),
                        scrollable = false,
                        popupPositionProvider = CenterMenu
                    ) {
                        Box(
                            modifier = Modifier
                                .size(224.dp, 104.dp)
                                .semantics { contentDescription = "Glass menu panel" },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(Modifier.size(8.dp, 20.dp).background(Color.Magenta))
                        }
                    }
                }
            }
        }
    }

    private fun UiAutomation.awaitMenu(
        bounds: Rect,
        predicate: (MenuFrame) -> Boolean
    ): MenuFrame {
        val deadline = SystemClock.uptimeMillis() + 3_000
        do {
            val frame = captureMenu(bounds)
            if (predicate(frame)) return frame
            SystemClock.sleep(30)
        } while (SystemClock.uptimeMillis() < deadline)
        throw AssertionError("Menu did not reach the expected position: ${captureMenu(bounds)}")
    }

    private fun UiAutomation.captureMenu(bounds: Rect): MenuFrame {
        val image = takeScreenshot()
        try {
            val center = bounds.centerX()
            val radius = bounds.width() / 5
            val sampleBand = (center - radius)..(center + radius)
            val marker = sampleBand.filter { x ->
                val pixel = image.getPixel(x, bounds.centerY())
                AndroidColor.red(pixel) > 235 && AndroidColor.blue(pixel) > 235
            }
            assertTrue("Menu marker is visible", marker.isNotEmpty())
            val edgeY = bounds.centerY() + bounds.height() / 4
            val edge = sampleBand.first { x ->
                val pixel = image.getPixel(x, edgeY)
                AndroidColor.blue(pixel) > AndroidColor.red(pixel)
            }
            val blendedPixels = sampleBand.count { x -> image.isBlended(x, edgeY) }
            return MenuFrame((marker.first() + marker.last()) / 2, edge, blendedPixels)
        } finally {
            image.recycle()
        }
    }

    private fun Bitmap.isBlended(
        x: Int,
        y: Int
    ): Boolean {
        val pixel = getPixel(x, y)
        return AndroidColor.red(pixel) > 25 && AndroidColor.blue(pixel) > 25
    }

    private data class MenuFrame(
        val markerX: Int,
        val backdropEdgeX: Int,
        val blendedPixels: Int
    )

    private object CenterMenu : PopupPositionProvider {
        override fun calculatePosition(
            anchorBounds: IntRect,
            windowSize: IntSize,
            layoutDirection: LayoutDirection,
            popupContentSize: IntSize
        ): IntOffset = IntOffset(
            x = (windowSize.width - popupContentSize.width) / 2,
            y = (windowSize.height - popupContentSize.height) / 2
        )
    }
}
