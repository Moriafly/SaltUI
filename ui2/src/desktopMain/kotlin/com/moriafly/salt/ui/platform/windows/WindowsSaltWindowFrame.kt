/*
 * Salt UI
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

package com.moriafly.salt.ui.platform.windows

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPlacement
import com.moriafly.salt.ui.ChangeSaltThemeIsDark
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.util.contains
import com.moriafly.salt.ui.window.CaptionButtonClose
import com.moriafly.salt.ui.window.CaptionButtonMaximize
import com.moriafly.salt.ui.window.CaptionButtonMinimize
import com.moriafly.salt.ui.window.CaptionButtonWidth
import com.moriafly.salt.ui.window.CaptionButtonsAlign
import com.moriafly.salt.ui.window.LocalIsHitTestInCaptionBarState
import com.moriafly.salt.ui.window.LocalSaltWindowInfo
import com.moriafly.salt.ui.window.LocalWindowState
import com.moriafly.salt.ui.window.SaltWindowInfo
import com.moriafly.salt.ui.window.SaltWindowProperties
import com.moriafly.salt.ui.window.rememberFontIconFamily
import java.awt.event.WindowEvent

@OptIn(ExperimentalLayoutApi::class)
@UnstableSaltUiApi
@Composable
internal fun FrameWindowScope.WindowsSaltWindowFrame(
    resizable: Boolean,
    properties: SaltWindowProperties<ComposeWindow>,
    content: @Composable FrameWindowScope.() -> Unit
) {
    val currentResizable by rememberUpdatedState(resizable)
    val currentProperties by rememberUpdatedState(properties)

    val isHitTestInCaptionBar = remember { mutableStateOf(false) }

    CompositionLocalProvider(
        LocalSaltWindowInfo provides SaltWindowInfo(
            captionBarHeight = properties.captionBarHeight,
            captionButtonsAlign = CaptionButtonsAlign.End,
            captionButtonsFullWidth = CaptionButtonWidth * 3f
        ),
        LocalIsHitTestInCaptionBarState provides isHitTestInCaptionBar,
    ) {
        val windowState = LocalWindowState.current

        val windowClientInsets = remember { MutableWindowInsets() }

        var captionBarRect by remember { mutableStateOf(Rect.Zero) }
        var minimizeButtonRect by remember { mutableStateOf(Rect.Zero) }
        var maximizeButtonRect by remember { mutableStateOf(Rect.Zero) }
        var closeButtonRect by remember { mutableStateOf(Rect.Zero) }

        val styler = remember(window) {
            WindowsSaltWindowStyler(
                window = window,
                hitTest = { x, y ->
                    when {
                        // Minimize button doesn't have a tooltip
                        // TODO https://github.com/microsoft/microsoft-ui-xaml/issues/9149
                        currentProperties.captionButtonsVisible &&
                            minimizeButtonRect.contains(x, y) ->
                            HitTestResult.HTMINBUTTON

                        currentProperties.captionButtonsVisible &&
                            currentResizable &&
                            windowState.placement != WindowPlacement.Fullscreen &&
                            maximizeButtonRect.contains(x, y) ->
                            HitTestResult.HTMAXBUTTON

                        currentProperties.captionButtonsVisible &&
                            closeButtonRect.contains(x, y) ->
                            HitTestResult.HTCLOSE

                        // Last hit test result is Caption
                        windowState.placement != WindowPlacement.Fullscreen &&
                            captionBarRect.contains(x, y) &&
                            isHitTestInCaptionBar.value ->
                            HitTestResult.HTCAPTION

                        else -> HitTestResult.HTCLIENT
                    }
                },
                onWindowInsetUpdate = { windowInsets ->
                    windowClientInsets.insets = windowInsets
                }
            )
        }

        LaunchedEffect(resizable) {
            styler.updateIsResizable(resizable)
        }

        val isFullscreen =
            windowState.placement == WindowPlacement.Fullscreen

        LaunchedEffect(isFullscreen) {
            if (isFullscreen) {
                styler.updateBorderAndShadow(false)
            } else {
                styler.updateBorderAndShadow(true)
            }
        }

        LaunchedEffect(properties.backgroundType, properties.backgroundIsDarkTheme) {
            styler.updateBackground(
                type = properties.backgroundType,
                isDarkTheme = properties.backgroundIsDarkTheme
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(windowClientInsets)
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(properties.captionBarHeight)
                    .onGloballyPositioned {
                        captionBarRect = it.boundsInWindow()
                    }
            )

            content()

            ChangeSaltThemeIsDark(
                isDarkTheme = properties.captionButtonIsDarkTheme
            ) {
                if (properties.captionButtonsVisible) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                    ) {
                        val iconFontFamily by rememberFontIconFamily()
                        CaptionButtonMinimize(
                            onClick = {
                                windowState.isMinimized = true
                            },
                            iconFontFamily = iconFontFamily,
                            modifier = Modifier
                                .onGloballyPositioned {
                                    minimizeButtonRect = it.boundsInWindow()
                                }
                        )
                        val isMaximized = windowState.placement == WindowPlacement.Maximized
                        CaptionButtonMaximize(
                            onClick = {
                                if (isMaximized) {
                                    windowState.placement = WindowPlacement.Floating
                                } else {
                                    windowState.placement = WindowPlacement.Maximized
                                }
                            },
                            iconFontFamily = iconFontFamily,
                            maximized = isMaximized,
                            modifier = Modifier
                                .onGloballyPositioned {
                                    maximizeButtonRect = it.boundsInWindow()
                                },
                            enabled = resizable &&
                                windowState.placement != WindowPlacement.Fullscreen
                        )
                        CaptionButtonClose(
                            onClick = {
                                window.dispatchEvent(
                                    WindowEvent(window, WindowEvent.WINDOW_CLOSING)
                                )
                            },
                            iconFontFamily = iconFontFamily,
                            modifier = Modifier
                                .onGloballyPositioned {
                                    closeButtonRect = it.boundsInWindow()
                                }
                        )
                    }
                }
            }
        }
    }
}
