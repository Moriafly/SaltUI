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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SwitcherTest {
    @Test
    fun zeroDimensionsRemainInvisibleAndRecoverAfterResizing() =
        runComposeUiTest {
            var dimensions by mutableStateOf(DpSize.Zero)
            var checked by mutableStateOf(false)
            val colors = SaltColors.defaultLight(onHighlight = Color.White)
            setContent {
                SaltTheme(
                    dynamicColors = SaltDynamicColors(
                        light = colors,
                        dark = colors
                    )
                ) {
                    CompositionLocalProvider(
                        LocalDensity provides Density(3f)
                    ) {
                        Box(
                            Modifier.size(
                                46.dp,
                                26.dp
                            ).background(Color.Black).testTag("host")
                        ) {
                            Switcher(
                                state = checked,
                                modifier = Modifier.width(
                                    dimensions.width
                                ).height(dimensions.height)
                            )
                        }
                    }
                }
            }
            mainClock.autoAdvance = false
            for (zeroSize in listOf(
                DpSize(
                    0.dp,
                    26.dp
                ),
                DpSize(
                    46.dp,
                    0.dp
                ),
                DpSize.Zero
            )) {
                for (state in listOf(
                    true,
                    false
                )) {
                    runOnUiThread {
                        dimensions = zeroSize
                        checked = state
                    }
                    repeat(
                        16
                    ) {
                        mainClock.advanceTimeBy(64)
                        val pixels = onNodeWithTag("host").captureToImage().toPixelMap()
                        for (y in 0 until pixels.height) {
                            for (x in 0 until pixels.width) {
                                assertTrue(
                                    pixels[x, y] == Color.Black,
                                    "Zero size $zeroSize must not draw outside its bounds"
                                )
                            }
                        }
                    }
                    runOnUiThread {
                        dimensions = DpSize(
                            46.dp,
                            26.dp
                        )
                    }
                    mainClock.advanceTimeByFrame()
                    val pixels = onNodeWithTag("host").captureToImage().toPixelMap()
                    val icon = readIcon(
                        pixels.width,
                        pixels.height
                    ) { x, y ->
                        val color = pixels[x, y]
                        color.red > 0.8f && color.green > 0.8f && color.blue > 0.8f
                    }
                    assertTrue(
                        icon.hollow != state,
                        "Restoring the size must render the selected state"
                    )
                }
            }
            mainClock.autoAdvance = true
        }

    @Test
    fun morphsAndBouncesBetweenOAndI() = verifyMorph(LayoutDirection.Ltr)

    @Test
    fun mirrorsMorphAndTravelInRtl() = verifyMorph(LayoutDirection.Rtl)

    private fun verifyMorph(direction: LayoutDirection) =
        runComposeUiTest {
            var checked by mutableStateOf(false)
            val colors = SaltColors.defaultLight(onHighlight = Color.White)
            setContent {
                SaltTheme(
                    dynamicColors = SaltDynamicColors(
                        light = colors,
                        dark = colors
                    )
                ) {
                    CompositionLocalProvider(
                        LocalDensity provides Density(3f),
                        LocalLayoutDirection provides direction
                    ) {
                        Box(
                            Modifier.background(Color.Black)
                        ) {
                            Switcher(
                                checked,
                                Modifier.testTag("switcher")
                            )
                        }
                    }
                }
            }

            fun toggle() {
                runOnUiThread { checked = !checked }
                waitForIdle()
            }

            fun frame(verifyHole: Boolean = false): IconFrame {
                val image = onNodeWithTag("switcher").captureToImage()
                val pixels = image.toPixelMap()
                if (verifyHole) {
                    val centerX = if (direction == LayoutDirection.Ltr) 39 else 99
                    val centerY = pixels.height / 2
                    val holeColor = pixels[centerX, centerY]
                    val trackColor = pixels[pixels.width / 2, centerY]
                    assertTrue(
                        kotlin.math.abs(holeColor.red - trackColor.red) < 0.01f &&
                            kotlin.math.abs(holeColor.green - trackColor.green) < 0.01f &&
                            kotlin.math.abs(holeColor.blue - trackColor.blue) < 0.01f,
                        "The O's center must reveal the actual track, with no icon fill"
                    )
                }
                return readIcon(
                    pixels.width,
                    pixels.height
                ) { x, y ->
                    val color = pixels[x, y]
                    color.red > 0.8f && color.green > 0.8f && color.blue > 0.8f
                }
            }

            fun isTrackContracted(): Boolean {
                val pixels = onNodeWithTag("switcher").captureToImage().toPixelMap()
                return pixels[1, pixels.height / 2] == Color.Black
            }

            val off = frame(verifyHole = true)
            assertTrue(
                !isTrackContracted(),
                "The resting track must fill its bounds"
            )
            assertTrue(
                off.hollow,
                "Off must display an O with a transparent center"
            )
            assertTrue(
                kotlin.math.abs(off.width - off.height) <= 2,
                "The resting O must be round"
            )
            assertTrue(
                kotlin.math.abs(off.height - 46.8f) <= 2,
                "O outer diameter must match SwitcherX"
            )
            assertTrue(
                kotlin.math.abs(off.holeWidth - 20.8f) <= 2,
                "O inner diameter must match SwitcherX"
            )

            mainClock.autoAdvance = false
            toggle()
            mainClock.advanceTimeBy(48)
            assertTrue(
                isTrackContracted(),
                "The track must contract while the icon travels"
            )
            mainClock.advanceTimeBy(192)
            val squeezed = frame()
            assertTrue(
                !isTrackContracted(),
                "The track must recover before the icon finishes bouncing"
            )
            mainClock.advanceTimeBy(800)
            val on = frame()
            assertTrue(
                !on.hollow,
                "On must display a solid I"
            )
            assertTrue(
                on.height > on.width * 3,
                "The I must be a narrow vertical bar"
            )
            assertTrue(
                kotlin.math.abs(on.width - 13f) <= 1,
                "I width must match SwitcherX"
            )
            assertTrue(
                kotlin.math.abs(on.width - (off.width - off.holeWidth) / 2f) <= 2,
                "The I must be as thick as the O's wall"
            )
            assertTrue(
                squeezed.width < on.width,
                "The I must squeeze past its resting width"
            )
            assertTrue(
                if (direction == LayoutDirection.Ltr) {
                    on.centerX > off.centerX
                } else {
                    on.centerX < off.centerX
                },
                "The icon must travel toward the logical end"
            )

            toggle()
            assertTrue(!checked)
            mainClock.advanceTimeBy(176)
            val stretched = frame()
            assertTrue(
                stretched.width > off.width,
                "The O must expand past its resting width"
            )
            mainClock.advanceTimeBy(800)
            assertTrue(
                frame() == off,
                "The reverse animation must return to the original O"
            )

            toggle()
            mainClock.advanceTimeBy(80)
            val beforeReversal = frame()
            toggle()
            mainClock.advanceTimeByFrame()
            val afterReversal = frame()
            assertTrue(
                kotlin.math.abs(afterReversal.centerX - beforeReversal.centerX) <= 6,
                "Reversing mid-travel must continue from the visible position without teleporting"
            )
            mainClock.advanceTimeBy(1000)
            assertTrue(
                !checked && frame() == off,
                "Rapid toggles must settle at the latest state"
            )
            mainClock.autoAdvance = true
        }

    private fun readIcon(
        width: Int,
        height: Int,
        isIcon: (Int, Int) -> Boolean
    ): IconFrame {
        val points = buildList {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    if (isIcon(
                            x,
                            y
                        )
                    ) {
                        add(x to y)
                    }
                }
            }
        }
        assertTrue(
            points.isNotEmpty(),
            "The icon must stay visible throughout the morph"
        )
        val left = points.minOf { it.first }
        val right = points.maxOf { it.first }
        val top = points.minOf { it.second }
        val bottom = points.maxOf { it.second }
        return IconFrame(
            width = right - left + 1,
            height = bottom - top + 1,
            centerX = (left + right) / 2,
            hollow = !isIcon(
                (left + right) / 2,
                (top + bottom) / 2
            ),
            holeWidth = (left..right).count {
                !isIcon(
                    it,
                    (top + bottom) / 2
                )
            }
        )
    }

    private data class IconFrame(
        val width: Int,
        val height: Int,
        val centerX: Int,
        val hollow: Boolean,
        val holeWidth: Int
    )
}
