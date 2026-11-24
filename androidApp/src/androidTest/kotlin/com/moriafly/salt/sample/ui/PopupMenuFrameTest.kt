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
import android.os.Build
import android.os.SystemClock
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.moriafly.salt.ui.Button
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
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * Records the native popup window's frame timings while exercising real open/select cycles.
 * Each test writes to a separate directory under the requested frame label.
 * Compare artifacts from the same device and build type; timing thresholds are device dependent.
 */
@OptIn(UnstableSaltUiApi::class)
@RunWith(AndroidJUnit4::class)
class PopupMenuFrameTest {
    @Test
    fun repeatedOpeningKeepsGlassMenusSelectable() = recordOpeningFrames(PopupMenuMaterial.Glass)

    @Test
    fun repeatedOpeningKeepsBlurMenusSelectable() = recordOpeningFrames(PopupMenuMaterial.Blur)

    private fun recordOpeningFrames(material: PopupMenuMaterial) {
        assumeTrue(Build.VERSION.SDK_INT >= 33)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        val context = instrumentation.targetContext
        val arguments = InstrumentationRegistry.getArguments()
        val label = arguments.getString("frameLabel") ?: "current"
        val iterations = arguments.getString("frameIterations")
            ?.toInt()
            ?.coerceAtLeast(4) ?: 15
        val directory = requireNotNull(context.getExternalFilesDir(null))
            .resolve("popup-frames/$label/${material.name}-${UUID.randomUUID()}")
        check(directory.mkdirs()) { "Could not create popup frame output directory: $directory" }

        // Bring the sample forward before ActivityScenario starts its managed activity. Some
        // devices otherwise block that launch when instrumentation begins behind another app.
        device.executeShellCommand(
            "am start -W -n ${context.packageName}/${MainActivity::class.java.name}"
        )
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val darkThemeModes = listOf(
                false,
                true
            )
            for (dark in darkThemeModes) {
                scenario.onActivity {
                    it.setContent {
                        key(dark) {
                            FrameMenu(
                                material = material,
                                dark = dark
                            )
                        }
                    }
                }
                val name = "${material.name}-${if (dark) "dark" else "light"}"
                repeat(iterations) { iteration ->
                    val opener = device.wait(
                        Until.findObject(By.text("Open $name menu")),
                        5_000
                    ) ?: throw AssertionError("Menu opener did not appear")
                    val openerBounds = opener.visibleBounds
                    device.executeShellCommand("dumpsys gfxinfo ${context.packageName} reset")
                    device.click(
                        openerBounds.centerX(),
                        openerBounds.centerY()
                    )
                    val menuOpened = device.wait(
                        Until.hasObject(By.text("Action 4")),
                        5_000
                    )
                    assertTrue(
                        "The menu should open",
                        menuOpened
                    )
                    SystemClock.sleep(500)
                    // Warm up three openings before collecting comparable frame data.
                    if (iteration >= 3) {
                        val frames = device.executeShellCommand(
                            "dumpsys gfxinfo ${context.packageName} framestats"
                        )
                        directory.resolve("$name-${iteration - 3}.txt").writeText(frames)
                    }
                    if (iteration == iterations - 1) {
                        val screenshot = instrumentation.uiAutomation.takeScreenshot()
                        try {
                            directory.resolve("$name.png").outputStream().use {
                                screenshot.compress(
                                    Bitmap.CompressFormat.PNG,
                                    100,
                                    it
                                )
                            }
                        } finally {
                            screenshot.recycle()
                        }
                    }
                    val action = device.findObject(By.text("Action 4")).visibleBounds
                    device.click(
                        action.centerX(),
                        action.centerY()
                    )
                    val selectionChanged = device.wait(
                        Until.hasObject(By.text("Selected ${iteration + 1}")),
                        5_000
                    )
                    assertTrue(
                        "Opening $iteration must leave Action 4 selectable",
                        selectionChanged
                    )
                    val menuClosed = device.wait(
                        Until.gone(By.text("Action 4")),
                        5_000
                    )
                    assertTrue(
                        "Selecting an item should close the menu",
                        menuClosed
                    )
                    SystemClock.sleep(150)
                }
            }
        }
    }

    @Composable
    private fun FrameMenu(
        material: PopupMenuMaterial,
        dark: Boolean
    ) {
        SaltTheme(configs = SaltConfigs.default(isDarkTheme = dark)) {
            PopupMenuBackdrop(Modifier.fillMaxSize()) {
                Canvas(Modifier.fillMaxSize()) {
                    val stripeWidth = size.width / 12f
                    repeat(12) { stripe ->
                        drawRect(
                            color = if (stripe % 2 == 0) Color(0xFF567ACD) else Color(0xFFDB9B75),
                            topLeft = Offset(
                                x = stripe * stripeWidth,
                                y = 0f
                            ),
                            size = Size(
                                width = stripeWidth,
                                height = size.height
                            )
                        )
                    }
                }
                var selected by remember { mutableIntStateOf(0) }
                Text(
                    "Selected $selected",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 64.dp)
                )
                Box(Modifier.align(Alignment.Center)) {
                    var expanded by remember { mutableStateOf(false) }
                    val name = "${material.name}-${if (dark) "dark" else "light"}"
                    Button(
                        text = "Open $name menu",
                        onClick = { expanded = true }
                    )
                    PopupMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.width(240.dp),
                        style = PopupMenuDefaults.style(material = material)
                    ) {
                        repeat(8) { index ->
                            PopupMenuItem(
                                text = { Text("Action $index") },
                                onClick = {
                                    selected++
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
