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

package com.moriafly.salt.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalFoundationApi::class, ExperimentalTestApi::class)
class ReverseScrollInteractionTest {
    @Test
    fun reverseScrollShowsMatchingIndicatorAndRespondsToMouseWheel() =
        runDesktopComposeUiTest {
            val state = ScrollState(0)

            setContent {
                val indicatorState = state.scrollIndicatorState
                val thumbTravel = with(LocalDensity.current) { 50.dp.roundToPx() }
                val scrollRange =
                    (indicatorState.contentSize - indicatorState.viewportSize).coerceAtLeast(1)
                val thumbOffset =
                    (indicatorState.scrollOffset.toFloat() / scrollRange * thumbTravel).roundToInt()

                Box(modifier = Modifier.size(width = 120.dp, height = 100.dp)) {
                    Column(
                        modifier =
                            Modifier
                                .size(width = 100.dp, height = 100.dp)
                                .testTag(ScrollerTag)
                                .verticalScroll(
                                    state = state,
                                    overscrollEffect = null,
                                    reverseScrolling = true,
                                )
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(Color.Red)
                                    .testTag(FirstPageTag)
                        )
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(Color.Blue)
                                    .testTag(LastPageTag)
                        )
                    }

                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .width(10.dp)
                                .height(100.dp)
                                .background(Color.LightGray)
                                .testTag(TrackTag)
                    )
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .offset { IntOffset(x = 0, y = thumbOffset) }
                                .size(width = 10.dp, height = 50.dp)
                                .background(Color.DarkGray)
                                .testTag(ThumbTag)
                    )
                }
            }

            waitForIdle()
            onNodeWithTag(LastPageTag).assertIsDisplayed()
            onNodeWithTag(FirstPageTag).assertIsNotDisplayed()
            assertThumbAtEnd()

            onNodeWithTag(ScrollerTag).performMouseInput {
                moveTo(center)
                scroll(-10_000f)
            }
            waitForIdle()
            assertEquals(
                expected = state.maxValue,
                actual = state.value,
                message = "Mouse wheel should reach the opposite end",
            )

            onNodeWithTag(FirstPageTag).assertIsDisplayed()
            onNodeWithTag(LastPageTag).assertIsNotDisplayed()
            assertThumbAtStart()
        }

    private fun ComposeUiTest.assertThumbAtEnd() {
        val trackBounds = onNodeWithTag(TrackTag).fetchSemanticsNode().boundsInRoot
        val thumbBounds = onNodeWithTag(ThumbTag).fetchSemanticsNode().boundsInRoot
        assertEquals(trackBounds.bottom, thumbBounds.bottom)
    }

    private fun ComposeUiTest.assertThumbAtStart() {
        val trackBounds = onNodeWithTag(TrackTag).fetchSemanticsNode().boundsInRoot
        val thumbBounds = onNodeWithTag(ThumbTag).fetchSemanticsNode().boundsInRoot
        assertEquals(trackBounds.top, thumbBounds.top)
    }

    private companion object {
        const val ScrollerTag = "reverseScroller"
        const val FirstPageTag = "firstPage"
        const val LastPageTag = "lastPage"
        const val TrackTag = "scrollTrack"
        const val ThumbTag = "scrollThumb"
    }
}
