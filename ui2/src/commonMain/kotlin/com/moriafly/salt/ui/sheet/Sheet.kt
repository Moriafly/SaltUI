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

@file:OptIn(
    com.moriafly.salt.ui.UnstableSaltUiApi::class,
    com.moriafly.salt.adaptive.window.UnstableSaltAdaptiveWindowApi::class
)

package com.moriafly.salt.ui.sheet

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.moriafly.salt.adaptive.window.LocalSaltAdaptiveWindow
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.material.DisableMaterial
import com.moriafly.salt.ui.sheet.internal.SheetDetentSaver
import com.moriafly.salt.ui.sheet.internal.SheetHost
import com.moriafly.salt.ui.sheet.internal.SheetPendingInput
import com.moriafly.salt.ui.sheet.internal.SheetSurface
import com.moriafly.salt.ui.sheet.internal.SheetTransitionState
import com.moriafly.salt.ui.sheet.internal.rememberSheetLease
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * A modal, adaptive Sheet with its own height, animation, input and host lifecycle.
 *
 * Keep this call in composition when [visible] is false so that the exit animation can finish.
 * [onDismissRequest] is called once per user dismissal attempt. It never changes [visible];
 * keep it true to confirm a draft first. Programmatic hiding never sends a dismissal request.
 * [modifier] applies to the surface, not to the full-window scrim.
 * Bottom presentation supports detents; phones use it in either orientation by default.
 * Tablets and desktops default to centered presentation.
 * Only one Sheet in the same owning window may have [visible] set to true.
 * A pending Sheet keeps the window modal while the previous Sheet finishes exiting.
 * Use [SheetContentLayout.EdgeToEdge] with [SheetScope.contentPadding] for edge-to-edge scrolling.
 */
@UnstableSaltUiApi
@Composable
fun Sheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    properties: SheetProperties = SheetProperties(),
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable SheetScope.() -> Unit
) {
    val density = LocalDensity.current
    val preferred =
        rememberSaveable(stateSaver = SheetDetentSaver) { mutableStateOf(properties.initialDetent) }
    val state =
        remember {
            SheetTransitionState(
                density = density,
                preferredDetent = { preferred.value },
                onDetentChanged = { preferred.value = it }
            )
        }
    val lease = rememberSheetLease(visible)
    val progress = remember { Animatable(0f) }
    val contentState = rememberSaveableStateHolder()
    var retained by remember { mutableStateOf(false) }
    val currentVisible by rememberUpdatedState(visible)
    val currentProperties by rememberUpdatedState(properties)
    val currentDismissRequest by rememberUpdatedState(onDismissRequest)
    val requestDismiss = remember {
        {
            if (currentVisible && currentProperties.dismissible) currentDismissRequest()
        }
    }

    SideEffect {
        state.updateVisibility(visible = visible)
    }
    LaunchedEffect(visible, lease) {
        if (visible) {
            lease.coordinator.acquire(lease.token)
            retained = true
        } else if (!retained) {
            return@LaunchedEffect
        }
        snapshotFlow { state.ready }.first { it }
        coroutineScope {
            launch { progress.animateTo(if (visible) 1f else 0f, tween(if (visible) 200 else 180)) }
            launch { state.animateVisible(visible) }
        }
        if (!visible) {
            retained = false
        }
    }

    if (visible || retained) {
        SheetHost(
            inputEnabled = visible,
            presentation = properties.presentation,
            adaptiveWindow = LocalSaltAdaptiveWindow.current,
            backgroundColor = SaltTheme.colors.background,
            onDismissRequest = requestDismiss
        ) { environment ->
            if (retained) {
                DisableMaterial {
                    contentState.SaveableStateProvider("sheet-content") {
                        SheetSurface(
                            state = state,
                            progress = { progress.value },
                            visible = visible,
                            onDismissRequest = requestDismiss,
                            modifier = modifier,
                            environment = environment,
                            title = title,
                            properties = properties,
                            actions = actions,
                            content = content
                        )
                    }
                }
            } else {
                SheetPendingInput()
            }
        }
        // Release ownership when the host leaves composition, including cancelled transitions.
        DisposableEffect(lease) {
            onDispose {
                lease.coordinator.release(lease.token)
            }
        }
    }
}
