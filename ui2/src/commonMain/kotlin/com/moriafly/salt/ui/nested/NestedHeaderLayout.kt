/*
 * Salt UI
 * Copyright (C) 2025 Moriafly
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused")

package com.moriafly.salt.ui.nested

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import com.moriafly.salt.ui.UnstableSaltUiApi
import kotlin.math.roundToInt

/**
 * A layout that implements a collapsing header pattern with nested scrolling support.
 *
 * This layout positions a [header] above a [content] body. As the user scrolls (either on the
 * header or the content), the header collapses upward until it is completely hidden or reaches
 * its minimum size, at which point the content continues to scroll.
 *
 * To ensure seamless scrolling continuity when dragging the header, the [contentScrollState]
 * (e.g., the [androidx.compose.foundation.lazy.LazyListState] of the content) should be provided.
 *
 * @param header The composable content for the collapsible header.
 * @param modifier The modifier to be applied to the layout.
 * @param state The state object to be used to control or observe the header's offset.
 * @param contentScrollState The [ScrollableState] of the child content (e.g., LazyListState).
 * Providing this enables the header to drive the content's scrolling
 * when the header is fully collapsed or expanded.
 * @param content The primary scrollable content of the layout.
 */
@UnstableSaltUiApi
@Composable
fun NestedHeaderLayout(
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    state: NestedHeaderState = rememberNestedHeaderState(),
    contentScrollState: ScrollableState? = null,
    content: @Composable () -> Unit
) {
    // Connection to handle nested scroll events from the child (e.g., LazyColumn)
    val connection = remember(state, contentScrollState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // When scrolling up (finger moves down), collapse the header first
                return if (available.y < 0) {
                    state.scroll(available.y)
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // When scrolling down (finger moves up) and content is already at the top,
                // expand the header
                return if (available.y > 0) {
                    state.scroll(available.y)
                } else {
                    Offset.Zero
                }
            }
        }
    }

    // Scrollable state for the parent layout itself
    // This ensures that dragging on the header area also drives the scrolling mechanism
    val parentScrollableState = rememberScrollableState { delta ->
        // Phase 1: Pre-scroll (Header consumes delta if needed)
        val preConsumed = connection.onPreScroll(
            available = Offset(0f, delta),
            source = NestedScrollSource.UserInput
        )

        // Phase 2: Dispatch remaining delta to the child content
        // This acts as a bridge: if the header is fully collapsed, dragging the header
        // should scroll the list below
        val availableForContent = delta - preConsumed.y
        var contentConsumed = 0f

        if (availableForContent != 0f && contentScrollState != null) {
            contentConsumed = contentScrollState.dispatchRawDelta(availableForContent)
        }

        // Phase 3: Post-scroll (Header consumes remaining delta if needed)
        val availableForPost = availableForContent - contentConsumed
        val postConsumed = connection.onPostScroll(
            consumed = Offset.Zero,
            available = Offset(0f, availableForPost),
            source = NestedScrollSource.UserInput
        )

        // Return total consumed to keep the fling animation active
        preConsumed.y + contentConsumed + postConsumed.y
    }

    SubcomposeLayout(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(connection)
            .scrollable(
                state = parentScrollableState,
                orientation = Orientation.Vertical,
                flingBehavior = ScrollableDefaults.flingBehavior()
            )
    ) { constraints ->
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        // Measure the header content
        val headerPlaceables = subcompose(NestedHeaderSlots.Header, header)
            .map { it.measure(looseConstraints) }
        val headerHeight = headerPlaceables.maxOfOrNull { it.height } ?: 0

        // Update the scroll bounds in the state based on the measured header height
        state.updateBounds(minOffset = -headerHeight.toFloat(), maxOffset = 0f)

        // Measure the body content
        // The content is forced to fill the maximum height of the parent to ensure
        // there is no empty space at the bottom when the header scrolls away
        val contentConstraints = constraints.copy(
            minHeight = constraints.maxHeight,
            maxHeight = constraints.maxHeight
        )
        val contentPlaceables = subcompose(NestedHeaderSlots.Content, content)
            .map { it.measure(contentConstraints) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            val currentOffset = state.offset.roundToInt()

            // Place the header
            headerPlaceables.forEach {
                it.place(0, currentOffset)
            }

            // Place the content immediately below the header's current position
            contentPlaceables.forEach {
                it.place(0, headerHeight + currentOffset)
            }
        }
    }
}

/**
 * Slots for [NestedHeaderLayout] subcomposition.
 */
private enum class NestedHeaderSlots {
    Header,
    Content
}

/**
 * Creates and remembers a [NestedHeaderState].
 */
@UnstableSaltUiApi
@Composable
fun rememberNestedHeaderState(): NestedHeaderState = remember { NestedHeaderState() }

/**
 * State object for [NestedHeaderLayout].
 *
 * Handles the calculation of the header's offset and bounds, and provides
 * methods to programmatically scroll or fling the header.
 */
@UnstableSaltUiApi
@Stable
class NestedHeaderState {
    /**
     * The minimum offset the header can scroll to (usually negative header height).
     */
    var minOffset by mutableFloatStateOf(0f)
        private set

    /**
     * The maximum offset the header can scroll to (usually 0).
     */
    var maxOffset by mutableFloatStateOf(0f)
        private set

    /**
     * The current vertical offset of the header in pixels.
     * 0 indicates fully expanded, [minOffset] indicates fully collapsed.
     */
    var offset by mutableFloatStateOf(0f)
        private set

    /**
     * A derived value representing the distance from the bottom of the collapsed state.
     * Useful for calculating alpha or translation for floating elements.
     */
    val floatBottomOffset by derivedStateOf {
        minOffset - offset
    }

    /**
     * Updates the scroll bounds.
     *
     * @param minOffset The new minimum offset.
     * @param maxOffset The new maximum offset.
     */
    internal fun updateBounds(minOffset: Float, maxOffset: Float) {
        this.minOffset = minOffset
        this.maxOffset = maxOffset
        // Coerce the current offset to be within the new bounds
        this.offset = this.offset.coerceIn(minOffset, maxOffset)
    }

    /**
     * Consumes scroll delta to update the header offset.
     *
     * @param delta The scroll delta in pixels.
     * @return The offset consumed by this scroll.
     */
    internal fun scroll(delta: Float): Offset {
        val oldOffset = offset
        val newOffset = (offset + delta).coerceIn(minOffset, maxOffset)
        offset = newOffset
        return Offset(0f, newOffset - oldOffset)
    }

    /**
     * Immediately collapses the header to its minimum offset.
     */
    fun collapse() {
        offset = minOffset
    }

    /**
     * Immediately expands the header to its maximum offset.
     */
    fun expand() {
        offset = maxOffset
    }
}
