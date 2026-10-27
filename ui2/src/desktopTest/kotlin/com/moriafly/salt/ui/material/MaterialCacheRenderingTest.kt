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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.ui.SaltConfigs
import com.moriafly.salt.ui.SaltColors
import com.moriafly.salt.ui.SaltDynamicColors
import com.moriafly.salt.ui.SaltMaterial
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.LocalHazeBlurStyle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import java.awt.Rectangle
import java.awt.Robot
import java.awt.GraphicsEnvironment
import org.junit.Assume.assumeFalse
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, UnstableSaltUiApi::class)
class MaterialCacheRenderingTest {
    @Test
    fun replacingSourceDuringReadbackCancelsTheOldCaptureAndDisplaysTheNewVersion() = runComposeUiTest {
        var version by mutableStateOf(0)
        var captures = 0
        val started = CompletableDeferred<Unit>()
        val cancelled = CompletableDeferred<Unit>()
        val materialState = MaterialState(readback = { layer ->
            captures++
            if (captures == 1) {
                started.complete(Unit)
                try {
                    awaitCancellation()
                } finally {
                    cancelled.complete(Unit)
                }
            } else {
                layer.toImageBitmap()
            }
        })
        setContent {
            SaltTheme(
                configs = SaltConfigs.default(isDarkTheme = false),
                material = SaltMaterial(MaterialType.BlurryGlass)
            ) {
                CompositionLocalProvider(LocalMaterialState provides materialState) {
                    Box(modifier = Modifier.size(160.dp)) {
                        MaterialSource(materialSelf = false, mode = MaterialSourceMode.Static(version)) {
                            val color = if (version == 0) { Color.Red } else { Color.Blue }
                            Box(modifier = Modifier.fillMaxSize().background(color))
                        }
                        Box(modifier = Modifier.fillMaxSize().material().testTag("material"))
                    }
                }
            }
        }
        waitUntil(timeoutMillis = 5_000) { started.isCompleted }
        val red = onNodeWithTag("material").captureToImage().toPixelMap()[80, 80]
        assertTrue(red.red > red.blue + 0.1f, "Readback must leave the live surface visible: $red")
        runOnIdle { version++ }
        waitUntil(timeoutMillis = 5_000) {
            cancelled.isCompleted &&
                materialState.sources.singleOrNull()?.entries?.any { it.image != null } == true
        }
        val blue = onNodeWithTag("material").captureToImage().toPixelMap()[80, 80]
        assertTrue(blue.blue > blue.red + 0.1f, "The new version must replace the pending capture: $blue")
    }

    @Test
    fun inheritedHazeStyleUsesLiveRenderingAndClearingItRestoresCachedPixels() = runComposeUiTest {
        var customStyle by mutableStateOf(false)
        lateinit var materialState: MaterialState
        setContent {
            SaltTheme(
                configs = SaltConfigs.default(isDarkTheme = false),
                material = SaltMaterial(MaterialType.BlurryGlass)
            ) {
                materialState = LocalMaterialState.current!!
                Box(modifier = Modifier.size(160.dp)) {
                    MaterialSource(materialSelf = false, mode = MaterialSourceMode.Static(Unit)) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Red))
                    }
                    val style = if (customStyle) { HazeBlurStyle { alpha(0f) } } else { HazeBlurStyle }
                    CompositionLocalProvider(LocalHazeBlurStyle provides style) {
                        Box(modifier = Modifier.fillMaxSize().material().testTag("material"))
                    }
                }
            }
        }
        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.any { it.image != null } == true
        }
        val cached = onNodeWithTag("material").captureToImage()
        val original = runOnIdle { materialState.sources.single().entries.single().image }
        runOnIdle { customStyle = true }
        val transparent = onNodeWithTag("material").captureToImage().toPixelMap()[80, 80]
        assertTrue(
            transparent.red > 0.99f && transparent.blue < 0.01f,
            "Inherited zero alpha must reveal the source: $transparent"
        )
        runOnIdle { customStyle = false }
        val restored = onNodeWithTag("material").captureToImage()
        assertTrue(cached.averageDifference(restored) < 0.005f)
        runOnIdle { assertSame(original, materialState.sources.single().entries.single().image) }
    }

    @Test
    fun multipleSourcesUseLiveRenderingAndRemovingOneRestoresTheCachedSource() = runComposeUiTest {
        var showSecondSource by mutableStateOf(false)
        var mode by mutableStateOf<MaterialSourceMode>(MaterialSourceMode.Static(Unit))
        lateinit var materialState: MaterialState
        setContent {
            SaltTheme(
                configs = SaltConfigs.default(isDarkTheme = false),
                material = SaltMaterial(MaterialType.BlurryGlass)
            ) {
                materialState = LocalMaterialState.current!!
                Box(modifier = Modifier.size(160.dp)) {
                    MaterialSource(materialSelf = false, mode = mode) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Red))
                    }
                    if (showSecondSource) {
                        MaterialSource(materialSelf = false, mode = mode) {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Blue))
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize().material().testTag("material"))
                }
            }
        }
        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.any { it.image != null } == true
        }
        val cachedRed = onNodeWithTag("material").captureToImage()
        runOnIdle { showSecondSource = true }
        val fallback = onNodeWithTag("material").captureToImage()
        val blue = fallback.toPixelMap()[80, 80]
        assertTrue(blue.blue > blue.red + 0.1f, "The second source must reach the live effect: $blue")
        runOnIdle { mode = MaterialSourceMode.Dynamic }
        val dynamic = onNodeWithTag("material").captureToImage()
        assertTrue(fallback.averageDifference(dynamic) < 0.015f)

        runOnIdle {
            mode = MaterialSourceMode.Static(Unit)
            showSecondSource = false
        }
        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.any { it.image != null } == true
        }
        val restored = onNodeWithTag("material").captureToImage()
        assertTrue(cachedRed.averageDifference(restored) < 0.005f)
    }

    @Test
    fun resolvedColorKeysRefreshCachedPixelsButEqualPalettesKeepThem() = runComposeUiTest {
        var colors by mutableStateOf(SaltColors.defaultLight(highlight = Color.Red))
        var sourceColor by mutableStateOf<Color?>(null)
        lateinit var materialState: MaterialState
        setContent {
            SaltTheme(
                configs = SaltConfigs.default(isDarkTheme = false),
                dynamicColors = SaltDynamicColors.default(light = colors),
                material = SaltMaterial(MaterialType.BlurryGlass)
            ) {
                materialState = LocalMaterialState.current!!
                Box(modifier = Modifier.size(160.dp)) {
                    MaterialSource(
                        materialSelf = false,
                        mode = MaterialSourceMode.Static(SaltTheme.colors.highlight)
                    ) {
                        val color = sourceColor ?: SaltTheme.colors.highlight
                        Box(modifier = Modifier.fillMaxSize().background(color))
                    }
                    Box(modifier = Modifier.fillMaxSize().material().testTag("material"))
                }
            }
        }
        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.any { it.image != null } == true
        }
        val red = onNodeWithTag("material").captureToImage()
        val original = runOnIdle { materialState.sources.single().entries.single().image }

        // Equal palette values must not accidentally turn an ordinary recomposition into a bake.
        // Changing unversioned source pixels makes unwanted rebaking observable in the image.
        runOnIdle {
            sourceColor = Color.Green
            colors = colors.copy()
        }
        val retained = onNodeWithTag("material").captureToImage()
        assertTrue(red.averageDifference(retained) < 0.005f)
        runOnIdle {
            assertSame(original, materialState.sources.single().entries.single().image)
        }

        runOnIdle {
            sourceColor = null
            colors = colors.copy(highlight = Color.Blue)
        }
        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.any {
                it.image != null && it.image !== original
            } == true
        }
        val blue = onNodeWithTag("material").captureToImage().toPixelMap()[80, 80]
        assertTrue(blue.blue > blue.red + 0.1f, "A resolved color key must refresh pixels: $blue")
    }

    @Test
    fun cachedBackgroundRendersInARealDesktopWindow() {
        assumeFalse("Requires a real display", GraphicsEnvironment.isHeadless())
        runDesktopComposeUiTest {
            var sourceColor by mutableStateOf(Color.Red)
            var version by mutableStateOf(0)
            lateinit var materialState: MaterialState
            lateinit var nativeWindow: ComposeWindow
            setContent {
                Window(
                    onCloseRequest = {},
                    title = "Material cache rendering test",
                    state = rememberWindowState(width = 240.dp, height = 180.dp),
                    undecorated = true,
                    alwaysOnTop = true
                ) {
                    nativeWindow = window
                    SaltTheme(material = SaltMaterial(MaterialType.BlurryGlass)) {
                        materialState = LocalMaterialState.current!!
                        Box(modifier = Modifier.fillMaxSize()) {
                            MaterialSource(
                                materialSelf = false,
                                mode = MaterialSourceMode.Static(version)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().background(sourceColor))
                            }
                            Box(modifier = Modifier.fillMaxSize().material())
                        }
                    }
                }
            }
            waitUntil(timeoutMillis = 5_000) {
                materialState.sources.singleOrNull()?.entries?.any { it.image != null } == true
            }
            val robot = Robot()
            fun screenPixel(): Color {
                val bounds = runOnIdle {
                    val location = nativeWindow.locationOnScreen
                    val centerX = location.x + nativeWindow.width / 2
                    val centerY = location.y + nativeWindow.height / 2
                    Rectangle(centerX - 4, centerY - 4, 8, 8)
                }
                val frame = robot.createScreenCapture(bounds)
                return Color(frame.getRGB(4, 4))
            }
            waitUntil(timeoutMillis = 5_000) {
                val pixel = screenPixel()
                pixel.red > pixel.blue + 0.1f
            }
            val original = screenPixel()
            runOnIdle { sourceColor = Color.Blue }
            waitForIdle()
            val reused = screenPixel()
            assertTrue(
                abs(original.red - reused.red) < 0.02f && abs(original.blue - reused.blue) < 0.02f
            )

            runOnIdle { version++ }
            waitUntil(timeoutMillis = 5_000) {
                val pixel = screenPixel()
                pixel.blue > pixel.red + 0.1f
            }
        }
    }

    @Test
    fun staticMaterialsMatchDynamicRendering() = runComposeUiTest {
        var mode by mutableStateOf<MaterialSourceMode>(MaterialSourceMode.Dynamic)
        var type by mutableStateOf(MaterialType.BlurryGlass)
        var dark by mutableStateOf(false)
        var layer by mutableStateOf(MaterialLayer.Background)
        lateinit var materialState: MaterialState
        setContent {
            SaltTheme(
                configs = SaltConfigs.default(isDarkTheme = dark),
                material = SaltMaterial(type)
            ) {
                materialState = LocalMaterialState.current!!
                Box(modifier = Modifier.size(241.dp, 161.dp)) {
                    MaterialSource(materialSelf = false, mode = mode) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(Color.Red, size = Size(size.width / 2, size.height))
                            drawRect(
                                Color.Blue,
                                topLeft = Offset(size.width / 2, 0f),
                                size = Size(size.width / 2, size.height)
                            )
                        }
                    }
                    val material = when (layer) {
                        MaterialLayer.Background -> Modifier.material()
                        MaterialLayer.SubBackground -> Modifier.subMaterial()
                    }
                    Box(modifier = Modifier.fillMaxSize().testTag("material").then(material))
                }
            }
        }

        for (materialType in MaterialType.entries.filter { it != MaterialType.None }) {
            for (isDark in listOf(false, true)) {
                for (materialLayer in MaterialLayer.entries) {
                    runOnIdle {
                        mode = MaterialSourceMode.Dynamic
                        type = materialType
                        dark = isDark
                        layer = materialLayer
                    }
                    val dynamic = onNodeWithTag("material").captureToImage()
                    runOnIdle { mode = MaterialSourceMode.Static(Unit) }
                    waitUntil(timeoutMillis = 5_000) {
                        materialState.sources.singleOrNull()?.entries?.any {
                            it.image != null
                        } == true
                    }
                    val cached = onNodeWithTag("material").captureToImage()
                    val difference = dynamic.averageDifference(cached)
                    assertTrue(
                        difference < 0.015f,
                        "$materialType/$materialLayer/dark=$isDark cache difference: $difference"
                    )
                }
            }
        }
    }

    @Test
    fun contentKeyRebuildsBackgroundWithoutFreezingForeground() = runComposeUiTest {
        var sourceColor by mutableStateOf(Color.Red)
        var foreground by mutableStateOf(Color.Green)
        var contentKey by mutableStateOf<Any>(Unit)
        var sourceCompositions by mutableStateOf(0)
        lateinit var materialState: MaterialState
        setContent {
            SaltTheme(material = SaltMaterial(MaterialType.BlurryGlass)) {
                materialState = LocalMaterialState.current!!
                Box(modifier = Modifier.size(160.dp)) {
                    MaterialSource(
                        modifier = Modifier.testTag("source-$sourceCompositions"),
                        materialSelf = false,
                        mode = MaterialSourceMode.Static(contentKey)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().background(sourceColor))
                    }
                    Box(modifier = Modifier.fillMaxSize().material().testTag("material")) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(foreground)
                                .testTag("foreground")
                        )
                    }
                }
            }
        }

        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.any { it.image != null } == true
        }
        val first = onNodeWithTag("material").captureToImage()
        val originalBake = runOnIdle { materialState.sources.single().entries.single().image }

        // Deliberately change the raw source without advancing its declared version. This makes
        // reuse visible and distinguishes the cached consumer from a live-rendering false positive.
        runOnIdle { sourceColor = Color.Blue }
        val reused = onNodeWithTag("material").captureToImage()
        assertTrue(
            first.averageDifference(reused) < 0.005f,
            "An unchanged key must allow pixel reuse"
        )

        runOnIdle {
            foreground = Color.Yellow
            sourceCompositions++
        }
        val updatedForeground = onNodeWithTag("foreground").captureToImage().toPixelMap()[8, 8]
        assertTrue(updatedForeground.red > 0.9f && updatedForeground.green > 0.9f)
        runOnIdle {
            assertSame(originalBake, materialState.sources.single().entries.single().image)
            contentKey = 1
        }
        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.any {
                it.image != null && it.image !== originalBake
            } == true
        }
        val updated = onNodeWithTag("material").captureToImage()
        assertTrue(
            first.averageDifference(updated) > 0.05f,
            "A new content key must update the material"
        )
    }

    @Test
    fun consumersShareBakedPixelsAndDynamicModeRestoresLiveUpdates() = runComposeUiTest {
        var mode by mutableStateOf<MaterialSourceMode>(MaterialSourceMode.Static(Unit))
        var sourceColor by mutableStateOf(Color.Red)
        lateinit var materialState: MaterialState
        setContent {
            SaltTheme(material = SaltMaterial(MaterialType.BlurryGlass)) {
                materialState = LocalMaterialState.current!!
                Box(modifier = Modifier.size(320.dp, 120.dp)) {
                    MaterialSource(materialSelf = false, mode = mode) {
                        Box(modifier = Modifier.fillMaxSize().background(sourceColor))
                    }
                    Box(modifier = Modifier.size(80.dp).material().testTag("first"))
                    Box(
                        modifier = Modifier
                            .offset(x = 160.dp)
                            .size(80.dp)
                            .material()
                            .testTag("second")
                    )
                }
            }
        }
        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.singleOrNull()?.image != null
        }
        val first = onNodeWithTag("first").captureToImage()
        val second = onNodeWithTag("second").captureToImage()
        assertTrue(
            first.averageDifference(second) < 0.005f,
            "Both panels must render the same background"
        )
        runOnIdle {
            assertTrue(materialState.sources.single().entries.single().consumers == 2)
            sourceColor = Color.Blue
            mode = MaterialSourceMode.Dynamic
        }
        val liveFirst = onNodeWithTag("first").captureToImage()
        val liveSecond = onNodeWithTag("second").captureToImage()
        assertTrue(
            first.averageDifference(liveFirst) > 0.05f,
            "Dynamic mode must restore source updates"
        )
        assertTrue(liveFirst.averageDifference(liveSecond) < 0.005f)

        // Re-enter with the same key after the old source registration has been disposed.
        runOnIdle { mode = MaterialSourceMode.Static(Unit) }
        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.singleOrNull()?.image != null
        }
        val replacement = onNodeWithTag("first").captureToImage()
        assertTrue(
            liveFirst.averageDifference(replacement) < 0.005f,
            "A new source must not reuse old pixels"
        )
    }

    @Test
    fun removingOneStyleKeepsOtherCachedPixelsAndReentryReusesItsImage() = runComposeUiTest {
        var showBackground by mutableStateOf(true)
        var sourceColor by mutableStateOf(Color.Red)
        var version by mutableStateOf(0)
        var mode by mutableStateOf<MaterialSourceMode>(MaterialSourceMode.Dynamic)
        lateinit var materialState: MaterialState
        setContent {
            SaltTheme(
                configs = SaltConfigs.default(isDarkTheme = false),
                material = SaltMaterial(MaterialType.BlurryGlass)
            ) {
                materialState = LocalMaterialState.current!!
                Box(
                    modifier = Modifier.size(320.dp, 120.dp)
                ) {
                    MaterialSource(
                        materialSelf = false,
                        mode = mode
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(sourceColor)
                        )
                    }
                    if (showBackground) {
                        Box(
                            modifier = Modifier.size(120.dp).material().testTag("background")
                        )
                    }
                    Box(
                        modifier = Modifier
                            .offset(x = 160.dp)
                            .size(120.dp)
                            .subMaterial()
                            .testTag("subBackground")
                    )
                }
            }
        }
        val liveRed = onNodeWithTag("subBackground").captureToImage()
        runOnIdle { mode = MaterialSourceMode.Static(version) }
        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.count { it.image != null } == 2
        }

        // Changing the unversioned raw source makes an accidental recapture visible.
        runOnIdle { sourceColor = Color.Blue }
        val retainedBackground = onNodeWithTag("background").captureToImage()
        runOnIdle { showBackground = false }
        val retainedRed = onNodeWithTag("subBackground").captureToImage()
        assertTrue(
            liveRed.averageDifference(retainedRed) < 0.015f,
            "Removing the first style must preserve the remaining style's capture"
        )
        runOnIdle { showBackground = true }
        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.count {
                it.image != null && it.consumers > 0
            } == 2
        }
        val reenteredRed = onNodeWithTag("background").captureToImage()
        val stillRed = onNodeWithTag("subBackground").captureToImage()
        assertTrue(liveRed.averageDifference(stillRed) < 0.015f)
        assertTrue(retainedBackground.averageDifference(reenteredRed) < 0.005f)

        runOnIdle {
            version++
            mode = MaterialSourceMode.Static(version)
        }
        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.count { it.image != null } == 2
        }
        val versionedBlue = onNodeWithTag("subBackground").captureToImage()
        val versionedBlueBackground = onNodeWithTag("background").captureToImage()
        runOnIdle { mode = MaterialSourceMode.Dynamic }
        val liveBlueBackground = onNodeWithTag("background").captureToImage()
        val liveBlueSubBackground = onNodeWithTag("subBackground").captureToImage()
        assertTrue(versionedBlueBackground.averageDifference(liveBlueBackground) < 0.015f)
        assertTrue(versionedBlue.averageDifference(liveBlueSubBackground) < 0.015f)
        assertTrue(liveRed.averageDifference(versionedBlue) > 0.05f)
    }

    @Test
    fun cachedConsumersFollowSourceCoordinatesAndBoundaries() = runComposeUiTest {
        var mode by mutableStateOf<MaterialSourceMode>(MaterialSourceMode.Dynamic)
        var panelX by mutableStateOf(40.dp)
        var type by mutableStateOf(MaterialType.BlurryGlass)
        var layer by mutableStateOf(MaterialLayer.Background)
        lateinit var materialState: MaterialState
        setContent {
            SaltTheme(material = SaltMaterial(type)) {
                materialState = LocalMaterialState.current!!
                Box(modifier = Modifier.size(440.dp, 160.dp).testTag("scene")) {
                    MaterialSource(
                        Modifier.offset(x = 40.dp).size(320.dp, 160.dp),
                        materialSelf = false,
                        mode = mode
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(Color.Red, size = Size(size.width / 2, size.height))
                            drawRect(
                                Color.Blue,
                                topLeft = Offset(size.width / 2, 0f),
                                size = Size(size.width / 2, size.height)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .offset(x = panelX, y = 32.dp)
                            .size(120.dp, 80.dp)
                            .then(
                                when (layer) {
                                    MaterialLayer.Background -> Modifier.material()
                                    MaterialLayer.SubBackground -> Modifier.subMaterial()
                                }
                            )
                            .testTag("panel")
                    )
                }
            }
        }

        for (materialType in MaterialType.entries.filter { it != MaterialType.None }) {
            for (materialLayer in MaterialLayer.entries) {
                for (position in listOf(40.dp, 140.dp, 240.dp, 290.dp, 0.dp)) {
                    runOnIdle {
                        mode = MaterialSourceMode.Dynamic
                        type = materialType
                        layer = materialLayer
                        panelX = position
                    }
                    val dynamic = onNodeWithTag("panel").captureToImage()
                    val dynamicScene = onNodeWithTag("scene").captureToImage()
                    runOnIdle { mode = MaterialSourceMode.Static(Unit) }
                    waitForIdle()
                    waitUntil(timeoutMillis = 5_000) {
                        materialState.sources.singleOrNull()?.entries?.isEmpty() != false ||
                            materialState.sources.singleOrNull()?.entries?.any {
                                it.image != null
                            } == true
                    }
                    val cached = onNodeWithTag("panel").captureToImage()
                    val cachedScene = onNodeWithTag("scene").captureToImage()
                    val difference = dynamic.averageDifference(cached)
                    assertTrue(
                        difference < 0.015f,
                        "$materialType/$materialLayer at $position cache difference: $difference"
                    )
                    val sceneDifference = dynamicScene.averageDifference(cachedScene)
                    assertTrue(
                        sceneDifference < 0.015f,
                        "$materialType/$materialLayer at $position: $sceneDifference"
                    )
                }
            }
        }
    }

    @Test
    fun transformedConsumerFallsBackWhileStaticModeIsActive() = runComposeUiTest {
        var mode by mutableStateOf<MaterialSourceMode>(MaterialSourceMode.Static(Unit))
        var scale by mutableStateOf(1f)
        var rotation by mutableStateOf(0f)
        lateinit var materialState: MaterialState
        setContent {
            SaltTheme(material = SaltMaterial(MaterialType.BlurryGlass)) {
                materialState = LocalMaterialState.current!!
                Box(modifier = Modifier.size(320.dp, 200.dp)) {
                    MaterialSource(materialSelf = false, mode = mode) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(Color.Red, size = Size(size.width / 2, size.height))
                            drawRect(
                                Color.Blue,
                                topLeft = Offset(size.width / 2, 0f),
                                size = Size(size.width / 2, size.height)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .offset(100.dp, 50.dp)
                            .size(120.dp, 80.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                rotationZ = rotation
                            }
                            .material()
                            .testTag("panel")
                    )
                }
            }
        }
        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.any { it.image != null } == true
        }
        for ((nextScale, nextRotation) in listOf(1.3f to 0f, 1f to 15f)) {
            runOnIdle {
                scale = nextScale
                rotation = nextRotation
            }
            val transformedStatic = onNodeWithTag("panel").captureToImage()
            runOnIdle {
                assertTrue(
                    materialState.sources.single().entries.none { it.consumers > 0 },
                    "Relative transforms must release the baked path"
                )
            }
            runOnIdle { mode = MaterialSourceMode.Dynamic }
            val dynamic = onNodeWithTag("panel").captureToImage()
            assertTrue(
                transformedStatic.averageDifference(dynamic) < 0.015f,
                "A transformed static-mode consumer must preserve live rendering"
            )
            runOnIdle { mode = MaterialSourceMode.Static(Unit) }
        }
    }

    @Test
    fun sourceSizeChangesRebuildCachedPixels() = runComposeUiTest {
        var edge by mutableStateOf(160.dp)
        lateinit var materialState: MaterialState
        setContent {
            SaltTheme(material = SaltMaterial(MaterialType.BlurryGlass)) {
                materialState = LocalMaterialState.current!!
                Box(modifier = Modifier.size(edge)) {
                    MaterialSource(materialSelf = false, mode = MaterialSourceMode.Static(Unit)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val color = if (size.width < 200.dp.toPx()) {
                                Color.Red
                            } else {
                                Color.Blue
                            }
                            drawRect(color)
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize().material().testTag("material"))
                }
            }
        }
        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.any { it.image != null } == true
        }
        val before = onNodeWithTag("material").captureToImage().toPixelMap()[80, 80]
        val originalBake = runOnIdle { materialState.sources.single().entries.single().image }
        runOnIdle { edge = 240.dp }
        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.any {
                it.size == materialState.sources.single().size &&
                    it.image?.width == it.imageSize.width &&
                    it.image?.height == it.imageSize.height &&
                    it.image !== originalBake
            } == true
        }
        val after = onNodeWithTag("material").captureToImage().toPixelMap()[120, 120]
        assertTrue(before.red > before.blue + 0.1f, "The original source must be red: $before")
        assertTrue(
            after.blue > after.red + 0.1f,
            "Resizing must render the new blue source: $after"
        )
    }

    @Test
    fun cacheAllocationFailuresPreserveLiveRendering() = runComposeUiTest {
        var policy by mutableStateOf("budget")
        var sourceColor by mutableStateOf(Color.Red)
        lateinit var materialState: MaterialState
        setContent {
            SaltTheme(
                configs = SaltConfigs.default(isDarkTheme = false),
                material = SaltMaterial(MaterialType.BlurryGlass)
            ) {
                materialState = remember(policy) {
                    when (policy) {
                        "budget" -> MaterialState(cacheBudgetBytes = 1)
                        "oom" -> MaterialState(readback = { throw OutOfMemoryError("test") })
                        else -> MaterialState(readback = { error("readback unavailable") })
                    }
                }
                CompositionLocalProvider(LocalMaterialState provides materialState) {
                    Box(modifier = Modifier.size(160.dp)) {
                        MaterialSource(
                            materialSelf = false,
                            mode = MaterialSourceMode.Static(Unit)
                        ) {
                            Box(modifier = Modifier.fillMaxSize().background(sourceColor))
                        }
                        Box(
                            modifier = Modifier.fillMaxSize().material().testTag("material")
                        )
                    }
                }
            }
        }
        for (failure in listOf("budget", "oom", "exception")) {
            runOnIdle {
                policy = failure
                sourceColor = Color.Red
            }
            if (failure != "budget") {
                waitUntil(timeoutMillis = 5_000) {
                    materialState.sources.singleOrNull()?.entries?.any {
                        it.result?.isFailure == true
                    } == true
                }
            }
            val red = onNodeWithTag("material").captureToImage().toPixelMap()[80, 80]
            runOnIdle { sourceColor = Color.Blue }
            val blue = onNodeWithTag("material").captureToImage().toPixelMap()[80, 80]
            assertTrue(red.red > red.blue + 0.1f, "$failure must initially render red: $red")
            assertTrue(blue.blue > blue.red + 0.1f, "$failure must keep live updates: $blue")
        }
    }

    @Test
    fun theCacheBudgetKeepsActiveImagesAndEvictsAnIdleImageForAnotherStyle() = runComposeUiTest {
        var showBackground by mutableStateOf(true)
        var showSubBackground by mutableStateOf(true)
        var sourceColor by mutableStateOf(Color.Red)
        val materialState = MaterialState(cacheBudgetBytes = 30_000)
        setContent {
            SaltTheme(
                configs = SaltConfigs.default(isDarkTheme = false),
                material = SaltMaterial(MaterialType.BlurryGlass)
            ) {
                CompositionLocalProvider(LocalMaterialState provides materialState) {
                    Box(modifier = Modifier.size(160.dp)) {
                        MaterialSource(
                            materialSelf = false,
                            mode = MaterialSourceMode.Static(Unit)
                        ) {
                            Box(modifier = Modifier.fillMaxSize().background(sourceColor))
                        }
                        if (showBackground) {
                            Box(
                                modifier = Modifier
                                    .offset(x = 8.dp, y = 16.dp)
                                    .size(56.dp)
                                    .material()
                                    .testTag("background")
                            )
                        }
                        if (showSubBackground) {
                            Box(
                                modifier = Modifier
                                    .offset(x = 88.dp, y = 16.dp)
                                    .size(56.dp)
                                    .subMaterial()
                                    .testTag("subBackground")
                            )
                        }
                    }
                }
            }
        }
        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.singleOrNull()?.image != null
        }
        val redBackground = onNodeWithTag("background").captureToImage()
        val redSubBackground = onNodeWithTag("subBackground").captureToImage()
        runOnIdle { sourceColor = Color.Blue }
        val retainedBackground = onNodeWithTag("background").captureToImage()
        val liveBlueSubBackground = onNodeWithTag("subBackground").captureToImage()
        assertTrue(redBackground.averageDifference(retainedBackground) < 0.005f)
        assertTrue(redSubBackground.averageDifference(liveBlueSubBackground) > 0.05f)

        val original = runOnIdle { materialState.sources.single().entries.single().image }
        runOnIdle {
            showBackground = false
            showSubBackground = false
        }
        runOnIdle { showSubBackground = true }
        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.singleOrNull()?.let {
                it.image != null && it.image !== original
            } == true
        }
        val cachedBlueSubBackground = onNodeWithTag("subBackground").captureToImage()
        assertTrue(liveBlueSubBackground.averageDifference(cachedBlueSubBackground) < 0.015f)
        runOnIdle {
            showBackground = true
            sourceColor = Color.Green
        }
        val liveGreen = onNodeWithTag("background").captureToImage().toPixelMap()[28, 28]
        val stillBlueSubBackground = onNodeWithTag("subBackground").captureToImage()
        assertTrue(liveGreen.green > liveGreen.blue + 0.1f)
        assertTrue(cachedBlueSubBackground.averageDifference(stillBlueSubBackground) < 0.005f)
    }

    @Test
    fun sourceEnvironmentInvalidatesEvenWithAFixedConsumerTheme() = runComposeUiTest {
        var dark by mutableStateOf(false)
        var direction by mutableStateOf(LayoutDirection.Ltr)
        lateinit var materialState: MaterialState
        setContent {
            SaltTheme(
                configs = SaltConfigs.default(isDarkTheme = dark),
                material = SaltMaterial(MaterialType.Mica)
            ) {
                materialState = LocalMaterialState.current!!
                CompositionLocalProvider(LocalLayoutDirection provides direction) {
                    Box(modifier = Modifier.size(160.dp)) {
                        MaterialSource(
                            materialSelf = false,
                            mode = MaterialSourceMode.Static(Unit)
                        ) {
                            val color = when {
                                SaltTheme.configs.isDarkTheme -> Color.Green
                                LocalLayoutDirection.current == LayoutDirection.Rtl -> Color.Blue
                                else -> Color.Red
                            }
                            Box(modifier = Modifier.fillMaxSize().background(color))
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .material(isDarkTheme = false)
                                .testTag("material")
                        )
                    }
                }
            }
        }
        var previous: ImageBitmap? = null
        for (step in 0..2) {
            runOnIdle {
                direction = if (step > 0) {
                    LayoutDirection.Rtl
                } else {
                    LayoutDirection.Ltr
                }
                dark = step == 2
            }
            waitUntil(timeoutMillis = 5_000) {
                materialState.sources.singleOrNull()?.entries?.any {
                    it.image != null && it.image !== previous
                } == true
            }
            val pixel = onNodeWithTag("material").captureToImage().toPixelMap()[80, 80]
            when (step) {
                0 -> assertTrue(pixel.red > pixel.blue + 0.01f)
                1 -> assertTrue(pixel.blue > pixel.red + 0.01f)
                2 -> assertTrue(pixel.green > pixel.red + 0.01f)
            }
            previous = runOnIdle { materialState.sources.single().entries.single().image }
        }
    }

    @Test
    fun resizingKeepsLivePixelsUntilTheFinalSizeIsSettled() = runComposeUiTest {
        var edge by mutableStateOf(160.dp)
        val capturedSizes = mutableListOf<IntSize>()
        val materialState = MaterialState(readback = {
            capturedSizes.add(it.size)
            it.toImageBitmap()
        })
        setContent {
            SaltTheme(
                configs = SaltConfigs.default(isDarkTheme = false),
                material = SaltMaterial(MaterialType.BlurryGlass)
            ) {
                CompositionLocalProvider(LocalMaterialState provides materialState) {
                    Box(modifier = Modifier.size(edge)) {
                        MaterialSource(
                            materialSelf = false,
                            mode = MaterialSourceMode.Static(Unit)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val color = if (size.width < 200.dp.toPx()) {
                                    Color.Red
                                } else {
                                    Color.Blue
                                }
                                drawRect(color)
                            }
                        }
                        Box(
                            modifier = Modifier.fillMaxSize().material().testTag("material")
                        )
                    }
                }
            }
        }
        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.any { it.image != null } == true
        }
        val red = onNodeWithTag("material").captureToImage().toPixelMap()[80, 80]
        mainClock.autoAdvance = false
        for (step in 1..10) {
            runOnIdle { edge = (160 + step * 8).dp }
            mainClock.advanceTimeByFrame()
        }
        mainClock.autoAdvance = true
        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.any {
                it.image != null && it.size == IntSize(240, 240)
            } == true
        }
        val blue = onNodeWithTag("material").captureToImage().toPixelMap()[120, 120]
        assertTrue(red.red > red.blue + 0.1f)
        assertTrue(blue.blue > blue.red + 0.1f)
        assertTrue(
            capturedSizes.size == 2,
            "A resize burst must not repeatedly bake: $capturedSizes"
        )
    }

    @Test
    fun staticMicaUpdatesWhenThemeChanges() = runComposeUiTest {
        var dark by mutableStateOf(false)
        lateinit var materialState: MaterialState
        setContent {
            SaltTheme(
                configs = SaltConfigs.default(isDarkTheme = dark),
                material = SaltMaterial(MaterialType.Mica)
            ) {
                materialState = LocalMaterialState.current!!
                Box(modifier = Modifier.size(160.dp)) {
                    MaterialSource(mode = MaterialSourceMode.Static(Unit)) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Red))
                    }
                    Box(modifier = Modifier.fillMaxSize().subMaterial().testTag("material"))
                }
            }
        }
        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.count { it.image != null } == 2
        }
        val light = onNodeWithTag("material").captureToImage().toPixelMap()[80, 80]
        runOnIdle { dark = true }
        waitUntil(timeoutMillis = 5_000) {
            materialState.sources.singleOrNull()?.entries?.count { it.image != null } == 2
        }
        val darkPixel = onNodeWithTag("material").captureToImage().toPixelMap()[80, 80]
        assertTrue(
            light.red + light.green + light.blue > 2.3f,
            "Light Mica must remain light: $light"
        )
        assertTrue(
            darkPixel.red + darkPixel.green + darkPixel.blue < 0.6f,
            "Dark Mica must rebuild with the theme: $darkPixel"
        )
    }

    private fun ImageBitmap.averageDifference(other: ImageBitmap): Float {
        val before = toPixelMap()
        val after = other.toPixelMap()
        var difference = 0f
        for (y in 0 until height) {
            for (x in 0 until width) {
                val left = before[x, y]
                val right = after[x, y]
                difference += abs(left.red - right.red) + abs(left.green - right.green) +
                    abs(left.blue - right.blue) + abs(left.alpha - right.alpha)
            }
        }
        return difference / (width * height * 4)
    }
}
