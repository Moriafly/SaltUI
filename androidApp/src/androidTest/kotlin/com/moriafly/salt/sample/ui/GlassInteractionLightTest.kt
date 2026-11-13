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

import android.graphics.Bitmap
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.moriafly.salt.ui.SaltConfigs
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.material.GlassEffect
import com.moriafly.salt.ui.material.glass
import com.moriafly.salt.ui.material.rememberGlassSource
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(UnstableSaltUiApi::class)
@RunWith(AndroidJUnit4::class)
class GlassInteractionLightTest {
    @Test
    fun lightFollowsTouchAndMouseAndDisappearsAfterExit() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    SaltTheme(
                        configs = SaltConfigs.default(isDarkTheme = true)
                    ) {
                        val source = rememberGlassSource()
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(
                                        width = 260.dp,
                                        height = 80.dp
                                    )
                                    .glass(
                                        source = source,
                                        effect = GlassEffect.Regular()
                                    )
                                    .clickable { }
                                    .semantics { contentDescription = "Glass surface" }
                            )
                        }
                    }
                }
            }

            val surface = device.wait(Until.findObject(By.desc("Glass surface")), 5_000)
                ?: throw AssertionError("Glass surface did not appear")
            val bounds = surface.visibleBounds
            val left = bounds.left + bounds.width() / 4
            val right = bounds.right - bounds.width() / 4
            val centerY = bounds.centerY()
            val initial = instrumentation.uiAutomation.takeScreenshot()
            val initialLeft = initial.brightness(left, centerY)
            val initialRight = initial.brightness(right, centerY)
            initial.recycle()

            val downTime = SystemClock.uptimeMillis()
            fun touch(
                action: Int,
                x: Int
            ) {
                val event = MotionEvent.obtain(
                    downTime,
                    SystemClock.uptimeMillis(),
                    action,
                    x.toFloat(),
                    centerY.toFloat(),
                    0
                )
                event.source = InputDevice.SOURCE_TOUCHSCREEN
                try {
                    assertTrue(instrumentation.uiAutomation.injectInputEvent(event, true))
                } finally {
                    event.recycle()
                }
            }

            fun hover(x: Int) {
                val time = SystemClock.uptimeMillis()
                val pointer = MotionEvent.PointerProperties().apply {
                    id = 0
                    toolType = MotionEvent.TOOL_TYPE_MOUSE
                }
                val coordinates = MotionEvent.PointerCoords().apply {
                    this.x = x.toFloat()
                    y = (centerY - bounds.height() / 5).toFloat()
                    size = 1f
                }
                val event = MotionEvent.obtain(
                    time,
                    time,
                    MotionEvent.ACTION_HOVER_MOVE,
                    1,
                    arrayOf(pointer),
                    arrayOf(coordinates),
                    0,
                    0,
                    1f,
                    1f,
                    0,
                    0,
                    InputDevice.SOURCE_MOUSE,
                    0
                )
                try {
                    assertTrue(instrumentation.uiAutomation.injectInputEvent(event, true))
                } finally {
                    event.recycle()
                }
            }

            fun waitForLight(
                message: String,
                condition: (Int, Int) -> Boolean
            ) {
                val deadline = SystemClock.uptimeMillis() + 5_000
                var leftChange = 0
                var rightChange = 0
                while (SystemClock.uptimeMillis() < deadline) {
                    val image = instrumentation.uiAutomation.takeScreenshot()
                    leftChange = image.brightness(left, centerY) - initialLeft
                    rightChange = image.brightness(right, centerY) - initialRight
                    image.recycle()
                    if (condition(leftChange, rightChange)) {
                        return
                    }
                    SystemClock.sleep(50)
                }
                throw AssertionError("$message (left=$leftChange, right=$rightChange)")
            }

            touch(
                action = MotionEvent.ACTION_DOWN,
                x = left
            )
            try {
                waitForLight("The light should appear at the touch position") { leftChange, rightChange ->
                    leftChange > 20 && leftChange > rightChange + 10
                }
                touch(
                    action = MotionEvent.ACTION_MOVE,
                    x = right
                )
                waitForLight("The light should follow the drag") { leftChange, rightChange ->
                    rightChange > 20 && rightChange > leftChange + 10
                }
            } finally {
                touch(
                    action = MotionEvent.ACTION_UP,
                    x = right
                )
            }
            waitForLight("The light should disappear after release") { leftChange, rightChange ->
                abs(leftChange) < 10 && abs(rightChange) < 10
            }

            hover(bounds.left - 20)
            hover(left)
            try {
                waitForLight("The light should follow mouse hover") { leftChange, rightChange ->
                    leftChange > 20 && leftChange > rightChange + 10
                }
            } finally {
                hover(bounds.left - 20)
            }
            waitForLight("The light should disappear when the mouse leaves") { leftChange, rightChange ->
                abs(leftChange) < 10 && abs(rightChange) < 10
            }
        }
    }

    private fun Bitmap.brightness(
        x: Int,
        y: Int
    ): Int {
        val color = getPixel(x, y)
        return android.graphics.Color.red(color) +
            android.graphics.Color.green(color) + android.graphics.Color.blue(color)
    }
}
