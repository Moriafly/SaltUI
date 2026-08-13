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

package com.moriafly.salt.sample.ui

import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.activity.compose.setContent
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.unit.Velocity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.moriafly.salt.ui.ScrollState
import com.moriafly.salt.ui.horizontalScroll
import com.moriafly.salt.ui.verticalScroll
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.currentCoroutineContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScrollInterruptionTest {
    private var scenario: ActivityScenario<MainActivity>? = null

    @After
    fun closeActivity() {
        scenario?.close()
        scenario = null
    }

    @Test
    fun horizontalSwipeDuringVerticalOverscrollMovesHorizontalContent() {
        launchActivity()
        pullPastVerticalStart()
        waitUntil { ScrollInterruptionTestState.overscrollAnimationRunning }

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val centerY = device.displayHeight / 2
        device.swipe(
            device.displayWidth * 4 / 5,
            centerY,
            device.displayWidth / 5,
            centerY,
            20
        )

        waitUntil { ScrollInterruptionTestState.horizontalState.value > 100 }
        assertTrue(ScrollInterruptionTestState.horizontalState.value > 100)
    }

    @Test
    fun verticalDragDuringVerticalOverscrollMovesVerticalContent() {
        launchActivity()
        pullPastVerticalStart()
        waitUntil { ScrollInterruptionTestState.overscrollAnimationRunning }

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val centerX = device.displayWidth / 2
        device.swipe(
            centerX,
            device.displayHeight * 4 / 5,
            centerX,
            device.displayHeight / 5,
            20
        )

        waitUntil { ScrollInterruptionTestState.verticalState.value > 100 }
        assertTrue(ScrollInterruptionTestState.verticalState.value > 100)
    }

    @Test
    fun tapDuringVerticalOverscrollOnlyStopsAnimation() {
        launchActivity()
        pullPastVerticalStart()
        waitUntil { ScrollInterruptionTestState.overscrollAnimationRunning }

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        assertTrue(device.click(device.displayWidth / 2, device.displayHeight / 2))

        waitUntil { !ScrollInterruptionTestState.overscrollAnimationRunning }
        assertFalse(ScrollInterruptionTestState.overscrollAnimationRunning)
        assertEquals(0, ScrollInterruptionTestState.clickCount)
        assertEquals(0, ScrollInterruptionTestState.horizontalState.value)
    }

    @Test
    fun motionInsidePointerSlopDuringVerticalOverscrollDoesNotClickOrScroll() {
        launchActivity()
        pullPastVerticalStart()
        waitUntil { ScrollInterruptionTestState.overscrollAnimationRunning }

        val center = screenCenter()
        InjectedTouchGesture(center).use { gesture ->
            gesture.moveTo(
                center -
                    Offset(
                        x = (ScrollInterruptionTestState.pointerSlop - 1f).coerceAtLeast(0f),
                        y = 0f,
                    )
            )
        }

        waitUntil { !ScrollInterruptionTestState.overscrollAnimationRunning }
        assertEquals(0, ScrollInterruptionTestState.clickCount)
        assertEquals(0, ScrollInterruptionTestState.horizontalState.value)
    }

    @Test
    fun motionAtPointerSlopDuringVerticalOverscrollStartsHorizontalDrag() {
        launchActivity()
        pullPastVerticalStart()
        waitUntil { ScrollInterruptionTestState.overscrollAnimationRunning }

        val center = screenCenter()
        InjectedTouchGesture(center).use { gesture ->
            gesture.moveTo(
                center - Offset(x = ScrollInterruptionTestState.pointerSlop, y = 0f)
            )
            waitUntil { ScrollInterruptionTestState.horizontalState.isScrollInProgress }
            gesture.moveTo(
                center - Offset(x = ScrollInterruptionTestState.pointerSlop + 32f, y = 0f)
            )
        }

        waitUntil { !ScrollInterruptionTestState.horizontalState.isScrollInProgress }
        assertEquals(0, ScrollInterruptionTestState.clickCount)
        assertTrue(ScrollInterruptionTestState.horizontalState.value > 0)
    }

    private fun launchActivity() {
        ScrollInterruptionTestState.reset()
        scenario = ActivityScenario.launch(MainActivity::class.java).also { scenario ->
            scenario.onActivity { activity ->
                ScrollInterruptionTestState.pointerSlop =
                    ViewConfiguration.get(activity).scaledTouchSlop.toFloat()
                activity.setContent { ScrollInterruptionTestContent() }
            }
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        waitUntil { ScrollInterruptionTestState.isReady }
    }

    private fun pullPastVerticalStart() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val centerX = device.displayWidth / 2
        device.swipe(
            centerX,
            device.displayHeight * 2 / 5,
            centerX,
            device.displayHeight * 4 / 5,
            20
        )
    }

    private fun screenCenter(): Offset {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        return Offset(x = device.displayWidth / 2f, y = device.displayHeight / 2f)
    }

    private fun waitUntil(timeoutMillis: Long = 5_000, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + timeoutMillis
        while (!condition()) {
            if (SystemClock.uptimeMillis() >= deadline) {
                throw AssertionError("Condition was not met within $timeoutMillis ms")
            }
            SystemClock.sleep(16)
        }
    }
}

@Composable
private fun ScrollInterruptionTestContent() {
    val horizontalState = remember { ScrollState(initial = 0) }
    val verticalState = remember { ScrollState(initial = 0) }
    val overscrollEffect = remember { SlowTestOverscrollEffect() }
    var clickCount by remember { mutableIntStateOf(0) }

    SideEffect {
        ScrollInterruptionTestState.horizontalState = horizontalState
        ScrollInterruptionTestState.verticalState = verticalState
        ScrollInterruptionTestState.clickCount = clickCount
        ScrollInterruptionTestState.isReady = true
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val pageWidth = maxWidth
        val pageHeight = maxHeight
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalState, overscrollEffect = null)
        ) {
            Column(
                modifier = Modifier
                    .width(pageWidth)
                    .height(pageHeight)
                    .verticalScroll(verticalState, overscrollEffect)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(pageHeight * 2)
                        .background(Color.Red)
                        .clickable { clickCount++ }
                )
            }
            Box(
                modifier = Modifier
                    .width(pageWidth)
                    .fillMaxHeight()
                    .background(Color.Blue)
            )
        }
    }
}

private object ScrollInterruptionTestState {
    @Volatile
    var isReady = false

    @Volatile
    var overscrollAnimationRunning = false

    @Volatile
    var clickCount = 0

    @Volatile
    var pointerSlop = 0f

    lateinit var horizontalState: ScrollState
    lateinit var verticalState: ScrollState

    fun reset() {
        isReady = false
        overscrollAnimationRunning = false
        clickCount = 0
        pointerSlop = 0f
    }
}

private class InjectedTouchGesture(startPosition: Offset) : AutoCloseable {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val downTime = SystemClock.uptimeMillis()
    private var position = startPosition
    private var isActive = true

    init {
        inject(MotionEvent.ACTION_DOWN)
    }

    fun moveTo(position: Offset) {
        check(isActive)
        this.position = position
        inject(MotionEvent.ACTION_MOVE)
    }

    override fun close() {
        if (!isActive) return
        inject(MotionEvent.ACTION_UP)
        isActive = false
    }

    private fun inject(action: Int) {
        val event =
            MotionEvent.obtain(
                downTime,
                SystemClock.uptimeMillis(),
                action,
                position.x,
                position.y,
                0,
            )
        event.source = InputDevice.SOURCE_TOUCHSCREEN
        try {
            assertTrue(instrumentation.uiAutomation.injectInputEvent(event, true))
        } finally {
            event.recycle()
        }
        SystemClock.sleep(16)
    }
}

private class SlowTestOverscrollEffect : OverscrollEffect {
    override val node: DelegatableNode = object : Modifier.Node() {}

    private var animationJob: Job? = null
    private var interruptionCaught = false

    override val isInProgress: Boolean
        get() = animationJob?.isActive == true

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset
    ): Offset {
        animationJob?.cancel()
        animationJob = null
        ScrollInterruptionTestState.overscrollAnimationRunning = false
        if (delta == Offset.Zero) {
            interruptionCaught = true
        }
        return performScroll(delta)
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity
    ) {
        performFling(velocity)
        if (interruptionCaught) {
            interruptionCaught = false
            return
        }
        val job = currentCoroutineContext()[Job]
        animationJob = job
        ScrollInterruptionTestState.overscrollAnimationRunning = true
        try {
            awaitCancellation()
        } finally {
            if (animationJob === job) {
                animationJob = null
                ScrollInterruptionTestState.overscrollAnimationRunning = false
            }
        }
    }
}
