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

package com.moriafly.salt.ui.dialog

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.moriafly.salt.ui.ControlSize

@Immutable
internal data class DialogMetrics(
    val shape: Shape,
    val actionShape: Shape,
    val controlSize: ControlSize,
    val contentPadding: PaddingValues,
    val textHorizontalPadding: Dp,
    val topPadding: Dp,
    val titleMessageSpacing: Dp,
    val customContentSpacing: Dp,
    val actionTopSpacing: Dp,
    val actionHorizontalPadding: Dp,
    val actionBottomPadding: Dp,
    val actionSpacing: Dp,
    val horizontalActionsFillWidth: Boolean,
    val actionMinWidth: Dp,
    val minimumHorizontalActionsWidth: Dp,
    val stackedActionsFontScale: Float,
    val ambientShadow: Shadow,
    val keyShadow: Shadow,
    val requiresFullHeightShadowHost: Boolean,
    val borderWidth: Dp,
    val titleFontWeight: FontWeight
)

/** Smoothing calibrated against Salt's concentric dialog reference geometry. */
internal const val SaltContinuousCornerSmoothing = 0.65f

internal expect fun platformDialogMetrics(): DialogMetrics
