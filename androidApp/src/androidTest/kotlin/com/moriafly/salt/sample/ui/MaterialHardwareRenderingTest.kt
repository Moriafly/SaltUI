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

package com.moriafly.salt.sample.ui

import android.graphics.Bitmap
import android.os.SystemClock
import android.view.Choreographer
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.moriafly.salt.ui.SaltMaterial
import com.moriafly.salt.ui.SaltConfigs
import com.moriafly.salt.ui.SaltColors
import com.moriafly.salt.ui.SaltDynamicColors
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.material.MaterialSource
import com.moriafly.salt.ui.material.MaterialSourceMode
import com.moriafly.salt.ui.material.MaterialType
import com.moriafly.salt.ui.material.material
import com.moriafly.salt.ui.material.subMaterial
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Exercises material readback and reuse through hardware-composited Android window pixels.
 */
@OptIn(UnstableSaltUiApi::class)
@RunWith(AndroidJUnit4::class)
class MaterialHardwareRenderingTest {
    private val materialState = AtomicReference<Any>()

    @Test
    fun resolvedThemeColorKeyUpdatesHardwareRenderedPixels() {
        var highlight by mutableStateOf(Color.Red)
        val bounds = AtomicReference(Rect.Zero)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    SaltTheme(
                        configs = SaltConfigs.default(isDarkTheme = false),
                        dynamicColors = SaltDynamicColors.default(
                            light = SaltColors.defaultLight(highlight = highlight)
                        ),
                        material = SaltMaterial(MaterialType.BlurryGlass)
                    ) {
                        materialState.set(checkNotNull(MaterialStateLocalForTests.current))
                        val sourceColor = SaltTheme.colors.highlight
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.size(160.dp)) {
                                MaterialSource(
                                    materialSelf = false,
                                    mode = MaterialSourceMode.Static(sourceColor)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize().background(sourceColor))
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .material()
                                        .onGloballyPositioned { bounds.set(it.boundsInWindow()) }
                                )
                            }
                        }
                    }
                }
            }
            awaitCache()
            awaitPixel(bounds) { it.red > it.blue + 0.1f }
            val original = materialCacheImages(materialState.get()).single()
            scenario.onActivity { highlight = Color.Blue }
            awaitCache(previous = original)
            awaitFrames()
            awaitPixel(bounds) { it.blue > it.red + 0.1f }
        }
    }

    @Test
    fun staticMaterialsMatchWholeHardwareRenderedPanels() {
        var mode by mutableStateOf<MaterialSourceMode>(MaterialSourceMode.Dynamic)
        var type by mutableStateOf(MaterialType.BlurryGlass)
        var dark by mutableStateOf(false)
        var subBackground by mutableStateOf(false)
        val bounds = AtomicReference(Rect.Zero)
        val markerBounds = AtomicReference(Rect.Zero)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    SaltTheme(
                        configs = SaltConfigs.default(isDarkTheme = dark),
                        material = SaltMaterial(type)
                    ) {
                        materialState.set(checkNotNull(MaterialStateLocalForTests.current))
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier.size(241.dp, 161.dp)
                                    .onGloballyPositioned { bounds.set(it.boundsInWindow()) }
                            ) {
                                MaterialSource(materialSelf = false, mode = mode) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawRect(
                                            brush = Brush.linearGradient(
                                                listOf(Color.Red, Color.Blue, Color.Green)
                                            )
                                        )
                                        drawRect(
                                            color = Color.White.copy(alpha = 0.5f),
                                            topLeft = Offset(size.width * 0.3f, size.height * 0.2f),
                                            size = Size(size.width * 0.2f, size.height * 0.5f)
                                        )
                                    }
                                }
                                val effect = if (subBackground) {
                                    Modifier.subMaterial()
                                } else {
                                    Modifier.material()
                                }
                                Box(modifier = Modifier.fillMaxSize().then(effect))
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .size(24.dp)
                                    .background(caseMarker(type, dark, subBackground))
                                    .onGloballyPositioned { markerBounds.set(it.boundsInWindow()) }
                            )
                        }
                    }
                }
            }
            for (materialType in MaterialType.entries.filter { it != MaterialType.None }) {
                for (isDark in listOf(false, true)) {
                    for (subLayer in listOf(false, true)) {
                        scenario.onActivity {
                            mode = MaterialSourceMode.Dynamic
                            type = materialType
                            dark = isDark
                            subBackground = subLayer
                        }
                        val marker = caseMarker(materialType, isDark, subLayer)
                        awaitPixel(markerBounds) { it.difference(marker) < 0.02f }
                        val live = panelImage(bounds.get())
                        try {
                            scenario.onActivity { mode = MaterialSourceMode.Static(Unit) }
                            awaitCache()
                            awaitFrames()
                            val cached = panelImage(bounds.get())
                            try {
                                val difference = imageDifference(live, cached)
                                if (difference >= 0.015f) {
                                    val directory = checkNotNull(
                                        InstrumentationRegistry.getInstrumentation()
                                            .targetContext.getExternalFilesDir("material-tests")
                                    )
                                    val images = listOf("live" to live, "cached" to cached)
                                    for ((name, image) in images) {
                                        java.io.File(directory, "$name.png").outputStream().use {
                                            image.compress(Bitmap.CompressFormat.PNG, 100, it)
                                        }
                                    }
                                }
                                assertTrue(
                                    "$materialType/dark=$isDark/sub=$subLayer: $difference",
                                    difference < 0.015f
                                )
                            } finally {
                                cached.recycle()
                            }
                        } finally {
                            live.recycle()
                        }
                    }
                }
            }
        }
    }

    private fun caseMarker(
        type: MaterialType,
        dark: Boolean,
        subBackground: Boolean
    ): Color = Color(
        red = type.ordinal * 0.2f,
        green = if (dark) { 0.9f } else { 0.1f },
        blue = if (subBackground) { 0.8f } else { 0.2f }
    )

    @Test
    fun staticBackgroundReusesPixelsAndRefreshesForContentAndSize() {
        var sourceColor by mutableStateOf(Color.Red)
        var foreground by mutableStateOf(Color.Green)
        var edge by mutableStateOf(160.dp)
        var mode by mutableStateOf<MaterialSourceMode>(MaterialSourceMode.Dynamic)
        val sourceBounds = AtomicReference(Rect.Zero)
        val panelBounds = AtomicReference(Rect.Zero)
        val foregroundBounds = AtomicReference(Rect.Zero)

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertTrue(
                    "This test requires a hardware-accelerated window",
                    activity.window.decorView.isHardwareAccelerated
                )
                activity.setContent {
                    SaltTheme(
                        configs = SaltConfigs.default(isDarkTheme = false),
                        material = SaltMaterial(MaterialType.BlurryGlass)
                    ) {
                        materialState.set(checkNotNull(MaterialStateLocalForTests.current))
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.size(edge)) {
                                MaterialSource(
                                    materialSelf = false,
                                    mode = mode,
                                    modifier = Modifier.onGloballyPositioned {
                                        sourceBounds.set(it.boundsInWindow())
                                    }
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val color = if (size.width > 180.dp.toPx()) {
                                            Color.Green
                                        } else {
                                            sourceColor
                                        }
                                        drawRect(color)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(120.dp)
                                        .material()
                                        .onGloballyPositioned {
                                            panelBounds.set(it.boundsInWindow())
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(foreground)
                                            .onGloballyPositioned {
                                                foregroundBounds.set(it.boundsInWindow())
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val live = awaitPixel(panelBounds) { it.red > it.blue + 0.1f && it.blue > 0.2f }
            scenario.onActivity { mode = MaterialSourceMode.Static(Unit) }
            awaitCache()
            val baked = awaitPixel(panelBounds) { it.difference(live) < 0.03f }
            assertTrue(
                "Baking must preserve the visible material: $live -> $baked",
                live.difference(baked) < 0.03f
            )

            scenario.onActivity {
                sourceColor = Color.Blue
                foreground = Color.Yellow
            }
            awaitPixel(sourceBounds, horizontal = 0.03f) { it.blue > 0.9f && it.red < 0.1f }
            awaitPixel(foregroundBounds) { it.red > 0.9f && it.green > 0.9f }
            val reused = awaitPixel(panelBounds) { it.difference(baked) < 0.03f }
            assertTrue(
                "An unchanged content version must reuse the baked background",
                baked.difference(reused) < 0.03f
            )

            scenario.onActivity { mode = MaterialSourceMode.Static(1) }
            awaitCache()
            awaitPixel(panelBounds) { it.blue > it.red + 0.1f }

            scenario.onActivity { edge = 200.dp }
            awaitPixel(panelBounds) { it.green > it.blue + 0.1f && it.green > it.red + 0.1f }
        }
    }

    @Test
    fun staticMicaMatchesLiveLayersAndUpdatesWithTheTheme() {
        var mode by mutableStateOf<MaterialSourceMode>(MaterialSourceMode.Dynamic)
        var dark by mutableStateOf(false)
        var subBackground by mutableStateOf(false)
        val bounds = AtomicReference(Rect.Zero)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    SaltTheme(
                        configs = SaltConfigs.default(isDarkTheme = dark),
                        material = SaltMaterial(MaterialType.Mica)
                    ) {
                        materialState.set(checkNotNull(MaterialStateLocalForTests.current))
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.size(160.dp)) {
                                MaterialSource(materialSelf = false, mode = mode) {
                                    Box(modifier = Modifier.fillMaxSize().background(Color.Red))
                                }
                                val material = if (subBackground) {
                                    Modifier.subMaterial()
                                } else {
                                    Modifier.material()
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .then(material)
                                        .onGloballyPositioned { bounds.set(it.boundsInWindow()) }
                                )
                            }
                        }
                    }
                }
            }
            for (subLayer in listOf(false, true)) {
                scenario.onActivity {
                    mode = MaterialSourceMode.Dynamic
                    dark = false
                    subBackground = subLayer
                }
                val liveLight = awaitPixel(bounds) {
                    it.red + it.green + it.blue > 2f && it.red > it.blue + 0.02f
                }
                scenario.onActivity { mode = MaterialSourceMode.Static(Unit) }
                awaitCache()
                val cachedLight = awaitPixel(bounds) { it.difference(liveLight) < 0.03f }
                assertTrue(
                    "Light Mica/sub=$subLayer cache difference: $liveLight -> $cachedLight",
                    liveLight.difference(cachedLight) < 0.03f
                )

                scenario.onActivity { dark = true }
                awaitCache()
                awaitPixel(bounds) { it.red + it.green + it.blue < 1f && it.red > it.blue + 0.01f }
                val cachedDark = pixel(bounds.get())
                scenario.onActivity { mode = MaterialSourceMode.Dynamic }
                val liveDark = awaitPixel(bounds) {
                    it.red + it.green + it.blue < 1f && it.red > it.blue + 0.01f
                }
                assertTrue(
                    "Dark Mica must rebuild with the theme: $liveDark -> $cachedDark",
                    liveDark.difference(cachedDark) < 0.03f
                )
            }
        }
    }

    private fun awaitFrames() {
        val complete = CountDownLatch(1)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        instrumentation.runOnMainSync {
            val choreographer = Choreographer.getInstance()
            var remaining = 3
            val callback = object : Choreographer.FrameCallback {
                override fun doFrame(frameTimeNanos: Long) {
                    remaining--
                    if (remaining == 0) {
                        complete.countDown()
                    } else {
                        choreographer.postFrameCallback(this)
                    }
                }
            }
            choreographer.postFrameCallback(callback)
        }
        assertTrue("Android frames stalled", complete.await(5, TimeUnit.SECONDS))
    }

    private fun panelImage(
        bounds: Rect
    ): Bitmap {
        val screenshot = checkNotNull(
            InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        )
        return try {
            Bitmap.createBitmap(
                screenshot,
                bounds.left.roundToInt(),
                bounds.top.roundToInt(),
                bounds.width.roundToInt(),
                bounds.height.roundToInt()
            )
        } finally {
            screenshot.recycle()
        }
    }

    private fun imageDifference(
        first: Bitmap,
        second: Bitmap
    ): Float {
        val count = first.width * first.height
        val firstPixels = IntArray(count)
        val secondPixels = IntArray(count)
        first.getPixels(firstPixels, 0, first.width, 0, 0, first.width, first.height)
        second.getPixels(secondPixels, 0, second.width, 0, 0, second.width, second.height)
        var difference = 0L
        for (index in firstPixels.indices) {
            for (shift in listOf(0, 8, 16)) {
                val a = (firstPixels[index] ushr shift) and 0xff
                val b = (secondPixels[index] ushr shift) and 0xff
                difference += abs(a - b)
            }
        }
        return difference.toFloat() / (count * 3 * 255)
    }

    private fun pixel(
        bounds: Rect,
        horizontal: Float = 0.5f
    ): Color {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        val screenshot = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
        return try {
            val x = (bounds.left + bounds.width * horizontal).roundToInt()
            val y = bounds.center.y.roundToInt()
            Color(screenshot.getPixel(x, y))
        } finally {
            screenshot.recycle()
        }
    }

    private fun awaitPixel(
        bounds: AtomicReference<Rect>,
        horizontal: Float = 0.5f,
        predicate: (Color) -> Boolean
    ): Color {
        val deadline = SystemClock.uptimeMillis() + 5_000
        var latest = Color.Transparent
        var previous = Color.Transparent
        var stableFrames = 0
        while (SystemClock.uptimeMillis() < deadline) {
            val currentBounds = bounds.get()
            if (currentBounds.width > 0f && currentBounds.height > 0f) {
                latest = pixel(currentBounds, horizontal)
                stableFrames = if (predicate(latest) && latest.difference(previous) < 0.005f) {
                    stableFrames + 1
                } else {
                    0
                }
                if (stableFrames >= 2) {
                    return latest
                }
                previous = latest
            }
            SystemClock.sleep(32)
        }
        error("The expected hardware-rendered pixel was not displayed: $latest")
    }

    private fun Color.difference(other: Color): Float =
        abs(red - other.red) + abs(green - other.green) + abs(blue - other.blue)

    private fun awaitCache(previous: ImageBitmap? = null) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (SystemClock.uptimeMillis() < deadline) {
            if (materialCacheImages(materialState.get()).any { it !== previous }) {
                return
            }
            SystemClock.sleep(5)
        }
        error("The expected material cache was not produced")
    }
}

@Suppress("UNCHECKED_CAST")
internal val MaterialStateLocalForTests: ProvidableCompositionLocal<Any?> by lazy {
    Class.forName("com.moriafly.salt.ui.material.MaterialKt")
        .getMethod("getLocalMaterialState")
        .invoke(null) as ProvidableCompositionLocal<Any?>
}

internal fun materialCacheEntries(
    state: Any?
): List<Any> {
    if (state == null) {
        return emptyList()
    }
    val sources = state.javaClass.getMethod("getSources").invoke(state) as List<*>
    val source = sources.singleOrNull() ?: return emptyList()
    val entries = source.javaClass.getMethod("getEntries").invoke(source) as List<*>
    return entries.toList().filterNotNull()
}

internal fun materialCacheImages(
    state: Any?
): List<ImageBitmap> = materialCacheEntries(state).mapNotNull {
    it.javaClass.getMethod("getImage").invoke(it) as ImageBitmap?
}
