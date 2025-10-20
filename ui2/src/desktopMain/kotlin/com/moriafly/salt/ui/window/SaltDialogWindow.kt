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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.awt.SwingDialog
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.window.DialogModalityType
import androidx.compose.ui.window.DialogState
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.DialogWindowScope
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.rememberDialogState
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.internal.SaltWindowEnvironment
import java.awt.Dialog.ModalityType
import java.awt.Dimension
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
@ExperimentalComposeUiApi
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
    properties: SaltWindowProperties<ComposeDialog> = SaltWindowProperties(),
    onPreviewKeyEvent: ((KeyEvent) -> Boolean) = { false },
    onKeyEvent: ((KeyEvent) -> Boolean) = { false },
    init: (ComposeDialog) -> Unit = {},
    content: @Composable DialogWindowScope.() -> Unit
) {
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
            CompositionLocalProvider(
                LocalDialogState provides state
            ) {
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
                            properties.onVisibleChanged(window, true)
                        }

                        override fun componentHidden(e: ComponentEvent?) {
                            properties.onVisibleChanged(window, false)
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

                content()
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
