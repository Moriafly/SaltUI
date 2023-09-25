@file:Suppress("UNUSED")

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

package com.moriafly.salt.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.moriafly.salt.ui.Dimens
import com.moriafly.salt.ui.ItemSpacer
import com.moriafly.salt.ui.ItemText
import com.moriafly.salt.ui.R
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TextButton
import com.moriafly.salt.ui.UnstableSaltApi

@UnstableSaltApi
@Composable
fun BottomSheetDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    BottomSheetDialog(
        onDismissRequest = onDismissRequest,
        properties = BottomSheetDialogProperties(
            enableEdgeToEdge = true,
            behaviorProperties = BottomSheetBehaviorProperties(
                state = BottomSheetBehaviorProperties.State.HalfExpanded,
                skipCollapsed = true,
                halfExpandedRatio = 0.66f
            )
        ),
        content = {
            Column(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(color = SaltTheme.colors.background)
            ) {
                content()
            }
        }
    )
}

/**
 * YesDialog
 */
@UnstableSaltApi
@Composable
fun YesDialog(
    onDismissRequest: () -> Unit,
    title: String,
    content: String
) {
    BasicDialog(
        onDismissRequest = onDismissRequest
    ) {
        DialogTitle(text = title)
        ItemSpacer()
        ItemText(text = content)
        Spacer(modifier = Modifier.height(Dimens.outerVerticalPadding * 2))
        TextButton(
            onClick = {
                onDismissRequest()
            },
            modifier = Modifier
                .padding(horizontal = Dimens.outerHorizontalPadding),
            text = stringResource(id = R.string.confirm).uppercase()
        )
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
    title: String,
    content: String
) {
    BasicDialog(
        onDismissRequest = onDismissRequest
    ) {
        DialogTitle(text = title)
        ItemSpacer()
        ItemText(text = content)
        Spacer(modifier = Modifier.height(Dimens.outerVerticalPadding * 2))
        Row(
            modifier = Modifier.padding(horizontal = Dimens.outerHorizontalPadding)
        ) {
            TextButton(
                onClick = {
                    onDismissRequest()
                },
                modifier = Modifier
                    .weight(1f),
                text = stringResource(id = R.string.cancel).uppercase(),
                textColor = SaltTheme.colors.subText,
                backgroundColor = Color.Transparent
            )
            Spacer(modifier = Modifier.width(Dimens.contentPadding))
            TextButton(
                onClick = {
                    onConfirm()
                },
                modifier = Modifier
                    .weight(1f),
                text = stringResource(id = R.string.confirm).uppercase()
            )
        }
    }
}

/**
 * The basic dialog has default corner background and vertical padding
 */
@Composable
private fun BasicDialog(
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
                .clip(RoundedCornerShape(DialogCorner))
                .background(color = SaltTheme.colors.background)
                .padding(vertical = Dimens.outerVerticalPadding * 2)
            // .background(Color.Blue)
        ) {
            content()
        }
    }
}

@Composable
private fun DialogTitle(
    text: String
) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = Dimens.outerHorizontalPadding),
        color = SaltTheme.colors.text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

private val DialogCorner = 20.dp