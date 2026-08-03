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

package com.moriafly.salt.ui.platform.macos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPlacement
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.LocalIsHitTestInCaptionBarState
import com.moriafly.salt.ui.window.LocalSaltWindowInfo
import com.moriafly.salt.ui.window.LocalWindowState
import com.moriafly.salt.ui.window.SaltWindowProperties

@UnstableSaltUiApi
@Composable
internal fun FrameWindowScope.MacOSSaltWindowFrame(
    properties: SaltWindowProperties<ComposeWindow>,
    content: @Composable FrameWindowScope.() -> Unit
) {
    val isHitTestInCaptionBar = remember { mutableStateOf(false) }
    val windowInfo = remember(properties.captionBarHeight) {
        macOSSaltWindowInfo(properties.captionBarHeight)
    }
    val nativeCaptionBarHeight = macOSCaptionBarHeightInWindowCoordinates(
        window = window,
        captionBarHeight = windowInfo.captionBarHeight
    )
    CompositionLocalProvider(
        LocalSaltWindowInfo provides windowInfo,
        LocalIsHitTestInCaptionBarState provides isHitTestInCaptionBar
    ) {
        val windowState = LocalWindowState.current
        val styler = remember(window) {
            MacOSSaltWindowStyler(window)
        }

        DisposableEffect(styler) {
            onDispose(styler::dispose)
        }

        LaunchedEffect(properties.backgroundType, properties.backgroundIsDarkTheme) {
            styler.updateBackground(
                type = properties.backgroundType,
                isDarkTheme = properties.backgroundIsDarkTheme
            )
        }

        // AppKit owns all geometry transitions after installation. In particular, do not key this
        // effect on WindowState.placement: AppKit may be waiting for the AWT resize callback while
        // a maximize transition is in progress, so re-entering native title-bar setup from that
        // callback would make the two event loops wait for each other.
        LaunchedEffect(
            styler,
            window.isUndecorated,
            nativeCaptionBarHeight
        ) {
            if (!window.isUndecorated) {
                styler.updateTitleBar(nativeCaptionBarHeight)
            }
        }

        if (properties.moveable) {
            MacOSCaptionBarDragHandler(
                onDrag = styler::performWindowDrag,
                onDoubleClick = {
                    windowState.placement = when (windowState.placement) {
                        WindowPlacement.Maximized -> WindowPlacement.Floating
                        else -> WindowPlacement.Maximized
                    }
                },
                window = window,
                captionBarHeightInWindowCoordinates = nativeCaptionBarHeight,
                isHitTestInCaptionBar = isHitTestInCaptionBar.value,
                canDrag = windowState.placement != WindowPlacement.Fullscreen
            )
        }

        content()
    }
}
