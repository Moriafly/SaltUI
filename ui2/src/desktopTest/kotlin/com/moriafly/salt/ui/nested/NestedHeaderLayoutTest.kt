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

package com.moriafly.salt.ui.nested

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.ui.UnstableSaltUiApi
import java.awt.Robot
import java.awt.event.InputEvent
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, UnstableSaltUiApi::class)
class NestedHeaderLayoutTest {
    @Test
    fun pagerListFocusAndScrollingKeepScreenInsets() = runDesktopComposeUiTest {
        lateinit var window: ComposeWindow
        val headerState = NestedHeaderState()
        var selected = -1
        setContent {
            Window(
                onCloseRequest = {},
                title = "Nested header focus regression",
                undecorated = true,
                alwaysOnTop = true,
                state = rememberWindowState(
                    position = WindowPosition.Absolute(100.dp, 100.dp),
                    size = DpSize(500.dp, 500.dp)
                )
            ) {
                SideEffect { window = this.window }
                NestedHeaderLayout(
                    header = {
                        Box(Modifier.fillMaxWidth().height(120.dp).testTag("header"))
                    },
                    modifier = Modifier.fillMaxSize().testTag("layout"),
                    state = headerState,
                    contentPadding = PaddingValues(start = 20.dp, top = 48.dp, end = 32.dp, bottom = 64.dp)
                ) { innerPadding ->
                    HorizontalPager(
                        state = rememberPagerState { 1 },
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = innerPadding
                    ) {
                        val focusRequester = remember { FocusRequester() }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                                .testTag("list")
                                .focusRequester(focusRequester)
                                .focusable()
                        ) {
                            items(50) { index ->
                                Box(
                                    Modifier.fillMaxWidth().height(40.dp)
                                        .background(Color.LightGray)
                                        .testTag("row-$index")
                                        .clickable {
                                            selected = index
                                            focusRequester.requestFocus()
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
        waitForIdle()
        runOnUiThread {
            window.toFront()
            window.requestFocus()
        }
        val robot = Robot().apply { autoDelay = 40 }
        val scale = bounds("layout").width / 500f
        assertEquals(48f * scale, bounds("header").top, 1f)
        assertEquals(168f * scale, bounds("list").top, 1f)

        click(robot, window, "row-0")
        waitUntil(timeoutMillis = 5_000) { selected == 0 }
        waitForIdle()
        onNodeWithTag("list").assertIsFocused()
        assertEquals(0f, headerState.offset, 1f, "Focusing a visible list must not collapse the header")
        assertEquals(168f * scale, bounds("row-0").top, 1f)

        robot.mouseWheel(30)
        waitUntil(timeoutMillis = 5_000) { headerState.offset == headerState.minOffset }
        waitForIdle()
        assertEquals(48f * scale, bounds("list").top, 1f, "Collapsed list must remain below the screen inset")
        assertEquals(20f * scale, bounds("list").left, 1f)
        assertEquals(468f * scale, bounds("list").right, 1f)
        assertEquals(436f * scale, bounds("list").bottom, 1f)

        onNodeWithTag("list").assertIsFocused()
        robot.mouseWheel(200)
        waitUntil(timeoutMillis = 5_000) { onAllNodesWithTagCount("row-49") == 1 }
        waitForIdle()
        assertTrue(bounds("row-49").top >= bounds("list").top)
        assertEquals(bounds("list").bottom, bounds("row-49").bottom, 1f)
        click(robot, window, "row-49")
        waitUntil(timeoutMillis = 5_000) { selected == 49 }
        onNodeWithTag("list").assertIsFocused()
        assertEquals(48f * scale, bounds("list").top, 1f)
    }

    @Test
    fun lazyListUpdatesPaddingAndDirectionWithoutLosingContent() = runDesktopComposeUiTest {
        val headerState = NestedHeaderState()
        var padding by mutableStateOf(PaddingValues(start = 20.dp, top = 48.dp, end = 32.dp, bottom = 64.dp))
        var direction by mutableStateOf(LayoutDirection.Ltr)
        setContent {
            Window(
                onCloseRequest = {},
                undecorated = true,
                state = rememberWindowState(size = DpSize(500.dp, 500.dp))
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides direction) {
                    NestedHeaderLayout(
                        header = { Box(Modifier.fillMaxWidth().height(120.dp).testTag("header")) },
                        modifier = Modifier.fillMaxSize().testTag("layout"),
                        state = headerState,
                        contentPadding = padding
                    ) { innerPadding ->
                        LazyColumn(Modifier.fillMaxSize(), contentPadding = innerPadding) {
                            item { Box(Modifier.fillMaxWidth().height(40.dp).testTag("row")) }
                        }
                    }
                }
            }
        }
        waitForIdle()
        val scale = bounds("layout").width / 500f
        assertEquals(168f * scale, bounds("row").top, 1f)
        assertEquals(bounds("header").left, bounds("row").left)
        assertEquals(bounds("header").right, bounds("row").right)

        runOnUiThread {
            headerState.collapse()
            direction = LayoutDirection.Rtl
            padding = PaddingValues(start = 16.dp, top = 60.dp, end = 36.dp, bottom = 72.dp)
        }
        waitForIdle()
        assertEquals(60f * scale, bounds("row").top, 1f)
        assertEquals(36f * scale, bounds("row").left, 1f)
        assertEquals(484f * scale, bounds("row").right, 1f)

        runOnUiThread { headerState.expand() }
        waitForIdle()
        assertEquals(60f * scale, bounds("header").top, 1f)
        assertEquals(180f * scale, bounds("row").top, 1f)
        assertEquals(bounds("header").left, bounds("row").left)
        assertEquals(bounds("header").right, bounds("row").right)

        runOnUiThread {
            padding = PaddingValues(0.dp)
            headerState.collapse()
        }
        waitForIdle()
        assertEquals(0f, bounds("row").top, 1f)
        assertEquals(bounds("layout").width, bounds("row").width, 1f)
    }

    private fun ComposeUiTest.bounds(tag: String) = onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot

    private fun ComposeUiTest.onAllNodesWithTagCount(tag: String) =
        onAllNodesWithTag(tag).fetchSemanticsNodes().size

    private fun ComposeUiTest.click(robot: Robot, window: ComposeWindow, tag: String) {
        val center = bounds(tag).center
        val transform = window.graphicsConfiguration.defaultTransform
        robot.mouseMove(
            window.x + (center.x / transform.scaleX).roundToInt(),
            window.y + (center.y / transform.scaleY).roundToInt()
        )
        waitForIdle()
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
        waitForIdle()
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
        waitForIdle()
    }
}
