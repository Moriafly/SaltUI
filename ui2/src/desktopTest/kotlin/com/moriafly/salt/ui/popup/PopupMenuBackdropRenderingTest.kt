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

package com.moriafly.salt.ui.popup

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import com.moriafly.salt.ui.SaltColors
import com.moriafly.salt.ui.SaltConfigs
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import kotlinx.coroutines.Dispatchers
import org.jetbrains.skia.Bitmap
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import androidx.compose.ui.graphics.Canvas as ImageCanvas

@OptIn(ExperimentalComposeUiApi::class, UnstableSaltUiApi::class)
class PopupMenuBackdropRenderingTest {
    @Test
    fun glassMenuRespectsComposeCoverage() = verifyComposeCoverage(PopupMenuMaterial.Glass)

    @Test
    fun blurMenuRespectsComposeCoverage() = verifyComposeCoverage(PopupMenuMaterial.Blur)

    @Test
    fun glassMenuReplacesSharpBackdropDetails() = verifyBackdropReplacement(PopupMenuMaterial.Glass)

    @Test
    fun blurMenuReplacesSharpBackdropDetails() = verifyBackdropReplacement(PopupMenuMaterial.Blur)

    @Test
    fun unspecifiedTintLeavesTheBackdropUntinted() {
        val backdrop: @Composable () -> Unit = {
            Canvas(Modifier.fillMaxSize()) { drawRect(Color.Blue) }
        }

        for (material in listOf(PopupMenuMaterial.Glass, PopupMenuMaterial.Blur)) {
            val untintedStyle = translucentStyle(material)
            val untinted = renderMenu(style = untintedStyle, backdrop = backdrop)
            val unspecified = renderMenu(
                style = untintedStyle.copy(tintColor = Color.Unspecified),
                backdrop = backdrop
            )

            assertEquals(untinted[180, 132], unspecified[180, 132], material.name)
        }
    }

    @Test
    fun glassLeavesAnUncapturedBackgroundVisible() {
        val pixels = renderMenu(
            style = translucentStyle(PopupMenuMaterial.Glass),
            backgroundColor = Color.Green
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(Color.Blue, radius = 30f, center = Offset(180f, 132f))
            }
        }

        val circle = pixels[180, 132]
        val surrounding = pixels[239, 132]
        assertTrue(circle.blue > circle.green + 0.2f, "Expected the opaque sampled circle")
        assertEquals(1f, surrounding.alpha, absoluteTolerance = 0.01f)
        assertTrue(surrounding.green > surrounding.blue + 0.5f, "Expected the visible green base")
    }

    @Test
    fun glassBlursAnOpaqueCircleWithTransparentSurroundings() {
        val pixels = renderMenu(style = translucentStyle(PopupMenuMaterial.Glass)) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.Blue,
                    radius = 30f,
                    center = Offset(180f, 132f)
                )
            }
        }

        val circle = pixels[180, 132]
        val edge = pixels[210, 132]
        val clear = pixels[235, 132]
        assertEquals(1f, circle.alpha, absoluteTolerance = 0.01f)
        assertTrue(circle.blue > circle.red + 0.2f, "Expected the sampled blue circle")
        assertEquals(0.25f, clear.alpha, absoluteTolerance = 0.01f)
        assertTrue(edge.alpha > clear.alpha && edge.alpha < circle.alpha, "Expected a blurred edge")
    }

    @Test
    fun darkGlassPreservesTheDefaultBackgroundTone() {
        val colors = SaltColors.defaultDark()
        val previousTint = colors.popup.copy(alpha = 0.78f)
        val previousStyle = PopupMenuStyle(
            material = PopupMenuMaterial.Glass,
            containerColor = colors.popup,
            tintColor = previousTint,
            borderColor = Color.White.copy(alpha = 0.16f),
            backdropColor = previousTint
        )
        val backdrop: @Composable () -> Unit = {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(colors.background)
                drawRect(colors.subBackground, size = Size(size.width / 2f, size.height))
            }
        }
        val previous = renderMenu(style = previousStyle, dark = true, backdrop = backdrop)
        val current = renderMenu(dark = true, backdrop = backdrop)

        assertEquals(previous[239, 132].red, current[239, 132].red, absoluteTolerance = 0.008f)
        assertEquals(previous[120, 132].red, current[120, 132].red, absoluteTolerance = 0.015f)
        assertEquals(1f, current[239, 132].alpha)
        assertEquals(1f, current[120, 132].alpha)
    }

    @Test
    fun darkGlassRetainsTheLightnessOfABrighterBackground() {
        val background = Color(0xFF4A4E46)
        val menu = renderMenu(dark = true) {}[239, 132].compositeOver(background)

        assertTrue(
            menu.luminance() >= background.luminance() - 0.014f,
            "The menu should stay close to the brightness of its background: $menu"
        )
    }

    @Test
    fun glassMatchesCapturedImageWhenTheSameColorsShowThrough() {
        val overlay: @Composable () -> Unit = {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(Color(0x80D2B8A2), size = Size(size.width / 2f, size.height))
            }
        }

        for (dark in listOf(false, true)) {
            val backdropColors = if (dark) {
                listOf(Color(0xFF174CCD), Color(0xFF303948))
            } else {
                listOf(Color(0xFF849AB1), Color(0xFFB3A6A2))
            }
            val acrylicImage = ImageBitmap(SCENE_WIDTH, SCENE_HEIGHT)
            val acrylicPaint = Paint().apply {
                shader = LinearGradientShader(
                    from = Offset.Zero,
                    to = Offset(SCENE_WIDTH.toFloat(), 0f),
                    colors = backdropColors
                )
            }
            ImageCanvas(acrylicImage).drawRect(
                left = 0f,
                top = 0f,
                right = SCENE_WIDTH.toFloat(),
                bottom = SCENE_HEIGHT.toFloat(),
                paint = acrylicPaint
            )
            val acrylicPixels = acrylicImage.toPixelMap()
            val transparentBackdrop = renderMenu(dark = dark, backdrop = overlay)
            val composeBackdrop = renderMenu(dark = dark) {
                Image(
                    bitmap = acrylicImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                overlay()
            }

            for (x in listOf(120, 239)) {
                val blendedColor = transparentBackdrop[x, 132].compositeOver(acrylicPixels[x, 132])
                val composeColor = composeBackdrop[x, 132]
                val colorDifference = maxOf(
                    abs(blendedColor.red - composeColor.red),
                    abs(blendedColor.green - composeColor.green),
                    abs(blendedColor.blue - composeColor.blue)
                )
                val tolerance = if (dark) 0.03f else 0.055f
                assertTrue(
                    colorDifference < tolerance,
                    "Tint differs from the captured image at x=$x, dark=$dark: " +
                        "$blendedColor vs $composeColor"
                )
            }
        }
    }

    private fun verifyComposeCoverage(material: PopupMenuMaterial) {
        val pixels = renderMenu(style = translucentStyle(material)) {
            Canvas(Modifier.fillMaxSize()) {
                val halfWidth = size.width / 2f
                val half = Size(halfWidth, size.height)
                drawRect(Color.Red, size = half)
                drawRect(
                    color = Color.Blue.copy(alpha = 0.5f),
                    topLeft = Offset(halfWidth, 0f),
                    size = half
                )
            }
        }

        val opaque = pixels[120, 132]
        val translucent = pixels[239, 132]
        val opaqueSide = pixels[179, 132]
        val translucentSide = pixels[180, 132]

        assertEquals(1f, opaque.alpha, absoluteTolerance = 0.01f)
        assertEquals(0.625f, translucent.alpha, absoluteTolerance = 0.01f)
        assertTrue(
            translucent.alpha < translucentSide.alpha &&
                translucentSide.alpha <= opaqueSide.alpha && opaqueSide.alpha < opaque.alpha,
            "Expected a filtered transition at the split: $opaqueSide, $translucentSide"
        )
        assertTrue(opaque.red > opaque.blue + 0.15f, "Expected the sampled red: $opaque")
        assertTrue(
            translucent.blue > translucent.red + 0.15f,
            "Expected the sampled blue: $translucent"
        )
    }

    private fun verifyBackdropReplacement(material: PopupMenuMaterial) {
        val pixels = renderMenu(style = translucentStyle(material)) {
            Canvas(Modifier.fillMaxSize()) {
                val width = size.width.toInt()
                for (x in 0 until width) {
                    val alpha = if (x < width / 2) 1f else 0.5f
                    val color = if (x % 2 == 0) Color.Black else Color.White
                    drawRect(
                        color.copy(alpha = alpha),
                        topLeft = Offset(x.toFloat(), 0f),
                        size = Size(1f, size.height)
                    )
                }
            }
        }

        for (x in listOf(120, 238)) {
            val outsideContrast = abs(pixels[x, 72].red - pixels[x + 1, 72].red)
            val insideContrast = abs(pixels[x, 132].red - pixels[x + 1, 132].red)
            assertTrue(outsideContrast > 0.95f, "Expected sharp stripes outside the menu")
            assertTrue(
                insideContrast < 0.06f,
                "$material lets sharp stripes through at x=$x: $insideContrast"
            )
        }
    }

    private fun translucentStyle(material: PopupMenuMaterial) = PopupMenuStyle(
        material = material,
        containerColor = Color.White,
        tintColor = Color.Transparent,
        borderColor = Color.Transparent,
        backdropColor = Color.White.copy(alpha = 0.25f)
    )

    private fun renderMenu(
        style: PopupMenuStyle? = null,
        dark: Boolean = false,
        backgroundColor: Color = Color.Transparent,
        backdrop: @Composable () -> Unit
    ): PixelMap =
        ImageComposeScene(
            width = SCENE_WIDTH,
            height = SCENE_HEIGHT,
            coroutineContext = Dispatchers.Unconfined
        ) {
            SaltTheme(configs = SaltConfigs.default(isDarkTheme = dark)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                ) {
                    PopupMenuBackdrop(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        backdrop()
                        PopupMenu(
                            expanded = true,
                            onDismissRequest = {},
                            scrollable = false,
                            popupPositionProvider = createMenuPositionProvider { _, _, _, _ ->
                                IntOffset(80, 100)
                            },
                            style = style ?: PopupMenuDefaults.style()
                        ) {
                            Box(Modifier.size(200.dp, 64.dp))
                        }
                    }
                }
            }
        }.use { scene ->
            repeat(4) { frame -> scene.render((frame + 1) * 16_666_667L).close() }
            scene.render(100_000_000L).use { image ->
                Bitmap.makeFromImage(image).use { bitmap ->
                    bitmap.asComposeImageBitmap().toPixelMap()
                }
            }
        }

    private companion object {
        const val SCENE_WIDTH = 360
        const val SCENE_HEIGHT = 240
    }
}
