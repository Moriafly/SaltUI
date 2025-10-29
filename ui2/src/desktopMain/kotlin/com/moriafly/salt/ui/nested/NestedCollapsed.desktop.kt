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
                if (!canScroll()) {
                    return Offset.Zero
                }

                state.contentOffset += available.y
                state.collapsedOffset += available.y * 2f

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
