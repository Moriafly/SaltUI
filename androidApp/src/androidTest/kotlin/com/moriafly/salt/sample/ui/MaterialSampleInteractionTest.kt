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

import android.graphics.Color
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Configurator
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/**
 * Opens the sample through its normal navigation using real touchscreen and system back events.
 */
@RunWith(AndroidJUnit4::class)
class MaterialSampleInteractionTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)

    @Test
    fun materialTestSupportsCachingControlsAndRestoresItsNavigation() {
        val configurator = Configurator.getInstance()
        val idleTimeout = configurator.waitForIdleTimeout
        configurator.waitForIdleTimeout = 0
        try {
            device.wakeUp()
            device.executeShellCommand("wm dismiss-keyguard")
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                assertTrue(device.wait(Until.hasObject(By.text("Main")), 10_000))
                click(By.text("Material"))
                click(By.textContains("Material cache test"))
                shown(By.text("Material test"))
                click(By.textContains("Static source"))
                shown(By.textContains("Static key 0"))
                val first = previewColor()
                click(By.text("Tap count: 0"))
                shown(By.text("Tap count: 1"))
                click(By.text("Change wallpaper"))
                shown(By.textContains("Static key 1"))
                awaitPreview { difference(it, first) > 0.08f }
                click(By.text("Refresh cache"))
                shown(By.textContains("Static key 2"))

                scenario.recreate()
                shown(By.text("Material test"))
                shown(By.textContains("Static key 2"))
                shown(By.text("Tap count: 1"))

                click(By.text("Material type"))
                click(By.text("Mica"))
                click(By.text("Panel layout"))
                click(By.text("Shared"))
                shown(By.text("Sub-background"))
                val light = previewColor()
                click(By.text("Dark preview"))
                awaitPreview { difference(it, light) > 0.1f }
                click(By.text("Animate panels"))
                val firstPosition = shown(By.text("Tap count: 1")).visibleBounds
                val motionDeadline = SystemClock.uptimeMillis() + 5_000
                var moved = false
                while (SystemClock.uptimeMillis() < motionDeadline && !moved) {
                    val current = shown(By.text("Tap count: 1")).visibleBounds
                    moved = current.left != firstPosition.left || current.top != firstPosition.top
                    SystemClock.sleep(32)
                }
                assertTrue("Animating panels must move the interactive foreground", moved)
                click(By.text("Animate panels"))
                click(By.text("Panel layout"))
                click(By.text("Small"))
                click(By.text("Panel layout"))
                click(By.text("Large"))
                shown(By.text("Tap count: 1"))
                val heightBefore = shown(By.desc("Material preview")).visibleBounds.height()
                val slider = shown(By.clazz("android.widget.SeekBar")).visibleBounds
                device.swipe(
                    slider.left + slider.width() / 4,
                    slider.centerY(),
                    slider.right - 2,
                    slider.centerY(),
                    30
                )
                val resizeDeadline = SystemClock.uptimeMillis() + 5_000
                var resized = false
                while (SystemClock.uptimeMillis() < resizeDeadline && !resized) {
                    val height = shown(By.desc("Material preview")).visibleBounds.height()
                    resized = abs(height - heightBefore) > 10
                    SystemClock.sleep(32)
                }
                assertTrue("Dragging the slider must resize the actual source", resized)

                device.pressBack()
                shown(By.text("Title Bar Backdrop Type"))
                device.pressBack()
                shown(By.text("Main"))
            }
        } finally {
            configurator.waitForIdleTimeout = idleTimeout
        }
    }

    private fun click(
        selector: BySelector
    ) {
        shown(selector)
        val deadline = SystemClock.uptimeMillis() + 5_000
        var previous = android.graphics.Rect()
        var stable = 0
        while (SystemClock.uptimeMillis() < deadline) {
            try {
                val target = device.findObject(selector)
                val bounds = target?.visibleBounds
                if (bounds != null && !bounds.isEmpty) {
                    stable = if (bounds == previous) { stable + 1 } else { 0 }
                    if (stable >= 3) {
                        target.click()
                        return
                    }
                    previous = bounds
                }
            } catch (_: StaleObjectException) {
                stable = 0
            }
            SystemClock.sleep(80)
        }
        error("The touch target did not settle: $selector")
    }

    private fun shown(
        selector: BySelector
    ): UiObject2 {
        fun visible(): UiObject2? = try {
            device.findObject(selector)?.takeIf { !it.visibleBounds.isEmpty }
        } catch (_: StaleObjectException) {
            null
        }
        val deadline = SystemClock.uptimeMillis() + 1_000
        while (SystemClock.uptimeMillis() < deadline) {
            visible()?.let { return it }
            SystemClock.sleep(32)
        }
        for (direction in listOf(-1, 1)) {
            repeat(8) {
                val scroll = UiScrollable(UiSelector().scrollable(true)).setAsVerticalList()
                if (direction < 0) {
                    scroll.scrollForward(80)
                } else {
                    scroll.scrollBackward(80)
                }
                device.wait(Until.hasObject(selector), 250)
                visible()?.let { return it }
            }
        }
        val directory = checkNotNull(
            instrumentation.targetContext.getExternalFilesDir("material-sample-tests")
        )
        device.takeScreenshot(java.io.File(directory, "failure.png"))
        device.dumpWindowHierarchy(java.io.File(directory, "failure.xml"))
        error("The sample did not show $selector")
    }

    private fun previewColor(): FloatArray {
        val bounds = shown(By.desc("Material preview")).visibleBounds
        val screenshot = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
        return try {
            val x = bounds.left + (bounds.width() * 0.75f).toInt()
            val y = bounds.top + (bounds.height() * 0.25f).toInt()
            val color = screenshot.getPixel(x, y)
            floatArrayOf(
                Color.red(color) / 255f,
                Color.green(color) / 255f,
                Color.blue(color) / 255f
            )
        } finally {
            screenshot.recycle()
        }
    }

    private fun awaitPreview(
        predicate: (FloatArray) -> Boolean
    ) {
        val deadline = SystemClock.uptimeMillis() + 8_000
        while (SystemClock.uptimeMillis() < deadline) {
            if (predicate(previewColor())) {
                return
            }
            SystemClock.sleep(32)
        }
        assertTrue("The material preview did not update", false)
    }

    private fun difference(
        first: FloatArray,
        second: FloatArray
    ): Float = first.indices.sumOf { abs(first[it] - second[it]).toDouble() }.toFloat()
}
