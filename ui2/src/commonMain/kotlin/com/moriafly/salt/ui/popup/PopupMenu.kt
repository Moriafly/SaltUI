/*
 * SaltUI
 * Copyright (C) 2023 Moriafly
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

@file:Suppress("unused")

package com.moriafly.salt.ui.popup

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * An anchored popup for custom menu content.
 * For keyboard-navigable actions and submenus, use [PopupActionMenu].
 */
@UnstableSaltUiApi
@Composable
fun PopupMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(16.dp, 0.dp),
    properties: PopupProperties = PopupProperties(focusable = true),
    backgroundColor: Color = SaltTheme.colors.popup,
    content: @Composable ColumnScope.() -> Unit
) {
    PopupMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        offset = offset,
        properties = properties,
        style = PopupMenuDefaults.style(containerColor = backgroundColor),
        content = content
    )
}

/**
 * An anchored popup for custom menu content. The caller controls [expanded].
 *
 * [popupPositionProvider] overrides automatic placement and receives the trigger bounds
 * and menu panel size in pixels. [anchor] supplies a pointer position for automatic placement.
 * [style] controls the surface appearance. Glass and blur sample the area wrapped in
 * [PopupMenuBackdrop]. The menu uses the style's container color when sampling is unavailable.
 * Set [scrollable] to false when [content] manages its own scrolling.
 */
@UnstableSaltUiApi
@Composable
fun PopupMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(16.dp, 0.dp),
    properties: PopupProperties = PopupProperties(focusable = true),
    style: PopupMenuStyle = PopupMenuDefaults.style(),
    anchor: PopupMenuAnchor? = null,
    popupPositionProvider: PopupPositionProvider? = null,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    PopupMenuPanel(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        offset = offset,
        properties = properties,
        style = style,
        anchor = anchor,
        popupPositionProvider = popupPositionProvider,
        scrollable = scrollable,
        useIntrinsicWidth = scrollable,
        content = content
    )
}
