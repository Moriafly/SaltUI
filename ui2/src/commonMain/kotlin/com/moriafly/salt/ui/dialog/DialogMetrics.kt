/*
 * Salt UI
 * Copyright (C) 2026 Moriafly
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
