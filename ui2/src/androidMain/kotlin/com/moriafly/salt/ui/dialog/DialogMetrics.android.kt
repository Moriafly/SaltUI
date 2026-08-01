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

@file:Suppress("ktlint:standard:filename")

package com.moriafly.salt.ui.dialog

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.ButtonDefaults
import com.moriafly.salt.ui.ControlSize
import com.moriafly.salt.ui.internal.SmoothRoundedRectangleShape

private val AndroidActionEdgeInset = 16.dp
private val AndroidDialogCornerRadius =
    ButtonDefaults.containerHeight(ControlSize.Regular) / 2 + AndroidActionEdgeInset - 2.dp

private val AndroidDialogMetrics = DialogMetrics(
    shape = SmoothRoundedRectangleShape(
        radius = AndroidDialogCornerRadius,
        smoothing = SaltContinuousCornerSmoothing
    ),
    actionShape = CircleShape,
    controlSize = ControlSize.Regular,
    contentPadding = PaddingValues(24.dp),
    textHorizontalPadding = 24.dp,
    topPadding = 24.dp,
    titleMessageSpacing = 12.dp,
    customContentSpacing = 16.dp,
    actionTopSpacing = 24.dp,
    actionHorizontalPadding = AndroidActionEdgeInset,
    actionBottomPadding = AndroidActionEdgeInset,
    actionSpacing = 12.dp,
    horizontalActionsFillWidth = true,
    actionMinWidth = 0.dp,
    minimumHorizontalActionsWidth = 248.dp,
    stackedActionsFontScale = 1.3f,
    ambientShadow = Shadow(
        radius = 64.dp,
        color = Color(0x13000000),
        offset = DpOffset(0.dp, 32.dp)
    ),
    keyShadow = Shadow(
        radius = 21.dp,
        color = Color(0x0E000000),
        offset = DpOffset(0.dp, 2.dp)
    ),
    requiresFullHeightShadowHost = true,
    borderWidth = 0.dp,
    titleFontWeight = FontWeight.SemiBold
)

internal actual fun platformDialogMetrics(): DialogMetrics = AndroidDialogMetrics
