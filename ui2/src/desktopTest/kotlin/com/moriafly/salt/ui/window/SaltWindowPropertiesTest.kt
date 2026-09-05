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

package com.moriafly.salt.ui.window

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class, ExperimentalComposeUiApi::class, UnstableSaltUiApi::class)
class SaltWindowPropertiesTest {
    @Test
    fun captionBarHeightUsesPlatformDefault() {
        assertEquals(
            expected = 52.dp,
            actual = defaultCaptionBarHeight(OS.MacOS(version = "26.0", build = ""))
        )
        assertEquals(
            expected = 40.dp,
            actual = defaultCaptionBarHeight(OS.Windows(windowsBuild = 26100))
        )
        assertEquals(
            expected = 40.dp,
            actual = defaultCaptionBarHeight(OS.Linux(version = "", distro = ""))
        )
    }

    @Test
    fun captionAppearanceUpdatesReadersWithoutRecomposingWindowContent() = runDesktopComposeUiTest {
        var darkCaption by mutableStateOf(false)
        val contentCompositions = AtomicInteger()
        setContent {
            SaltTheme {
                SaltWindow(
                    onCloseRequest = {},
                    title = "Caption property invalidation",
                    state = rememberWindowState(size = DpSize(640.dp, 480.dp)),
                    properties = SaltWindowProperties.default(captionButtonIsDarkTheme = darkCaption)
                ) {
                    Column {
                        CaptionAppearanceReadout()
                        UnrelatedWindowContent(contentCompositions)
                    }
                }
            }
        }
        onNodeWithText("Light caption", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Persistent library content").assertIsDisplayed()
        val initialCompositions = runOnIdle { contentCompositions.get() }

        runOnIdle { darkCaption = true }
        waitUntil(timeoutMillis = 5_000) {
            onAllNodesWithText("Dark caption", useUnmergedTree = true).fetchSemanticsNodes().size == 1
        }
        onNodeWithText("Dark caption", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Persistent library content").assertIsDisplayed()
        runOnIdle { assertEquals(initialCompositions, contentCompositions.get()) }

        runOnIdle { darkCaption = false }
        waitUntil(timeoutMillis = 5_000) {
            onAllNodesWithText("Light caption", useUnmergedTree = true).fetchSemanticsNodes().size == 1
        }
        onNodeWithText("Light caption", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Persistent library content").assertIsDisplayed()
        runOnIdle { assertEquals(initialCompositions, contentCompositions.get()) }
    }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun CaptionAppearanceReadout() {
    val properties = LocalSaltWindowProperties.current
    Text(if (properties.captionButtonIsDarkTheme) "Dark caption" else "Light caption")
}

@Composable
private fun UnrelatedWindowContent(compositions: AtomicInteger) {
    SideEffect { compositions.incrementAndGet() }
    Text("Persistent library content")
}
