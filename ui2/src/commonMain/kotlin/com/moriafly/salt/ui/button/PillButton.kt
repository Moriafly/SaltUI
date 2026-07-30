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

package com.moriafly.salt.ui.button

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.enabledAlpha
import com.moriafly.salt.ui.material.subMaterial
import com.moriafly.salt.ui.thenIf

/**
 * A button that can be rendered as either a perfect circle or a pill (capsule) shape.
 *
 * - **Circle button**: When [text] is `null`, the button has a fixed width and height of
 *   [PillButtonDefaults.Height], rendering as a perfect circle containing only the [icon].
 * - **Pill button**: When [text] is provided, the button expands horizontally into a capsule
 *   shape with the [icon] on the leading side and [text] on the trailing side.
 *
 * @param onClick Called when the button is clicked.
 * @param modifier [Modifier] to be applied to the button.
 * @param enabled Whether the button is enabled and responds to click events.
 * @param text Optional text content displayed to the right of the [icon].
 * When provided, the button becomes a pill shape; when `null`, it is a perfect circle.
 * @param icon Icon content displayed inside the button.
 */
@UnstableSaltUiApi
@Composable
fun PillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: (@Composable () -> Unit)? = null,
    icon: @Composable () -> Unit
) {
    BasicPillButton(
        modifier = modifier
            .run {
                if (text != null) {
                    height(PillButtonDefaults.Height)
                } else {
                    size(PillButtonDefaults.Height)
                }
            }
            .clip(CircleShape)
            .subMaterial(fallback = SaltTheme.colors.subBackground)
            .border(Dp.Hairline, SaltTheme.colors.stroke, CircleShape)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .thenIf(text != null) {
                padding(horizontal = PillButtonDefaults.HorizontalPadding)
            }
            .enabledAlpha(enabled),
        text = text,
        icon = icon
    )
}

/**
 * Low-level layout for pill/circle buttons.
 *
 * Contains only the content arrangement logic without any background, border, or click handling.
 * For a fully styled button, use [PillButton].
 *
 * @param modifier [Modifier] to be applied to the container.
 * @param text Optional text content displayed to the right of the [icon].
 * @param icon Icon content displayed inside the button.
 */
@UnstableSaltUiApi
@Composable
fun BasicPillButton(
    modifier: Modifier = Modifier,
    text: (@Composable () -> Unit)? = null,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(PillButtonDefaults.IconSize),
            contentAlignment = Alignment.Center,
            propagateMinConstraints = true
        ) {
            icon()
        }

        if (text != null) {
            Spacer(Modifier.width(4.dp))
            text()
        }
    }
}

/**
 * Default values used by [PillButton] and [BasicPillButton].
 */
@UnstableSaltUiApi
object PillButtonDefaults {
    /**
     * The recommended icon size for content inside a [PillButton].
     */
    internal val IconSize: Dp = if (OS.isDesktop()) 18.dp else 22.dp

    /**
     * The default height of a [PillButton].
     */
    internal val Height: Dp = if (OS.isDesktop()) 36.dp else 48.dp

    /**
     * The default horizontal content padding for a pill-shaped [PillButton].
     */
    internal val HorizontalPadding: Dp = if (OS.isDesktop()) 10.dp else 16.dp
}
