/*
 * Copyright 2020 The Android Open Source Project
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

@file:Suppress()

package com.moriafly.salt.ui.gestures

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.coroutineScope

/**
 * Default implementation of [ScrollableState] interface that contains necessary information about
 * the ongoing fling and provides smooth scrolling capabilities.
 *
 * This is the simplest way to set up a [scrollable] modifier. When constructing this
 * [ScrollableState], you must provide a [consumeScrollDelta] lambda, which will be invoked whenever
 * scroll happens (by gesture input, by smooth scrolling, by flinging or nested scroll) with the
 * delta in pixels. The amount of scrolling delta consumed must be returned from this lambda to
 * ensure proper nested scrolling behaviour.
 *
 * @param consumeScrollDelta callback invoked when drag/fling/smooth scrolling occurs. The callback
 *   receives the delta in pixels. Callers should update their state in this lambda and return the
 *   amount of delta consumed
 */
@Suppress("ktlint:standard:function-naming", "FunctionName")
fun SaltScrollableState(
    consumeScrollDelta: (Float) -> Float
): ScrollableState = DefaultSaltScrollableState(consumeScrollDelta)

private class DefaultSaltScrollableState(
    val onDelta: (Float) -> Float
) : ScrollableState {
    private val scrollScope: ScrollScope =
        object : ScrollScope {
            override fun scrollBy(pixels: Float): Float {
                if (pixels.isNaN()) return 0f
                val delta = onDelta(pixels)
                isLastScrollForwardState.value = delta > 0
                isLastScrollBackwardState.value = delta < 0
                return delta
            }
        }

    private val scrollMutex = MutatorMutex()

    private val isScrollingState = mutableStateOf(false)
    private val isLastScrollForwardState = mutableStateOf(false)
    private val isLastScrollBackwardState = mutableStateOf(false)

    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit,
    ): Unit = coroutineScope {
        scrollMutex.mutateWith(scrollScope, scrollPriority) {
            // TODO Pre-fix https://issuetracker.google.com/issues/456779479
            if (scrollPriority == MutatePriority.UserInput) {
                isScrollingState.value = true
                try {
                    block()
                } finally {
                    isScrollingState.value = false
                }
            } else {
                block()
            }
        }
    }

    override fun dispatchRawDelta(delta: Float): Float = onDelta(delta)

    override val isScrollInProgress: Boolean
        get() = isScrollingState.value

    override val lastScrolledForward: Boolean
        get() = isLastScrollForwardState.value

    override val lastScrolledBackward: Boolean
        get() = isLastScrollBackwardState.value
}
