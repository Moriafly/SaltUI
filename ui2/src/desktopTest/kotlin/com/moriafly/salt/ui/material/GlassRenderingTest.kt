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

package com.moriafly.salt.ui.material

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import com.moriafly.salt.ui.Card
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
class GlassRenderingTest {
    @Test
    fun regularGlassPreservesComposeCoverage() {
        for (isDarkTheme in listOf(false, true)) {
            val pixels = renderGlass(isDarkTheme) { StripedBackdrop() }
            val opaque = pixels[65, 90]
            val translucent = pixels[150, 90]
            val empty = pixels[235, 90]

            assertEquals(1f, opaque.alpha, absoluteTolerance = 0.01f)
            assertTrue(opaque.blue > opaque.red, "Expected the captured blue, dark=$isDarkTheme")
            assertTrue(
                empty.alpha > 0f && empty.alpha < translucent.alpha &&
                    translucent.alpha < opaque.alpha,
                "Expected the background to show through, dark=$isDarkTheme: $empty, $translucent"
            )
        }
    }

    @Test
    fun darkGlassIsLighterThanThePageAndCard() {
        val pixels = renderGlass(isDarkTheme = true) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SaltTheme.colors.background)
            ) {
                Card(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                ) {}
            }
        }

        val page = pixels[65, 20]
        val card = pixels[235, 20]
        val glass = pixels[65, 90]
        val glassOverCard = pixels[235, 90]
        assertTrue(
            page.luminance() < card.luminance() && card.luminance() < glass.luminance() &&
                glass.luminance() < glassOverCard.luminance(),
            "Expected distinct dark surfaces: page=$page, card=$card, glass=$glass, " +
                "glass over card=$glassOverCard"
        )
    }

    @Test
    fun darkGlassLiftsAnUncapturedWindowBackground() {
        val glass = renderGlass(isDarkTheme = true) {}[150, 90]
        val windowBackgrounds = listOf(Color(0xFF444547), Color(0xFF2A51B9), Color(0xFF274EE2))
        for (background in windowBackgrounds) {
            val surface = glass.compositeOver(background)
            assertTrue(
                surface.luminance() > background.luminance() + 0.008f,
                "Expected an elevated surface over $background: $surface"
            )
        }

        val blueSurface = glass.compositeOver(Color(0xFF274EE2))
        assertTrue(blueSurface.blue - blueSurface.red > 0.4f, "Expected blue to show through")
    }

    @Test
    fun lightGlassKeepsContrastInSampledContent() {
        val pixels = renderGlass(isDarkTheme = false) {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(Color.White)
                drawRect(Color.Black, size = Size(size.width / 2f, size.height))
            }
        }

        val dark = pixels[65, 90]
        val light = pixels[235, 90]
        assertTrue(
            light.red - dark.red > 0.4f,
            "Expected visible contrast through light glass: $dark, $light"
        )
    }

    @Test
    fun regularGlassMatchesAnEquivalentComposeImage() {
        for (isDarkTheme in listOf(false, true)) {
            val image = backdropImage(isDarkTheme)
            val imagePixels = image.toPixelMap()
            val translucent = renderGlass(isDarkTheme) { StripedBackdrop() }
            val captured = renderGlass(isDarkTheme) { StripedBackdrop(image) }

            for (x in listOf(65, 150, 235)) {
                val overWindow = translucent[x, 90].compositeOver(imagePixels[x, 90])
                val overImage = captured[x, 90]
                val difference = maxOf(
                    abs(overWindow.red - overImage.red),
                    abs(overWindow.green - overImage.green),
                    abs(overWindow.blue - overImage.blue)
                )
                assertTrue(
                    difference <= 0.04f,
                    "Expected similar colors at x=$x, dark=$isDarkTheme: $overWindow vs $overImage"
                )
            }
        }
    }

    private fun renderGlass(
        isDarkTheme: Boolean,
        backdrop: @Composable () -> Unit
    ): PixelMap =
        ImageComposeScene(
            width = SCENE_WIDTH,
            height = SCENE_HEIGHT,
            coroutineContext = Dispatchers.Unconfined
        ) {
            SaltTheme(configs = SaltConfigs.default(isDarkTheme = isDarkTheme)) {
                val source = rememberGlassSource()
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassSource(source)
                    ) {
                        backdrop()
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(width = 240.dp, height = 100.dp)
                            .glass(source)
                    )
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

    @Composable
    private fun StripedBackdrop(image: ImageBitmap? = null) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (image != null) {
                Image(
                    bitmap = image,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize()
                )
            }

            Canvas(Modifier.matchParentSize()) {
                val stripe = Size(size.width / 3f, size.height)
                drawRect(Color(0xFF2D63A8), size = stripe)
                drawRect(
                    color = Color(0x80C7B09C),
                    topLeft = Offset(stripe.width, 0f),
                    size = stripe
                )
            }
        }
    }

    private fun backdropImage(isDarkTheme: Boolean): ImageBitmap {
        val colors = if (isDarkTheme) {
            listOf(Color(0xFF174CCD), Color(0xFF515B56))
        } else {
            listOf(Color(0xFF93ABC4), Color(0xFFC9B6AF))
        }
        val image = ImageBitmap(SCENE_WIDTH, SCENE_HEIGHT)
        val paint = Paint().apply {
            shader = LinearGradientShader(
                from = Offset.Zero,
                to = Offset(SCENE_WIDTH.toFloat(), 0f),
                colors = colors
            )
        }
        ImageCanvas(image).drawRect(
            left = 0f,
            top = 0f,
            right = SCENE_WIDTH.toFloat(),
            bottom = SCENE_HEIGHT.toFloat(),
            paint = paint
        )
        return image
    }

    private companion object {
        const val SCENE_WIDTH = 300
        const val SCENE_HEIGHT = 180
    }
}
