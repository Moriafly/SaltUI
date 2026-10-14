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

package com.moriafly.salt.ui.sheet.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import kotlinx.coroutines.flow.first

/**
 * Main-thread registry: only the owning window's sheets coordinate their scrims.
 */
private val coordinators = mutableMapOf<WindowInfo, SheetHostCoordinator>()

internal class SheetHostCoordinator {
    private val attachedWindows = mutableStateListOf<SheetLease>()
    private val presentations = mutableStateListOf<SheetLease>()
    val requests = mutableStateMapOf<SheetLease, State<Boolean>>()
    var appliedDimHolder: SheetLease? by mutableStateOf(null)
        private set

    val hasVisibleSheet: Boolean get() = requests.values.any { it.value }
    val activeLease: SheetLease? get() = requests.entries.firstOrNull { it.value.value }?.key

    val scrimAlpha: Float
        get() {
            val progress = presentations.fold(0f) { total, lease ->
                total + lease.state.scrimProgress
            }
            return SheetDefaults.SCRIM_OPACITY * progress.coerceIn(0f, 1f)
        }

    // The background scrim stays below the oldest surface until that surface finishes exiting.
    val holder: SheetLease? get() = presentations.firstOrNull()

    // Keep dimming below every transitioning surface, following native window attachment order.
    val dimHolder: SheetLease? get() = attachedWindows.firstOrNull { !it.dimReleased }

    fun updateWindowAttachment(lease: SheetLease, attached: Boolean) {
        lease.windowAttached = attached
        if (attached) {
            if (lease !in attachedWindows) attachedWindows.add(lease)
        } else {
            attachedWindows.remove(lease)
            if (appliedDimHolder === lease) appliedDimHolder = null
        }
    }

    fun confirmDimApplied(lease: SheetLease) {
        appliedDimHolder = lease
    }

    suspend fun releaseDim(lease: SheetLease) {
        lease.dimReleased = true
        // Keep the outgoing window until its successor has submitted its native dim request.
        snapshotFlow {
            !hasVisibleSheet || dimHolder == null || appliedDimHolder === dimHolder
        }.first { it }
    }

    fun register(lease: SheetLease, request: State<Boolean>) {
        requests[lease] = request
    }

    fun acquire(lease: SheetLease) {
        check(requests.values.count { it.value } <= 1) {
            "Only one Sheet per window may have visible = true"
        }
        if (lease !in presentations) presentations.add(lease)
    }

    suspend fun awaitPresentationReady() {
        snapshotFlow { activeLease?.state?.ready != false }.first { it }
        // Start the transitions after the incoming surface has been measured.
        withFrameNanos { }
    }

    fun release(lease: SheetLease) {
        presentations.remove(lease)
    }

    fun unregister(lease: SheetLease) {
        updateWindowAttachment(lease = lease, attached = false)
        requests.remove(lease)
        release(lease)
    }
}

internal class SheetLease(
    val coordinator: SheetHostCoordinator,
    val state: SheetTransitionState
) {
    // Android uses attachment only to avoid yielding focus to the owner during replacement.
    var windowAttached by mutableStateOf(false)
    var dimReleased by mutableStateOf(false)

    val scrimAlpha: Float
        get() = if (coordinator.holder === this) {
            coordinator.scrimAlpha
        } else {
            0f
        }
}

@Composable
internal fun rememberSheetLease(visible: Boolean, state: SheetTransitionState): SheetLease {
    val owner = LocalWindowInfo.current
    val request = rememberUpdatedState(visible)
    val lease = remember(owner) {
        val coordinator = coordinators.getOrPut(owner) { SheetHostCoordinator() }
        SheetLease(coordinator = coordinator, state = state)
    }
    DisposableEffect(lease) {
        lease.coordinator.register(lease = lease, request = request)
        onDispose {
            lease.coordinator.unregister(lease)
            if (lease.coordinator.requests.isEmpty()) coordinators.remove(owner)
        }
    }
    return lease
}
