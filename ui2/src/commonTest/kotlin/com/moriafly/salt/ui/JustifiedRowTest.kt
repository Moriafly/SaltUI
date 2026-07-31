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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class, UnstableSaltUiApi::class)
class JustifiedRowTest {
    @Test
    fun defaultParameters_placeChildrenAtOppositeEdges() = runComposeUiTest {
        setContent {
            SaltTheme {
                Box(
                    modifier = Modifier
                        .width(ROW_WIDTH)
                        .testTag(ROW_TAG)
                ) {
                    JustifiedRow(
                        startContent = {
                            TestContent(START_TAG, width = 30.dp, height = 10.dp)
                        },
                        endContent = {
                            TestContent(END_TAG, width = 20.dp, height = 20.dp)
                        }
                    )
                }
            }
        }

        assertBounds(START_TAG, left = 0.dp, top = 0.dp, width = 30.dp, height = 10.dp)
        assertBounds(END_TAG, left = 80.dp, top = 0.dp, width = 20.dp, height = 20.dp)
    }

    @Test
    fun contentFits_placesChildrenAtOppositeEdges() = runComposeUiTest {
        setContent {
            TestJustifiedRow(
                startContent = {
                    TestContent(START_TAG, width = 30.dp, height = 10.dp)
                },
                endContent = {
                    TestContent(END_TAG, width = 20.dp, height = 20.dp)
                }
            )
        }

        assertBounds(START_TAG, left = 0.dp, top = 0.dp, width = 30.dp, height = 10.dp)
        assertBounds(END_TAG, left = 80.dp, top = 0.dp, width = 20.dp, height = 20.dp)
    }

    @Test
    fun overflowingEnd_preservesSmallerStartWidth() = runComposeUiTest {
        setContent {
            TestJustifiedRow(
                startContent = {
                    TestContent(START_TAG, width = 20.dp)
                },
                endContent = {
                    TestContent(END_TAG, width = 100.dp)
                }
            )
        }

        assertBounds(START_TAG, left = 0.dp, top = 0.dp, width = 20.dp, height = 10.dp)
        assertBounds(END_TAG, left = 30.dp, top = 0.dp, width = 70.dp, height = 10.dp)
    }

    @Test
    fun overflowingStart_preservesSmallerEndWidth() = runComposeUiTest {
        setContent {
            TestJustifiedRow(
                startContent = {
                    TestContent(START_TAG, width = 100.dp)
                },
                endContent = {
                    TestContent(END_TAG, width = 20.dp)
                }
            )
        }

        assertBounds(START_TAG, left = 0.dp, top = 0.dp, width = 70.dp, height = 10.dp)
        assertBounds(END_TAG, left = 80.dp, top = 0.dp, width = 20.dp, height = 10.dp)
    }

    @Test
    fun bothSidesOverflow_splitsAvailableWidthEvenly() = runComposeUiTest {
        setContent {
            TestJustifiedRow(
                startContent = {
                    TestContent(START_TAG, width = 100.dp)
                },
                endContent = {
                    TestContent(END_TAG, width = 100.dp)
                }
            )
        }

        assertBounds(START_TAG, left = 0.dp, top = 0.dp, width = 45.dp, height = 10.dp)
        assertBounds(END_TAG, left = 55.dp, top = 0.dp, width = 45.dp, height = 10.dp)
    }

    @Test
    fun zeroWidthEndContent_stillReservesSpaceBetween() = runComposeUiTest {
        setContent {
            TestJustifiedRow(
                startContent = {
                    TestContent(START_TAG, width = 100.dp)
                },
                endContent = {
                    TestContent(END_TAG, width = 0.dp)
                }
            )
        }

        assertBounds(START_TAG, left = 0.dp, top = 0.dp, width = 90.dp, height = 10.dp)
        assertBounds(END_TAG, left = 100.dp, top = 0.dp, width = 0.dp, height = 10.dp)
    }

    @Test
    fun emptyEndContent_doesNotReserveSpaceBetween() = runComposeUiTest {
        setContent {
            TestJustifiedRow(
                startContent = {
                    TestContent(START_TAG, width = 100.dp)
                },
                endContent = {}
            )
        }

        assertBounds(START_TAG, left = 0.dp, top = 0.dp, width = 100.dp, height = 10.dp)
    }

    @Test
    fun emptyStartContent_doesNotReserveSpaceBetween() = runComposeUiTest {
        setContent {
            TestJustifiedRow(
                startContent = {},
                endContent = {
                    TestContent(END_TAG, width = 100.dp)
                }
            )
        }

        assertBounds(END_TAG, left = 0.dp, top = 0.dp, width = 100.dp, height = 10.dp)
    }

    @Test
    fun multipleChildren_preservesGroupAlignmentAndEndAlignment() = runComposeUiTest {
        setContent {
            TestJustifiedRow(
                modifier = Modifier
                    .width(ROW_WIDTH)
                    .height(40.dp)
                    .testTag(ROW_TAG),
                verticalAlignment = Alignment.CenterVertically,
                startContent = {
                    TestContent(START_TAG, width = 20.dp, height = 10.dp)
                },
                endContent = {
                    TestContent(END_TAG, width = 30.dp, height = 20.dp)
                    TestContent(SHORT_END_TAG, width = 10.dp, height = 5.dp)
                }
            )
        }

        assertBounds(START_TAG, left = 0.dp, top = 15.dp, width = 20.dp, height = 10.dp)
        assertBounds(END_TAG, left = 70.dp, top = 10.dp, width = 30.dp, height = 20.dp)
        assertBounds(SHORT_END_TAG, left = 90.dp, top = 10.dp, width = 10.dp, height = 5.dp)
    }

    @Test
    fun rtl_placesStartAndEndAtRelativeEdges() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                TestJustifiedRow(
                    startContent = {
                        TestContent(START_TAG, width = 20.dp)
                    },
                    endContent = {
                        TestContent(END_TAG, width = 30.dp)
                    }
                )
            }
        }

        assertBounds(START_TAG, left = 80.dp, top = 0.dp, width = 20.dp, height = 10.dp)
        assertBounds(END_TAG, left = 0.dp, top = 0.dp, width = 30.dp, height = 10.dp)
    }

    @Test
    fun unboundedWidth_wrapsBothChildrenAndSpaceBetween() = runComposeUiTest {
        setContent {
            UnboundedWidth {
                TestJustifiedRow(
                    modifier = Modifier.testTag(ROW_TAG),
                    startContent = {
                        TestContent(START_TAG, width = 20.dp)
                    },
                    endContent = {
                        TestContent(END_TAG, width = 30.dp)
                    }
                )
            }
        }

        assertBounds(START_TAG, left = 0.dp, top = 0.dp, width = 20.dp, height = 10.dp)
        assertBounds(END_TAG, left = 30.dp, top = 0.dp, width = 30.dp, height = 10.dp)
    }

    private fun ComposeUiTest.assertBounds(
        tag: String,
        left: Dp,
        top: Dp,
        width: Dp,
        height: Dp
    ) {
        val rowBounds = onNodeWithTag(ROW_TAG).getUnclippedBoundsInRoot()
        val childBounds = onNodeWithTag(tag).getUnclippedBoundsInRoot()

        assertEquals(left, childBounds.left - rowBounds.left, "$tag left")
        assertEquals(top, childBounds.top - rowBounds.top, "$tag top")
        assertEquals(width, childBounds.right - childBounds.left, "$tag width")
        assertEquals(height, childBounds.bottom - childBounds.top, "$tag height")
    }

    @Composable
    private fun TestJustifiedRow(
        startContent: @Composable () -> Unit,
        endContent: @Composable () -> Unit,
        modifier: Modifier = Modifier
            .width(ROW_WIDTH)
            .testTag(ROW_TAG),
        verticalAlignment: Alignment.Vertical = Alignment.Top
    ) {
        JustifiedRow(
            startContent = startContent,
            endContent = endContent,
            modifier = modifier,
            verticalAlignment = verticalAlignment,
            spaceBetween = SPACE_BETWEEN
        )
    }

    @Composable
    private fun TestContent(
        tag: String,
        width: Dp,
        height: Dp = 10.dp
    ) {
        Box(
            modifier = Modifier
                .width(width)
                .height(height)
                .testTag(tag)
        )
    }

    @Composable
    private fun UnboundedWidth(content: @Composable () -> Unit) {
        Layout(content = content) { measurables, constraints ->
            val placeable = measurables.single().measure(
                constraints.copy(
                    minWidth = 0,
                    maxWidth = Constraints.Infinity
                )
            )
            layout(width = placeable.width, height = placeable.height) {
                placeable.place(x = 0, y = 0)
            }
        }
    }

    private companion object {
        val ROW_WIDTH = 100.dp
        val SPACE_BETWEEN = 10.dp

        const val ROW_TAG = "row"
        const val START_TAG = "start"
        const val END_TAG = "end"
        const val SHORT_END_TAG = "shortEnd"
    }
}
