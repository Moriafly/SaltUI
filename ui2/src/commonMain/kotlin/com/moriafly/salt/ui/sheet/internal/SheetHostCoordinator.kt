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

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo

/**
 * Main-thread registry: only the owning window's sheets share a presentation lease.
 */
private val coordinators = mutableMapOf<WindowInfo, SheetHostCoordinator>()

internal class SheetHostCoordinator {
    val requests = mutableMapOf<SheetLease, State<Boolean>>()
    var holder: SheetLease? by mutableStateOf(null)
        private set

    suspend fun acquire(lease: SheetLease) {
        check(requests.values.count { it.value } <= 1) {
            "Only one Sheet per window may have visible = true"
        }
        // Transfer the scrim without waiting for the previous surface to finish exiting.
        val inheritedProgress = holder?.scrimAnimation?.value ?: lease.scrimAnimation.value
        lease.scrimAnimation.snapTo(inheritedProgress)
        holder = lease
    }

    fun release(lease: SheetLease) {
        if (holder === lease) holder = null
    }
}

internal class SheetLease(
    val coordinator: SheetHostCoordinator
) {
    val scrimAnimation = Animatable(0f)

    // Retained surfaces keep moving, but only the current host draws the shared scrim.
    val scrimProgress: Float
        get() = if (coordinator.holder === this) scrimAnimation.value else 0f
}

@Composable
internal fun rememberSheetLease(visible: Boolean): SheetLease {
    val owner = LocalWindowInfo.current
    val request = rememberUpdatedState(visible)
    val lease = remember(owner) {
        val coordinator = coordinators.getOrPut(owner) { SheetHostCoordinator() }
        SheetLease(coordinator)
    }
    DisposableEffect(lease) {
        lease.coordinator.requests[lease] = request
        onDispose {
            lease.coordinator.release(lease)
            lease.coordinator.requests.remove(lease)
            if (lease.coordinator.requests.isEmpty()) coordinators.remove(owner)
        }
    }
    return lease
}
