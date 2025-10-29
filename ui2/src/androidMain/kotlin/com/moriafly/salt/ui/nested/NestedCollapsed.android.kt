/**
 * Salt UI
 * Copyright (C) 2025 Moriafly
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

@file:Suppress("ktlint:standard:filename")

package com.moriafly.salt.ui.nested

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import com.moriafly.salt.ui.UnstableSaltUiApi

@UnstableSaltUiApi
internal actual class DefaultCollapsedScrollBehavior actual constructor(
    actual override val state: CollapsedState,
    actual override val snapAnimationSpec: AnimationSpec<Float>?,
    actual override val flingAnimationSpec: DecayAnimationSpec<Float>?,
    actual val canScroll: () -> Boolean
) : CollapsedScrollBehavior {
    actual override val isPinned: Boolean = false
    actual override var nestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Don't intercept if scrolling down.
                if (!canScroll() || available.y > 0f) {
                    return Offset.Zero
                }

                val previousOffset = state.collapsedOffset
                state.collapsedOffset += available.y

                val consumed = state.collapsedOffset - previousOffset

                // Return only the delta that was *actually* consumed by the state
                return Offset(0f, consumed)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (!canScroll()) return Offset.Zero
                state.contentOffset += consumed.y

                if (available.y < 0f || consumed.y < 0f) {
                    // When scrolling up, just update the state's bar offset
                    val oldBarOffset = state.collapsedOffset
                    state.collapsedOffset += consumed.y
                    return Offset(0f, state.collapsedOffset - oldBarOffset)
                }

                if (available.y > 0f) {
                    // Adjust the bar offset in case the consumed delta Y is less than what was
                    // recorded as available delta Y in the pre-scroll
                    val oldBarOffset = state.collapsedOffset
                    state.collapsedOffset += available.y
                    return Offset(0f, state.collapsedOffset - oldBarOffset)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (available.y > 0) {
                    // Reset the total content offset to zero when scrolling all the way down. This
                    // will eliminate some float precision inaccuracies
                    state.contentOffset = 0f
                }
                val superConsumed = super.onPostFling(consumed, available)
                return superConsumed +
                    settleCollapsed(state, available.y, flingAnimationSpec, snapAnimationSpec)
            }
        }
}
