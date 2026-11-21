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

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.rememberScrollState
import com.moriafly.salt.ui.thenIf
import com.moriafly.salt.ui.verticalScroll

/**
 * A menu of actions and choices declared in [items].
 *
 * The caller controls [expanded] and should set it to false in [onDismissRequest]. Keep the menu
 * in composition while closed so the exit transition can finish.
 *
 * [header] can contain interactive content such as a filter field. [anchor] supplies the pointer
 * position for automatic placement. [popupPositionProvider] overrides placement and receives
 * the trigger bounds and panel size in pixels.
 */
@UnstableSaltUiApi
@Composable
fun PopupActionMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    anchor: PopupMenuAnchor? = null,
    offset: DpOffset = DpOffset.Zero,
    properties: PopupProperties = PopupProperties(focusable = true),
    popupPositionProvider: PopupPositionProvider? = null,
    style: PopupMenuStyle = PopupMenuDefaults.style(),
    header: (@Composable () -> Unit)? = null,
    items: PopupMenuScope.() -> Unit
) {
    val desktop = OS.isDesktop()
    val visible = remember { MutableTransitionState(false) }
    val navigation = remember { MenuNavigation() }
    val currentItems by rememberUpdatedState(items)
    val openEntries by remember { derivedStateOf { buildMenuEntries(currentItems) } }
    val entries = if (expanded) openEntries else navigation.lastEntries
    SideEffect {
        if (expanded) {
            navigation.lastEntries = entries
        } else {
            navigation.cursor = -1
            if (desktop || (visible.isIdle && !visible.currentState)) {
                navigation.lastEntries = emptyList()
            }
        }
    }

    MenuPopup(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = offset,
        properties = properties,
        style = style,
        anchor = anchor,
        popupPositionProvider = popupPositionProvider,
        scrollable = false,
        modifier = modifier,
        useIntrinsicWidth = desktop,
        visible = visible
    ) {
        val focus = remember { FocusRequester() }
        var hasFocus by remember { mutableStateOf(false) }
        val focusable = expanded && properties.focusable
        LaunchedEffect(focusable) {
            if (focusable && !hasFocus) {
                focus.requestFocus()
            }
        }
        Column(
            modifier = Modifier
                .thenIf(desktop) { widthIn(min = 176.dp, max = 420.dp) }
                .thenIf(!desktop) { widthIn(min = 196.dp, max = 340.dp).width(IntrinsicSize.Max) }
                .onKeyEvent { event ->
                    focusable && navigation.handleKey(event, entries, onDismissRequest)
                }
                .focusRequester(focus)
                .onFocusChanged { hasFocus = it.hasFocus }
                .focusable(enabled = focusable)
        ) {
            if (header != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                        .clip(menuRowShape(desktop))
                ) {
                    header()
                }
            }

            MenuActions(
                entries = entries,
                cursor = navigation.cursor,
                active = expanded,
                desktop = desktop,
                onDismissRequest = onDismissRequest
            )
        }
    }
}

@Composable
private fun MenuActions(
    entries: List<MenuEntry>,
    cursor: Int,
    active: Boolean,
    desktop: Boolean,
    onDismissRequest: () -> Unit
) {
    val scroll = rememberScrollState()
    val reserveSelectionSpace = desktop && entries.any { entry ->
        entry is MenuEntry.Action && entry.selection != null
    }
    val reserveIconSpace = entries.any { it is MenuEntry.Action && it.leadingIcon != null }
    Column(modifier = Modifier.verticalScroll(scroll, enabled = active && scroll.maxValue > 0)) {
        entries.forEachIndexed { index, entry ->
            when (entry) {
                is MenuEntry.Action -> {
                    MenuRow(
                        enabled = active && entry.enabled,
                        desktop = desktop,
                        onClick = { entry.activate(onDismissRequest) },
                        modifier = entry.rowModifier,
                        revealForKeyboard = cursor == index,
                        useMenuFocus = true,
                        selection = entry.selection,
                        reserveSelectionSpace = reserveSelectionSpace
                    ) {
                        MenuItemContent(
                            text = entry.text,
                            leadingIcon = entry.leadingIcon,
                            trailingContent = entry.trailingContent,
                            reserveIconSpace = reserveIconSpace
                        )
                    }
                }

                MenuEntry.Divider -> MenuDivider()
            }
        }
    }
}

private fun MenuEntry.Action.activate(onDismissRequest: () -> Unit) {
    if (closeOnClick) {
        onDismissRequest()
    }
    onClick()
}

private class MenuNavigation {
    var cursor by mutableIntStateOf(-1)
    var lastEntries: List<MenuEntry> = emptyList()

    fun handleKey(
        event: KeyEvent,
        entries: List<MenuEntry>,
        onDismissRequest: () -> Unit
    ): Boolean {
        if (event.type != KeyEventType.KeyDown) {
            return false
        }
        return when (event.key) {
            Key.DirectionDown, Key.DirectionUp, Key.MoveHome, Key.MoveEnd -> {
                val enabled = entries.indices.filter { entries[it].enabled }
                if (enabled.isNotEmpty()) {
                    val selected = enabled.indexOf(cursor)
                    cursor = when (event.key) {
                        Key.MoveHome -> enabled.first()
                        Key.MoveEnd -> enabled.last()
                        Key.DirectionDown -> enabled[(selected + 1) % enabled.size]
                        else -> enabled[if (selected <= 0) enabled.lastIndex else selected - 1]
                    }
                }
                true
            }

            Key.Enter, Key.NumPadEnter, Key.Spacebar -> {
                val action = entries.getOrNull(cursor) as? MenuEntry.Action
                if (action == null || !action.enabled) {
                    return false
                }
                action.activate(onDismissRequest)
                true
            }

            else -> false
        }
    }
}
