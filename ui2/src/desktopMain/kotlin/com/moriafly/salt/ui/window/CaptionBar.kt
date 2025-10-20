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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.FontLoadResult
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    iconFontFamily: FontFamily?,
) {
}

@UnstableSaltUiApi
@Composable
internal fun CaptionButtonMaximizeRestore(
    iconFontFamily: FontFamily?,
) {
}

@UnstableSaltUiApi
@Composable
internal fun CaptionButtonClose(
    onClick: () -> Unit,
    iconFontFamily: FontFamily?,
    modifier: Modifier = Modifier
) {
    CaptionButton(
        onClick = onClick,
        iconText = CaptionButtonCloseIconGlyph.toString(),
        iconFontFamily = iconFontFamily,
        modifier = modifier
    )
}

@UnstableSaltUiApi
@Composable
private fun CaptionButton(
    onClick: () -> Unit,
    iconText: String,
    iconFontFamily: FontFamily?,
    modifier: Modifier = Modifier
) {
    val saltWindowProperties = LocalSaltWindowProperties.current
    Box(
        modifier = modifier
            .width(CaptionButtonWidth)
            .height(saltWindowProperties.captionButtonHeight)
            .clickable {
                onClick()
            }
    ) {
        Text(
            text = iconText,
            modifier = Modifier
                .align(Alignment.Center),
            fontSize = 10.sp,
            fontFamily = iconFontFamily
        )
    }
}

private val CaptionButtonWidth = 46.83f.dp

private const val CaptionButtonMinimizeIconGlyph = '\uE921'
private const val CaptionButtonMaximizeIconGlyph = '\uE922'
private const val CaptionButtonRestoreIconGlyph = '\uE923'
private const val CaptionButtonCloseIconGlyph = '\uE8BB'
