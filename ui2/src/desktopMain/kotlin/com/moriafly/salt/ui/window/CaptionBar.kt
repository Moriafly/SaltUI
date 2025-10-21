/*
 * Salt UI
 * Copyright (C) 2025 Moriafly
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

package com.moriafly.salt.ui.window

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.FontLoadResult
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * The CaptionBarHitTest is a crucial component. It should be placed between the content and the
 * clickable components in the title bar. This is particularly useful in certain scenarios, as it
 * allows for both correctly responding to the clickable components in the title bar and handling
 * the drag events of the title bar.
 */
@UnstableSaltUiApi
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CaptionBarHitTest(
    modifier: Modifier = Modifier
) {
    val saltWindowProperties = LocalSaltWindowProperties.current
    val isHitTestInCaptionBarState = LocalIsHitTestInCaptionBarState.current
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(saltWindowProperties.captionBarHeight)
            .onPointerEvent(PointerEventType.Enter) {
                isHitTestInCaptionBarState.value = true
            }
            .onPointerEvent(PointerEventType.Exit) {
                isHitTestInCaptionBarState.value = false
            }
    )
}

@OptIn(ExperimentalTextApi::class)
@Composable
internal fun rememberFontIconFamily(): State<FontFamily?> {
    val fontIconFamily = remember { mutableStateOf<FontFamily?>(null) }
    // Get windows system font icon, if get failed fall back to fluent svg icon
    val fontFamilyResolver = LocalFontFamilyResolver.current
    LaunchedEffect(fontFamilyResolver) {
        @Suppress("SpellCheckingInspection")
        fontIconFamily.value = sequenceOf("Segoe Fluent Icons", "Segoe MDL2 Assets")
            .mapNotNull {
                val fontFamily = FontFamily(it)
                runCatching {
                    val result = fontFamilyResolver.resolve(fontFamily).value as FontLoadResult
                    if (result.typeface == null || result.typeface?.familyName != it) {
                        null
                    } else {
                        fontFamily
                    }
                }.getOrNull()
            }
            .firstOrNull()
    }
    return fontIconFamily
}

@UnstableSaltUiApi
@Composable
internal fun CaptionButtonMinimize(
    onClick: () -> Unit,
    iconFontFamily: FontFamily?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val windowInfo = LocalWindowInfo.current
    CaptionButton(
        onClick = onClick,
        iconGlyph = CaptionButtonMinimizeIconGlyph,
        iconFontFamily = iconFontFamily,
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
internal fun CaptionButtonMaximize(
    onClick: () -> Unit,
    iconFontFamily: FontFamily?,
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
        iconFontFamily = iconFontFamily,
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
internal fun CaptionButtonClose(
    onClick: () -> Unit,
    iconFontFamily: FontFamily?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val windowInfo = LocalWindowInfo.current
    CaptionButton(
        onClick = onClick,
        iconGlyph = CaptionButtonCloseIconGlyph,
        iconFontFamily = iconFontFamily,
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
    iconFontFamily: FontFamily?,
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
            .width(CaptionButtonWidth)
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
            fontSize = 10.sp,
            fontFamily = iconFontFamily
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
            disabled = TextLightDisabled
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
            disabled = TextLightDisabled
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
            disabled = TextLightDisabled
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
            disabled = TextLightDisabled
        )
    }
}

private val CaptionButtonWidth = 46.83f.dp

private const val CaptionButtonMinimizeIconGlyph = '\uE921'
private const val CaptionButtonMaximizeIconGlyph = '\uE922'
private const val CaptionButtonRestoreIconGlyph = '\uE923'
private const val CaptionButtonCloseIconGlyph = '\uE8BB'
