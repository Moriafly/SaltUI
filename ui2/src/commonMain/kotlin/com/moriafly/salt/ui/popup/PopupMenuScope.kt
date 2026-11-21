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

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * Declares the entries shown by [PopupActionMenu] for keyboard and pointer navigation.
 */
@LayoutScopeMarker
@UnstableSaltUiApi
sealed interface PopupMenuScope {
    /**
     * Adds an action. Set [closeOnClick] to false to keep the menu open after activation.
     */
    fun item(
        text: @Composable () -> Unit,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        leadingIcon: (@Composable () -> Unit)? = null,
        trailingContent: (@Composable () -> Unit)? = null,
        enabled: Boolean = true,
        closeOnClick: Boolean = true
    )

    /**
     * Adds a single-choice entry. The caller controls which entry in the group is [selected].
     */
    fun radioItem(
        text: @Composable () -> Unit,
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        leadingIcon: (@Composable () -> Unit)? = null,
        trailingContent: (@Composable () -> Unit)? = null,
        enabled: Boolean = true,
        closeOnClick: Boolean = true
    )

    /**
     * Adds an independent choice. [onCheckedChange] receives the requested value on activation.
     */
    fun toggleItem(
        text: @Composable () -> Unit,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier = Modifier,
        leadingIcon: (@Composable () -> Unit)? = null,
        trailingContent: (@Composable () -> Unit)? = null,
        enabled: Boolean = true,
        closeOnClick: Boolean = true
    )

    fun divider()

    /**
     * Opens a child menu when activated or hovered on desktop. On touch devices, [text] is
     * also displayed as the title of the child page.
     */
    fun submenu(
        text: @Composable () -> Unit,
        modifier: Modifier = Modifier,
        leadingIcon: (@Composable () -> Unit)? = null,
        enabled: Boolean = true,
        items: PopupMenuScope.() -> Unit
    )
}

@OptIn(UnstableSaltUiApi::class)
internal sealed interface MenuEntry {
    val enabled: Boolean

    sealed interface Item : MenuEntry {
        val rowModifier: Modifier
        val text: @Composable () -> Unit
        val leadingIcon: (@Composable () -> Unit)?
    }

    class Action(
        val onClick: () -> Unit,
        override val rowModifier: Modifier,
        override val text: @Composable () -> Unit,
        override val leadingIcon: (@Composable () -> Unit)?,
        val trailingContent: (@Composable () -> Unit)?,
        override val enabled: Boolean,
        val closeOnClick: Boolean,
        val selection: MenuSelection? = null
    ) : Item

    class Submenu(
        val items: PopupMenuScope.() -> Unit,
        override val rowModifier: Modifier,
        override val text: @Composable () -> Unit,
        override val leadingIcon: (@Composable () -> Unit)?,
        override val enabled: Boolean
    ) : Item

    data object Divider : MenuEntry {
        override val enabled: Boolean = false
    }
}

@OptIn(UnstableSaltUiApi::class)
internal fun buildMenuEntries(items: PopupMenuScope.() -> Unit): List<MenuEntry> = buildList {
    MenuEntryBuilder(this).items()
}

@OptIn(UnstableSaltUiApi::class)
private class MenuEntryBuilder(
    private val entries: MutableList<MenuEntry>
) : PopupMenuScope {
    override fun item(
        text: @Composable () -> Unit,
        onClick: () -> Unit,
        modifier: Modifier,
        leadingIcon: (@Composable () -> Unit)?,
        trailingContent: (@Composable () -> Unit)?,
        enabled: Boolean,
        closeOnClick: Boolean
    ) {
        entries += MenuEntry.Action(
            onClick = onClick,
            rowModifier = modifier,
            text = text,
            leadingIcon = leadingIcon,
            trailingContent = trailingContent,
            enabled = enabled,
            closeOnClick = closeOnClick
        )
    }

    override fun radioItem(
        text: @Composable () -> Unit,
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier,
        leadingIcon: (@Composable () -> Unit)?,
        trailingContent: (@Composable () -> Unit)?,
        enabled: Boolean,
        closeOnClick: Boolean
    ) {
        entries += MenuEntry.Action(
            onClick = onClick,
            rowModifier = modifier,
            text = text,
            leadingIcon = leadingIcon,
            trailingContent = trailingContent,
            enabled = enabled,
            closeOnClick = closeOnClick,
            selection = MenuSelection.Radio(selected)
        )
    }

    override fun toggleItem(
        text: @Composable () -> Unit,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier,
        leadingIcon: (@Composable () -> Unit)?,
        trailingContent: (@Composable () -> Unit)?,
        enabled: Boolean,
        closeOnClick: Boolean
    ) {
        entries += MenuEntry.Action(
            onClick = { onCheckedChange(!checked) },
            rowModifier = modifier,
            text = text,
            leadingIcon = leadingIcon,
            trailingContent = trailingContent,
            enabled = enabled,
            closeOnClick = closeOnClick,
            selection = MenuSelection.Toggle(checked)
        )
    }

    override fun divider() {
        entries += MenuEntry.Divider
    }

    override fun submenu(
        text: @Composable () -> Unit,
        modifier: Modifier,
        leadingIcon: (@Composable () -> Unit)?,
        enabled: Boolean,
        items: PopupMenuScope.() -> Unit
    ) {
        entries += MenuEntry.Submenu(
            items = items,
            rowModifier = modifier,
            text = text,
            leadingIcon = leadingIcon,
            enabled = enabled
        )
    }
}
