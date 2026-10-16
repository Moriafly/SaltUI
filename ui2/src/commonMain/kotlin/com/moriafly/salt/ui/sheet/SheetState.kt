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

package com.moriafly.salt.ui.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.moriafly.salt.ui.UnstableSaltUiApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Observes the presentation lifecycle of a [Sheet]. Visibility is still controlled by `visible`.
 *
 * Use [rememberSheetState] in composition, or construct this state in a UI state holder such as a
 * navigation scene. Pass it to exactly one [Sheet] call at a time, outside the Sheet's content.
 * Access this state on the UI thread. Presentation state is transient and is not saved or restored.
 */
@UnstableSaltUiApi
@Stable
class SheetState {
    private var owner: Any? = null
    private var presentation: CompletableDeferred<Unit>? = null

    /**
     * Whether no presentation is active. Initially true, including before the first presentation.
     *
     * Remains false throughout entry, display and exit. Becomes true after the exit animation
     * finishes and removal of the host is applied, or when the Sheet call leaves composition.
     * Updates follow applied composition, rather than the caller's pending `visible` value.
     */
    var isHidden: Boolean by mutableStateOf(true)
        private set

    /**
     * Waits for the current presentation to finish exiting and its host to leave composition.
     * Returns immediately if [isHidden] is true; does not wait for a future presentation.
     * Does not wait for separate content-state cleanup effects or application work to finish.
     *
     * Set `visible` to false to request hiding and keep the Sheet call composed while waiting.
     * Reopening before exit completes continues the same presentation, so this call keeps waiting.
     * Multiple callers may await independently. Cancelling a waiter does not affect the Sheet.
     *
     * Throws [CancellationException] if this coroutine is cancelled, or if the state is detached
     * from its Sheet before a normal exit completes. Detachment does not count as an animated exit.
     */
    suspend fun awaitHidden() {
        currentCoroutineContext().ensureActive()
        presentation?.await()
    }

    internal fun attach(owner: Any) {
        check(this.owner == null) { "A SheetState can only be attached to one Sheet at a time." }
        this.owner = owner
    }

    internal fun updatePresentation(owner: Any, present: Boolean) {
        check(this.owner === owner)
        if (present) {
            if (presentation == null) {
                presentation = CompletableDeferred()
                isHidden = false
            }
        } else {
            val completion = presentation
            presentation = null
            isHidden = true
            completion?.complete(Unit)
        }
    }

    internal fun detach(owner: Any) {
        if (this.owner !== owner) return
        this.owner = null
        val completion = presentation
        presentation = null
        isHidden = true
        completion?.cancel(
            CancellationException("Sheet left composition before its exit completed.")
        )
    }
}

/**
 * Remembers a [SheetState] for observing a Sheet's presentation lifecycle.
 */
@UnstableSaltUiApi
@Composable
fun rememberSheetState(): SheetState = remember { SheetState() }
