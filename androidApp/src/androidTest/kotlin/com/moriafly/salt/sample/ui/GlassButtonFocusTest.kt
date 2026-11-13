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
import android.graphics.Rect
import android.os.SystemClock
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.moriafly.salt.ui.BasicButton
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.ButtonAppearance
import com.moriafly.salt.ui.ButtonDefaults
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.material.GlassEffect
import com.moriafly.salt.ui.material.glass
import com.moriafly.salt.ui.material.rememberGlassSource
import kotlin.math.abs
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(UnstableSaltUiApi::class)
@RunWith(AndroidJUnit4::class)
class GlassButtonFocusTest {
    private val device get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Test
    fun keyboardFocusFollowsGlassButtonsAndTheyRemainClickable() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    SaltTheme {
                        val source = rememberGlassSource()
                        var firstClicks by remember { mutableIntStateOf(0) }
                        var secondClicks by remember { mutableIntStateOf(0) }
                        var glassAppearance by remember { mutableStateOf(true) }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                                .padding(32.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { firstClicks++ },
                                modifier = Modifier
                                    .width(200.dp)
                                    .onPreviewKeyEvent { event ->
                                        if (event.key == Key.F2 && event.type == KeyEventType.KeyDown) {
                                            glassAppearance = !glassAppearance
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                    .glass(
                                        source = source,
                                        effect = GlassEffect.Solid()
                                    ),
                                appearance = if (glassAppearance) {
                                    ButtonAppearance.Glass
                                } else {
                                    ButtonAppearance.Plain
                                }
                            ) {
                                Text("Glass action")
                            }
                            BasicButton(
                                onClick = { secondClicks++ },
                                modifier = Modifier
                                    .width(200.dp)
                                    .glass(
                                        source = source,
                                        effect = GlassEffect.Solid()
                                    ),
                                colors = ButtonDefaults.colors(ButtonAppearance.Glass),
                                indication = null
                            ) {
                                Text("Glass icon")
                            }
                            Text("First: $firstClicks, second: $secondClicks")
                            Text("Appearance: ${if (glassAppearance) "Glass" else "Plain"}")
                        }
                    }
                }
            }

            val first = button("Glass action")
            val firstBounds = first.visibleBounds
            val secondBounds = button("Glass icon").visibleBounds
            val unfocused = screenshot()

            device.pressKeyCode(KeyEvent.KEYCODE_TAB)
            waitForFocus("Glass action")
            waitForScreenshot("The focused glass button must show an outline") {
                changedPixels(unfocused, it, firstBounds) > 10
            }
            device.pressKeyCode(KeyEvent.KEYCODE_F2)
            shown("Appearance: Plain")
            assertTrue("Changing appearance must retain focus", button("Glass action").isFocused)
            waitForScreenshot("Plain appearance must retain the keyboard focus outline") {
                changedPixels(unfocused, it, firstBounds) > 10
            }
            device.pressKeyCode(KeyEvent.KEYCODE_F2)
            shown("Appearance: Glass")
            assertTrue("Changing appearance must retain focus", button("Glass action").isFocused)
            waitForScreenshot("The glass focus outline must return after changing appearance") {
                changedPixels(unfocused, it, firstBounds) > 10
            }
            device.pressKeyCode(KeyEvent.KEYCODE_ENTER)
            shown("First: 1, second: 0")
            device.executeShellCommand("input mouse tap ${firstBounds.centerX()} ${firstBounds.centerY()}")
            shown("First: 2, second: 0")
            waitForScreenshot("Mouse click must not leave a keyboard focus outline") {
                changedPixels(unfocused, it, firstBounds) <= 10
            }

            device.pressKeyCode(KeyEvent.KEYCODE_TAB)
            waitForFocus("Glass action")
            waitForScreenshot("Tab must restore the keyboard focus outline") {
                changedPixels(unfocused, it, firstBounds) > 10
            }
            device.pressKeyCode(KeyEvent.KEYCODE_TAB)
            waitForFocus("Glass icon")
            assertFalse("The first button should lose focus", button("Glass action").isFocused)
            waitForScreenshot("The outline must follow focus to the basic button") {
                changedPixels(unfocused, it, firstBounds) <= 10 &&
                    changedPixels(unfocused, it, secondBounds) > 10
            }

            device.pressKeyCode(KeyEvent.KEYCODE_ENTER)
            shown("First: 2, second: 1")
            device.click(firstBounds.centerX(), firstBounds.centerY())
            shown("First: 3, second: 1")
            waitForScreenshot("Touch must not leave a keyboard focus outline") {
                changedPixels(unfocused, it, firstBounds) <= 10 &&
                    changedPixels(unfocused, it, secondBounds) <= 10
            }
        }
    }

    @Test
    fun keyboardFocusOutlineFollowsStandardButtons() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    SaltTheme {
                        var clicks by remember { mutableIntStateOf(0) }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                                .padding(32.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { clicks++ },
                                text = "Filled button",
                                modifier = Modifier.width(200.dp)
                            )
                            Button(
                                onClick = { clicks++ },
                                text = "Subtle button",
                                modifier = Modifier.width(200.dp),
                                appearance = ButtonAppearance.Subtle
                            )
                            Button(
                                onClick = { clicks++ },
                                text = "Plain button",
                                modifier = Modifier.width(200.dp),
                                appearance = ButtonAppearance.Plain
                            )
                            BasicButton(
                                onClick = { clicks++ },
                                modifier = Modifier.width(200.dp),
                                border = BorderStroke(3.dp, Color.Black)
                            ) {
                                Text("Bordered button")
                            }
                            Text("Clicks: $clicks")
                        }
                    }
                }
            }

            val names = listOf("Filled button", "Subtle button", "Plain button", "Bordered button")
            val bounds = names.map { button(it).visibleBounds }
            val unfocused = screenshot()

            names.forEachIndexed { index, name ->
                device.pressKeyCode(KeyEvent.KEYCODE_TAB)
                waitForFocus(name)
                waitForScreenshot("$name must show its keyboard focus outline") {
                    changedPixels(unfocused, it, bounds[index]) > 10 &&
                        (index == 0 || changedPixels(unfocused, it, bounds[index - 1]) <= 10)
                }
            }

            device.pressKeyCode(KeyEvent.KEYCODE_ENTER)
            shown("Clicks: 1")
            device.executeShellCommand("input mouse tap ${bounds[0].centerX()} ${bounds[0].centerY()}")
            shown("Clicks: 2")
            waitForScreenshot("Mouse click must not show a keyboard focus outline") {
                bounds.all { area -> changedPixels(unfocused, it, area) <= 10 }
            }
        }
    }

    private fun shown(text: String): UiObject2 =
        device.wait(Until.findObject(By.text(text)), 5_000)
            ?: throw AssertionError("Could not find $text")

    private fun button(text: String): UiObject2 =
        generateSequence(shown(text)) { it.parent }.firstOrNull { it.isClickable }
            ?: throw AssertionError("Could not find a clickable button for $text")

    private fun waitForFocus(text: String) {
        val deadline = SystemClock.uptimeMillis() + 5_000
        while (!button(text).isFocused && SystemClock.uptimeMillis() < deadline) {
            SystemClock.sleep(50)
        }
        assertTrue("$text did not receive keyboard focus", button(text).isFocused)
    }

    private fun screenshot(): Bitmap {
        device.waitForIdle()
        return InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
    }

    private fun waitForScreenshot(message: String, condition: (Bitmap) -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 5_000
        while (SystemClock.uptimeMillis() < deadline) {
            val image = screenshot()
            val matched = condition(image)
            image.recycle()
            if (matched) {
                return
            }
            SystemClock.sleep(50)
        }
        throw AssertionError(message)
    }

    private fun changedPixels(before: Bitmap, after: Bitmap, bounds: Rect): Int {
        val center = bounds.centerX()
        val halfWidth = bounds.width() / 6
        val bottom = minOf(bounds.bottom, bounds.top + bounds.height() / 5)
        var changed = 0
        for (y in bounds.top until bottom) {
            for (x in center - halfWidth until center + halfWidth) {
                val first = before.getPixel(x, y)
                val second = after.getPixel(x, y)
                val difference = abs(android.graphics.Color.red(first) - android.graphics.Color.red(second)) +
                    abs(android.graphics.Color.green(first) - android.graphics.Color.green(second)) +
                    abs(android.graphics.Color.blue(first) - android.graphics.Color.blue(second))
                if (difference > 80) {
                    changed++
                }
            }
        }
        return changed
    }
}
