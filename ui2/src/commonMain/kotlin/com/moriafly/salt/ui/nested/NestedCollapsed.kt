/*
 * Salt UI
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

package com.moriafly.salt.ui.nested

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.nested.CollapsedState.Companion.Saver
import kotlin.math.abs
import kotlin.math.roundToInt

@UnstableSaltUiApi
@Composable
fun NestedCollapsed(
    collapsed: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    state: CollapsedState = rememberCollapsedState(),
    content: @Composable (PaddingValues) -> Unit
) {
    val scrollBehavior = rememberDefaultCollapsedScrollBehavior(state)
    NestedCollapsedLayout(
        collapsed = collapsed,
        scrollBehavior = scrollBehavior,
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        content = content
    )
}

@UnstableSaltUiApi
@Composable
private fun NestedCollapsedLayout(
    collapsed: @Composable () -> Unit,
    scrollBehavior: CollapsedScrollBehavior,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    // Create the backing value for the content padding
    // These values will be updated during measurement, but before subcomposing the body content
    // Remembering and updating a single PaddingValues avoids needing to recompose when the values
    // change
    val contentPadding = remember {
        object : PaddingValues {
            var paddingHolder by mutableStateOf(PaddingValues(0.dp))

            override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
                paddingHolder.calculateLeftPadding(layoutDirection)

            override fun calculateTopPadding(): Dp = paddingHolder.calculateTopPadding()

            override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
                paddingHolder.calculateRightPadding(layoutDirection)

            override fun calculateBottomPadding(): Dp = paddingHolder.calculateBottomPadding()
        }
    }

    val topBarContent: @Composable () -> Unit = remember(collapsed) {
        {
            Box(
                modifier = Modifier
                    .clipToBounds()
                    .adjustCollapsedOffsetLimit(scrollBehavior)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = scrollBehavior.state.collapsedOffset.roundToInt()
                        )
                    }
            ) {
                collapsed()
            }
        }
    }
    val bodyContent: @Composable () -> Unit =
        remember(content, contentPadding) {
            {
                Box {
                    content(contentPadding)
                }
            }
        }

    SubcomposeLayout(modifier) { constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight

        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        val topBarPlaceable =
            subcompose(NestedCollapsedContent.Collapsed, topBarContent)
                .first()
                .measure(looseConstraints)

        contentPadding.paddingHolder =
            PaddingValues(
                top = (topBarPlaceable.height.toDp() + scrollBehavior.state.collapsedOffset.toDp())
                    .coerceAtLeast(0.dp)
            )

        val contentPlaceable =
            subcompose(NestedCollapsedContent.Content, bodyContent)
                .first()
                .measure(looseConstraints)

        layout(layoutWidth, layoutHeight) {
            // Placing to control drawing order to match default elevation of each placeable
            contentPlaceable.place(0, 0)
            topBarPlaceable.place(0, 0)
        }
    }
}

@UnstableSaltUiApi
private enum class NestedCollapsedContent {
    Collapsed,
    Content
}

@UnstableSaltUiApi
private fun Modifier.adjustCollapsedOffsetLimit(scrollBehavior: CollapsedScrollBehavior) =
    onSizeChanged { size ->
        // TODO There is an issue with event distribution in nested scrolling on the desktop, which
        //      prevents pre-consumption of the scroll. Therefore, the barOffsetLimit is not
        //      restricted here
        if (!OS.isDesktop()) {
            val offset = size.height.toFloat()
            scrollBehavior.state.collapsedOffsetLimit = -offset
        }
    }

/**
 * Creates a [CollapsedState] that is remembered across compositions.
 *
 * @param initialCollapsedOffset the initial value for [CollapsedState.collapsedOffset].
 */
@UnstableSaltUiApi
@Composable
internal fun rememberCollapsedState(
    initialCollapsedOffsetLimit: Float = -Float.MAX_VALUE,
    initialCollapsedOffset: Float = 0f,
    initialContentOffset: Float = 0f
): CollapsedState =
    rememberSaveable(saver = Saver) {
        CollapsedState(
            initialCollapsedOffsetLimit = initialCollapsedOffsetLimit,
            initialCollapsedOffset = initialCollapsedOffset,
            initialContentOffset = initialContentOffset
        )
    }

/**
 * A state object that can be hoisted to control and observe the top app bar state. The state is
 * read and updated by a [CollapsedScrollBehavior] implementation.
 *
 * In most cases, this state will be created via [rememberCollapsedState].
 *
 * @param initialCollapsedOffset the initial value for [CollapsedState.collapsedOffset]
 */
@UnstableSaltUiApi
@Stable
class CollapsedState(
    initialCollapsedOffsetLimit: Float,
    initialCollapsedOffset: Float,
    initialContentOffset: Float
) {
    /**
     * The top app bar's height offset limit in pixels, which represents the limit that a top app
     * bar is allowed to collapsed to.
     *
     * Use this limit to coerce the [collapsedOffset] value when it's updated.
     */
    var collapsedOffsetLimit = initialCollapsedOffsetLimit

    /**
     * The current offset for the app bar.
     */
    var collapsedOffset: Float
        get() = _collapsedOffset.floatValue
        set(newOffset) {
            _collapsedOffset.floatValue =
                newOffset.coerceIn(minimumValue = collapsedOffsetLimit, maximumValue = 0f)
        }

    /**
     * The total offset of the content scrolled under the top app bar.
     *
     * This value is updated by a [CollapsedScrollBehavior] whenever a nested scroll connection
     * consumes scroll events. A common implementation would update the value to be the sum of all
     * [NestedScrollConnection.onPostScroll] `consumed.y` values.
     */
    var contentOffset by mutableFloatStateOf(initialContentOffset)

    /**
     * A value that represents the Collapsed height percentage of the app bar.
     *
     * A `0.0` represents a fully expanded bar, and `1.0` represents a fully Collapsed bar (computed
     * as [collapsedOffset] / [collapsedOffsetLimit]).
     */
    val collapsedFraction: Float
        get() =
            if (collapsedOffsetLimit != 0f) {
                collapsedOffset / collapsedOffsetLimit
            } else {
                0f
            }

    companion object {
        /** The default [Saver] implementation for [CollapsedState]. */
        val Saver: Saver<CollapsedState, *> =
            listSaver(
                save = { listOf(it.collapsedOffsetLimit, it.collapsedOffset, it.contentOffset) },
                restore = {
                    CollapsedState(
                        initialCollapsedOffsetLimit = it[0],
                        initialCollapsedOffset = it[1],
                        initialContentOffset = it[2]
                    )
                }
            )
    }

    private var _collapsedOffset = mutableFloatStateOf(initialCollapsedOffset)
}

/**
 * A TopAppBarScrollBehavior defines how an app bar should behave when the content under it is
 * scrolled.
 *
 * @see [rememberDefaultCollapsedScrollBehavior]
 */
@UnstableSaltUiApi
@Stable
internal interface CollapsedScrollBehavior {
    /**
     * A [CollapsedState] that is attached to this behavior and is read and updated when scrolling
     * happens.
     */
    val state: CollapsedState

    /**
     * Indicates whether the top app bar is pinned.
     *
     * A pinned app bar will stay fixed in place when content is scrolled and will not react to any
     * drag gestures.
     */
    val isPinned: Boolean

    /**
     * An optional [AnimationSpec] that defines how the top app bar snaps to either fully Collapsed
     * or fully extended state when a fling or a drag scrolled it into an intermediate position.
     */
    val snapAnimationSpec: AnimationSpec<Float>?

    /**
     * An optional [DecayAnimationSpec] that defined how to fling the top app bar when the user
     * flings the app bar itself, or the content below it.
     */
    val flingAnimationSpec: DecayAnimationSpec<Float>?

    /**
     * A [NestedScrollConnection] that should be attached to a [Modifier.nestedScroll] in order to
     * keep track of the scroll events.
     */
    val nestedScrollConnection: NestedScrollConnection
}

@UnstableSaltUiApi
@Composable
internal fun rememberDefaultCollapsedScrollBehavior(
    state: CollapsedState = rememberCollapsedState(),
    canScroll: () -> Boolean = { true },
    // TODO Load the motionScheme tokens from the component tokens file
    snapAnimationSpec: AnimationSpec<Float>? = tween(easing = LinearEasing),
    flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay()
): CollapsedScrollBehavior =
    remember(
        state,
        canScroll
    ) {
        DefaultCollapsedScrollBehavior(
            state = state,
            snapAnimationSpec = snapAnimationSpec,
            flingAnimationSpec = flingAnimationSpec,
            canScroll = canScroll
        )
    }

/**
 * A top app bar that is set up with this [CollapsedScrollBehavior] will immediately collapsed when
 * the nested content is pulled up, and will expand back the Collapsed area when the content is
 * pulled all the way down.
 *
 * @param state a [CollapsedState]
 * @param canScroll a callback used to determine whether scroll events are to be handled by this
 * [DefaultCollapsedScrollBehavior]
 */
@UnstableSaltUiApi
internal expect class DefaultCollapsedScrollBehavior(
    state: CollapsedState,
    snapAnimationSpec: AnimationSpec<Float>?,
    flingAnimationSpec: DecayAnimationSpec<Float>?,
    canScroll: () -> Boolean = { true }
) : CollapsedScrollBehavior {
    override val state: CollapsedState
    override val snapAnimationSpec: AnimationSpec<Float>?
    override val flingAnimationSpec: DecayAnimationSpec<Float>?

    @Suppress("unused")
    val canScroll: () -> Boolean

    override val isPinned: Boolean
    override var nestedScrollConnection: NestedScrollConnection
}

/**
 * Settles the app bar by flinging, in case the given velocity is greater than zero, and snapping
 * after the fling settles.
 */
@UnstableSaltUiApi
internal suspend fun settleCollapsed(
    state: CollapsedState,
    velocity: Float,
    flingAnimationSpec: DecayAnimationSpec<Float>?,
    snapAnimationSpec: AnimationSpec<Float>?
): Velocity {
    // Check if the app bar is completely Collapsed/expanded. If so, no need to settle the app bar,
    // and just return Zero Velocity
    // Note that we don't check for 0f due to float precision with the CollapsedFraction
    // calculation
    if (state.collapsedFraction < 0.01f || state.collapsedFraction == 1f) {
        return Velocity.Zero
    }
    var remainingVelocity = velocity
    // In case there is an initial velocity that was left after a previous user fling, animate to
    // continue the motion to expand or collapsed the app bar
    if (flingAnimationSpec != null && abs(velocity) > 1f) {
        var lastValue = 0f
        AnimationState(initialValue = 0f, initialVelocity = velocity).animateDecay(
            flingAnimationSpec
        ) {
            val delta = value - lastValue
            val initialHeightOffset = state.collapsedOffset
            state.collapsedOffset = initialHeightOffset + delta
            val consumed = abs(initialHeightOffset - state.collapsedOffset)
            @Suppress("AssignedValueIsNeverRead")
            lastValue = value
            remainingVelocity = this.velocity
            // avoid rounding errors and stop if anything is unconsumed
            if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
        }
    }
    // Snap if animation specs were provided
    if (snapAnimationSpec != null) {
        if (state.collapsedOffset in state.collapsedOffsetLimit..0f) {
            AnimationState(initialValue = state.collapsedOffset).animateTo(
                if (state.collapsedFraction < 0.5f) {
                    0f
                } else {
                    state.collapsedOffsetLimit
                },
                animationSpec = snapAnimationSpec
            ) {
                state.collapsedOffset = value
            }
        }
    }

    return Velocity(0f, remainingVelocity)
}
