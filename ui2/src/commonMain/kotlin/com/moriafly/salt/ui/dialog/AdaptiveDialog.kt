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

@UnstableSaltUiApi
enum class AdaptiveDialogSize(
    val maxWidth: Dp
) {
    Min(320.dp),
    Standard(448.dp),
    Max(540.dp)
}
