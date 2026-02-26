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

package com.moriafly.salt.ui.platform.linux

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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPlacement
import com.moriafly.salt.ui.ChangeSaltThemeIsDark
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.CaptionButtonClose
import com.moriafly.salt.ui.window.CaptionButtonMaximize
import com.moriafly.salt.ui.window.CaptionButtonMinimize
import com.moriafly.salt.ui.window.CaptionButtonsAlign
import com.moriafly.salt.ui.window.LocalIsHitTestInCaptionBarState
import com.moriafly.salt.ui.window.LocalSaltWindowInfo
import com.moriafly.salt.ui.window.LocalWindowState
import com.moriafly.salt.ui.window.SaltWindowBackgroundType
import com.moriafly.salt.ui.window.SaltWindowInfo
import com.moriafly.salt.ui.window.SaltWindowProperties
import com.moriafly.salt.ui.window.rememberFontIconFamily
import java.awt.Cursor
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.event.WindowEvent

@OptIn(ExperimentalLayoutApi::class)
@UnstableSaltUiApi
@Composable
internal fun FrameWindowScope.LinuxSaltWindowFrame(
    properties: SaltWindowProperties<ComposeWindow>,
    content: @Composable FrameWindowScope.() -> Unit
) {
    val isHitTestInCaptionBar = remember { mutableStateOf(false) }
    val currentProperties by rememberUpdatedState(properties)

    CompositionLocalProvider(
        LocalSaltWindowInfo provides SaltWindowInfo(
            captionBarHeight = properties.captionBarHeight,
            captionButtonsAlign = CaptionButtonsAlign.End,
            captionButtonsFullWidth = 80.dp
        ),
        LocalIsHitTestInCaptionBarState provides isHitTestInCaptionBar,
    ) {
        val windowState = LocalWindowState.current
        val windowClientInsets = remember { MutableWindowInsets() }
        val styler = remember(window) {
            LinuxSaltWindowStyler(window)
        }

        DisposableEffect(window) {
            var dragStartScreenPos: Point? = null
            var dragStartWindowPos: Point? = null
            var isDragging = false

            val mouseListener = object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    if (e.button != MouseEvent.BUTTON1) return
                    if (!isHitTestInCaptionBar.value) return
                    if (!currentProperties.moveable) return
                    if (windowState.placement == WindowPlacement.Fullscreen) return

                    val captionBarHeightPx = (currentProperties.captionBarHeight.value *
                        window.graphicsConfiguration.defaultTransform.scaleY).toInt()
                    if (e.y > captionBarHeightPx) return

                    // Drag from maximized: restore first, reposition proportionally
                    if (windowState.placement == WindowPlacement.Maximized) {
                        val oldWidth = window.width
                        val ratio = e.x.toFloat() / oldWidth
                        windowState.placement = WindowPlacement.Floating
                        window.setLocation(
                            (e.xOnScreen - window.width * ratio).toInt(),
                            e.yOnScreen - e.y
                        )
                    }

                    dragStartScreenPos = Point(e.xOnScreen, e.yOnScreen)
                    dragStartWindowPos = window.location
                    isDragging = true
                }

                override fun mouseReleased(e: MouseEvent) {
                    if (e.button != MouseEvent.BUTTON1) return
                    if (isDragging) {
                        window.cursor = Cursor.getDefaultCursor()
                    }
                    isDragging = false
                    dragStartScreenPos = null
                    dragStartWindowPos = null
                }

                override fun mouseClicked(e: MouseEvent) {
                    if (e.button != MouseEvent.BUTTON1) return
                    if (e.clickCount != 2) return
                    if (!isHitTestInCaptionBar.value) return

                    val captionBarHeightPx = (currentProperties.captionBarHeight.value *
                        window.graphicsConfiguration.defaultTransform.scaleY).toInt()
                    if (e.y > captionBarHeightPx) return

                    if (windowState.placement == WindowPlacement.Maximized) {
                        windowState.placement = WindowPlacement.Floating
                    } else {
                        windowState.placement = WindowPlacement.Maximized
                    }
                }
            }

            val mouseMotionListener = object : MouseMotionAdapter() {
                override fun mouseDragged(e: MouseEvent) {
                    if (!isDragging) return
                    val startScreen = dragStartScreenPos ?: return
                    val startWindow = dragStartWindowPos ?: return
                    window.setLocation(
                        startWindow.x + e.xOnScreen - startScreen.x,
                        startWindow.y + e.yOnScreen - startScreen.y
                    )
                }
            }

            window.addMouseListener(mouseListener)
            window.addMouseMotionListener(mouseMotionListener)

            onDispose {
                window.removeMouseListener(mouseListener)
                window.removeMouseMotionListener(mouseMotionListener)
            }
        }

        LaunchedEffect(properties.backgroundIsDarkTheme) {
            styler.updateBackground(
                type = SaltWindowBackgroundType.None,
                isDarkTheme = properties.backgroundIsDarkTheme
            )
        }

        LaunchedEffect(properties.captionBarHeight) {
            styler.disableTitleBar(properties.captionBarHeight.value)
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
                            enabled = properties.minimizeButtonEnabled
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
                            enabled = properties.maximizeOrRestoreButtonEnabled &&
                                    windowState.placement != WindowPlacement.Fullscreen
                        )
                        CaptionButtonClose(
                            onClick = {
                                window.dispatchEvent(
                                    WindowEvent(window, WindowEvent.WINDOW_CLOSING)
                                )
                            },
                            iconFontFamily = iconFontFamily,
                        )
                    }
                }
            }
        }
    }
}
