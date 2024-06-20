/**
 * SaltUI
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

@file:Suppress("UNUSED")

package com.moriafly.salt.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.moriafly.salt.ui.ItemEdit
import com.moriafly.salt.ui.ItemOutHalfSpacer
import com.moriafly.salt.ui.ItemOutSpacer
import com.moriafly.salt.ui.ItemText
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.TextButton
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.internal.stringResourceCancel
import com.moriafly.salt.ui.internal.stringResourceConfirm

/**
 * YesDialog
 */
@UnstableSaltApi
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
        ItemOutSpacer()
        DialogTitle(text = title)
        ItemOutSpacer()
        ItemText(text = content)
        ItemOutSpacer()
        TextButton(
            onClick = {
                onDismissRequest()
            },
            modifier = Modifier
                .padding(horizontal = SaltTheme.dimens.outerHorizontalPadding),
            text = confirmText
        )
        ItemOutSpacer()
    }
}

/**
 * YesNoDialog
 */
@UnstableSaltApi
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
        ItemOutSpacer()
        DialogTitle(text = title)
        ItemOutSpacer()
        ItemText(text = content)
        ItemOutHalfSpacer()
        drawContent?.invoke()
        ItemOutHalfSpacer()
        Row(
            modifier = Modifier.padding(horizontal = SaltTheme.dimens.outerHorizontalPadding)
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
            Spacer(modifier = Modifier.width(SaltTheme.dimens.outerHorizontalPadding))
            TextButton(
                onClick = {
                    onConfirm()
                },
                modifier = Modifier
                    .weight(1f),
                text = confirmText
            )
        }
        ItemOutSpacer()
    }
}

/**
 * Input text dialog
 */
@UnstableSaltApi
@Composable
fun InputDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    title: String,
    text: String,
    onChange: (String) -> Unit,
    hint: String? = null
) {
    BasicDialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        ItemOutSpacer()
        DialogTitle(text = title)
        ItemOutHalfSpacer()

        val focusRequester = remember { FocusRequester() }
        ItemEdit(
            text = text,
            onChange = onChange,
            hint = hint,
            modifier = Modifier
                .focusRequester(focusRequester),
            paddingValues = PaddingValues(horizontal = SaltTheme.dimens.outerHorizontalPadding, vertical = SaltTheme.dimens.outerVerticalPadding)
        )
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        ItemOutHalfSpacer()
        Row(
            modifier = Modifier.padding(horizontal = SaltTheme.dimens.outerHorizontalPadding)
        ) {
            TextButton(
                onClick = {
                    onDismissRequest()
                },
                modifier = Modifier
                    .weight(1f),
                text = stringResourceCancel().uppercase(),
                textColor = SaltTheme.colors.subText,
                backgroundColor = SaltTheme.colors.subBackground
            )
            Spacer(modifier = Modifier.width(SaltTheme.dimens.contentPadding))
            TextButton(
                onClick = {
                    onConfirm()
                },
                modifier = Modifier
                    .weight(1f),
                text = stringResourceConfirm().uppercase()
            )
        }
        ItemOutSpacer()
    }
}

/**
 * The basic dialog has default corner background and vertical padding
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
                .clip(RoundedCornerShape(SaltTheme.dimens.dialogCorner))
                .background(color = SaltTheme.colors.background)
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
        modifier = Modifier.padding(horizontal = SaltTheme.dimens.outerHorizontalPadding),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}