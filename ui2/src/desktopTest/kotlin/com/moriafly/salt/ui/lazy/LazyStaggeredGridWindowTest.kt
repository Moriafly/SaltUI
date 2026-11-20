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

package com.moriafly.salt.ui.lazy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.ui.lazy.layout.LazyLayoutCacheWindow
import com.moriafly.salt.ui.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import com.moriafly.salt.ui.lazy.staggeredgrid.StaggeredGridCells
import com.moriafly.salt.ui.lazy.staggeredgrid.StaggeredGridItemSpan
import java.awt.Robot
import java.awt.event.InputEvent
import kotlin.math.roundToInt
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LazyStaggeredGridWindowTest {
    @Test
    fun changingCacheWindowKeepsFullSpanItemsReachable() = runDesktopComposeUiTest {
        lateinit var composeWindow: ComposeWindow
        var cacheWhileIdle by mutableStateOf(false)
        var selected by mutableStateOf("")

        setContent {
            Window(
                onCloseRequest = {},
                undecorated = true,
                state = rememberWindowState(size = DpSize(420.dp, 320.dp))
            ) {
                composeWindow = window
                Column(Modifier.fillMaxSize()) {
                    BasicText(
                        text = if (cacheWhileIdle) "Idle caching on" else "Idle caching off",
                        modifier = Modifier
                            .testTag("toggle-cache")
                            .clickable { cacheWhileIdle = !cacheWhileIdle }
                    )
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize().testTag("grid"),
                        cacheWindow = LazyLayoutCacheWindow(
                            ahead = 180.dp,
                            behind = 90.dp,
                            isNonScrollCachingEnabled = cacheWhileIdle
                        )
                    ) {
                        item(key = "start", span = StaggeredGridItemSpan.FullLine) {
                            BasicText("Start", Modifier.height(48.dp).testTag("start"))
                        }
                        items(count = 40, key = { it }) { index ->
                            val height = if (index % 3 == 0) 84.dp else 36.dp
                            Box(Modifier.fillMaxWidth().height(height))
                        }
                        item(key = "end", span = StaggeredGridItemSpan.FullLine) {
                            BasicText(
                                text = "End",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("end")
                                    .clickable { selected = "end" }
                            )
                        }
                    }
                }
            }
        }
        waitForIdle()
        runOnUiThread {
            composeWindow.toFront()
            composeWindow.requestFocus()
        }
        val robot = Robot().apply { autoDelay = 35 }

        fun moveTo(tag: String) {
            val center = onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot.center
            val transform = composeWindow.graphicsConfiguration.defaultTransform
            robot.mouseMove(
                composeWindow.x + (center.x / transform.scaleX).roundToInt(),
                composeWindow.y + (center.y / transform.scaleY).roundToInt()
            )
        }

        fun click(tag: String) {
            moveTo(tag)
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
        }

        waitUntil { onNodeWithTag("start").isDisplayed() }
        moveTo("grid")
        robot.mouseWheel(200)
        waitUntil(timeoutMillis = 5_000) { onNodeWithTag("end").isDisplayed() }

        click("toggle-cache")
        waitUntil { cacheWhileIdle }
        click("end")
        waitUntil { selected == "end" }

        moveTo("grid")
        robot.mouseWheel(-200)
        waitUntil(timeoutMillis = 5_000) { onNodeWithTag("start").isDisplayed() }
    }
}
