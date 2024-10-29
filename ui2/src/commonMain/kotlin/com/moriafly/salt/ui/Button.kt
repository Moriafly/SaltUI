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

@file:Suppress("UNUSED")

package com.moriafly.salt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

/**
 * Default text content button
 */
@Composable
fun TextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textColor: Color = Color.White,
    backgroundColor: Color = SaltTheme.colors.highlight
) {
    BasicButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        backgroundColor = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth(),
            color = textColor,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

/**
 * Basic button
 *
 * No min [SaltDimens.item] height limit
 */
@Composable
fun BasicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = SaltTheme.colors.highlight,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .semantics(true) {
                role = Role.Button
            }
            .clip(RoundedCornerShape(SaltTheme.dimens.corner))
            .background(color = backgroundColor)
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .innerPadding(),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}