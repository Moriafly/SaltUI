/*
 * SaltUI
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

package com.moriafly.salt.ui.popup

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.CardDefaults
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.enabledAlpha
import com.moriafly.salt.ui.internal.SmoothRoundedRectangleShape

internal sealed interface MenuSelection {
    val marked: Boolean

    class Radio(
        override val marked: Boolean
    ) : MenuSelection

    class Toggle(
        override val marked: Boolean
    ) : MenuSelection
}

@OptIn(UnstableSaltUiApi::class)
@Composable
internal fun MenuRow(
    enabled: Boolean,
    desktop: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selection: MenuSelection? = null,
    content: @Composable RowScope.() -> Unit
) {
    val shape = menuRowShape(desktop)
    val indication = LocalIndication.current
    val activation = when (selection) {
        is MenuSelection.Radio -> Modifier.selectable(
            selected = selection.marked,
            interactionSource = null,
            indication = indication,
            enabled = enabled,
            role = Role.RadioButton,
            onClick = onClick
        )

        is MenuSelection.Toggle -> Modifier.toggleable(
            value = selection.marked,
            interactionSource = null,
            indication = indication,
            enabled = enabled,
            role = Role.Checkbox,
            onValueChange = { onClick() }
        )

        null -> Modifier.clickable(
            interactionSource = null,
            indication = indication,
            enabled = enabled,
            role = Role.Button,
            onClick = onClick
        )
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (desktop) 32.dp else 44.dp)
            .clip(shape)
            .then(activation)
            .padding(horizontal = if (desktop) 6.dp else 12.dp, vertical = 4.dp)
            .enabledAlpha(enabled),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (desktop && selection != null) {
            MenuSelectionMark(selection.marked, size = 16.dp)
            Spacer(Modifier.width(8.dp))
        }
        content()
        if (!desktop && selection != null) {
            Spacer(Modifier.width(8.dp))
            MenuSelectionMark(selection.marked, size = 20.dp)
        }
    }
}

@Composable
internal fun RowScope.MenuItemContent(
    text: @Composable () -> Unit,
    leadingIcon: (@Composable () -> Unit)?,
    trailingContent: (@Composable () -> Unit)?,
    reserveIconSpace: Boolean = leadingIcon != null
) {
    if (reserveIconSpace) {
        Box(
            modifier = Modifier.widthIn(min = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            leadingIcon?.invoke()
        }
        Spacer(Modifier.width(8.dp))
    }
    Box(Modifier.weight(1f)) { text() }

    if (trailingContent != null) {
        Spacer(Modifier.width(8.dp))
        trailingContent()
    }
}

@Composable
internal fun menuRowShape(desktop: Boolean): SmoothRoundedRectangleShape {
    val radius = if (desktop) 6.dp else mobileMenuCornerRadius() - MobileMenuContentPadding
    return remember(radius) { SmoothRoundedRectangleShape(radius) }
}

internal val MobileMenuContentPadding = 6.dp

@OptIn(UnstableSaltUiApi::class)
@Composable
internal fun mobileMenuCornerRadius(): Dp {
    val cardShape = CardDefaults.shape
    val cardRadius = when (cardShape) {
        is SmoothRoundedRectangleShape -> cardShape.radius
        is RoundedCornerShape -> with(LocalDensity.current) {
            // The standard card uses a fixed radius. Percentage corners use the menu minimum.
            cardShape.topStart.toPx(Size.Zero, this).toDp()
        }
        else -> 0.dp
    }
    return maxOf(24.dp, cardRadius)
}
