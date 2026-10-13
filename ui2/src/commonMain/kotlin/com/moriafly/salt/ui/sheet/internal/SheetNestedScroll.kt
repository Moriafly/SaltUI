/*
 * SaltUI
 * Copyright (C) 2026 Moriafly
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

@file:OptIn(com.moriafly.salt.ui.UnstableSaltUiApi::class)

package com.moriafly.salt.ui.sheet.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import com.moriafly.salt.ui.sheet.SheetContentInteraction

internal class SheetDirectInput {
    var active = false
}

internal class SheetNestedScroll(
    private val state: SheetTransitionState,
    private val enabled: () -> Boolean,
    private val interaction: () -> SheetContentInteraction,
    private val dismissible: () -> Boolean,
    private val onDismissRequest: () -> Unit
) : NestedScrollConnection {
    private var moved = false

    private fun consume(delta: Float): Offset {
        if (delta == 0f || !state.ready) return Offset.Zero
        val consumed = state.drag(delta, dismissible())
        moved = moved || consumed != 0f
        return Offset(0f, consumed)
    }

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
        if (enabled() && source == NestedScrollSource.UserInput && available.y < 0f &&
            interaction() == SheetContentInteraction.ResizeFirst
        ) {
            consume(available.y)
        } else {
            Offset.Zero
        }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset =
        if (enabled() &&
            source == NestedScrollSource.UserInput
        ) {
            consume(available.y)
        } else {
            Offset.Zero
        }

    override suspend fun onPreFling(available: Velocity): Velocity = finish(available)

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = finish(
        available
    )

    private suspend fun finish(available: Velocity): Velocity {
        if (!moved) return Velocity.Zero
        moved = false
        state.settle(available.y, dismissible(), onDismissRequest)
        return Velocity(0f, available.y)
    }
}
