/*
 * Salt UI
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
import android.view.MotionEvent
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.moriafly.salt.ui.SaltColors
import com.moriafly.salt.ui.SaltDynamicColors
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Switcher
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs
import kotlin.math.roundToInt

/** Real input and hardware-composited frames, including the narrowing O-to-I transition. */
@RunWith(AndroidJUnit4::class)
class SwitcherHardwareRenderingTest {
    @Test
    fun zeroDimensionsRemainInvisibleAndRecoverAfterResizing() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val sizes = listOf(
            DpSize(
                0.dp,
                26.dp
            ),
            DpSize(
                46.dp,
                0.dp
            ),
            DpSize(
                0.dp,
                0.dp
            )
        )
        var collapsed by mutableStateOf(true)
        var checked by mutableStateOf(false)
        val bounds = Array(
            6
        ) { Rect.Zero }
        val measured = Array(
            6
        ) { IntSize.Zero }
        var buttonBounds = Rect.Zero
        val colors = SaltColors.defaultLight(onHighlight = Color.White)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    SaltTheme(
                        dynamicColors = SaltDynamicColors(
                            light = colors,
                            dark = colors
                        )
                    ) {
                        CompositionLocalProvider(
                            LocalDensity provides Density(3f)
                        ) {
                            Box(
                                Modifier.fillMaxSize().background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Column {
                                    repeat(
                                        6
                                    ) { index ->
                                        val dimensions = sizes[index % sizes.size]
                                        Box(
                                            Modifier.size(
                                                46.dp,
                                                26.dp
                                            ).onGloballyPositioned {
                                                bounds[index] =
                                                    it.boundsInWindow()
                                            }
                                        ) {
                                            Switcher(
                                                state = if (index < 3) checked else !checked,
                                                modifier = Modifier
                                                    .width(
                                                        if (collapsed) dimensions.width else 46.dp
                                                    )
                                                    .height(
                                                        if (collapsed) dimensions.height else 26.dp
                                                    )
                                                    .onGloballyPositioned {
                                                        measured[index] =
                                                            it.size
                                                    }
                                            )
                                        }
                                    }
                                    Box(
                                        Modifier.size(
                                            70.dp,
                                            40.dp
                                        ).background(Color.DarkGray)
                                            .onGloballyPositioned {
                                                buttonBounds =
                                                    it.boundsInWindow()
                                            }
                                            .clickable { checked = !checked }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            fun verifyFrame() {
                instrumentation.waitForIdleSync()
                val screenshot = instrumentation.uiAutomation.takeScreenshot()
                try {
                    bounds.forEachIndexed { index, slot ->
                        val dimensions = sizes[index % sizes.size]
                        val expected = if (collapsed) {
                            IntSize(
                                (dimensions.width.value * 3).roundToInt(),
                                (dimensions.height.value * 3).roundToInt()
                            )
                        } else {
                            IntSize(
                                138,
                                78
                            )
                        }
                        assertTrue(
                            "Case $index must receive the external constraints: ${measured[index]}",
                            measured[index] == expected
                        )
                        assertTrue(slot.width > 0f)
                        var whitePixels = 0
                        for (y in slot.top.roundToInt() until slot.bottom.roundToInt()) {
                            for (x in slot.left.roundToInt() until slot.right.roundToInt()) {
                                val pixel = screenshot.getPixel(
                                    x,
                                    y
                                )
                                if (android.graphics.Color.red(pixel) > 204 &&
                                    android.graphics.Color.green(pixel) > 204 &&
                                    android.graphics.Color.blue(pixel) > 204
                                ) {
                                    whitePixels++
                                }
                            }
                        }
                        assertTrue(
                            "Case $index: collapsed=$collapsed, visible icon pixels=$whitePixels",
                            if (collapsed) whitePixels == 0 else whitePixels > 0
                        )
                    }
                } finally {
                    screenshot.recycle()
                }
            }

            fun click() {
                val downTime = SystemClock.uptimeMillis()
                for (action in listOf(
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_UP
                )) {
                    val event = MotionEvent.obtain(
                        downTime,
                        SystemClock.uptimeMillis(),
                        action,
                        buttonBounds.center.x,
                        buttonBounds.center.y,
                        0
                    )
                    instrumentation.sendPointerSync(event)
                    event.recycle()
                }
            }

            SystemClock.sleep(500)
            verifyFrame()
            repeat(
                2
            ) {
                click()
                repeat(
                    12
                ) {
                    SystemClock.sleep(80)
                    verifyFrame()
                }
            }
            scenario.onActivity { collapsed = false }
            SystemClock.sleep(200)
            verifyFrame()
            click()
            SystemClock.sleep(80)
            scenario.onActivity { collapsed = true }
            SystemClock.sleep(200)
            verifyFrame()
            SystemClock.sleep(800)
            scenario.onActivity { collapsed = false }
            SystemClock.sleep(200)
            verifyFrame()
        }
    }

    @Test
    fun onAnimationKeepsBothSidesOfTheIconRounded() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        val oldScale = device.executeShellCommand(
            "settings get global animator_duration_scale"
        ).trim()
        var scenario: ActivityScenario<MainActivity>? = null
        try {
            device.executeShellCommand("settings put global animator_duration_scale 5")
            var checked by mutableStateOf(false)
            var currentBounds = Rect.Zero
            var buttonBounds = Rect.Zero
            val colors = SaltColors.defaultLight(onHighlight = Color.White)
            scenario = ActivityScenario.launch(MainActivity::class.java)
            scenario.onActivity { activity ->
                activity.setContent {
                    SaltTheme(
                        dynamicColors = SaltDynamicColors(
                            light = colors,
                            dark = colors
                        )
                    ) {
                        CompositionLocalProvider(
                            LocalDensity provides Density(3f)
                        ) {
                            Box(
                                Modifier.fillMaxSize().background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Switcher(
                                        checked,
                                        Modifier.onGloballyPositioned {
                                            currentBounds =
                                                it.boundsInWindow()
                                        }
                                    )
                                    Box(
                                        Modifier.size(
                                            70.dp,
                                            40.dp
                                        ).background(Color.DarkGray)
                                            .onGloballyPositioned {
                                                buttonBounds =
                                                    it.boundsInWindow()
                                            }
                                            .clickable { checked = !checked }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            instrumentation.waitForIdleSync()
            SystemClock.sleep(1000)
            assertTrue(buttonBounds.width > 0)

            fun crop(
                image: Bitmap,
                bounds: Rect
            ) = Bitmap.createBitmap(
                image,
                bounds.left.roundToInt(),
                bounds.top.roundToInt(),
                bounds.width.roundToInt(),
                bounds.height.roundToInt()
            )
            val downTime = SystemClock.uptimeMillis()
            for (action in listOf(
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_UP
            )) {
                val event = MotionEvent.obtain(
                    downTime,
                    SystemClock.uptimeMillis(),
                    action,
                    buttonBounds.center.x,
                    buttonBounds.center.y,
                    0
                )
                instrumentation.sendPointerSync(event)
                event.recycle()
            }
            var frame = 0
            val iconWidths = mutableSetOf<Int>()
            val asymmetricFrames = mutableListOf<String>()
            val start = SystemClock.uptimeMillis()
            while (SystemClock.uptimeMillis() - start < 4400) {
                val screenshot = instrumentation.uiAutomation.takeScreenshot()
                val current = crop(
                    screenshot,
                    currentBounds
                )

                fun white(
                    x: Int,
                    y: Int
                ): Boolean {
                    val pixel = current.getPixel(
                        x,
                        y
                    )
                    return android.graphics.Color.red(pixel) > 204 &&
                        android.graphics.Color.green(pixel) > 204 &&
                        android.graphics.Color.blue(pixel) > 204
                }
                val rows = (0 until current.height).mapNotNull { y ->
                    val xs = (0 until current.width).filter {
                        white(
                            it,
                            y
                        )
                    }
                    if (xs.isEmpty()) null else xs.first() to xs.last()
                }
                assertTrue(
                    "Icon must remain visible in hardware frame $frame",
                    rows.isNotEmpty()
                )
                val left = rows.minOf { it.first }
                val right = rows.maxOf { it.second }
                iconWidths += right - left + 1
                val asymmetry = rows.maxOf { (rowLeft, rowRight) ->
                    abs((rowLeft - left) - (right - rowRight))
                }
                if (asymmetry > 2) asymmetricFrames += "frame $frame: $asymmetry px"
                current.recycle()
                screenshot.recycle()
                frame++
                SystemClock.sleep(60)
            }
            assertTrue(
                "Real touch must enable the switch",
                checked
            )
            assertTrue(
                "The animation must have been sampled repeatedly",
                frame >= 10
            )
            assertTrue(
                "Must observe the narrowing and re-expanding I, not only endpoints",
                iconWidths.size >= 5
            )
            assertTrue(
                "Both curved sides must remain visible: $asymmetricFrames",
                asymmetricFrames.isEmpty()
            )
            val screenshot = instrumentation.uiAutomation.takeScreenshot()
            val final = crop(
                screenshot,
                currentBounds
            )
            assertTrue(
                "The enabled I must be solid",
                android.graphics.Color.red(
                    final.getPixel(
                        99,
                        39
                    )
                ) > 240
            )
            final.recycle()
            screenshot.recycle()
        } finally {
            scenario?.close()
            if (oldScale == "null") {
                device.executeShellCommand("settings delete global animator_duration_scale")
            } else {
                device.executeShellCommand("settings put global animator_duration_scale $oldScale")
            }
        }
    }
}
