/*
 * Salt UI
 * Copyright (C) 2023 Moriafly
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

@file:Suppress("unused")

package com.moriafly.salt.ui.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.moriafly.salt.ui.BasicButton
import com.moriafly.salt.ui.ButtonAppearance
import com.moriafly.salt.ui.ButtonColors
import com.moriafly.salt.ui.ButtonDefaults
import com.moriafly.salt.ui.ControlSize
import com.moriafly.salt.ui.ItemEdit
import com.moriafly.salt.ui.ProvideContentColorTextStyle
import com.moriafly.salt.ui.ProvideControlSize
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.internal.stringResourceCancel
import com.moriafly.salt.ui.internal.stringResourceConfirm
import com.moriafly.salt.ui.material.DisableMaterial
import com.moriafly.salt.ui.thenIf

/**
 * Displays a compact informational dialog with one confirmation action.
 */
@OptIn(UnstableSaltUiApi::class)
@Composable
fun YesDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(
        usePlatformDefaultWidth = false
    ),
    title: String,
    content: String,
    confirmText: String = stringResourceConfirm()
) {
    BasicAdaptiveDialog(
        onDismissRequest = onDismissRequest,
        size = AdaptiveDialogSize.Min,
        properties = properties
    ) {
        DialogSurface(
            onDismissRequest = onDismissRequest,
            dismissOnClickOutside = properties.dismissOnClickOutside
        ) {
            AlertDialogContent(
                title = title,
                message = content
            ) {
                DialogActions(
                    onConfirm = onDismissRequest,
                    confirmText = confirmText
                )
            }
        }
    }
}

/**
 * Displays a compact decision dialog with balanced confirmation and dismissal actions.
 */
@OptIn(UnstableSaltUiApi::class)
@Composable
fun YesNoDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    properties: DialogProperties = DialogProperties(
        usePlatformDefaultWidth = false
    ),
    title: String,
    content: String,
    drawContent: (@Composable () -> Unit)? = null,
    cancelText: String = stringResourceCancel(),
    confirmText: String = stringResourceConfirm()
) {
    BasicAdaptiveDialog(
        onDismissRequest = onDismissRequest,
        size = AdaptiveDialogSize.Min,
        properties = properties
    ) {
        DialogSurface(
            onDismissRequest = onDismissRequest,
            dismissOnClickOutside = properties.dismissOnClickOutside
        ) {
            AlertDialogContent(
                title = title,
                message = content,
                customContent = drawContent
            ) {
                DialogActions(
                    onConfirm = onConfirm,
                    confirmText = confirmText,
                    onDismiss = onDismissRequest,
                    dismissText = cancelText
                )
            }
        }
    }
}

/**
 * Displays an adaptive text-input dialog with confirmation and dismissal actions.
 */
@UnstableSaltUiApi
@Composable
fun InputDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    properties: DialogProperties = DialogProperties(
        usePlatformDefaultWidth = false
    ),
    title: String,
    text: String,
    onChange: (String) -> Unit,
    hint: String? = null,
    cancelText: String = stringResourceCancel(),
    confirmText: String = stringResourceConfirm()
) {
    BasicAdaptiveDialog(
        onDismissRequest = onDismissRequest,
        size = AdaptiveDialogSize.Standard,
        properties = properties
    ) {
        DialogSurface(
            onDismissRequest = onDismissRequest,
            dismissOnClickOutside = properties.dismissOnClickOutside
        ) {
            val focusRequester = remember { FocusRequester() }
            AlertDialogContent(
                title = title,
                message = "",
                messageVisible = false,
                customContent = {
                    RoundedColumn(
                        paddingValues = PaddingValues(0.dp)
                    ) {
                        ItemEdit(
                            text = text,
                            onChange = onChange,
                            hint = hint,
                            modifier = Modifier.focusRequester(focusRequester)
                        )
                    }
                }
            ) {
                DialogActions(
                    onConfirm = onConfirm,
                    confirmText = confirmText,
                    onDismiss = onDismissRequest,
                    dismissText = cancelText
                )
            }
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    }
}

/**
 * A styled adaptive dialog surface with platform-resolved geometry and content padding.
 */
@OptIn(UnstableSaltUiApi::class)
@Composable
fun BasicDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(
        usePlatformDefaultWidth = false
    ),
    content: @Composable () -> Unit
) {
    BasicAdaptiveDialog(
        onDismissRequest = onDismissRequest,
        size = AdaptiveDialogSize.Standard,
        properties = properties
    ) {
        DialogSurface(
            onDismissRequest = onDismissRequest,
            dismissOnClickOutside = properties.dismissOnClickOutside
        ) {
            Column(
                modifier = Modifier.padding(DialogDefaults.contentPadding)
            ) {
                content()
            }
        }
    }
}

/** Default visual tokens shared by Salt dialogs. */
@Suppress("ConstPropertyName")
object DialogDefaults {
    /** Platform-resolved surface shape for a dialog. */
    val shape: Shape
        get() = platformDialogMetrics().shape

    /** Default padding for arbitrary content hosted by [BasicDialog]. */
    val contentPadding: PaddingValues
        get() = platformDialogMetrics().contentPadding

    /** Platform-resolved control size used by actions and form controls inside dialogs. */
    val controlSize: ControlSize
        get() = platformDialogMetrics().controlSize

    /** Wide, low-opacity shadow that separates a dialog from the background. */
    val ambientShadow: Shadow
        get() = platformDialogMetrics().ambientShadow

    /** Compact shadow that visually anchors the dialog near its surface. */
    val keyShadow: Shadow
        get() = platformDialogMetrics().keyShadow

    /** Elevated surface color used by dialogs. */
    val containerColor: Color
        @Composable
        get() = SaltTheme.colors.popup

    /** Typography for a dialog title, derived from the theme's main text style. */
    val titleTextStyle: TextStyle
        @Composable
        get() {
            val mainStyle = SaltTheme.textStyles.main
            return mainStyle.copy(
                fontWeight = mainStyle.fontWeight
                    ?: platformDialogMetrics().titleFontWeight
            )
        }

    /** Typography for a dialog message, inherited directly from the theme. */
    val messageTextStyle: TextStyle
        @Composable
        get() = SaltTheme.textStyles.main

    /** Subtle platform-resolved surface border, if the platform requires one. */
    val border: BorderStroke?
        @Composable
        get() {
            val width = platformDialogMetrics().borderWidth
            return if (width > 0.dp) {
                BorderStroke(width, SaltTheme.colors.stroke)
            } else {
                null
            }
        }
}

/** A short, theme-derived dialog title. */
@Composable
fun DialogTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = DialogDefaults.titleTextStyle,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

/** Supporting text that explains the purpose or consequence of a dialog. */
@Composable
fun DialogMessage(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = DialogDefaults.messageTextStyle
    )
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun DialogSurface(
    onDismissRequest: () -> Unit,
    dismissOnClickOutside: Boolean,
    content: @Composable () -> Unit
) {
    val shape = DialogDefaults.shape
    val surfaceBorder = DialogDefaults.border
    val metrics = platformDialogMetrics()
    val surfaceBounds = remember { mutableStateOf<Rect?>(null) }
    DisableMaterial {
        ProvideControlSize(DialogDefaults.controlSize) {
            ProvideContentColorTextStyle(
                contentColor = SaltTheme.colors.text,
                textStyle = SaltTheme.textStyles.main
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .thenIf(metrics.requiresFullHeightShadowHost) {
                            fillMaxHeight()
                                .pointerInput(
                                    onDismissRequest,
                                    dismissOnClickOutside
                                ) {
                                    if (!dismissOnClickOutside) return@pointerInput
                                    awaitEachGesture {
                                        val down = awaitFirstDown(requireUnconsumed = false)
                                        val up = waitForUpOrCancellation()
                                        val bounds = surfaceBounds.value
                                        if (bounds != null &&
                                            up != null &&
                                            !bounds.contains(down.position) &&
                                            !bounds.contains(up.position)
                                        ) {
                                            onDismissRequest()
                                        }
                                    }
                                }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                surfaceBounds.value = coordinates.boundsInParent()
                            }
                            .dropShadow(
                                shape = shape,
                                shadow = DialogDefaults.ambientShadow
                            )
                            .dropShadow(
                                shape = shape,
                                shadow = DialogDefaults.keyShadow
                            )
                            .clip(shape)
                            .background(DialogDefaults.containerColor)
                            .thenIf(surfaceBorder != null) {
                                border(requireNotNull(surfaceBorder), shape)
                            }
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertDialogContent(
    title: String,
    message: String,
    messageVisible: Boolean = true,
    customContent: (@Composable () -> Unit)? = null,
    actions: @Composable () -> Unit
) {
    val metrics = platformDialogMetrics()
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(metrics.topPadding))
            DialogTitle(
                text = title,
                modifier = Modifier.padding(horizontal = metrics.textHorizontalPadding)
            )
            if (messageVisible) {
                Spacer(Modifier.height(metrics.titleMessageSpacing))
                DialogMessage(
                    text = message,
                    modifier = Modifier.padding(horizontal = metrics.textHorizontalPadding)
                )
            }
            if (customContent != null) {
                Spacer(Modifier.height(metrics.customContentSpacing))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = metrics.textHorizontalPadding)
                ) {
                    customContent()
                }
            }
        }
        Spacer(Modifier.height(metrics.actionTopSpacing))
        actions()
    }
}

@Composable
private fun DialogActions(
    onConfirm: () -> Unit,
    confirmText: String,
    onDismiss: (() -> Unit)? = null,
    dismissText: String? = null
) {
    val metrics = platformDialogMetrics()
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = metrics.actionHorizontalPadding,
                end = metrics.actionHorizontalPadding,
                bottom = metrics.actionBottomPadding
            )
    ) {
        val hasDismissAction = onDismiss != null && dismissText != null
        val stackActions = hasDismissAction && (
            maxWidth < metrics.minimumHorizontalActionsWidth ||
                fontScale >= metrics.stackedActionsFontScale
        )
        if (stackActions) {
            Column {
                DialogActionButton(
                    onClick = onConfirm,
                    text = confirmText,
                    colors = ButtonDefaults.colors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(metrics.actionSpacing))
                DialogActionButton(
                    onClick = requireNotNull(onDismiss),
                    text = requireNotNull(dismissText),
                    colors = ButtonDefaults.colors(ButtonAppearance.Subtle),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else if (hasDismissAction) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                DialogActionButton(
                    onClick = requireNotNull(onDismiss),
                    text = requireNotNull(dismissText),
                    colors = ButtonDefaults.colors(ButtonAppearance.Subtle),
                    modifier = if (metrics.horizontalActionsFillWidth) {
                        Modifier.weight(1f)
                    } else {
                        Modifier.widthIn(min = metrics.actionMinWidth)
                    }
                )
                Spacer(Modifier.width(metrics.actionSpacing))
                DialogActionButton(
                    onClick = onConfirm,
                    text = confirmText,
                    colors = ButtonDefaults.colors(),
                    modifier = if (metrics.horizontalActionsFillWidth) {
                        Modifier.weight(1f)
                    } else {
                        Modifier.widthIn(min = metrics.actionMinWidth)
                    }
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                DialogActionButton(
                    onClick = onConfirm,
                    text = confirmText,
                    colors = ButtonDefaults.colors(),
                    modifier = if (metrics.horizontalActionsFillWidth) {
                        Modifier.fillMaxWidth()
                    } else {
                        Modifier.widthIn(min = metrics.actionMinWidth)
                    }
                )
            }
        }
    }
}

@Composable
private fun DialogActionButton(
    onClick: () -> Unit,
    text: String,
    colors: ButtonColors,
    modifier: Modifier = Modifier
) {
    val metrics = platformDialogMetrics()
    BasicButton(
        onClick = onClick,
        modifier = modifier,
        size = metrics.controlSize,
        shape = metrics.actionShape,
        colors = colors
    ) {
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
