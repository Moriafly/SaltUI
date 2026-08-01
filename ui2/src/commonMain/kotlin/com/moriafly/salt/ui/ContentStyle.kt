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

package com.moriafly.salt.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * The preferred color for content at the current position in the composition hierarchy.
 *
 * Components that draw typography or iconography should use this value by default. [SaltTheme]
 * provides [SaltColors.text] at the root, while surfaces and controls provide their corresponding
 * content color for their children.
 */
val LocalContentColor = compositionLocalOf { Color.Black }

/** The preferred [TextStyle] for text at the current position in the hierarchy. */
val LocalTextStyle = compositionLocalOf(structuralEqualityPolicy()) { TextStyle.Default }

/**
 * Provides [value] as the preferred text style, merged with the current [LocalTextStyle].
 */
@Composable
fun ProvideTextStyle(
    value: TextStyle,
    content: @Composable () -> Unit
) {
    val mergedStyle = LocalTextStyle.current.merge(value)
    CompositionLocalProvider(
        LocalTextStyle provides mergedStyle,
        content = content
    )
}

@Composable
internal fun ProvideContentColorTextStyle(
    contentColor: Color,
    textStyle: TextStyle,
    content: @Composable () -> Unit
) {
    val mergedStyle = LocalTextStyle.current.merge(textStyle)
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalTextStyle provides mergedStyle,
        content = content
    )
}
