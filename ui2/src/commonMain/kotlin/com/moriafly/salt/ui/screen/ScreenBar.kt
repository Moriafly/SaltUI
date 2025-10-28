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

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.screen.TopScreenBarState.Companion.Saver
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
            .offset {
                IntOffset(
                    x = 0,
                    y = scrollBehavior.state.offset.roundToInt()
                )
            }
    ) {
        content()
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
            scrollBehavior.state.offset <= -collapsedHeightPx
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
 * @param initialOffset the initial value for [TopScreenBarState.offset].
 */
@UnstableSaltUiApi
@Composable
internal fun rememberTopScreenBarState(
    initialOffset: Float = 0f,
): TopScreenBarState =
    rememberSaveable(saver = Saver) {
        TopScreenBarState(initialOffset)
    }

/**
 * A state object that can be hoisted to control and observe the top app bar state. The state is
 * read and updated by a [TopScreenBarScrollBehavior] implementation.
 *
 * In most cases, this state will be created via [rememberTopScreenBarState].
 *
 * @param initialOffset the initial value for [TopScreenBarState.offset]
 */
@UnstableSaltUiApi
@Stable
internal class TopScreenBarState(
    initialOffset: Float
) {
    /**
     * The current offset for the app bar.
     */
    var offset: Float
        get() = _offset.floatValue
        set(newOffset) {
            _offset.floatValue =
                newOffset.coerceIn(minimumValue = -Float.MAX_VALUE, maximumValue = 0f)
        }

    companion object {
        /** The default [Saver] implementation for [TopScreenBarState]. */
        val Saver: Saver<TopScreenBarState, *> =
            listSaver(
                save = { listOf(it.offset) },
                restore = {
                    TopScreenBarState(
                        initialOffset = it[0]
                    )
                }
            )
    }

    private var _offset = mutableFloatStateOf(initialOffset)
}

/**
 * A TopAppBarScrollBehavior defines how an app bar should behave when the content under it is
 * scrolled.
 *
 * @see [TopScreenBarDefaults.exitUntilCollapsedScrollBehavior]
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
     * A [NestedScrollConnection] that should be attached to a [Modifier.nestedScroll] in order to
     * keep track of the scroll events.
     */
    val nestedScrollConnection: NestedScrollConnection
}

@UnstableSaltUiApi
internal object TopScreenBarDefaults {
    val CollapsedHeight = 56.dp

    @Composable
    fun exitUntilCollapsedScrollBehavior(
        state: TopScreenBarState = rememberTopScreenBarState(),
        canScroll: () -> Boolean = { true }
    ): TopScreenBarScrollBehavior =
        remember(
            state,
            canScroll
        ) {
            ExitUntilCollapsedScrollBehavior(
                state = state,
                canScroll = canScroll
            )
        }
}

/**
 * A top app bar that is set up with this [TopScreenBarScrollBehavior] will immediately collapse when
 * the nested content is pulled up, and will expand back the collapsed area when the content is
 * pulled all the way down.
 *
 * @param state a [TopScreenBarState]
 * @param canScroll a callback used to determine whether scroll events are to be handled by this
 *   [ExitUntilCollapsedScrollBehavior]
 */
@UnstableSaltUiApi
private class ExitUntilCollapsedScrollBehavior(
    override val state: TopScreenBarState,
    val canScroll: () -> Boolean = { true }
) : TopScreenBarScrollBehavior {
    override val isPinned: Boolean = false
    override var nestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!canScroll()) return Offset.Zero
                state.offset += available.y
                return Offset.Zero
            }
        }
}
