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

@file:Suppress("unused", "ktlint:standard:property-naming")

package com.moriafly.salt.ui.platform.linux

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.LocalSaltWindowProperties
import kotlin.math.roundToInt

@UnstableSaltUiApi
@Composable
internal fun LinuxCaptionButtonFullscreen(
    onClick: () -> Unit,
    isFullscreen: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val windowInfo = LocalWindowInfo.current
    CaptionButton(
        onClick = onClick,
        icon = if (isFullscreen) {
            CaptionButtonIcon.BackToWindow
        } else {
            CaptionButtonIcon.Fullscreen
        },
        colors = if (windowInfo.isWindowFocused) {
            if (SaltTheme.configs.isDarkTheme) {
                CaptionButtonColors.MinMaxDark
            } else {
                CaptionButtonColors.MinMaxLight
            }
        } else {
            if (SaltTheme.configs.isDarkTheme) {
                CaptionButtonColors.MinMaxInactiveDark
            } else {
                CaptionButtonColors.MinMaxInactiveLight
            }
        },
        modifier = modifier,
        enabled = enabled
    )
}

@UnstableSaltUiApi
@Composable
internal fun LinuxCaptionButtonMinimize(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val windowInfo = LocalWindowInfo.current
    CaptionButton(
        onClick = onClick,
        icon = CaptionButtonIcon.Minimize,
        colors = if (windowInfo.isWindowFocused) {
            if (SaltTheme.configs.isDarkTheme) {
                CaptionButtonColors.MinMaxDark
            } else {
                CaptionButtonColors.MinMaxLight
            }
        } else {
            if (SaltTheme.configs.isDarkTheme) {
                CaptionButtonColors.MinMaxInactiveDark
            } else {
                CaptionButtonColors.MinMaxInactiveLight
            }
        },
        modifier = modifier,
        enabled = enabled
    )
}

@UnstableSaltUiApi
@Composable
internal fun LinuxCaptionButtonMaximize(
    onClick: () -> Unit,
    maximized: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val windowInfo = LocalWindowInfo.current
    CaptionButton(
        onClick = onClick,
        icon = if (maximized) {
            CaptionButtonIcon.Restore
        } else {
            CaptionButtonIcon.Maximize
        },
        colors = if (windowInfo.isWindowFocused) {
            if (SaltTheme.configs.isDarkTheme) {
                CaptionButtonColors.MinMaxDark
            } else {
                CaptionButtonColors.MinMaxLight
            }
        } else {
            if (SaltTheme.configs.isDarkTheme) {
                CaptionButtonColors.MinMaxInactiveDark
            } else {
                CaptionButtonColors.MinMaxInactiveLight
            }
        },
        modifier = modifier,
        enabled = enabled
    )
}

@UnstableSaltUiApi
@Composable
internal fun LinuxCaptionButtonClose(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val windowInfo = LocalWindowInfo.current
    CaptionButton(
        onClick = onClick,
        icon = CaptionButtonIcon.Close,
        colors = if (windowInfo.isWindowFocused) {
            if (SaltTheme.configs.isDarkTheme) {
                CaptionButtonColors.CloseDark
            } else {
                CaptionButtonColors.CloseLight
            }
        } else {
            if (SaltTheme.configs.isDarkTheme) {
                CaptionButtonColors.CloseInactiveDark
            } else {
                CaptionButtonColors.CloseInactiveLight
            }
        },
        modifier = modifier,
        enabled = enabled
    )
}

@UnstableSaltUiApi
@Composable
private fun CaptionButton(
    onClick: () -> Unit,
    icon: CaptionButtonIcon,
    colors: CaptionButtonColors,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val saltWindowProperties = LocalSaltWindowProperties.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor = when {
        isPressed -> colors.pressedBackground
        isHovered -> colors.hoverBackground
        else -> Color.Unspecified
    }

    Box(
        modifier = modifier
            .width(LinuxCaptionButtonWidth)
            .height(saltWindowProperties.captionButtonHeight)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                onClick()
            }
    ) {
        val color = when {
            !enabled -> colors.disabled
            isPressed -> colors.pressed
            isHovered -> colors.hover
            else -> colors.rest
        }
        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .size(CaptionButtonIconSize)
        ) {
            drawCaptionButtonIcon(icon = icon, color = color)
        }
    }
}

private fun DrawScope.drawCaptionButtonIcon(
    icon: CaptionButtonIcon,
    color: Color
) {
    val strokeWidth = CaptionButtonIconStrokeWidth.toPx().roundToInt().coerceAtLeast(1).toFloat()
    val stroke = Stroke(width = strokeWidth)
    fun Float.alignToStroke(): Float =
        (this - strokeWidth / 2f).roundToInt() + strokeWidth / 2f

    val edge = CaptionButtonIconEdge.toPx().alignToStroke()
    val cornerLength = CaptionButtonFullscreenCornerLength.toPx()
    val restoreOffset = CaptionButtonRestoreOffset.toPx()
    val left = edge
    val top = edge
    val right = (size.width - CaptionButtonIconEdge.toPx()).alignToStroke()
    val bottom = (size.height - CaptionButtonIconEdge.toPx()).alignToStroke()

    when (icon) {
        CaptionButtonIcon.Fullscreen -> {
            drawLine(color, Offset(left, top), Offset(left + cornerLength, top), strokeWidth)
            drawLine(color, Offset(left, top), Offset(left, top + cornerLength), strokeWidth)
            drawLine(color, Offset(right - cornerLength, top), Offset(right, top), strokeWidth)
            drawLine(color, Offset(right, top), Offset(right, top + cornerLength), strokeWidth)
            drawLine(color, Offset(left, bottom - cornerLength), Offset(left, bottom), strokeWidth)
            drawLine(color, Offset(left, bottom), Offset(left + cornerLength, bottom), strokeWidth)
            drawLine(color, Offset(right, bottom - cornerLength), Offset(right, bottom), strokeWidth)
            drawLine(color, Offset(right - cornerLength, bottom), Offset(right, bottom), strokeWidth)
        }

        CaptionButtonIcon.BackToWindow,
        CaptionButtonIcon.Restore -> {
            val frontLeft = left
            val frontTop = (top + restoreOffset).alignToStroke()
            val frontRight = (right - restoreOffset).alignToStroke()
            val frontBottom = bottom
            val backLeft = (left + restoreOffset).alignToStroke()
            val backTop = top
            val backRight = right
            val backBottom = (bottom - restoreOffset).alignToStroke()

            drawLine(color, Offset(backLeft, backTop), Offset(backRight, backTop), strokeWidth)
            drawLine(color, Offset(backRight, backTop), Offset(backRight, backBottom), strokeWidth)
            drawLine(color, Offset(backLeft, backTop), Offset(backLeft, frontTop), strokeWidth)
            drawRect(
                color = color,
                topLeft = Offset(frontLeft, frontTop),
                size = Size(frontRight - frontLeft, frontBottom - frontTop),
                style = stroke
            )
        }

        CaptionButtonIcon.Minimize -> drawLine(
            color = color,
            start = Offset(0f, center.y.alignToStroke()),
            end = Offset(size.width, center.y.alignToStroke()),
            strokeWidth = strokeWidth
        )

        CaptionButtonIcon.Maximize -> drawRect(
            color = color,
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top),
            style = stroke
        )

        CaptionButtonIcon.Close -> {
            drawLine(color, Offset(left, top), Offset(right, bottom), strokeWidth)
            drawLine(color, Offset(right, top), Offset(left, bottom), strokeWidth)
        }
    }
}

private enum class CaptionButtonIcon {
    Fullscreen,
    BackToWindow,
    Minimize,
    Maximize,
    Restore,
    Close
}

private class CaptionButtonColors(
    val rest: Color,
    val hover: Color,
    val hoverBackground: Color,
    val pressed: Color,
    val pressedBackground: Color,
    val disabled: Color
) {
    companion object {
        private val TextLightDisabled = Color.Black.copy(alpha = 0.3614f)
        private val TextDarkDisabled = Color.White.copy(alpha = 0.3628f)

        val MinMaxLight = CaptionButtonColors(
            rest = Color.Black.copy(alpha = 0.8956f),
            hover = Color.Black.copy(alpha = 0.8956f),
            hoverBackground = Color.Black.copy(alpha = 0.0373f),
            pressed = Color.Black.copy(alpha = 0.6063f),
            pressedBackground = Color.Black.copy(alpha = 0.0214f),
            disabled = TextLightDisabled
        )

        val MinMaxDark = CaptionButtonColors(
            rest = Color.White,
            hover = Color.White,
            hoverBackground = Color.White.copy(alpha = 0.0605f),
            pressed = Color.White.copy(alpha = 0.7860f),
            pressedBackground = Color.White.copy(alpha = 0.0419f),
            disabled = TextDarkDisabled
        )

        val MinMaxInactiveLight = CaptionButtonColors(
            rest = TextLightDisabled,
            hover = Color.Black.copy(alpha = 0.8956f),
            hoverBackground = Color.Black.copy(alpha = 0.0373f),
            pressed = Color.Black.copy(alpha = 0.4458f),
            pressedBackground = Color.Black.copy(alpha = 0.0214f),
            disabled = TextLightDisabled
        )

        val MinMaxInactiveDark = CaptionButtonColors(
            rest = Color.White.copy(alpha = 0.3628f),
            hover = Color.White,
            hoverBackground = Color.White.copy(alpha = 0.0605f),
            pressed = Color.White.copy(alpha = 0.5442f),
            pressedBackground = Color.White.copy(alpha = 0.0419f),
            disabled = TextDarkDisabled
        )

        val CloseLight = CaptionButtonColors(
            rest = Color.Black.copy(alpha = 0.8956f),
            hover = Color.White,
            hoverBackground = Color(0xFFC42B1C),
            pressed = Color.White.copy(alpha = 0.7f),
            pressedBackground = Color(0xFFC42B1C).copy(alpha = 0.9f),
            disabled = TextLightDisabled
        )

        val CloseDark = CaptionButtonColors(
            rest = Color.White,
            hover = Color.White,
            hoverBackground = Color(0xFFC42B1C),
            pressed = Color.White.copy(alpha = 0.7f),
            pressedBackground = Color(0xFFC42B1C).copy(alpha = 0.9f),
            disabled = TextDarkDisabled
        )

        val CloseInactiveLight = CaptionButtonColors(
            rest = TextLightDisabled,
            hover = Color.White,
            hoverBackground = Color(0xFFC42B1C),
            pressed = Color.White.copy(alpha = 0.7f),
            pressedBackground = Color(0xFFC42B1C).copy(alpha = 0.9f),
            disabled = TextLightDisabled
        )

        val CloseInactiveDark = CaptionButtonColors(
            rest = Color.White.copy(alpha = 0.3628f),
            hover = Color.White,
            hoverBackground = Color(0xFFC42B1C),
            pressed = Color.White.copy(alpha = 0.7f),
            pressedBackground = Color(0xFFC42B1C).copy(alpha = 0.9f),
            disabled = TextDarkDisabled
        )
    }
}

internal val LinuxCaptionButtonWidth = 46.83f.dp
private val CaptionButtonIconSize = 14.dp
private val CaptionButtonIconStrokeWidth = 1.dp
private val CaptionButtonIconEdge = 2.5f.dp
private val CaptionButtonFullscreenCornerLength = 3.dp
private val CaptionButtonRestoreOffset = 2.5f.dp
