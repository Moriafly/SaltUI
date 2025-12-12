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

@file:Suppress("unused")

package com.moriafly.salt.ui.window

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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.awt.SwingDialog
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.window.DialogModalityType
import androidx.compose.ui.window.DialogState
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.DialogWindowScope
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.rememberDialogState
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.ChangeSaltThemeIsDark
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.platform.windows.HitTestResult
import com.moriafly.salt.ui.util.contains
import com.moriafly.salt.ui.window.internal.SaltWindowEnvironment
import com.moriafly.salt.ui.window.internal.SaltWindowStyler
import java.awt.Dialog.ModalityType
import java.awt.Dimension
import java.awt.Window
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

/**
 * # Salt Dialog Window
 *
 * Composes platform dialog in the current composition. When Dialog enters the composition,
 * a new platform dialog will be created and receives the focus. When Dialog leaves the
 * composition, dialog will be disposed and closed.
 *
 * Dialog is a modal window. It means it blocks the parent [SaltWindow] / [SaltDialogWindow] in
 * which composition context it was created.
 *
 * @param properties [SaltWindowProperties]
 * @param init https://youtrack.jetbrains.com/issue/CMP-8719
 * ```
 * init = { it.modalityType = ModalityType.MODELESS }
 * ```
 *
 * @see [DialogWindow]
 * @see [SwingDialog]
 * @see [SaltWindowProperties]
 */
@UnstableSaltUiApi
@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SaltDialogWindow(
    onCloseRequest: () -> Unit,
    state: DialogState = rememberDialogState(),
    visible: Boolean = true,
    title: String = "Untitled",
    icon: Painter? = null,
    decoration: WindowDecoration = WindowDecoration.SystemDefault,
    transparent: Boolean = false,
    resizable: Boolean = true,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = false,
    modalityType: DialogModalityType = DialogModalityType.DocumentModal,
    properties: SaltWindowProperties<ComposeDialog> = SaltWindowProperties.default(),
    onPreviewKeyEvent: ((KeyEvent) -> Boolean) = { false },
    onKeyEvent: ((KeyEvent) -> Boolean) = { false },
    init: (ComposeDialog) -> Unit = {},
    content: @Composable DialogWindowScope.() -> Unit
) {
    require(properties.captionButtonHeight <= properties.captionBarHeight) {
        "Caption button height must be less than caption bar height"
    }

    val currentProperties by rememberUpdatedState(properties)

    SaltWindowEnvironment {
        SwingDialog(
            onCloseRequest = onCloseRequest,
            state = state,
            visible = visible,
            title = title,
            icon = icon,
            decoration = decoration,
            transparent = transparent,
            resizable = resizable,
            enabled = enabled,
            focusable = focusable,
            alwaysOnTop = alwaysOnTop,
            onPreviewKeyEvent = onPreviewKeyEvent,
            onKeyEvent = onKeyEvent,
            modalityType = modalityType.toAwtModalityType(),
            init = init
        ) {
            val isHitTestInCaptionBar = remember { mutableStateOf(false) }
            val density = LocalDensity.current

            @Suppress("UNCHECKED_CAST")
            CompositionLocalProvider(
                LocalDialogState provides state,
                LocalSaltWindowProperties provides properties as SaltWindowProperties<Window>,
                LocalIsHitTestInCaptionBarState provides isHitTestInCaptionBar,
                LocalSaltWindowInfo provides SaltWindowInfo(
                    captionBarHeight = properties.captionBarHeight,
                    captionButtonsAlign = CaptionButtonsAlign.End,
                    captionButtonsFullWidth = CaptionButtonWidth
                ),
                LocalDensity provides Density(
                    density = density.density * properties.extraDisplayScale,
                    fontScale = density.fontScale * properties.extraFontScale
                )
            ) {
                val windowClientInsets = remember { MutableWindowInsets() }

                var captionBarRect by remember { mutableStateOf(Rect.Zero) }
                var closeButtonRect by remember { mutableStateOf(Rect.Zero) }

                if (OS.isWindows()) {
                    val saltWindowStyler = remember(window) {
                        SaltWindowStyler(
                            window = window,
                            hitTest = { x, y ->
                                when {
                                    currentProperties.captionButtonsVisible &&
                                        closeButtonRect.contains(x, y) ->
                                        HitTestResult.HTCLOSE

                                    // Last hit test result is Caption
                                    captionBarRect.contains(x, y) && isHitTestInCaptionBar.value ->
                                        HitTestResult.HTCAPTION

                                    else -> HitTestResult.HTCLIENT
                                }
                            },
                            onWindowInsetUpdate = { windowInsets ->
                                windowClientInsets.insets = windowInsets
                            }
                        ).apply {
                            updateBorderAndShadow(true)
                            updateIsToolWindow(true)
                        }
                    }

                    LaunchedEffect(resizable) {
                        saltWindowStyler.updateIsResizable(resizable)
                    }

                    LaunchedEffect(properties.backgroundType, properties.backgroundIsDarkTheme) {
                        saltWindowStyler.updateBackground(
                            type = properties.backgroundType,
                            isDarkTheme = properties.backgroundIsDarkTheme
                        )
                    }
                }

                val minSize = properties.minSize
                LaunchedEffect(minSize) {
                    require(minSize.width.isSpecified && minSize.height.isSpecified) {
                        "minSize.width and minSize.height must be specified"
                    }

                    // TODO https://bugs.openjdk.org/browse/JDK-8221452
                    window.minimumSize =
                        Dimension(minSize.width.value.toInt(), minSize.height.value.toInt())
                }

                DisposableEffect(window) {
                    val adapter = object : WindowAdapter(), ComponentListener {
                        override fun windowActivated(e: WindowEvent?) {
                        }

                        override fun windowDeactivated(e: WindowEvent?) {
                        }

                        override fun windowIconified(e: WindowEvent?) {
                        }

                        override fun windowDeiconified(e: WindowEvent?) {
                        }

                        override fun windowStateChanged(e: WindowEvent) {
                        }

                        override fun componentResized(e: ComponentEvent?) {
                        }

                        override fun componentMoved(e: ComponentEvent?) {
                        }

                        override fun componentShown(e: ComponentEvent?) {
                            currentProperties.onVisibleChange(window, true)
                        }

                        override fun componentHidden(e: ComponentEvent?) {
                            currentProperties.onVisibleChange(window, false)
                        }
                    }

                    window.addWindowListener(adapter)
                    window.addWindowStateListener(adapter)
                    window.addComponentListener(adapter)

                    onDispose {
                        window.removeWindowListener(adapter)
                        window.removeWindowStateListener(adapter)
                        window.removeComponentListener(adapter)
                    }
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
    }
}

@UnstableSaltUiApi
val LocalDialogState = staticCompositionLocalOf<DialogState> {
    error("LocalDialogState is not provided")
}

/**
 * Returns the AWT [java.awt.Dialog.ModalityType] corresponding to the given Compose
 * [DialogModalityType].
 */
@OptIn(ExperimentalComposeUiApi::class)
internal fun DialogModalityType.toAwtModalityType(): ModalityType = when (this) {
    DialogModalityType.Modeless -> ModalityType.MODELESS
    DialogModalityType.DocumentModal -> ModalityType.DOCUMENT_MODAL
    DialogModalityType.ApplicationModal -> ModalityType.APPLICATION_MODAL
    else -> error("Unknown dialog modality type: $this")
}
