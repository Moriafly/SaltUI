/*
 * Copyright 2026 The Android Open Source Project
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

@file:Suppress("unused", "INVISIBLE_REFERENCE", "ktlint:standard:kdoc", "UnusedReceiverParameter")

package com.moriafly.salt.ui.gestures

import androidx.compose.foundation.gestures.NestedScrollScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.util.fastAll
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class TrackpadScrollingLogic(
    scrollingLogic: ScrollingLogic,
    onScrollStopped: suspend (velocity: Velocity) -> Unit,
    density: Density,
) : NonTouchScrollingLogic(scrollingLogic, onScrollStopped, density) {
    override fun isScrollingEvent(pointerEvent: PointerEvent): Boolean =
        pointerEvent.type == PointerEventType.PanStart ||
            pointerEvent.type == PointerEventType.PanMove ||
            pointerEvent.type == PointerEventType.PanEnd

    private class TrackpadScrollDelta(
        val value: Offset,
        val timeMillis: Long,
        val isEnd: Boolean
    ) {
        operator fun plus(other: TrackpadScrollDelta) =
            TrackpadScrollDelta(
                value = value + other.value,
                // Pick time from last one
                timeMillis = maxOf(timeMillis, other.timeMillis),
                // Combine together the flag to end the gesture
                isEnd = isEnd || other.isEnd,
            )
    }

    private val channel = Channel<TrackpadScrollDelta>(capacity = Channel.UNLIMITED)

    private var receivingPanEventsJob: Job? = null

    override fun startReceivingEvents(coroutineScope: CoroutineScope) {
        if (receivingPanEventsJob == null) {
            receivingPanEventsJob =
                coroutineScope.launch {
                    try {
                        while (coroutineContext.isActive) {
                            scrollingLogic.dispatchTrackpadScroll(channel.receive())
                        }
                    } finally {
                        receivingPanEventsJob = null
                    }
                }
        }
    }

    override fun onScrollingEvent(pointerEvent: PointerEvent, bounds: IntSize): Boolean {
        val change = pointerEvent.changes.firstOrNull() ?: return isScrolling
        // A zero-delta PanStart must leave descendants free to claim the next pan event.
        if (pointerEvent.type == PointerEventType.PanStart &&
            change.panOffset.isZero() &&
            change.historical.fastAll { it.panOffset.isZero() }
        ) {
            return isScrolling
        }

        var sent = false
        change.historical.fastForEach { historicalChange ->
            val delta = -historicalChange.panOffset
            if (canConsumePan(delta)) {
                sent = channel.trySend(
                    TrackpadScrollDelta(delta, historicalChange.uptimeMillis, isEnd = false)
                ).isSuccess || sent
            }
        }
        val delta = -change.panOffset
        val isPanEnd = pointerEvent.type == PointerEventType.PanEnd
        if (canConsumePan(delta) || isPanEnd) {
            sent = channel.trySend(
                TrackpadScrollDelta(delta, change.uptimeMillis, isEnd = isPanEnd)
            ).isSuccess || sent
        }
        return sent || isScrolling
    }

    private fun Offset.isZero(): Boolean = x == 0f && y == 0f

    private fun canConsumePan(delta: Offset): Boolean = with(scrollingLogic) {
        canConsumeDelta(delta.toSingleAxisDeltaFromAngle())
    }

    private fun Channel<TrackpadScrollDelta>.sumOrNull(): TrackpadScrollDelta? {
        var sum: TrackpadScrollDelta? = null
        for (i in untilNull { tryReceive().getOrNull() }) {
            sum = if (sum == null) i else sum + i
        }
        return sum
    }

    private fun trackVelocity(scrollDelta: TrackpadScrollDelta) {
        velocityTracker.addDelta(scrollDelta.timeMillis, scrollDelta.value)
    }

    private suspend fun ScrollingLogic.dispatchTrackpadScroll(scrollDelta: TrackpadScrollDelta) {
        var targetScrollDelta = scrollDelta
        trackVelocity(scrollDelta)
        // Sum delta from all pending events to drain the channel.
        channel.sumOrNull()?.let {
            trackVelocity(it)
            targetScrollDelta += it
        }

        userScroll {
            dispatchTrackpadScroll(
                targetScrollDelta.value.toSingleAxisDeltaFromAngle()
            )
            while (!targetScrollDelta.isEnd) {
                targetScrollDelta = channel.busyReceive()
                trackVelocity(targetScrollDelta)
                channel.sumOrNull()?.let {
                    trackVelocity(it)
                    targetScrollDelta += it
                }
                dispatchTrackpadScroll(
                    targetScrollDelta.value.toSingleAxisDeltaFromAngle()
                )
            }
        }

        onScrollStopped(velocityTracker.calculateVelocity())
    }

    private fun NestedScrollScope.dispatchTrackpadScroll(delta: Float) =
        with(scrollingLogic) {
            val offset = delta.toOffset()
            val consumed = scrollByWithOverscroll(offset, NestedScrollSource.UserInput)
            consumed.toSingleAxisDeltaFromAngle()
        }
}
