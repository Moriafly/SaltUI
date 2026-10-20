/*
 * SaltUI
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

@file:Suppress("unused", "ktlint:standard:property-naming")

package com.moriafly.salt.ui.window

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.platform.linux.linuxCaptionBarHitTest
import com.moriafly.salt.ui.platform.windows.windowsCaptionBarTouchInput

/**
 * **Hit-test configuration for SaltUI windows only, not a standalone window-dragging component.**
 *
 * This invisible region identifies caption-bar input for the platform window handling installed
 * by [SaltWindow] or [SaltDialogWindow]. It does not provide window movement on its own. Movement
 * is handled by the SaltUI window implementation and controlled by [SaltWindowProperties.moveable].
 * Use it only inside one of these windows; it cannot add dragging support to an arbitrary window.
 *
 * Place it above the window content and below interactive caption-bar controls in drawing order,
 * so those controls keep receiving their own input. Its default size fills the window width and
 * uses [SaltWindowInfo.captionBarHeight].
 *
 * Each window may have **zero or one** active [CaptionBarHitTest]. A second instance in the same
 * window throws [IllegalStateException], even when the instances are in different layout branches.
 * Child [SaltWindow] and [SaltDialogWindow] instances have independent registrations and may each
 * have their own region. Leaving the composition releases the registration for that window.
 *
 * Keep this configuration outside `AnimatedContent`, `Crossfade`, or overlapping
 * `AnimatedVisibility` branches. Outgoing content still occupies its registration until it
 * leaves the composition, so incoming and outgoing regions count as two active instances.
 * Keep one configuration at window level and animate only the page content or caption controls.
 *
 * Only [SaltWindow] and [SaltDialogWindow] establish independent registration scopes. Do not
 * declare [CaptionBarHitTest] in Compose `Popup`, `Dialog`, `DialogWindow`, or plain `Window`
 * content: these child window and overlay compositions inherit the parent's registration rather
 * than establishing a SaltUI window scope, even when backed by a separate native window.
 *
 * @throws IllegalStateException If used outside a SaltUI window or more than once in the same
 * window.
 */
@UnstableSaltUiApi
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CaptionBarHitTest(
    modifier: Modifier = Modifier
) {
    if (OS.isDesktop()) {
        val registration = LocalCaptionBarHitTestRegistration.current
        val saltWindowInfo = LocalSaltWindowInfo.current
        val isHitTestInCaptionBarState = LocalIsHitTestInCaptionBarState.current
        val saltWindowProperties = LocalSaltWindowProperties.current

        // Register during apply: abandoned recompositions must not claim the slot. The keys
        // release the old window resources if this content changes window scope.
        DisposableEffect(registration, isHitTestInCaptionBarState) {
            check(!registration.isRegistered) {
                "Only one CaptionBarHitTest may be active in a SaltUI window scope. " +
                    "Popup, Dialog, and non-SaltUI child windows inherit the host's " +
                    "registration instead of creating their own."
            }
            registration.isRegistered = true
            onDispose {
                registration.isRegistered = false
                isHitTestInCaptionBarState.value = false
            }
        }

        Spacer(
            modifier = modifier
                .fillMaxWidth()
                .height(saltWindowInfo.captionBarHeight)
                .run {
                    when (OS.current) {
                        is OS.Windows -> windowsCaptionBarTouchInput(saltWindowProperties.moveable)
                        is OS.Linux -> linuxCaptionBarHitTest()
                        else -> this
                    }
                }
                .onPointerEvent(PointerEventType.Enter) {
                    isHitTestInCaptionBarState.value = true
                }
                .onPointerEvent(PointerEventType.Exit) {
                    isHitTestInCaptionBarState.value = false
                }
        )
    }
}

// Each SaltUI window provides a fresh registration inside its native window content, so child
// windows do not inherit their parent's registration. This is not observable composition state.
internal class CaptionBarHitTestRegistration {
    var isRegistered = false
}

internal val LocalCaptionBarHitTestRegistration =
    staticCompositionLocalOf<CaptionBarHitTestRegistration> {
        error("CaptionBarHitTest requires content hosted by a SaltWindow or SaltDialogWindow.")
    }
