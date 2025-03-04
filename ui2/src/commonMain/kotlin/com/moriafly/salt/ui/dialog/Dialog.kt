/**
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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.moriafly.salt.ui.ItemOuterEdit
import com.moriafly.salt.ui.ItemOuterTip
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.TextButton
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.internal.stringResourceCancel
import com.moriafly.salt.ui.internal.stringResourceConfirm
import com.moriafly.salt.ui.outerPadding

/**
 * Yes Dialog.
 */
@Composable
fun YesDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    title: String,
    content: String,
    confirmText: String = stringResourceConfirm().uppercase()
) {
    BasicDialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        DialogTitle(text = title)
        ItemOuterTip(text = content)
        TextButton(
            onClick = {
                onDismissRequest()
            },
            modifier = Modifier
                .fillMaxWidth()
                .outerPadding(),
            text = confirmText
        )
    }
}

/**
 * Yes or No Dialog.
 */
@Composable
fun YesNoDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    title: String,
    content: String,
    drawContent: (@Composable () -> Unit)? = null,
    cancelText: String = stringResourceCancel().uppercase(),
    confirmText: String = stringResourceConfirm().uppercase()
) {
    BasicDialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        DialogTitle(text = title)
        ItemOuterTip(text = content)
        drawContent?.invoke()
        Row(
            modifier = Modifier.outerPadding()
        ) {
            TextButton(
                onClick = {
                    onDismissRequest()
                },
                modifier = Modifier
                    .weight(1f),
                text = cancelText,
                textColor = SaltTheme.colors.subText,
                backgroundColor = SaltTheme.colors.subBackground
            )
            Spacer(modifier = Modifier.width(SaltTheme.dimens.padding))
            TextButton(
                onClick = {
                    onConfirm()
                },
                modifier = Modifier
                    .weight(1f),
                text = confirmText
            )
        }
    }
}

/**
 * Input Dialog.
 */
@UnstableSaltUiApi
@Composable
fun InputDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    title: String,
    text: String,
    onChange: (String) -> Unit,
    hint: String? = null,
    cancelText: String = stringResourceCancel().uppercase(),
    confirmText: String = stringResourceConfirm().uppercase()
) {
    BasicDialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        DialogTitle(text = title)

        val focusRequester = remember { FocusRequester() }
        ItemOuterEdit(
            text = text,
            onChange = onChange,
            hint = hint,
            modifier = Modifier
                .focusRequester(focusRequester)
        )
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Row(
            modifier = Modifier.outerPadding()
        ) {
            TextButton(
                onClick = {
                    onDismissRequest()
                },
                modifier = Modifier
                    .weight(1f),
                text = cancelText,
                textColor = SaltTheme.colors.subText,
                backgroundColor = SaltTheme.colors.subBackground
            )
            Spacer(modifier = Modifier.width(SaltTheme.dimens.padding))
            TextButton(
                onClick = {
                    onConfirm()
                },
                modifier = Modifier
                    .weight(1f),
                text = confirmText
            )
        }
    }
}

/**
 * The basic dialog has default corner background and vertical padding.
 */
@Composable
fun BasicDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SaltTheme.shapes.large)
                .background(color = SaltTheme.colors.background)
                .outerPadding(horizontal = false)
        ) {
            content()
        }
    }
}

@Composable
fun DialogTitle(
    text: String
) {
    Text(
        text = text,
        modifier = Modifier
            .outerPadding(),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}
