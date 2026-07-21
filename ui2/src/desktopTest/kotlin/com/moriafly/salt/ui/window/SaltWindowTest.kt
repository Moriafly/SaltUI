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

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.util.findSkiaLayer
import java.awt.Color
import java.awt.Dimension
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(
    ExperimentalTestApi::class,
    ExperimentalComposeUiApi::class,
    UnstableSaltUiApi::class
)
class SaltWindowTest {
    @Test
    fun saltWindow_displaysContent() = runDesktopComposeUiTest {
        lateinit var composeWindow: ComposeWindow
        setContent {
            SaltTheme {
                SaltWindow(
                    onCloseRequest = {},
                    state = rememberWindowState(),
                    title = "Test Window",
                    init = { composeWindow = it }
                ) {
                    Box(
                        modifier = Modifier
                            .testTag("saltWindowContent")
                    ) {
                        Text("Hello SaltWindow")
                    }
                }
            }
        }

        onNodeWithTag("saltWindowContent").assertIsDisplayed()
        assertEquals(Color.BLACK, composeWindow.background)
    }

    @Test
    fun saltWindow_undecoratedTransparent_displaysContent() = runDesktopComposeUiTest {
        setContent {
            SaltTheme {
                SaltWindow(
                    onCloseRequest = {},
                    state = rememberWindowState(),
                    title = "Transparent Window",
                    decoration = WindowDecoration.Undecorated(),
                    transparent = true
                ) {
                    Box(
                        modifier = Modifier
                            .testTag("transparentWindowContent")
                    ) {
                        Text("Hello Transparent SaltWindow")
                    }
                }
            }
        }

        onNodeWithTag("transparentWindowContent").assertIsDisplayed()
    }

    @Test
    fun saltWindow_macosVibrancy_displaysContent() {
        if (OS.current !is OS.MacOS) return

        runDesktopComposeUiTest {
            lateinit var composeWindow: ComposeWindow
            var backgroundType by mutableStateOf(SaltWindowBackgroundType.Vibrancy)
            setContent {
                SaltTheme {
                    SaltWindow(
                        onCloseRequest = {},
                        state = rememberWindowState(),
                        title = "Vibrancy Window",
                        properties = SaltWindowProperties.default(
                            backgroundType = backgroundType
                        ),
                        init = { composeWindow = it }
                    ) {
                        Box(
                            modifier = Modifier
                                .testTag("vibrancyWindowContent")
                        ) {
                            Text("Hello Vibrancy SaltWindow")
                        }
                    }
                }
            }

            onNodeWithTag("vibrancyWindowContent").assertIsDisplayed()
            assertEquals(0, composeWindow.findSkiaLayer()?.background?.alpha)

            runOnUiThread {
                composeWindow.size = Dimension(720, 540)
            }
            waitForIdle()
            assertEquals(Dimension(720, 540), composeWindow.size)
            onNodeWithTag("vibrancyWindowContent").assertIsDisplayed()

            runOnUiThread {
                backgroundType = SaltWindowBackgroundType.None
            }
            waitForIdle()
            onNodeWithTag("vibrancyWindowContent").assertIsDisplayed()
            assertEquals(Color.BLACK, composeWindow.background)
        }
    }
}
