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

package com.moriafly.salt.ui.platform.linux

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.WindowPlacement
import com.moriafly.salt.ui.ChangeSaltThemeIsDark
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.platform.linux.decoration.LinuxCaptionButtonClose
import com.moriafly.salt.ui.platform.linux.decoration.LinuxCaptionButtonMaximize
import com.moriafly.salt.ui.platform.linux.decoration.LinuxCaptionButtonMinimize
import com.moriafly.salt.ui.platform.linux.decoration.LinuxCaptionButtonWidth
import com.moriafly.salt.ui.platform.linux.decoration.LinuxClientShadow
import com.moriafly.salt.ui.platform.linux.decoration.LinuxClientShadowEffect
import com.moriafly.salt.ui.platform.linux.decoration.linuxClientShadow
import com.moriafly.salt.ui.platform.linux.input.LinuxWindowGestures
import com.moriafly.salt.ui.platform.linux.input.LinuxWindowInputEffect
import com.moriafly.salt.ui.platform.linux.input.LocalLinuxWindowGestures
import com.moriafly.salt.ui.platform.linux.window.LinuxCaptionBarHitTest
import com.moriafly.salt.ui.platform.linux.window.LocalLinuxCaptionBarHitTest
import com.moriafly.salt.ui.platform.linux.window.UndecoratedWindowResizer
import com.moriafly.salt.ui.thenIf
import com.moriafly.salt.ui.window.CaptionButtonsAlign
import com.moriafly.salt.ui.window.LocalIsHitTestInCaptionBarState
import com.moriafly.salt.ui.window.LocalSaltWindowInfo
import com.moriafly.salt.ui.window.LocalWindowState
import com.moriafly.salt.ui.window.SaltWindowInfo
import com.moriafly.salt.ui.window.SaltWindowProperties
import com.moriafly.salt.ui.window.internal.LocalSaltWindowContentDecoration
import com.moriafly.salt.ui.window.internal.SaltWindowContentDecoration
import java.awt.event.WindowEvent

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@UnstableSaltUiApi
@Composable
internal fun FrameWindowScope.LinuxSaltWindowFrame(
    resizable: Boolean,
    decoration: WindowDecoration,
    properties: SaltWindowProperties<ComposeWindow>,
    content: @Composable FrameWindowScope.() -> Unit
) {
    val currentProperties by rememberUpdatedState(properties)
    val isHitTestInCaptionBar = remember { mutableStateOf(false) }
    val captionBarHitTest = remember { LinuxCaptionBarHitTest() }
    val undecoratedWindowResizer = remember {
        UndecoratedWindowResizer(
            onResizeEdgeChange = { edge ->
                currentProperties.onResizeEdgeChange(window, edge)
            }
        )
    }
    val windowState = LocalWindowState.current
    val clientShadow = LinuxClientShadow.shouldUseClientShadow(decoration)
    val isMaximizedOrFullScreen = windowState.placement == WindowPlacement.Maximized ||
        windowState.placement == WindowPlacement.Fullscreen
    // Maximized and fullscreen windows drop the shadow, matching the platform convention
    val shadowMargin = if (clientShadow && !isMaximizedOrFullScreen) {
        LinuxClientShadow.margin
    } else {
        0.dp
    }

    val windowGestures = remember(window) {
        LinuxWindowGestures(
            window = window,
            caption = captionBarHitTest,
            resizer = undecoratedWindowResizer,
            captionHovered = isHitTestInCaptionBar
        )
    }

    CompositionLocalProvider(
        LocalSaltWindowInfo provides SaltWindowInfo(
            captionBarHeight = properties.captionBarHeight,
            captionButtonsAlign = CaptionButtonsAlign.End,
            captionButtonsFullWidth = LinuxCaptionButtonWidth * 3f
        ),
        LocalSaltWindowContentDecoration provides SaltWindowContentDecoration(
            inset = shadowMargin,
            cornerRadius = if (shadowMargin > 0.dp) LinuxClientShadow.cornerRadius else 0.dp
        ),
        LocalLinuxCaptionBarHitTest provides captionBarHitTest,
        LocalLinuxWindowGestures provides windowGestures,
        LocalIsHitTestInCaptionBarState provides isHitTestInCaptionBar,
    ) {
        LinuxWindowInputEffect(
            window = window,
            gestures = windowGestures,
            moveable = properties.moveable,
            resizable = resizable && !isMaximizedOrFullScreen,
            canDrag = windowState.placement != WindowPlacement.Fullscreen,
            canMaximize = resizable && properties.maximizeOrRestoreButtonEnabled
        )

        if (clientShadow) {
            LinuxClientShadowEffect(
                window = window,
                margin = shadowMargin
            )
        }

        LaunchedEffect(decoration) {
            // Read the requested thickness through Compose's public window property before
            // replacing its Compose input layer with our native window input handler.
            undecoratedWindowResizer.resizerThickness =
                if (decoration == WindowDecoration.SystemDefault) {
                    4.dp
                } else {
                    window.undecoratedResizerThickness
                }
            window.undecoratedResizerThickness = 0.dp
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(shadowMargin)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .thenIf(shadowMargin > 0.dp) {
                        linuxClientShadow()
                    }
                    .thenIf(clientShadow) {
                        clipToBounds()
                            .background(SaltTheme.colors.background)
                    }
            ) {
                content()

                ChangeSaltThemeIsDark(
                    isDarkTheme = properties.captionButtonIsDarkTheme
                ) {
                    if (properties.captionButtonsVisible) {
                        DisposableEffect(captionBarHitTest) {
                            onDispose { captionBarHitTest.buttonsBounds = Rect.Zero }
                        }
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .onGloballyPositioned {
                                    captionBarHitTest.buttonsBounds = it.boundsInWindow()
                                }
                        ) {
                            LinuxCaptionButtonMinimize(
                                onClick = {
                                    windowState.isMinimized = true
                                },
                                enabled = properties.minimizeButtonEnabled
                            )
                            val isMaximized = windowState.placement == WindowPlacement.Maximized
                            LinuxCaptionButtonMaximize(
                                onClick = {
                                    if (isMaximized) {
                                        windowState.placement = WindowPlacement.Floating
                                    } else {
                                        windowState.placement = WindowPlacement.Maximized
                                    }
                                },
                                maximized = isMaximized,
                                enabled = properties.maximizeOrRestoreButtonEnabled &&
                                    resizable &&
                                    windowState.placement != WindowPlacement.Fullscreen
                            )
                            LinuxCaptionButtonClose(
                                onClick = {
                                    window.dispatchEvent(
                                        WindowEvent(window, WindowEvent.WINDOW_CLOSING)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            undecoratedWindowResizer.Content(
                modifier = Modifier.layoutId("UndecoratedWindowResizer")
            )
        }
    }
}
