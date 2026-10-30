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

import android.content.pm.ActivityInfo
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream

@RunWith(AndroidJUnit4::class)
class SampleShowcaseTest {
    private val device get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Test
    fun itemEditingAndDropdownUpdateDisplayedValues() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
            waitForOrientation(isLandscape = false)
            click(By.text("Items"))
            val edit = shown(By.clazz("android.widget.EditText"))
            edit.click()
            dismissKeyboardOnboarding()
            edit.click()
            shown(By.clazz("android.widget.EditText").focused(true))
            device.executeShellCommand("input text Alice")
            shown(By.text("Alice"))
            device.pressBack()

            click(By.text("Quality"))
            click(By.text("Low"))
            shown(By.text("Low"))
            scrollTo("Notifications")
            shown(By.text("Alice"))
        }
    }

    @Test
    fun inputDialogAcceptsKeyboardInputAndKeepsConfirmedValue() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
            waitForOrientation(isLandscape = false)
            scrollTo("Dialog")
            click(By.text("Dialog"))
            click(By.text("InputDialog"))
            shown(By.text("Edit name"))
            val edit = shown(By.clazz("android.widget.EditText"))
            edit.click()
            dismissKeyboardOnboarding()
            edit.click()
            shown(By.clazz("android.widget.EditText").focused(true))
            device.executeShellCommand("input text Ada")
            shown(By.text("Ada"))
            click(By.text("Save"))
            shown(By.text("Saved name"))
            shown(By.text("Ada"))
        }
    }

    @Test
    fun bottomSheetExpandsWithTouchInTheAppWindow() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
            waitForOrientation(isLandscape = false)
            click(By.text("Bottom sheet scaffold"))
            val handle = shown(By.text("Now playing")).visibleBounds
            device.swipe(
                handle.centerX(),
                handle.centerY(),
                handle.centerX(),
                handle.centerY() - device.displayHeight / 3,
                20
            )
            shown(By.text("The Night Owls"))
        }
    }

    @Test
    fun landscapeSidebarNavigatesToItems() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            waitForOrientation(isLandscape = true)
            shown(By.text("Main"))
            click(By.text("Items"))
            shown(By.text("Enter a name"))
        }
    }

    private fun shown(selector: BySelector): UiObject2 {
        val result = device.wait(Until.findObject(selector), 5_000)
        if (result == null) {
            val hierarchy = ByteArrayOutputStream()
            device.dumpWindowHierarchy(hierarchy)
            val labels = Regex("text=\"([^\"]+)\"")
                .findAll(hierarchy.toString())
                .map { it.groupValues[1] }
                .toList()
            assertNotNull("Could not find $selector; visible text: $labels", result)
        }
        return result!!
    }

    private fun click(selector: BySelector) {
        shown(selector).click()
    }

    private fun waitForOrientation(isLandscape: Boolean) {
        val deadline = SystemClock.uptimeMillis() + 5_000
        while ((device.displayWidth > device.displayHeight) != isLandscape &&
            SystemClock.uptimeMillis() < deadline
        ) {
            SystemClock.sleep(50)
        }
        assertTrue((device.displayWidth > device.displayHeight) == isLandscape)
        device.waitForIdle()
    }

    private fun dismissKeyboardOnboarding() {
        if (device.wait(Until.hasObject(By.text("Try out your stylus")), 2_000)) {
            click(By.text("Cancel"))
        }
    }

    private fun scrollTo(text: String) {
        repeat(5) {
            val target = device.findObject(By.text(text))
            if (target != null && target.visibleBounds.centerY() < device.displayHeight * 4 / 5) {
                return
            }
            val width = device.displayWidth
            val height = device.displayHeight
            device.swipe(width / 2, height * 4 / 5, width / 2, height / 3, 20)
        }
        shown(By.text(text))
    }
}
