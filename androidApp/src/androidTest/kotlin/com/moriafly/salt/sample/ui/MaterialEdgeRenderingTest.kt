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
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.moriafly.salt.ui.SaltConfigs
import com.moriafly.salt.ui.SaltMaterial
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.material.MaterialSource
import com.moriafly.salt.ui.material.MaterialType
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MaterialEdgeRenderingTest {
    @OptIn(UnstableSaltUiApi::class)
    @Test
    fun blurryGlassCoversTheRightmostPixelColumn() {
        val activityReference = AtomicReference<MainActivity>()

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activityReference.set(activity)
                activity.setContent {
                    SaltTheme(
                        configs = SaltConfigs.default(isDarkTheme = true),
                        material = SaltMaterial.default(MaterialType.BlurryGlass)
                    ) {
                        MaterialSource(
                            modifier = Modifier.fillMaxSize(),
                            materialSelf = true
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(ComposeColor.Red)
                            )
                        }
                    }
                }
            }

            val instrumentation = InstrumentationRegistry.getInstrumentation()
            instrumentation.waitForIdleSync()
            Thread.sleep(500)

            val bitmap = captureWindow(activityReference.get())
            val center = sampleColumn(bitmap, bitmap.width / 2)
            val rightEdge = sampleColumn(bitmap, bitmap.width - 1)

            assertTrue(
                "Expected the blurry-glass overlay to darken the red source, but was $center",
                center.red < 220
            )
            assertTrue(
                "Expected the right edge $rightEdge to match the covered center $center",
                colorDistance(center, rightEdge) <= 24
            )
        }
    }

    private fun captureWindow(activity: MainActivity): Bitmap {
        val view = activity.window.decorView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val result = AtomicReference<Int>()
        val latch = CountDownLatch(1)

        PixelCopy.request(
            activity.window,
            bitmap,
            {
                result.set(it)
                latch.countDown()
            },
            Handler(Looper.getMainLooper())
        )

        assertTrue("PixelCopy timed out", latch.await(5, TimeUnit.SECONDS))
        assertEquals(PixelCopy.SUCCESS, result.get())
        return bitmap
    }

    private fun sampleColumn(bitmap: Bitmap, x: Int): Rgb {
        val centerY = bitmap.height / 2
        val radius = 24
        var red = 0
        var green = 0
        var blue = 0

        for (y in centerY - radius until centerY + radius) {
            val color = bitmap.getPixel(x, y)
            red += Color.red(color)
            green += Color.green(color)
            blue += Color.blue(color)
        }

        val count = radius * 2
        return Rgb(red / count, green / count, blue / count)
    }

    private fun colorDistance(first: Rgb, second: Rgb): Int =
        abs(first.red - second.red) +
            abs(first.green - second.green) +
            abs(first.blue - second.blue)

    private data class Rgb(
        val red: Int,
        val green: Int,
        val blue: Int
    )
}
