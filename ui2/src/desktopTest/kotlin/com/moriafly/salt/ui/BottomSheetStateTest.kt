/*
 * Salt UI
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

package com.moriafly.salt.ui

import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(UnstableSaltUiApi::class)
class BottomSheetStateTest {
    @Test
    fun userInputDragInterruptsExistingMutation() = runBlocking {
        val state = BottomSheetState(
            initialValue = BottomSheetValue.Collapsed,
            density = Density(1f)
        )
        val firstDragStarted = CompletableDeferred<Unit>()
        val firstDrag = launch(start = CoroutineStart.UNDISPATCHED) {
            state.dragAsUserInput {
                firstDragStarted.complete(Unit)
                awaitCancellation()
            }
        }

        firstDragStarted.await()
        state.dragAsUserInput {}
        firstDrag.join()

        assertTrue(firstDrag.isCancelled)
    }

    @Test
    fun positiveReleaseVelocityTargetsCollapsedAnchorAfterExpansionInterrupt() {
        val state = bottomSheetState(BottomSheetValue.Collapsed)

        val target = state.anchoredDraggableState.computeTarget(
            offset = 100f,
            currentValue = BottomSheetValue.Collapsed,
            velocity = 1_000f
        )

        assertEquals(BottomSheetValue.Collapsed, target)
    }

    @Test
    fun negativeReleaseVelocityTargetsExpandedAnchorAfterCollapseInterrupt() {
        val state = bottomSheetState(BottomSheetValue.Expanded)

        val target = state.anchoredDraggableState.computeTarget(
            offset = 900f,
            currentValue = BottomSheetValue.Expanded,
            velocity = -1_000f
        )

        assertEquals(BottomSheetValue.Expanded, target)
    }

    private fun bottomSheetState(initialValue: BottomSheetValue): BottomSheetState =
        BottomSheetState(
            initialValue = initialValue,
            density = Density(1f)
        ).also { state ->
            state.anchoredDraggableState.updateAnchors(
                DraggableAnchors {
                    BottomSheetValue.Expanded at 0f
                    BottomSheetValue.Collapsed at 1_000f
                },
                newTarget = initialValue
            )
        }
}
