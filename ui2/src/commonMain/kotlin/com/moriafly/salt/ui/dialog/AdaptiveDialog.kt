/*
 * Salt UI
 * Copyright (C) 2026 Moriafly
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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * A low-level dialog that adapts its maximum content size to the available window space.
 *
 * The content is constrained to the maximum width specified by [size] and to the window size
 * minus 16 dp on each side. This component does not provide a background, shape, or padding;
 * callers are responsible for styling [content].
 *
 * @param onDismissRequest Called when the user requests to dismiss the dialog, such as by
 * pressing the back button or clicking outside the dialog.
 * @param size The maximum width preset used to constrain the dialog content.
 * @param properties The platform-specific properties used to configure the dialog.
 * @param content The content displayed inside the dialog.
 */
@UnstableSaltUiApi
@Composable
fun BasicAdaptiveDialog(
    onDismissRequest: () -> Unit,
    size: AdaptiveDialogSize = AdaptiveDialogSize.Standard,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        val windowInfo = LocalWindowInfo.current
        val windowWidth = windowInfo.containerDpSize.width
        val windowHeight = windowInfo.containerDpSize.height

        Box(
            modifier = Modifier
                .sizeIn(
                    maxWidth = (windowWidth - 32.dp)
                        .coerceAtMost(size.maxWidth),
                    maxHeight = windowHeight - 32.dp
                )
        ) {
            content()
        }
    }
}

/**
 * Predefined maximum widths for content displayed by [BasicAdaptiveDialog].
 *
 * @property maxWidth The maximum width of the dialog content when enough window space is
 * available.
 */
@UnstableSaltUiApi
enum class AdaptiveDialogSize(
    val maxWidth: Dp
) {
    /** A compact dialog with a maximum width of 320 dp. */
    Min(320.dp),

    /** A standard dialog with a maximum width of 448 dp. */
    Standard(448.dp),

    /** A large dialog with a maximum width of 540 dp. */
    Max(540.dp)
}
