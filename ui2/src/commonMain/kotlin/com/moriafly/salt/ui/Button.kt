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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

/**
 * Button component.
 *
 * @param onClick Button click event.
 * @param modifier Modifier.
 * @param enabled Whether the button is enabled.
 * @param backgroundColor Background color.
 * @param contentPadding Button content padding.
 * @param content Button content.
 */
@Composable
fun Button(
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
                onClick = onClick
            )
            .padding(contentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/**
 * Default text content button.
 */
@Deprecated(
    message = "Use Button instead",
    replaceWith = ReplaceWith(
        expression = "Button(onClick = onClick, modifier = modifier, enabled = enabled, " +
            "backgroundColor = backgroundColor) { Text(text = text, color = textColor) }",
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
    @Suppress("DEPRECATION")
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
 * Basic button.
 *
 * No min [SaltDimens.item] height limit.
 */
@Deprecated(
    message = "Use Button instead",
    replaceWith = ReplaceWith(
        expression = "Button(onClick = onClick, modifier = modifier, enabled = enabled, " +
            "backgroundColor = backgroundColor) { content() }",
        imports = arrayOf("com.moriafly.salt.ui.Button", "com.moriafly.salt.ui.SaltTheme")
    )
)
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
            .semantics {
                role = Role.Button
            }
            .clip(SaltTheme.shapes.medium)
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

object ButtonDefaults {
    val ContentPadding: PaddingValues
        @Composable
        get() = PaddingValues(
            horizontal = SaltTheme.dimens.padding,
            vertical = SaltTheme.dimens.padding * 0.5f
        )
}
