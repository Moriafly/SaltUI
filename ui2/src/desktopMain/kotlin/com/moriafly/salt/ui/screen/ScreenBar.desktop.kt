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
                state.contentOffset += available.y
                state.barOffset = state.contentOffset

                return Offset.Zero
            }

            /**
             * TODO A slight mouse wheel scroll on the desktop does not trigger the [onPostFling]
             *      event
             */
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity =
                super.onPostFling(consumed, available)
        }
}
