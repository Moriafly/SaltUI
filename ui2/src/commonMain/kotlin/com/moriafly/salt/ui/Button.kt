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

@file:Suppress("unused")

package com.moriafly.salt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun Button(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    type: ButtonType = ButtonType.Highlight,
    maxLines: Int = Int.MAX_VALUE
) {
    BasicButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        backgroundColor = when (type) {
            ButtonType.Highlight -> SaltTheme.colors.highlight
            ButtonType.Sub -> SaltTheme.colors.subBackground
        }
    ) {
        Text(
            text = text,
            color = when (type) {
                ButtonType.Highlight -> SaltTheme.colors.onHighlight
                ButtonType.Sub -> SaltTheme.colors.subText
            },
            maxLines = maxLines
        )
    }
}

enum class ButtonType {
    Highlight,
    Sub
}

/**
 * Default text content button.
 */
@Deprecated(
    message = "Use Button instead",
    replaceWith = ReplaceWith(
        expression = "Button(onClick = onClick, text = text, modifier = modifier, " +
            "enabled = enabled)",
        imports = arrayOf("com.moriafly.salt.ui.Button", "com.moriafly.salt.ui.SaltTheme")
    )
)
@Composable
fun TextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textColor: Color = SaltTheme.colors.onHighlight,
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
            color = textColor,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

/**
 * Basic Button component.
 *
 * @param onClick Button click event.
 * @param modifier Modifier.
 * @param enabled Whether the button is enabled.
 * @param backgroundColor Background color.
 * @param contentPadding Button content padding.
 * @param content Button content.
 */
@Composable
fun BasicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = SaltTheme.colors.highlight,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .clip(SaltTheme.shapes.medium)
            .background(color = backgroundColor)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(contentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

expect object ButtonDefaults {
    val ContentPadding: PaddingValues
}
