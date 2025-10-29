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

@file:Suppress("ktlint:standard:filename")

package com.moriafly.salt.ui.screen

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import com.moriafly.salt.ui.UnstableSaltUiApi

@UnstableSaltUiApi
internal actual class DefaultTopScreenBarScrollBehavior actual constructor(
    actual override val state: TopScreenBarState,
    actual override val snapAnimationSpec: AnimationSpec<Float>?,
    actual override val flingAnimationSpec: DecayAnimationSpec<Float>?,
    actual val canScroll: () -> Boolean
) : TopScreenBarScrollBehavior {
    actual override val isPinned: Boolean = false
    actual override var nestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Don't intercept if scrolling down.
                if (!canScroll() || available.y > 0f) {
                    return Offset.Zero
                }

                val previousOffset = state.barOffset
                state.barOffset += available.y

                val consumed = state.barOffset - previousOffset

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
                    val oldBarOffset = state.barOffset
                    state.barOffset += consumed.y
                    return Offset(0f, state.barOffset - oldBarOffset)
                }

                if (available.y > 0f) {
                    // Adjust the bar offset in case the consumed delta Y is less than what was
                    // recorded as available delta Y in the pre-scroll
                    val oldBarOffset = state.barOffset
                    state.barOffset += available.y
                    return Offset(0f, state.barOffset - oldBarOffset)
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
                    settleAppBar(state, available.y, flingAnimationSpec, snapAnimationSpec)
            }
        }
}
