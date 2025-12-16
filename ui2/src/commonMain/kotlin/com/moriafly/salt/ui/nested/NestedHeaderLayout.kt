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
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.nested.NestedHeaderState.Companion.Saver
import kotlin.math.roundToInt

/**
 * A layout that implements a collapsing header pattern with nested scrolling support.
 *
 * This layout positions a [header] above a [content] body.
 *
 * Crucially, the [content] is always placed at the top-left (0,0) of the layout to ensure
 * that the focus system and accessibility services perceive it as fully visible. To prevent
 * the content from being obscured by the header, a [PaddingValues] is passed to the [content]
 * lambda, which must be applied to the internal list (e.g., via `contentPadding`).
 *
 * @param header The composable content for the collapsible header.
 * @param modifier The modifier to be applied to the layout.
 * @param state The state object to be used to control or observe the header's offset.
 * @param content The primary scrollable content of the layout. Accepts [PaddingValues] to apply top padding.
 */
@UnstableSaltUiApi
@Composable
fun NestedHeaderLayout(
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    state: NestedHeaderState = rememberNestedHeaderState(),
    content: @Composable (PaddingValues) -> Unit
) {
    // Connection to handle nested scroll events from the child (e.g., LazyColumn)
    val connection = remember(state) {
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
    // This ensures that dragging on the header area drives the header's collapse/expand
    val parentScrollableState = rememberScrollableState { delta ->
        // Phase 1: Pre-scroll (Header consumes delta if needed)
        val preConsumed = connection.onPreScroll(
            available = Offset(0f, delta),
            source = NestedScrollSource.UserInput
        )

        // Phase 2: Post-scroll (Header consumes remaining delta if needed)
        // Since we removed the content bridge, any delta not consumed by the header
        // when dragging the header itself effectively disappears here (stops scrolling)
        val availableForPost = delta - preConsumed.y
        val postConsumed = connection.onPostScroll(
            consumed = Offset.Zero,
            available = Offset(0f, availableForPost),
            source = NestedScrollSource.UserInput
        )

        // Return total consumed
        preConsumed.y + postConsumed.y
    }

    // Create a mutable PaddingValues object that allows us to update the top padding
    // during the measurement phase without causing a full recomposition loop
    val contentPadding = remember {
        object : PaddingValues {
            var topPadding by mutableStateOf(0.dp)

            override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp = 0.dp

            override fun calculateTopPadding(): Dp = topPadding

            override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp = 0.dp

            override fun calculateBottomPadding(): Dp = 0.dp
        }
    }

    // Wrap the content lambda to inject our mutable padding
    val contentWithPadding: @Composable () -> Unit = remember(content, contentPadding) {
        { content(contentPadding) }
    }

    SubcomposeLayout(
        modifier = modifier
            .clipToBounds()
            .nestedScroll(connection)
            .scrollable(
                state = parentScrollableState,
                orientation = Orientation.Vertical,
                flingBehavior = ScrollableDefaults.flingBehavior()
            )
    ) { constraints ->
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        // 1. Measure Header
        val headerPlaceables = subcompose(NestedHeaderSlots.Header, header)
            .map { it.measure(looseConstraints) }
        val headerHeight = headerPlaceables.maxOfOrNull { it.height } ?: 0

        // 2. Update State & Padding
        // We update the state bounds based on the measured header height
        state.updateBounds(minOffset = -headerHeight.toFloat(), maxOffset = 0f)

        // KEY FIX: Calculate the visible height of the header and set it as top padding
        // This pushes the *content items* down, but keeps the *content container* at (0,0)
        val currentHeaderOffset = state.offset
        val visibleHeaderHeight = (headerHeight + currentHeaderOffset).coerceAtLeast(0f).toDp()

        // Update the backing value for PaddingValues directly
        contentPadding.topPadding = visibleHeaderHeight

        // 3. Measure Content
        // Content gets the full height constraints since it starts at (0,0)
        val contentConstraints = constraints.copy(
            minHeight = constraints.maxHeight,
            maxHeight = constraints.maxHeight
        )
        val contentPlaceables = subcompose(NestedHeaderSlots.Content, contentWithPadding)
            .map { it.measure(contentConstraints) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            val currentOffsetInt = state.offset.roundToInt()

            // Place Content
            // KEY FIX: Always place content at (0, 0)
            // The contentPadding passed above handles the visual offset of the items
            // This makes the Focus system believe the list is fully "In View"
            contentPlaceables.forEach {
                it.place(0, 0)
            }

            // Place Header (Moves up and down visually)
            headerPlaceables.forEach {
                it.place(0, currentOffsetInt)
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
fun rememberNestedHeaderState(): NestedHeaderState =
    rememberSaveable(saver = Saver) {
        NestedHeaderState()
    }

/**
 * State object for [NestedHeaderLayout].
 *
 * Handles the calculation of the header's offset and bounds.
 */
@UnstableSaltUiApi
@Stable
class NestedHeaderState(
    initialOffset: Float = 0f
) {
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
    var offset by mutableFloatStateOf(initialOffset)
        private set

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

    companion object {
        /**
         * The default [Saver] implementation for [NestedHeaderState].
         */
        val Saver: Saver<NestedHeaderState, Float> = Saver(
            save = { it.offset },
            restore = { NestedHeaderState(initialOffset = it) }
        )
    }
}
