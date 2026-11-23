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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.generated.resources.Res
import com.moriafly.salt.ui.generated.resources.back
import com.moriafly.salt.ui.icons.ArrowBack
import com.moriafly.salt.ui.icons.SaltIcons
import com.moriafly.salt.ui.rememberScrollState
import com.moriafly.salt.ui.thenIf
import com.moriafly.salt.ui.verticalScroll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import org.jetbrains.compose.resources.stringResource

/**
 * An action menu with platform-adapted layout, selection and submenus.
 *
 * The caller controls [expanded] and should set it to false in [onDismissRequest]. Keep the menu
 * in composition while closed to allow the touch exit transition to finish.
 *
 * [header] can contain interactive content, such as a filter field. [anchor] captures the
 * opening pointer position for automatic placement. [popupPositionProvider] overrides automatic
 * placement and receives the trigger bounds and menu panel size in pixels. [style] is shared
 * by the root menu and its submenus.
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
    val navigation = remember { MenuNavigation() }
    val visible = remember { MutableTransitionState(false) }
    val pages = rememberMenuPages(
        expanded = expanded,
        desktop = desktop,
        visible = visible,
        navigation = navigation,
        items = items
    )

    PopupMenuPanel(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        offset = offset,
        properties = properties,
        style = style,
        anchor = anchor,
        popupPositionProvider = popupPositionProvider,
        scrollable = false,
        useIntrinsicWidth = desktop,
        isSlideSelecting = { navigation.isSlideSelecting },
        visible = visible
    ) {
        if (pages.isNotEmpty()) {
            ActionMenuPages(
                pages = pages,
                navigation = navigation,
                desktop = desktop,
                expanded = expanded,
                focusable = properties.focusable,
                style = style,
                header = header,
                onDismissRequest = onDismissRequest
            )
        }
    }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun rememberMenuPages(
    expanded: Boolean,
    desktop: Boolean,
    visible: MutableTransitionState<Boolean>,
    navigation: MenuNavigation,
    items: PopupMenuScope.() -> Unit
): List<MenuPage> {
    val currentItems by rememberUpdatedState(items)
    val openPages by remember(navigation) {
        derivedStateOf { menuPages(currentItems, navigation.path) }
    }
    val pages = if (expanded) openPages else navigation.lastPages
    val settledClosed = !expanded && (desktop || (visible.isIdle && !visible.currentState))
    SideEffect {
        if (expanded) {
            navigation.lastPages = pages
            val currentPage = pages.lastOrNull()
            if (currentPage != null && currentPage.path != navigation.path) {
                navigation.path = currentPage.path
            }
        } else {
            navigation.reset()
            if (settledClosed) {
                navigation.lastPages = emptyList()
            }
        }
    }
    return pages
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun ActionMenuPages(
    pages: List<MenuPage>,
    navigation: MenuNavigation,
    desktop: Boolean,
    expanded: Boolean,
    focusable: Boolean,
    style: PopupMenuStyle,
    header: (@Composable () -> Unit)?,
    onDismissRequest: () -> Unit
) {
    val currentPage = pages.last()
    val direction = LocalLayoutDirection.current
    val focus = remember { FocusRequester() }
    var hasFocus by remember { mutableStateOf(false) }
    LaunchedEffect(expanded, focusable) {
        if (expanded && focusable && !hasFocus) {
            focus.requestFocus()
        }
    }
    Column(
        modifier = Modifier
            .onKeyEvent { event ->
                expanded && focusable && navigation.handleKey(
                    event = event,
                    page = currentPage,
                    direction = direction,
                    onDismissRequest = onDismissRequest
                )
            }
            .focusRequester(focus)
            .onFocusChanged { hasFocus = it.hasFocus }
            .focusable(enabled = expanded && focusable)
    ) {
        if (desktop) {
            MenuPageContent(
                page = pages.first(),
                pages = pages,
                navigation = navigation,
                desktop = true,
                active = expanded,
                style = style,
                header = header,
                onDismissRequest = onDismissRequest
            )
        } else {
            AnimatedContent(
                targetState = currentPage,
                contentKey = { it.path },
                transitionSpec = { menuPageTransition(direction) },
                label = "Menu page"
            ) { page ->
                MenuPageContent(
                    page = page,
                    pages = pages,
                    navigation = navigation,
                    desktop = false,
                    active = expanded && currentPage.path == page.path,
                    style = style,
                    header = if (page.path.isEmpty()) header else null,
                    onDismissRequest = onDismissRequest,
                    modifier = Modifier
                        .widthIn(min = 196.dp, max = 340.dp)
                        .width(IntrinsicSize.Max)
                )
            }
        }
    }
}

private fun AnimatedContentTransitionScope<MenuPage>.menuPageTransition(
    direction: LayoutDirection
): ContentTransform {
    val depthDirection = if (targetState.path.size > initialState.path.size) 1 else -1
    val layoutDirection = if (direction == LayoutDirection.Ltr) 1 else -1
    val towardEnd = depthDirection * layoutDirection
    val exitDuration = 60
    val enterSlideDuration = 145
    val enter = fadeIn(tween(105, delayMillis = exitDuration)) +
        slideInHorizontally(tween(enterSlideDuration, delayMillis = exitDuration)) {
            towardEnd * it / 20
        }
    return (enter togetherWith fadeOut(tween(exitDuration))) using
        SizeTransform { _, _ ->
            tween(exitDuration + enterSlideDuration, easing = FastOutSlowInEasing)
        }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun MenuPageContent(
    page: MenuPage,
    pages: List<MenuPage>,
    navigation: MenuNavigation,
    desktop: Boolean,
    active: Boolean,
    style: PopupMenuStyle,
    header: (@Composable () -> Unit)?,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()
    val child = if (desktop) pages.getOrNull(page.path.size + 1) else null
    val openIndex = child?.path?.lastOrNull()
    val reserveSelectionSpace = desktop && page.entries.any { entry ->
        entry is MenuEntry.Action && entry.selection != null
    }
    val reserveIconSpace = page.entries.any { entry ->
        entry is MenuEntry.Item && entry.leadingIcon != null
    }
    val drag = if (desktop) null else remember { MenuDragSelection<Int>() }
    val dragModifier = if (drag == null) {
        Modifier
    } else {
        Modifier
            .onGloballyPositioned { drag.coordinates = it }
            .menuDragSelection(
                selection = drag,
                enabled = active,
                canSlide = { scroll.maxValue == 0 },
                entryEnabled = { page.entries.getOrNull(it)?.enabled == true },
                onSlideSelectingChange = { navigation.isSlideSelecting = it },
                onActivate = { index ->
                    navigation.activate(
                        page = page,
                        index = index,
                        keyboard = false,
                        onDismissRequest = onDismissRequest
                    )
                }
            )
    }

    Column(modifier = modifier) {
        val parent = page.parent
        if (!desktop && parent != null) {
            MenuPageBack(
                title = parent.text,
                enabled = active,
                onClick = { navigation.back(page, keepFocus = false) }
            )
        }

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

        Column(
            modifier = Modifier
                .verticalScroll(scroll, enabled = active && scroll.maxValue > 0)
                .then(dragModifier)
        ) {
            page.entries.forEachIndexed { index, entry ->
                when (entry) {
                    is MenuEntry.Item -> {
                        MenuPageItem(
                            entry = entry,
                            index = index,
                            page = page,
                            child = child?.takeIf { openIndex == index },
                            pages = pages,
                            navigation = navigation,
                            desktop = desktop,
                            active = active,
                            reserveSelectionSpace = reserveSelectionSpace,
                            reserveIconSpace = reserveIconSpace,
                            drag = drag,
                            style = style,
                            onDismissRequest = onDismissRequest
                        )
                    }

                    MenuEntry.Divider -> MenuDivider()
                }
            }
        }
    }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun MenuPageBack(
    title: @Composable () -> Unit,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val back = stringResource(Res.string.back)
    MenuRow(
        modifier = Modifier.semantics { onClick(label = back, action = null) },
        enabled = enabled,
        desktop = false,
        highlighted = false,
        onClick = onClick
    ) {
        Icon(
            imageVector = SaltIcons.ArrowBack,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = SaltTheme.colors.subText
        )
        Spacer(Modifier.width(8.dp))
        title()
    }
    MenuDivider()
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun MenuPageItem(
    entry: MenuEntry.Item,
    index: Int,
    page: MenuPage,
    child: MenuPage?,
    pages: List<MenuPage>,
    navigation: MenuNavigation,
    desktop: Boolean,
    active: Boolean,
    reserveSelectionSpace: Boolean,
    reserveIconSpace: Boolean,
    drag: MenuDragSelection<Int>?,
    style: PopupMenuStyle,
    onDismissRequest: () -> Unit
) {
    val keyboardSelected = navigation.cursor == MenuCursor(page.path, index)
    val highlighted = keyboardSelected || drag?.highlightedItem == index
    val enabled = active && entry.enabled
    val interactionSource = if (desktop && enabled) {
        remember { MutableInteractionSource() }
    } else {
        null
    }
    if (interactionSource != null) {
        val openChildHover = if (entry is MenuEntry.Submenu) {
            remember { HoverInteraction.Enter() }
        } else {
            null
        }
        val currentPage by rememberUpdatedState(page)
        LaunchedEffect(interactionSource, navigation, index, openChildHover) {
            interactionSource.interactions
                .filter { interaction ->
                    when (interaction) {
                        is HoverInteraction.Enter -> interaction !== openChildHover
                        is HoverInteraction.Exit -> interaction.enter !== openChildHover
                        else -> false
                    }
                }
                .collectLatest { interaction ->
                    if (interaction is HoverInteraction.Enter) {
                        // Give the pointer time to reach an already open child.
                        if (navigation.hasOtherOpenChild(currentPage, index)) {
                            delay(SUBMENU_SWITCH_DELAY_MILLIS)
                        }
                        navigation.hover(currentPage, index)
                    }
                }
        }
        if (child != null && openChildHover != null) {
            DisposableEffect(interactionSource, openChildHover) {
                // Keep the same indication active after the pointer leaves the parent row.
                interactionSource.tryEmit(openChildHover)
                onDispose { interactionSource.tryEmit(HoverInteraction.Exit(openChildHover)) }
            }
        }
    }

    val rowModifier = entry.rowModifier.thenIf(drag != null) {
        onGloballyPositioned { drag?.rows?.set(index, it) }
    }
    Box {
        MenuRow(
            modifier = rowModifier,
            enabled = enabled,
            desktop = desktop,
            highlighted = highlighted,
            revealForKeyboard = keyboardSelected,
            selection = (entry as? MenuEntry.Action)?.selection,
            reserveSelectionSpace = reserveSelectionSpace,
            interactionSource = interactionSource,
            onClick = {
                navigation.activate(
                    page = page,
                    index = index,
                    keyboard = false,
                    onDismissRequest = onDismissRequest
                )
            }
        ) {
            MenuEntryContent(entry, reserveIconSpace)
        }

        if (child != null) {
            PopupMenuPanel(
                expanded = active,
                onDismissRequest = null,
                offset = DpOffset.Zero,
                properties = MenuSubmenuProperties,
                style = style,
                anchor = null,
                popupPositionProvider = rememberSubmenuPositionProvider(),
                scrollable = false
            ) {
                MenuPageContent(
                    page = child,
                    pages = pages,
                    navigation = navigation,
                    desktop = true,
                    active = active,
                    style = style,
                    header = null,
                    onDismissRequest = onDismissRequest
                )
            }
        }
    }
}

@Composable
private fun RowScope.MenuEntryContent(
    entry: MenuEntry.Item,
    reserveIconSpace: Boolean
) {
    val trailingContent: (@Composable () -> Unit)? = when (entry) {
        is MenuEntry.Action -> entry.trailingContent
        is MenuEntry.Submenu -> {
            { MenuSubmenuChevron() }
        }
    }
    MenuItemContent(
        text = entry.text,
        leadingIcon = entry.leadingIcon,
        trailingContent = trailingContent,
        reserveIconSpace = reserveIconSpace
    )
}

private const val SUBMENU_SWITCH_DELAY_MILLIS = 80L

private val MenuSubmenuProperties = PopupProperties(
    focusable = false,
    dismissOnBackPress = false,
    dismissOnClickOutside = false
)

@Composable
private fun rememberSubmenuPositionProvider(): PopupPositionProvider {
    val density = LocalDensity.current
    return remember(density) {
        val margin = with(density) { 8.dp.roundToPx() }
        val padding = with(density) { DesktopMenuContentPadding.roundToPx() }
        val overlap = with(density) { 4.dp.roundToPx() }
        createMenuPositionProvider { anchor, window, direction, size ->
            val right = anchor.right - overlap
            val left = anchor.left + overlap - size.width
            val maxX = (window.width - size.width - margin).coerceAtLeast(margin)
            val maxY = (window.height - size.height - margin).coerceAtLeast(margin)
            val preferred = if (direction == LayoutDirection.Ltr) right else left
            val alternate = if (direction == LayoutDirection.Ltr) left else right
            val x = when {
                preferred in margin..maxX -> preferred
                alternate in margin..maxX -> alternate
                window.width - anchor.right >= anchor.left -> maxX
                else -> margin
            }
            IntOffset(x, (anchor.top - padding).coerceIn(margin, maxY))
        }
    }
}

private class MenuPage(
    val path: List<Int>,
    val parent: MenuEntry.Submenu?,
    val entries: List<MenuEntry>
)

private data class MenuCursor(
    val path: List<Int>,
    val index: Int
)

@OptIn(UnstableSaltUiApi::class)
private fun menuPages(
    items: PopupMenuScope.() -> Unit,
    path: List<Int>
): List<MenuPage> = buildList {
    var page = MenuPage(emptyList(), null, buildMenuEntries(items))
    add(page)
    for (index in path) {
        val submenu = page.entries.getOrNull(index) as? MenuEntry.Submenu
        if (submenu == null || !submenu.enabled) {
            break
        }
        page = MenuPage(page.path + index, submenu, buildMenuEntries(submenu.items))
        add(page)
    }
}

@OptIn(UnstableSaltUiApi::class)
private class MenuNavigation {
    var path by mutableStateOf(emptyList<Int>())
    var cursor by mutableStateOf<MenuCursor?>(null)
        private set
    var lastPages: List<MenuPage> = emptyList()
    var isSlideSelecting: Boolean = false

    fun reset() {
        path = emptyList()
        cursor = null
        isSlideSelecting = false
    }

    fun activate(page: MenuPage, index: Int, keyboard: Boolean, onDismissRequest: () -> Unit) {
        when (val entry = page.entries.getOrNull(index)?.takeIf { it.enabled }) {
            is MenuEntry.Action -> {
                if (entry.closeOnClick) onDismissRequest()
                entry.onClick()
            }

            is MenuEntry.Submenu -> {
                path = page.path + index
                val first = if (keyboard) {
                    buildMenuEntries(entry.items).indexOfFirst {
                        it.enabled
                    }
                } else {
                    -1
                }
                cursor = if (first >= 0) MenuCursor(path, first) else null
            }

            else -> Unit
        }
    }

    fun hasOtherOpenChild(page: MenuPage, index: Int): Boolean {
        val openChildIndex = path.getOrNull(page.path.size)
        return openChildIndex != null && openChildIndex != index
    }

    fun hover(page: MenuPage, index: Int) {
        val entry = page.entries.getOrNull(index)?.takeIf { it.enabled } ?: return
        if (entry is MenuEntry.Submenu) {
            val next = page.path + index
            if (path.size < next.size || path.take(next.size) != next) {
                path = next
            }
        } else {
            path = page.path
        }
        cursor = null
    }

    fun back(page: MenuPage, keepFocus: Boolean) {
        val parentIndex = page.path.lastOrNull() ?: return
        path = page.path.dropLast(1)
        cursor = if (keepFocus) MenuCursor(path, parentIndex) else null
    }

    fun handleKey(
        event: KeyEvent,
        page: MenuPage,
        direction: LayoutDirection,
        onDismissRequest: () -> Unit
    ): Boolean {
        if (event.type != KeyEventType.KeyDown) {
            return false
        }
        val forward = if (direction ==
            LayoutDirection.Ltr
        ) {
            Key.DirectionRight
        } else {
            Key.DirectionLeft
        }
        val backward = if (direction ==
            LayoutDirection.Ltr
        ) {
            Key.DirectionLeft
        } else {
            Key.DirectionRight
        }
        val index = cursor?.takeIf { it.path == page.path }?.index ?: -1
        return when (event.key) {
            Key.DirectionDown, Key.DirectionUp, Key.MoveHome, Key.MoveEnd -> {
                val enabled = page.entries.indices.filter { page.entries[it].enabled }
                if (enabled.isNotEmpty()) {
                    val selected = enabled.indexOf(index)
                    val next = when (event.key) {
                        Key.MoveHome -> enabled.first()
                        Key.MoveEnd -> enabled.last()
                        Key.DirectionDown -> enabled[(selected + 1) % enabled.size]
                        else -> enabled[if (selected <= 0) enabled.lastIndex else selected - 1]
                    }
                    cursor = MenuCursor(page.path, next)
                }
                true
            }

            Key.Enter, Key.NumPadEnter, Key.Spacebar -> {
                if (index < 0) return false
                activate(page, index, keyboard = true, onDismissRequest = onDismissRequest)
                true
            }

            forward -> {
                if (page.entries.getOrNull(index) !is MenuEntry.Submenu) return false
                activate(page, index, keyboard = true, onDismissRequest = onDismissRequest)
                true
            }

            backward, Key.Escape -> {
                if (page.path.isEmpty()) return false
                back(page, keepFocus = true)
                true
            }

            else -> false
        }
    }
}
