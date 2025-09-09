/*
 * Copyright 2020 The Android Open Source Project
 * Copyright (C) 2025 Moriafly
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

@file:Suppress("unused")

package com.moriafly.salt.ui

import androidx.annotation.FloatRange
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import com.moriafly.salt.ui.BottomSheetState.Companion.Saver
import com.moriafly.salt.ui.BottomSheetValue.Collapsed
import com.moriafly.salt.ui.BottomSheetValue.Expanded
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.jvm.JvmName
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Possible values of [BottomSheetState].
 */
@UnstableSaltUiApi
enum class BottomSheetValue {
    /**
     * The bottom sheet is visible, but only showing its peek height.
     */
    Collapsed,

    /**
     * The bottom sheet is visible at its maximum height.
     */
    Expanded
}

/**
 * State of the persistent bottom sheet in [BottomSheetScaffold].
 *
 * @param initialValue the initial value of the state.
 * @param density the density that this state can use to convert values to and from dp.
 * @param animationSpec the default animation that will be used to animate to a new state.
 * @param confirmValueChange optional callback invoked to confirm or veto a pending state change.
 */
@UnstableSaltUiApi
@Stable
class BottomSheetState(
    initialValue: BottomSheetValue,
    density: Density,
    animationSpec: AnimationSpec<Float> = BottomSheetDefaults.AnimationSpec,
    confirmValueChange: (BottomSheetValue) -> Boolean = { true }
) {
    val anchoredDraggableState =
        AnchoredDraggableState(
            initialValue = initialValue,
            animationSpec = animationSpec,
            confirmValueChange = confirmValueChange,
            positionalThreshold = {
                with(density) { BottomSheetScaffoldPositionalThreshold.toPx() }
            },
            velocityThreshold = { with(density) { BottomSheetScaffoldVelocityThreshold.toPx() } },
        )

    /**
     * The current value of the [BottomSheetState].
     */
    val currentValue: BottomSheetValue
        get() = anchoredDraggableState.currentValue

    /**
     * The target value the state will settle at once the current interaction ends, or the
     * [currentValue] if there is no interaction in progress.
     */
    val targetValue: BottomSheetValue
        get() = anchoredDraggableState.targetValue

    /**
     * Whether the bottom sheet is expanded.
     *
     * @see isCurrentlyExpanded
     */
    @Deprecated(
        message = "Use isCurrentlyExpanded instead",
        replaceWith = ReplaceWith("isCurrentlyExpanded")
    )
    val isExpanded: Boolean
        get() = anchoredDraggableState.currentValue == Expanded

    /**
     * Whether the bottom sheet is collapsed.
     *
     * @see isCurrentlyCollapsed
     */
    @Deprecated(
        message = "Use isCurrentlyCollapsed instead",
        replaceWith = ReplaceWith("isCurrentlyCollapsed")
    )
    val isCollapsed: Boolean
        get() = anchoredDraggableState.currentValue == Collapsed

    /**
     * Whether the bottom sheet's *current visual state* is expanded.
     * Note: This may be true even during a collapse animation.
     */
    val isCurrentlyExpanded: Boolean
        get() = anchoredDraggableState.currentValue == Expanded

    /**
     * Whether the bottom sheet's *current visual state* is collapsed.
     * Note: This may be true even during an expand animation.
     */
    val isCurrentlyCollapsed: Boolean
        get() = anchoredDraggableState.currentValue == Collapsed

    /**
     * Whether the bottom sheet is fully expanded.
     *
     * - FullyExpanded: currentValue = Expanded, targetValue = Expanded
     * - FullyExpanded -> FullyCollapsed: currentValue = Expanded, targetValue = Collapsed
     */
    val isFullyExpanded: Boolean
        get() = anchoredDraggableState.currentValue == Expanded &&
            targetValue == Expanded

    /**
     * Whether the bottom sheet is fully collapsed.
     *
     * - FullyCollapsed: currentValue = Collapsed, targetValue = Collapsed
     * - FullyCollapsed -> FullyExpanded: currentValue = Collapsed, targetValue = Expanded
     */
    val isFullyCollapsed: Boolean
        get() = anchoredDraggableState.currentValue == Collapsed &&
            targetValue == Collapsed

    /**
     * The fraction of the offset from [Collapsed] to [Expanded], between [0f..1f].
     */
    @FloatRange(from = 0.0, to = 1.0)
    fun progress(): Float {
        val fromOffset = anchoredDraggableState.anchors.positionOf(Collapsed)
        val toOffset = anchoredDraggableState.anchors.positionOf(Expanded)

        // If either offset is NaN, return the initial collapsed/expanded state
        if (fromOffset.isNaN() || toOffset.isNaN()) {
            return when (anchoredDraggableState.initialValue) {
                Collapsed -> 0f
                Expanded -> 1f
            }
        }

        val currentOffset =
            anchoredDraggableState.offset.coerceIn(
                // fromOffset might be > toOffset
                min(fromOffset, toOffset),
                max(fromOffset, toOffset)
            )
        val fraction = (currentOffset - fromOffset) / (toOffset - fromOffset)
        return if (fraction.isNaN()) 1f else abs(fraction)
    }

    /**
     * Expand the bottom sheet with an animation and suspend until the animation finishes or is
     * cancelled. Note: If the peek height is equal to the sheet height, this method will animate to
     * the [Collapsed] state.
     *
     * This method will throw [CancellationException] if the animation is interrupted.
     */
    suspend fun expand() {
        val target =
            if (anchoredDraggableState.anchors.hasPositionFor(Expanded)) {
                Expanded
            } else {
                Collapsed
            }
        anchoredDraggableState.animateTo(target)
    }

    /**
     * Collapse the bottom sheet with animation and suspend until it if fully collapsed or animation
     * has been cancelled. This method will throw [CancellationException] if the animation is
     * interrupted.
     */
    suspend fun collapse() = anchoredDraggableState.animateTo(Collapsed)

    /**
     * Require the current offset.
     *
     * @throws IllegalStateException if the offset has not been initialized yet
     */
    fun requireOffset() = anchoredDraggableState.requireOffset()

    internal suspend fun animateTo(
        target: BottomSheetValue,
        velocity: Float = anchoredDraggableState.lastVelocity
    ) = anchoredDraggableState.animateTo(target, velocity)

    internal suspend fun snapTo(target: BottomSheetValue) = anchoredDraggableState.snapTo(target)

    companion object {
        /** The default [Saver] implementation for [BottomSheetState]. */
        fun Saver(
            animationSpec: AnimationSpec<Float>,
            confirmStateChange: (BottomSheetValue) -> Boolean,
            density: Density
        ): Saver<BottomSheetState, *> =
            Saver(
                save = { it.anchoredDraggableState.currentValue },
                restore = {
                    BottomSheetState(
                        initialValue = it,
                        density = density,
                        animationSpec = animationSpec,
                        confirmValueChange = confirmStateChange
                    )
                }
            )
    }
}

/**
 * Create a [BottomSheetState] and [remember] it.
 *
 * @param initialValue the initial value of the state.
 * @param animationSpec the default animation that will be used to animate to a new state.
 * @param confirmStateChange optional callback invoked to confirm or veto a pending state change.
 */
@UnstableSaltUiApi
@Composable
fun rememberBottomSheetState(
    initialValue: BottomSheetValue = Collapsed,
    animationSpec: AnimationSpec<Float> = BottomSheetDefaults.AnimationSpec,
    confirmStateChange: (BottomSheetValue) -> Boolean = { true }
): BottomSheetState {
    val density = LocalDensity.current
    return rememberSaveable(
        animationSpec,
        saver =
            Saver(
                animationSpec = animationSpec,
                confirmStateChange = confirmStateChange,
                density = density
            )
    ) {
        BottomSheetState(
            initialValue = initialValue,
            animationSpec = animationSpec,
            confirmValueChange = confirmStateChange,
            density = density
        )
    }
}

/**
 * Standard bottom sheets co-exist with the screen’s main UI region and allow for simultaneously
 * viewing and interacting with both regions. They are commonly used to keep a feature or secondary
 * content visible on screen when content in main UI region is frequently scrolled or panned.
 *
 * @param sheetContent the content of the bottom sheet.
 * @param modifier an optional [Modifier] for the root of the scaffold.
 * @param sheetGesturesEnabled whether the bottom sheet can be interacted with by gestures.
 * @param sheetPeekHeight the height of the bottom sheet when it is collapsed. If the peek height
 * equals the sheet's full height, the sheet will only have a collapsed state.
 * @param nestedScrollConnection the [NestedScrollConnection] to be used by the [sheetContent].
 * @param content the main content of the screen. You should use the provided [PaddingValues] to
 * properly offset the content, so that it is not obstructed by the bottom sheet when collapsed.
 */
@UnstableSaltUiApi
@Composable
fun BottomSheetScaffold(
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    state: BottomSheetState = rememberBottomSheetState(),
    sheetGesturesEnabled: Boolean = true,
    sheetPeekHeight: Dp = BottomSheetDefaults.SheetPeekHeight,
    nestedScrollConnection: NestedScrollConnection =
        remember(state.anchoredDraggableState) {
            ConsumeSwipeWithinBottomSheetBoundsNestedScrollConnection(
                state = state.anchoredDraggableState,
                orientation = Orientation.Vertical
            )
        },
    content: @Composable (PaddingValues) -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
    ) {
        BottomSheetScaffoldLayout(
            body = {
                content(PaddingValues(bottom = sheetPeekHeight))
            },
            bottomSheet = {
                BottomSheet(
                    state = state,
                    modifier = Modifier
                        .thenIf(sheetGesturesEnabled) {
                            nestedScroll(nestedScrollConnection)
                        }
                        .fillMaxWidth()
                        .requiredHeightIn(min = sheetPeekHeight),
                    sheetGesturesEnabled = sheetGesturesEnabled,
                    sheetPeekHeight = sheetPeekHeight,
                    content = sheetContent
                )
            }
        )
    }
}

@UnstableSaltUiApi
@Composable
private fun BottomSheetScaffoldLayout(
    body: @Composable () -> Unit,
    bottomSheet: @Composable () -> Unit
) {
    Layout(
        contents =
            listOf<@Composable () -> Unit>(
                body,
                bottomSheet
            )
    ) { (bodyMeasurables, sheetMeasurables), constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        val sheetPlaceables = sheetMeasurables.fastMap { it.measure(looseConstraints) }

        val bodyConstraints = looseConstraints.copy(maxHeight = layoutHeight)
        val bodyPlaceables = bodyMeasurables.fastMap { it.measure(bodyConstraints) }

        layout(layoutWidth, layoutHeight) {
            // Placement order is important for elevation
            bodyPlaceables.fastForEach { it.placeRelative(0, 0) }
            sheetPlaceables.fastForEach { it.placeRelative(0, 0) }
        }
    }
}

@UnstableSaltUiApi
@Composable
private fun BottomSheet(
    state: BottomSheetState,
    sheetGesturesEnabled: Boolean,
    sheetPeekHeight: Dp,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scope = rememberCoroutineScope()
    val peekHeightPx = with(LocalDensity.current) { sheetPeekHeight.toPx() }
    Surface(
        modifier
            .draggableAnchors(state.anchoredDraggableState, Orientation.Vertical) {
                sheetSize,
                constraints
                ->
                val layoutHeight = constraints.maxHeight
                val sheetHeight = sheetSize.height.toFloat()
                val newAnchors = DraggableAnchors {
                    Collapsed at layoutHeight - peekHeightPx
                    if (sheetHeight > 0f && sheetHeight != peekHeightPx) {
                        Expanded at layoutHeight - sheetHeight
                    }
                }
                val newTarget =
                    when (state.anchoredDraggableState.targetValue) {
                        Collapsed -> Collapsed
                        Expanded -> if (newAnchors.hasPositionFor(Expanded)) Expanded else Collapsed
                    }
                return@draggableAnchors newAnchors to newTarget
            }
            .anchoredDraggable(
                state = state.anchoredDraggableState,
                orientation = Orientation.Vertical,
                enabled = sheetGesturesEnabled
            )
            .semantics {
                // If we don't have anchors yet, or have only one anchor we don't want any
                // accessibility actions
                if (state.anchoredDraggableState.anchors.size > 1) {
                    if (state.isCurrentlyCollapsed) {
                        expand {
                            if (state.anchoredDraggableState.confirmValueChange(Expanded)) {
                                scope.launch { state.expand() }
                            }
                            true
                        }
                    } else {
                        collapse {
                            if (state.anchoredDraggableState.confirmValueChange(Collapsed)) {
                                scope.launch { state.collapse() }
                            }
                            true
                        }
                    }
                }
            }
    ) {
        Column {
            content()
        }
    }
}

@Suppress("FunctionName")
@UnstableSaltUiApi
private fun ConsumeSwipeWithinBottomSheetBoundsNestedScrollConnection(
    state: AnchoredDraggableState<*>,
    orientation: Orientation,
): NestedScrollConnection =
    object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val delta = available.toFloat()
            return if (delta < 0 && source == NestedScrollSource.UserInput) {
                state.dispatchRawDelta(delta).toOffset()
            } else {
                Offset.Zero
            }
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset = if (source == NestedScrollSource.UserInput) {
            state.dispatchRawDelta(available.toFloat()).toOffset()
        } else {
            Offset.Zero
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            val toFling = available.toFloat()
            val currentOffset = state.requireOffset()
            return if (toFling < 0 && currentOffset > state.anchors.minPosition()) {
                state.settle(velocity = toFling)
                // since we go to the anchor with tween settling, consume all for the best UX
                available
            } else {
                Velocity.Zero
            }
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            state.settle(velocity = available.toFloat())
            return available
        }

        private fun Float.toOffset(): Offset =
            Offset(
                x = if (orientation == Orientation.Horizontal) this else 0f,
                y = if (orientation == Orientation.Vertical) this else 0f,
            )

        @JvmName("velocityToFloat")
        private fun Velocity.toFloat() = if (orientation == Orientation.Horizontal) x else y

        @JvmName("offsetToFloat")
        private fun Offset.toFloat(): Float = if (orientation == Orientation.Horizontal) x else y
    }

/**
 * Contains useful defaults for [BottomSheetScaffold].
 */
@UnstableSaltUiApi
object BottomSheetDefaults {
    /**
     * The default peek height used by [BottomSheetScaffold].
     */
    val SheetPeekHeight = 56.dp

    /**
     * The default animation spec used by [BottomSheetState].
     */
    val AnimationSpec: AnimationSpec<Float> =
        tween(durationMillis = 300, easing = FastOutSlowInEasing)
}

private val BottomSheetScaffoldPositionalThreshold = 56.dp
private val BottomSheetScaffoldVelocityThreshold = 125.dp
