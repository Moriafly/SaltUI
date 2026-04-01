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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.LocalSaltWindowProperties

/**
 * Character glyph rendering may have compatibility issues.
 *
 * TODO Temporary solution; this should later be refactored to use an icon for display.
 */
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
        iconGlyph = if (isFullscreen) {
            CaptionButtonBackToWindowIconGlyph
        } else {
            CaptionButtonFullscreenIconGlyph
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
        iconGlyph = CaptionButtonMinimizeIconGlyph,
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
        iconGlyph = if (maximized) {
            CaptionButtonRestoreIconGlyph
        } else {
            CaptionButtonMaximizeIconGlyph
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
        iconGlyph = CaptionButtonCloseIconGlyph,
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
    iconGlyph: Char,
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
        Text(
            text = iconGlyph.toString(),
            modifier = Modifier
                .align(Alignment.Center),
            color = color,
            fontSize = 16.sp,
        )
    }
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

private const val CaptionButtonFullscreenIconGlyph = '\u2610'
private const val CaptionButtonMinimizeIconGlyph = '\u2014'

// TODO Temporarily reusing the maximize icon glyph for "back to window".
private const val CaptionButtonBackToWindowIconGlyph = '\u25A1'
private const val CaptionButtonMaximizeIconGlyph = '\u25A1'
private const val CaptionButtonRestoreIconGlyph = '\u29C9'
private const val CaptionButtonCloseIconGlyph = '\u2715'
