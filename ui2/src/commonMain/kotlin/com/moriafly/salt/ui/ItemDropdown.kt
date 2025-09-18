/*
 * Salt UI
 * Copyright (C) 2023 Moriafly
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused")

package com.moriafly.salt.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.popup.PopupMenu
import com.moriafly.salt.ui.popup.PopupState
import com.moriafly.salt.ui.popup.rememberPopupState

/**
 * A scope for the content of [ItemDropdown].
 *
 * It inherits from [ColumnScope], allowing standard column-based arrangements,
 * and also provides direct access to the [PopupState].
 */
@Stable
interface ItemDropdownScope : ColumnScope {
    /**
     * The [PopupState] that controls the menu, allowing you to dismiss it from within the content.
     */
    val state: PopupState
}

private class ItemDropdownScopeImpl(
    override val state: PopupState,
    private val columnScope: ColumnScope
) : ItemDropdownScope,
    ColumnScope by columnScope

/**
 * A clickable list item that displays a label, a current value, and reveals a popup menu.
 *
 * This composable is structured as a row containing an optional leading icon,
 * a main text label with an optional subtitle, a value text on the end, and a dropdown arrow.
 * Clicking anywhere on the item will trigger the popup menu.
 *
 * @param text The primary text label for the item.
 * @param value The current value text displayed at the end of the item.
 * @param modifier The [Modifier] to be applied to this component.
 * @param state State object that controls the menu's expanded state.
 * @param enabled Controls the enabled state of the component. When `false`, the item will not
 * be clickable and will appear disabled.
 * @param iconPainter An optional [Painter] for the leading icon.
 * @param iconPaddingValues Padding to apply around the icon, if it exists.
 * @param iconColor Tint color for the icon. If `null`, the icon will be rendered with its
 * original colors.
 * @param sub Optional subtitle text displayed below the primary [text].
 * @param content The composable content to be displayed inside the popup menu. The lambda
 * receiver is an [ItemDropdownScope], which provides access to the [PopupState] and
 * inherits from [ColumnScope].
 */
@UnstableSaltUiApi
@Composable
fun ItemDropdown(
    text: String,
    value: String,
    modifier: Modifier = Modifier,
    state: PopupState = rememberPopupState(),
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = SaltTheme.colors.text,
    sub: String? = null,
    content: @Composable ItemDropdownScope.() -> Unit
) {
    Box(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(SaltTheme.dimens.item)
                .enabledAlpha(enabled)
                .clickable(enabled = enabled) {
                    state.expend()
                }
                .innerPadding(vertical = false),
            verticalAlignment = Alignment.CenterVertically
        ) {
            iconPainter?.let {
                Image(
                    modifier = Modifier
                        .size(SaltTheme.dimens.itemIcon)
                        .padding(iconPaddingValues),
                    painter = iconPainter,
                    contentDescription = null,
                    colorFilter = iconColor?.let { ColorFilter.tint(iconColor) }
                )
                Spacer(Modifier.width(SaltTheme.dimens.subPadding))
            }

            JustifiedRow(
                startContent = {
                    Column {
                        Text(
                            text = text
                        )
                        sub?.let {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = sub,
                                color = SaltTheme.colors.subText,
                                style = SaltTheme.textStyles.sub
                            )
                        }
                    }
                },
                endContent = {
                    Text(
                        text = value,
                        color = SaltTheme.colors.subText,
                        textAlign = TextAlign.End,
                        style = SaltTheme.textStyles.sub
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .innerPadding(horizontal = false),
                verticalAlignment = Alignment.CenterVertically
            )

            Spacer(Modifier.width(SaltTheme.dimens.subPadding))

            ItemPopupArrow()
        }

        PopupMenu(
            expanded = state.expend,
            onDismissRequest = {
                state.dismiss()
            }
        ) {
            val scope = ItemDropdownScopeImpl(state, this)
            scope.content()
        }
    }
}

/**
 * A clickable list item that displays a label, a current value, and reveals a popup menu.
 *
 * This composable is structured as a row containing an optional leading icon,
 * a main text label with an optional subtitle, a value text on the end, and a dropdown arrow.
 * Clicking anywhere on the item will trigger the popup menu.
 *
 * @param state State object that controls the menu's expanded state.
 * @param text The primary text label for the item.
 * @param value The current value text displayed at the end of the item.
 * @param modifier The [Modifier] to be applied to this component.
 * @param enabled Controls the enabled state of the component. When `false`, the item will not
 * be clickable and will appear disabled.
 * @param iconPainter An optional [Painter] for the leading icon.
 * @param iconPaddingValues Padding to apply around the icon, if it exists.
 * @param iconColor Tint color for the icon. If `null`, the icon will be rendered with its
 * original colors.
 * @param sub Optional subtitle text displayed below the primary [text].
 * @param content The composable content to be displayed inside the popup menu. The lambda
 * receiver is an [ItemDropdownScope], which provides access to the [PopupState] and
 * inherits from [ColumnScope].
 */
@Deprecated("Use overload without state or with default state")
@UnstableSaltUiApi
@Composable
fun ItemDropdown(
    state: PopupState,
    text: String,
    value: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = SaltTheme.colors.text,
    sub: String? = null,
    content: @Composable ItemDropdownScope.() -> Unit
) {
    ItemDropdown(
        text,
        value = value,
        modifier = modifier,
        state = state,
        enabled = enabled,
        iconPainter = iconPainter,
        iconPaddingValues = iconPaddingValues,
        iconColor = iconColor,
        sub = sub,
        content = content
    )
}

// TODO DropdownMenuItem
