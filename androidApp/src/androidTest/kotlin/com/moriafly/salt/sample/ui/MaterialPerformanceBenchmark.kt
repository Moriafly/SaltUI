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

import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Bundle
import android.os.Debug
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.os.SystemClock
import android.view.FrameMetrics
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.moriafly.salt.ui.SaltConfigs
import com.moriafly.salt.ui.SaltMaterial
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.material.MaterialSource
import com.moriafly.salt.ui.material.MaterialSourceMode
import com.moriafly.salt.ui.material.MaterialType
import com.moriafly.salt.ui.material.material
import com.moriafly.salt.ui.material.subMaterial
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Measures a real Android window, including rendering and process memory. Run through
 * scripts/benchmark-material-cache.ps1; ordinary correctness runs skip this benchmark.
 */
@OptIn(UnstableSaltUiApi::class)
@RunWith(AndroidJUnit4::class)
class MaterialPerformanceBenchmark {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val materialState = AtomicReference<Any>()
    private val sourceSize = AtomicReference(IntSize.Zero)

    @Test
    fun measureMaterialRenderingAndMemory() {
        val arguments = InstrumentationRegistry.getArguments()
        assumeTrue(arguments.getString("materialBenchmark") == "true")
        assumeTrue(Build.VERSION.SDK_INT >= 31)
        val type = MaterialType.valueOf(arguments.getString("materialType", "BlurryGlass"))
        val scene = arguments.getString("scene", "Large")
        val cached = arguments.getString("mode", "Dynamic") == "Static"
        val durationMs = arguments.getString("durationMs", "2000").toLong()
        val frames = ArrayList<LongArray>()
        val bakeFrames = ArrayList<LongArray>()
        var recording = false
        var recordingBake = false
        var callbackDrops = 0
        var bakeCallbackDrops = 0
        val metricsThread = HandlerThread("material-frame-metrics").apply { start() }
        val metricsHandler = Handler(metricsThread.looper)
        val listener = Window.OnFrameMetricsAvailableListener { _, metrics, dropped ->
            // All collection state is confined to this handler thread.
            if ((recording || recordingBake) &&
                metrics.getMetric(FrameMetrics.FIRST_DRAW_FRAME) == 0L
            ) {
                val sample = longArrayOf(
                    metrics.getMetric(FrameMetrics.TOTAL_DURATION),
                    metrics.getMetric(FrameMetrics.GPU_DURATION),
                    metrics.getMetric(FrameMetrics.DRAW_DURATION),
                    metrics.getMetric(FrameMetrics.COMMAND_ISSUE_DURATION),
                    metrics.getMetric(FrameMetrics.DEADLINE)
                )
                if (recording) {
                    frames.add(sample)
                    callbackDrops += dropped
                } else {
                    bakeFrames.add(sample)
                    bakeCallbackDrops += dropped
                }
            }
        }

        var mode by mutableStateOf<MaterialSourceMode>(MaterialSourceMode.Dynamic)
        var animate by mutableStateOf(false)
        var phase by mutableFloatStateOf(0f)
        var buildMs = 0.0
        var cacheBytes = 0L
        var cacheCount = 0
        var readbackMs = emptyList<Double>()
        try {
            ActivityScenario.launch(MainActivity::class.java).use { activityScenario ->
                activityScenario.onActivity { activity ->
                    val debuggable = activity.applicationInfo.flags and
                        ApplicationInfo.FLAG_DEBUGGABLE != 0
                    assertTrue("Use the non-debuggable benchmark variant", !debuggable)
                    assertTrue(activity.window.decorView.isHardwareAccelerated)
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    activity.window.addOnFrameMetricsAvailableListener(listener, metricsHandler)
                    activity.setContent {
                        SaltTheme(
                            configs = SaltConfigs.default(isDarkTheme = false),
                            material = SaltMaterial(type)
                        ) {
                            materialState.set(checkNotNull(MaterialStateLocalForTests.current))
                            BenchmarkScene(
                                scene = scene,
                                mode = mode,
                                phase = { phase }
                            )
                            LaunchedEffect(animate) {
                                if (animate) {
                                    val start = withFrameNanos { it }
                                    while (true) {
                                        withFrameNanos {
                                            phase = sin((it - start) / 1_000_000_000.0 * 3.5)
                                                .toFloat()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                instrumentation.waitForIdleSync()
                SystemClock.sleep(500)
                if (cached) {
                    onMetricsThread(metricsHandler) { recordingBake = true }
                    val started = SystemClock.elapsedRealtimeNanos()
                    instrumentation.runOnMainSync { mode = MaterialSourceMode.Static(Unit) }
                    val expectedCount = if (scene == "Shared") {
                        2
                    } else {
                        1
                    }
                    awaitBakedEntries(expectedCount)
                    buildMs = (SystemClock.elapsedRealtimeNanos() - started) / 1_000_000.0
                    val images = materialCacheImages(materialState.get())
                    cacheCount = images.size
                    cacheBytes = images.sumOf { it.asAndroidBitmap().allocationByteCount.toLong() }
                    readbackMs = materialCacheEntries(materialState.get()).mapNotNull { entry ->
                        // Older baseline APKs have no per-entry readback timing.
                        entry.javaClass.methods.firstOrNull { it.name == "getReadbackNanos" }
                            ?.invoke(entry)?.let { (it as Long) / 1_000_000.0 }
                    }
                    // Include the publication frame and allow its GPU metrics to arrive.
                    SystemClock.sleep(100)
                    onMetricsThread(metricsHandler) { recordingBake = false }
                }

                // Warm both pipelines with the same moving panels before collecting frame data.
                instrumentation.runOnMainSync { animate = true }
                SystemClock.sleep(1_000)
                instrumentation.runOnMainSync { animate = false }
                val beforeMemory = memorySnapshot()

                instrumentation.runOnMainSync { animate = true }
                onMetricsThread(metricsHandler) { recording = true }
                val cpuStart = Process.getElapsedCpuTime()
                val wallStart = SystemClock.elapsedRealtime()
                SystemClock.sleep(durationMs)
                onMetricsThread(metricsHandler) { recording = false }
                val wallMs = SystemClock.elapsedRealtime() - wallStart
                val cpuMs = Process.getElapsedCpuTime() - cpuStart
                instrumentation.runOnMainSync { animate = false }
                val afterMemory = memorySnapshot()
                assertTrue("Too few rendered frames: ${frames.size}", frames.size >= 20)

                val modeName = if (cached) {
                    "Static"
                } else {
                    "Dynamic"
                }
                val result = JSONObject()
                    .put("materialType", type.name)
                    .put("scene", scene)
                    .put("mode", modeName)
                    .put("iteration", arguments.getString("iteration", "1").toInt())
                    .put("sdk", Build.VERSION.SDK_INT)
                    .put("device", Build.MODEL)
                    .put("debuggable", false)
                    .put("sourceWidth", sourceSize.get().width)
                    .put("sourceHeight", sourceSize.get().height)
                    .put("cacheCount", cacheCount)
                    .put("cacheBitmapBytes", cacheBytes)
                    .put("cacheReadyMs", buildMs)
                    .put("readbackMs", JSONArray(readbackMs))
                    .put("bakeTotalMs", distribution(bakeFrames.map { it[0] }))
                    .put("bakeGpuMs", distribution(bakeFrames.map { it[1] }))
                    .put("bakeMetricsCallbackDrops", bakeCallbackDrops)
                    .put("bakeFramesNs", JSONArray(bakeFrames.map { JSONArray(it.toList()) }))
                    .put("wallMs", wallMs)
                    .put("cpuMs", cpuMs)
                    .put("cpuMsPerFrame", cpuMs.toDouble() / frames.size)
                    .put("frameCount", frames.size)
                    .put("metricsCallbackDrops", callbackDrops)
                    .put("totalMs", distribution(frames.map { it[0] }))
                    .put("gpuMs", distribution(frames.map { it[1] }))
                    .put("drawMs", distribution(frames.map { it[2] }))
                    .put("commandMs", distribution(frames.map { it[3] }))
                    .put("deadlineMisses", frames.count { it[4] > 0 && it[0] > it[4] })
                    .put("memoryBefore", beforeMemory)
                    .put("memoryAfter", afterMemory)
                    .put("framesNs", JSONArray(frames.map { JSONArray(it.toList()) }))
                instrumentation.sendStatus(2, Bundle().apply {
                    putString("materialBenchmark", result.toString())
                })
                activityScenario.onActivity {
                    it.window.removeOnFrameMetricsAvailableListener(listener)
                }
            }
        } finally {
            metricsThread.quitSafely()
            metricsThread.join()
        }
    }

    @Composable
    private fun BenchmarkScene(
        scene: String,
        mode: MaterialSourceMode,
        phase: () -> Float
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            MaterialSource(
                modifier = Modifier.onSizeChanged(sourceSize::set),
                materialSelf = false,
                mode = mode
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawRect(
                        brush = Brush.linearGradient(listOf(Color.Red, Color.Blue, Color.Green))
                    )
                    val cellWidth = size.width / 16
                    val cellHeight = size.height / 24
                    for (row in 0 until 24) {
                        for (column in 0 until 16) {
                            if ((row + column) % 2 == 0) {
                                drawRect(
                                    color = Color.White.copy(alpha = 0.35f),
                                    topLeft = Offset(column * cellWidth, row * cellHeight),
                                    size = Size(cellWidth, cellHeight)
                                )
                            }
                        }
                    }
                }
            }

            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }
            val widthFraction = when (scene) {
                "Small" -> 0.25f
                "Shared" -> 0.4f
                else -> 0.8f
            }
            val heightFraction = when (scene) {
                "Small" -> 0.2f
                "Shared" -> 0.35f
                else -> 0.8f
            }
            val panelCount = if (scene == "Shared") {
                4
            } else {
                1
            }
            for (index in 0 until panelCount) {
                val baseX = if (panelCount == 1) {
                    (1 - widthFraction) / 2
                } else {
                    0.05f + (index % 2) * 0.5f
                }
                val baseY = if (panelCount == 1) {
                    (1 - heightFraction) / 2
                } else {
                    0.1f + (index / 2) * 0.45f
                }
                val effect = if (scene == "Shared" && index % 2 == 1) {
                    Modifier.subMaterial()
                } else {
                    Modifier.material()
                }
                Box(
                    modifier = Modifier
                        .offset {
                            val motion = phase() * 0.025f
                            IntOffset(
                                x = ((baseX + motion) * widthPx).roundToInt(),
                                y = ((baseY + motion) * heightPx).roundToInt()
                            )
                        }
                        .size(maxWidth * widthFraction, maxHeight * heightFraction)
                        .then(effect)
                ) {
                    BasicText(
                        text = "Material ${index + 1}",
                        style = TextStyle(color = Color.White, fontSize = 18.sp)
                    )
                }
            }
        }
    }

    private fun awaitBakedEntries(
        count: Int
    ) {
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (SystemClock.uptimeMillis() < deadline) {
            if (materialCacheImages(materialState.get()).size == count) {
                return
            }
            SystemClock.sleep(5)
        }
        error("Expected $count baked images; the scene may have fallen back to live rendering")
    }

    private fun memorySnapshot(): JSONObject {
        Runtime.getRuntime().gc()
        SystemClock.sleep(150)
        val samples = (0 until 3).map {
            Debug.MemoryInfo().also { info ->
                Debug.getMemoryInfo(info)
                SystemClock.sleep(50)
            }
        }
        val result = JSONObject()
        for (name in listOf("total-pss", "java-heap", "native-heap", "graphics", "private-other")) {
            val values = samples.map { it.getMemoryStat("summary.$name").toLong() }.sorted()
            result.put("${name}Kb", values[values.size / 2])
        }
        return result
    }

    private fun distribution(
        values: List<Long>
    ): JSONObject {
        val sorted = values.filter { it >= 0 }.sorted()
        if (sorted.isEmpty()) {
            return JSONObject().put("supported", false)
        }
        fun percentile(fraction: Double): Double {
            val index = (ceil(sorted.size * fraction).toInt() - 1).coerceAtLeast(0)
            return sorted[index] / 1_000_000.0
        }
        return JSONObject()
            .put("supported", true)
            .put("median", percentile(0.5))
            .put("p95", percentile(0.95))
            .put("max", sorted.last() / 1_000_000.0)
            .put("mean", sorted.average() / 1_000_000.0)
    }

    private fun onMetricsThread(
        handler: Handler,
        block: () -> Unit
    ) {
        val complete = CountDownLatch(1)
        handler.post {
            block()
            complete.countDown()
        }
        assertTrue("Frame metrics handler stalled", complete.await(5, TimeUnit.SECONDS))
    }
}
