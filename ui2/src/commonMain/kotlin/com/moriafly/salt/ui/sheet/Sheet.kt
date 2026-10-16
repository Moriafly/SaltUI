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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.moriafly.salt.adaptive.window.LocalSaltAdaptiveWindow
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.material.DisableMaterial
import com.moriafly.salt.ui.sheet.internal.SheetDefaults
import com.moriafly.salt.ui.sheet.internal.SheetDetentSaver
import com.moriafly.salt.ui.sheet.internal.SheetHost
import com.moriafly.salt.ui.sheet.internal.SheetPendingInput
import com.moriafly.salt.ui.sheet.internal.SheetScrim
import com.moriafly.salt.ui.sheet.internal.SheetSurface
import com.moriafly.salt.ui.sheet.internal.SheetTransitionState
import com.moriafly.salt.ui.sheet.internal.rememberSheetLease

/**
 * A modal, adaptive Sheet with its own height, animation, input and host lifecycle.
 *
 * Keep this call in composition when [visible] is false so that the exit animation can finish.
 * After the exit animation, content state saved with `rememberSaveable` and the selected detent
 * are discarded unless [SheetProperties.retainStateWhenHidden] is enabled. Retaining state does
 * not keep the content composed; ordinary `remember` values and content effects are disposed.
 * Reopening before the exit finishes keeps the current content state and interrupts the motion.
 * [onDismissRequest] is called once per user dismissal attempt. It never changes [visible];
 * keep it true to confirm a draft first. Programmatic hiding never sends a dismissal request.
 * [modifier] applies to the surface, not to the full-window scrim.
 * [SheetDisplayMode.Auto] selects a mode from the owning window's width and height.
 * [SheetDisplayMode.Bottom] supports height detents.
 *
 * Only one Sheet in the same owning window may have [visible] set to true.
 * To replace a Sheet, hide the current one and show the next in the same state update.
 * Their surface animations overlap while the next Sheet takes over the scrim and input.
 *
 * Use [SheetContentLayout.EdgeToEdge] with [SheetScope.contentPadding] for edge-to-edge scrolling.
 * To await the end of an exit animation, use the overload accepting a [SheetState].
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
    Sheet(
        visible = visible,
        onDismissRequest = onDismissRequest,
        state = rememberSheetState(),
        modifier = modifier,
        title = title,
        properties = properties,
        actions = actions,
        content = content
    )
}

/**
 * A [Sheet] whose presentation lifecycle can be observed through [state].
 *
 * [visible] remains the sole visibility input. The state does not show or hide the Sheet; use
 * [SheetState.awaitHidden] to keep a navigation scene composed until its exit finishes.
 * Keep this call in composition while waiting and use a separate state for each Sheet call.
 * See the overload without a state parameter for layout, dismissal and content retention behavior.
 */
@UnstableSaltUiApi
@Composable
fun Sheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    state: SheetState,
    modifier: Modifier = Modifier,
    title: String? = null,
    properties: SheetProperties = SheetProperties(),
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable SheetScope.() -> Unit
) {
    val density = LocalDensity.current
    val preferred =
        rememberSaveable(stateSaver = SheetDetentSaver) { mutableStateOf(properties.initialDetent) }
    val transitionState =
        remember {
            SheetTransitionState(
                density = density,
                preferredDetent = { preferred.value },
                onDetentChanged = { preferred.value = it }
            )
        }
    val lease = rememberSheetLease(visible = visible, state = transitionState)
    val coordinator = lease.coordinator
    val contentState = rememberSaveableStateHolder()
    var contentKey by rememberSaveable { mutableIntStateOf(0) }
    var retained by remember { mutableStateOf(false) }
    val present = visible || retained
    val owner = remember { Any() }
    val currentVisible by rememberUpdatedState(visible)
    val currentProperties by rememberUpdatedState(properties)
    val currentDismissRequest by rememberUpdatedState(onDismissRequest)
    val requestDismiss = remember {
        {
            if (currentVisible && currentProperties.dismissible) currentDismissRequest()
        }
    }

    DisposableEffect(state) {
        state.attach(owner)
        onDispose { state.detach(owner) }
    }
    SideEffect {
        transitionState.updateVisibility(visible = visible)
        state.updatePresentation(owner = owner, present = present)
    }
    LaunchedEffect(visible, lease) {
        if (visible) {
            retained = true
        } else if (!retained) {
            return@LaunchedEffect
        }
        coordinator.awaitPresentationReady()
        if (visible) coordinator.acquire(lease)
        transitionState.animateVisible(visible)
        if (!visible) {
            retained = false
            coordinator.release(lease)
        }
    }
    LaunchedEffect(visible, retained, properties.retainStateWhenHidden, properties.initialDetent) {
        if (!visible && !retained && !properties.retainStateWhenHidden) {
            contentState.removeState(contentKey)
            contentKey++
            preferred.value = currentProperties.initialDetent
            transitionState.resetGeometry()
        }
    }

    if (present) {
        // Dispose the dim animation with the host so hidden Sheets cannot retain a gray overlay.
        val replacing = !visible && coordinator.hasVisibleSheet
        val dimAlpha = animateFloatAsState(
            targetValue = if (replacing) SheetDefaults.SCRIM_OPACITY else 0f,
            animationSpec = tween(200),
            label = "Sheet replacement dim"
        )
        SheetHost(
            lease = lease,
            inputEnabled = visible,
            displayMode = properties.displayMode,
            adaptiveWindow = LocalSaltAdaptiveWindow.current,
            backgroundColor = SaltTheme.colors.background,
            onDismissRequest = requestDismiss
        ) { environment ->
            if (retained) {
                DisableMaterial {
                    contentState.SaveableStateProvider(contentKey) {
                        SheetSurface(
                            state = transitionState,
                            scrimAlpha = { if (environment.drawsScrim) lease.scrimAlpha else 0f },
                            dimAlpha = { dimAlpha.value },
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
                Box(modifier = Modifier.fillMaxSize()) {
                    // Keep the scrim present before the incoming surface is composed.
                    SheetScrim(
                        alpha = { if (environment.drawsScrim) lease.scrimAlpha else 0f },
                        onDismissRequest = requestDismiss
                    )
                    SheetPendingInput()
                }
            }
        }
    }
}
