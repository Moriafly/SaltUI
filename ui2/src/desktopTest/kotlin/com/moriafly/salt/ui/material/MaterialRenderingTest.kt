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

package com.moriafly.salt.ui.material

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.SaltConfigs
import com.moriafly.salt.ui.SaltMaterial
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, UnstableSaltUiApi::class)
class MaterialRenderingTest {
    @Test
    fun blurryGlassUpdatesCapturedSource() = verifySourceUpdates(MaterialType.BlurryGlass)

    @Test
    fun acrylicUpdatesCapturedSource() = verifySourceUpdates(MaterialType.Acrylic)

    @Test
    fun micaUpdatesAppearanceWhenThemeChanges() = verifySourceUpdates(MaterialType.Mica)

    @Test
    fun premiumUpdatesCapturedSource() = verifySourceUpdates(MaterialType.Premium)

    private fun verifySourceUpdates(type: MaterialType) = runComposeUiTest {
        var darkTheme by mutableStateOf(false)
        var layer by mutableStateOf(MaterialLayer.Background)
        var sourceColor by mutableStateOf(Color.Black)
        setContent {
            SaltTheme(
                configs = SaltConfigs.default(isDarkTheme = darkTheme),
                material = SaltMaterial(type)
            ) {
                Box(Modifier.size(160.dp)) {
                    MaterialSource(materialSelf = false) {
                        Box(Modifier.fillMaxSize().background(sourceColor))
                    }
                    Box(
                        Modifier.fillMaxSize().testTag("material").then(
                            when (layer) {
                                MaterialLayer.Background -> Modifier.material()
                                MaterialLayer.SubBackground -> Modifier.subMaterial()
                            }
                        )
                    )
                }
            }
        }

        fun renderedBrightness(): Float {
            mainClock.advanceTimeBy(100)
            val pixels = onNodeWithTag("material").captureToImage().toPixelMap()
            var brightness = 0f
            for (y in pixels.height / 2 - 8 until pixels.height / 2 + 8) {
                for (x in pixels.width / 2 - 8 until pixels.width / 2 + 8) {
                    val pixel = pixels[x, y]
                    brightness += (pixel.red + pixel.green + pixel.blue) / 3f
                }
            }
            return brightness / 256f
        }

        for (isDark in listOf(false, true)) {
            for (materialLayer in MaterialLayer.entries) {
                runOnIdle {
                    darkTheme = isDark
                    layer = materialLayer
                    sourceColor = Color.Black
                }
                val blackSource = renderedBrightness()
                runOnIdle { sourceColor = Color.White }
                val whiteSource = renderedBrightness()
                if (type == MaterialType.Mica) {
                    // Mica replaces source luminosity entirely. Verify its visible theme
                    // response instead of expecting grayscale source changes to show through.
                    for (brightness in listOf(blackSource, whiteSource)) {
                        assertTrue(
                            if (isDark) brightness < 0.2f else brightness > 0.8f,
                            "Mica/$materialLayer must follow dark=$isDark: $brightness"
                        )
                    }
                } else {
                    assertTrue(
                        whiteSource > blackSource + 0.005f,
                        "$type/$materialLayer (dark=$isDark) must render updated source pixels: " +
                            "$blackSource -> $whiteSource"
                    )
                }
            }
        }
    }
}
