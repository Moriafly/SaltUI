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

package com.moriafly.salt.ui.screen

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.screen.TopScreenBarState.Companion.Saver
import kotlin.math.abs
import kotlin.math.roundToInt

@UnstableSaltUiApi
@Composable
internal fun TopScreenBar(
    scrollBehavior: TopScreenBarScrollBehavior,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clipToBounds()
            .adjustHeightOffsetLimit(scrollBehavior)
            .offset {
                IntOffset(
                    x = 0,
                    y = scrollBehavior.state.barOffset.roundToInt()
                )
            }
    ) {
        content()
    }
}

@UnstableSaltUiApi
private fun Modifier.adjustHeightOffsetLimit(scrollBehavior: TopScreenBarScrollBehavior) =
    onSizeChanged { size ->
        // TODO There is an issue with event distribution in nested scrolling on the desktop, which
        //      prevents pre-consumption of the scroll. Therefore, the barOffsetLimit is not
        //      restricted here
        if (!OS.isDesktop()) {
            val offset = size.height.toFloat()
            scrollBehavior.state.barOffsetLimit = -offset
        }
    }

@UnstableSaltUiApi
@Composable
internal fun CollapsedTopBar(
    scrollBehavior: TopScreenBarScrollBehavior,
    modifier: Modifier = Modifier,
    collapsedHeight: Dp = 56.dp,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val collapsedHeightPx = with(density) { collapsedHeight.roundToPx() }

    val isCollapsed by remember(collapsedHeightPx) {
        derivedStateOf {
            scrollBehavior.state.barOffset <= -collapsedHeightPx
        }
    }
    val alpha by animateFloatAsState(
        targetValue = if (isCollapsed) 1f else 0f,
        animationSpec = tween(durationMillis = 300)
    )
    val translationY by animateDpAsState(
        targetValue = if (isCollapsed) 0.dp else 8.dp,
        animationSpec = tween(durationMillis = 300)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(collapsedHeight)
            .clipToBounds()
            .graphicsLayer {
                this.alpha = alpha
                this.translationY = translationY.toPx()
            }
    ) {
        content()
    }
}

/**
 * Creates a [TopScreenBarState] that is remembered across compositions.
 *
 * @param initialBarOffset the initial value for [TopScreenBarState.barOffset].
 */
@UnstableSaltUiApi
@Composable
internal fun rememberTopScreenBarState(
    initialBarOffsetLimit: Float = -Float.MAX_VALUE,
    initialBarOffset: Float = 0f,
    initialContentOffset: Float = 0f
): TopScreenBarState =
    rememberSaveable(saver = Saver) {
        TopScreenBarState(
            initialBarOffsetLimit = initialBarOffsetLimit,
            initialBarOffset = initialBarOffset,
            initialContentOffset = initialContentOffset
        )
    }

/**
 * A state object that can be hoisted to control and observe the top app bar state. The state is
 * read and updated by a [TopScreenBarScrollBehavior] implementation.
 *
 * In most cases, this state will be created via [rememberTopScreenBarState].
 *
 * @param initialBarOffset the initial value for [TopScreenBarState.barOffset]
 */
@UnstableSaltUiApi
@Stable
internal class TopScreenBarState(
    initialBarOffsetLimit: Float,
    initialBarOffset: Float,
    initialContentOffset: Float
) {
    /**
     * The top app bar's height offset limit in pixels, which represents the limit that a top app
     * bar is allowed to collapse to.
     *
     * Use this limit to coerce the [barOffset] value when it's updated.
     */
    var barOffsetLimit = initialBarOffsetLimit

    /**
     * The current offset for the app bar.
     */
    var barOffset: Float
        get() = _barOffset.floatValue
        set(newOffset) {
            _barOffset.floatValue =
                newOffset.coerceIn(minimumValue = barOffsetLimit, maximumValue = 0f)
        }

    /**
     * The total offset of the content scrolled under the top app bar.
     *
     * This value is updated by a [TopScreenBarScrollBehavior] whenever a nested scroll connection
     * consumes scroll events. A common implementation would update the value to be the sum of all
     * [NestedScrollConnection.onPostScroll] `consumed.y` values.
     */
    var contentOffset by mutableFloatStateOf(initialContentOffset)

    /**
     * A value that represents the collapsed height percentage of the app bar.
     *
     * A `0.0` represents a fully expanded bar, and `1.0` represents a fully collapsed bar (computed
     * as [barOffset] / [barOffsetLimit]).
     */
    val collapsedFraction: Float
        get() =
            if (barOffsetLimit != 0f) {
                barOffset / barOffsetLimit
            } else {
                0f
            }

    companion object {
        /** The default [Saver] implementation for [TopScreenBarState]. */
        val Saver: Saver<TopScreenBarState, *> =
            listSaver(
                save = { listOf(it.barOffsetLimit, it.barOffset, it.contentOffset) },
                restore = {
                    TopScreenBarState(
                        initialBarOffsetLimit = it[0],
                        initialBarOffset = it[1],
                        initialContentOffset = it[2]
                    )
                }
            )
    }

    private var _barOffset = mutableFloatStateOf(initialBarOffset)
}

/**
 * A TopAppBarScrollBehavior defines how an app bar should behave when the content under it is
 * scrolled.
 *
 * @see [rememberDefaultTopScreenBarScrollBehavior]
 */
@UnstableSaltUiApi
@Stable
internal interface TopScreenBarScrollBehavior {
    /**
     * A [TopScreenBarState] that is attached to this behavior and is read and updated when scrolling
     * happens.
     */
    val state: TopScreenBarState

    /**
     * Indicates whether the top app bar is pinned.
     *
     * A pinned app bar will stay fixed in place when content is scrolled and will not react to any
     * drag gestures.
     */
    val isPinned: Boolean

    /**
     * An optional [AnimationSpec] that defines how the top app bar snaps to either fully collapsed
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
internal fun rememberDefaultTopScreenBarScrollBehavior(
    state: TopScreenBarState = rememberTopScreenBarState(),
    canScroll: () -> Boolean = { true },
    // TODO Load the motionScheme tokens from the component tokens file
    snapAnimationSpec: AnimationSpec<Float>? = tween(easing = LinearEasing),
    flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay()
): TopScreenBarScrollBehavior =
    remember(
        state,
        canScroll
    ) {
        DefaultTopScreenBarScrollBehavior(
            state = state,
            snapAnimationSpec = snapAnimationSpec,
            flingAnimationSpec = flingAnimationSpec,
            canScroll = canScroll
        )
    }

/**
 * A top app bar that is set up with this [TopScreenBarScrollBehavior] will immediately collapse when
 * the nested content is pulled up, and will expand back the collapsed area when the content is
 * pulled all the way down.
 *
 * @param state a [TopScreenBarState]
 * @param canScroll a callback used to determine whether scroll events are to be handled by this
 * [DefaultTopScreenBarScrollBehavior]
 */
@UnstableSaltUiApi
internal expect class DefaultTopScreenBarScrollBehavior(
    state: TopScreenBarState,
    snapAnimationSpec: AnimationSpec<Float>?,
    flingAnimationSpec: DecayAnimationSpec<Float>?,
    canScroll: () -> Boolean = { true }
) : TopScreenBarScrollBehavior {
    override val state: TopScreenBarState
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
internal suspend fun settleAppBar(
    state: TopScreenBarState,
    velocity: Float,
    flingAnimationSpec: DecayAnimationSpec<Float>?,
    snapAnimationSpec: AnimationSpec<Float>?
): Velocity {
    // Check if the app bar is completely collapsed/expanded. If so, no need to settle the app bar,
    // and just return Zero Velocity
    // Note that we don't check for 0f due to float precision with the collapsedFraction
    // calculation
    if (state.collapsedFraction < 0.01f || state.collapsedFraction == 1f) {
        return Velocity.Zero
    }
    var remainingVelocity = velocity
    // In case there is an initial velocity that was left after a previous user fling, animate to
    // continue the motion to expand or collapse the app bar
    if (flingAnimationSpec != null && abs(velocity) > 1f) {
        var lastValue = 0f
        AnimationState(initialValue = 0f, initialVelocity = velocity).animateDecay(
            flingAnimationSpec
        ) {
            val delta = value - lastValue
            val initialHeightOffset = state.barOffset
            state.barOffset = initialHeightOffset + delta
            val consumed = abs(initialHeightOffset - state.barOffset)
            @Suppress("AssignedValueIsNeverRead")
            lastValue = value
            remainingVelocity = this.velocity
            // avoid rounding errors and stop if anything is unconsumed
            if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
        }
    }
    // Snap if animation specs were provided
    if (snapAnimationSpec != null) {
        if (state.barOffset in state.barOffsetLimit..0f) {
            AnimationState(initialValue = state.barOffset).animateTo(
                if (state.collapsedFraction < 0.5f) {
                    0f
                } else {
                    state.barOffsetLimit
                },
                animationSpec = snapAnimationSpec
            ) {
                state.barOffset = value
            }
        }
    }

    return Velocity(0f, remainingVelocity)
}

@UnstableSaltUiApi
internal object TopScreenBarDefaults {
    val CollapsedHeight = 56.dp
}
