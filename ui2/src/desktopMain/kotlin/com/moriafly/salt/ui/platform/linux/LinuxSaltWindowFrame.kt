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

package com.moriafly.salt.ui.platform.linux

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPlacement
import com.moriafly.salt.ui.ChangeSaltThemeIsDark
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.CaptionButtonsAlign
import com.moriafly.salt.ui.window.LocalIsHitTestInCaptionBarState
import com.moriafly.salt.ui.window.LocalSaltWindowInfo
import com.moriafly.salt.ui.window.LocalWindowState
import com.moriafly.salt.ui.window.SaltWindowInfo
import com.moriafly.salt.ui.window.SaltWindowProperties
import java.awt.event.WindowEvent

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@UnstableSaltUiApi
@Composable
internal fun FrameWindowScope.LinuxSaltWindowFrame(
    resizable: Boolean,
    properties: SaltWindowProperties<ComposeWindow>,
    content: @Composable FrameWindowScope.() -> Unit
) {
    val currentProperties by rememberUpdatedState(properties)
    val isHitTestInCaptionBar = remember { mutableStateOf(false) }
    val undecoratedWindowResizer = remember { UndecoratedWindowResizer(window) }

    CompositionLocalProvider(
        LocalSaltWindowInfo provides SaltWindowInfo(
            captionBarHeight = properties.captionBarHeight,
            captionButtonsAlign = CaptionButtonsAlign.End,
            captionButtonsFullWidth = LinuxCaptionButtonWidth * 3f
        ),
        LocalIsHitTestInCaptionBarState provides isHitTestInCaptionBar,
    ) {
        val windowState = LocalWindowState.current

        if (currentProperties.moveable) {
            LinuxCaptionBarDragHandler(
                window = window,
                captionBarHeight = properties.captionBarHeight,
                isMoveable = properties.moveable,
                isHitTestInCaptionBar = isHitTestInCaptionBar.value,
                canDrag = windowState.placement != WindowPlacement.Fullscreen,
                windowState = windowState,
            )
        }

        LaunchedEffect(window.undecoratedResizerThickness) {
            if (window.undecoratedResizerThickness != 0.dp) {
                window.undecoratedResizerThickness = 0.dp
            }
        }

        LaunchedEffect(resizable, windowState.placement) {
            val isMaximizedOrFullScreen = windowState.placement == WindowPlacement.Maximized ||
                windowState.placement == WindowPlacement.Fullscreen
            undecoratedWindowResizer.enabled = resizable && !isMaximizedOrFullScreen
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            content()

            ChangeSaltThemeIsDark(
                isDarkTheme = properties.captionButtonIsDarkTheme
            ) {
                if (properties.captionButtonsVisible) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
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

            undecoratedWindowResizer.Content(
                modifier = Modifier.layoutId("UndecoratedWindowResizer")
            )
        }
    }
}
