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

@file:OptIn(com.moriafly.salt.ui.UnstableSaltUiApi::class)

package com.moriafly.salt.ui.sample.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.ButtonAppearance
import com.moriafly.salt.ui.ItemButton
import com.moriafly.salt.ui.ItemCheck
import com.moriafly.salt.ui.ItemEdit
import com.moriafly.salt.ui.ItemOuterTip
import com.moriafly.salt.ui.ItemPopup
import com.moriafly.salt.ui.ItemSlider
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.dialog.YesNoDialog
import com.moriafly.salt.ui.popup.PopupMenuItem
import com.moriafly.salt.ui.popup.rememberPopupState
import com.moriafly.salt.ui.sample.ui.screen.basic.BasicScreenColumn
import com.moriafly.salt.ui.screen.ScreenCard
import com.moriafly.salt.ui.sheet.Sheet
import com.moriafly.salt.ui.sheet.SheetContentInteraction
import com.moriafly.salt.ui.sheet.SheetContentLayout
import com.moriafly.salt.ui.sheet.SheetDetent
import com.moriafly.salt.ui.sheet.SheetPresentation
import com.moriafly.salt.ui.sheet.SheetProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val exampleNames = listOf(
    "Short content",
    "Playback queue",
    "Draft form",
    "Horizontal interactions"
)
private val heightNames = listOf(
    "Content (natural height)",
    "Medium / Large",
    "320 dp / Large",
    "25% / 75% / 100%",
    "Merged duplicate heights"
)

/**
 * Interactive coverage of the public Sheet API.
 */
@Suppress("ktlint:compose:modifier-missing-check")
@Composable
fun SheetScreen() {
    var visible by rememberSaveable { mutableStateOf(false) }
    var example by rememberSaveable { mutableIntStateOf(0) }
    var presentation by rememberSaveable { mutableIntStateOf(0) }
    var heights by rememberSaveable { mutableIntStateOf(0) }
    var dismissible by rememberSaveable { mutableStateOf(true) }
    var dragEnabled by rememberSaveable { mutableStateOf(true) }
    var resizeFirst by rememberSaveable { mutableStateOf(true) }
    var showTitle by rememberSaveable { mutableStateOf(true) }
    var showActions by rememberSaveable { mutableStateOf(true) }
    var edgeToEdge by rememberSaveable { mutableStateOf(true) }
    var protectDraft by rememberSaveable { mutableStateOf(true) }
    var retainOnRequest by rememberSaveable { mutableStateOf(false) }
    var longTitle by rememberSaveable { mutableStateOf(false) }
    var draft by rememberSaveable { mutableStateOf("") }
    var savedDraft by rememberSaveable { mutableStateOf("") }
    var confirmDiscard by rememberSaveable { mutableStateOf(false) }
    var details by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val presentations = SheetPresentation.entries
    val requestClose = {
        if (!retainOnRequest) {
            if (example == 2 && protectDraft && draft != savedDraft) {
                confirmDiscard = true
            } else {
                visible = false
            }
        }
    }
    val programClose = { visible = false }
    val quickReopen = {
        scope.launch {
            visible = false
            delay(timeMillis = 80)
            visible = true
        }
        Unit
    }
    BasicScreenColumn(
        title = "Sheet"
    ) {
        ScreenCard(
            header = "Open a sheet",
            footer = "Automatic: bottom in portrait on phones, " +
                "floating in landscape or on large screens. Choose a mode to override."
        ) {
            SheetOptionItem(
                text = "Example content",
                options = exampleNames,
                selected = example,
                onSelect = { example = it }
            )
            ItemButton(
                onClick = {
                    visible = true
                },
                text = "Open ${exampleNames[example]}"
            )
        }
        ScreenCard(
            header = "Layout and height"
        ) {
            SheetOptionItem(
                text = "Presentation",
                options = presentations.map { it.name },
                selected = presentation,
                onSelect = { presentation = it }
            )
            SheetOptionItem(
                text = "Height",
                options = heightNames,
                selected = heights,
                onSelect = { heights = it }
            )
            ItemSwitcher(
                state = dragEnabled,
                onChange = { dragEnabled = it },
                text = "Allow dragging",
                sub = "Bottom sheets show a handle when resizing or drag dismissal is available"
            )
            ItemSwitcher(
                state = resizeFirst,
                onChange = { resizeFirst = it },
                text = "Expand before scrolling",
                sub = "Turn off to use ScrollFirst"
            )
            ItemSwitcher(
                state = showTitle,
                onChange = { showTitle = it },
                text = "Show title"
            )
            ItemSwitcher(
                state = showActions,
                onChange = { showActions = it },
                text = "Show actions"
            )
            ItemSwitcher(
                state = edgeToEdge,
                onChange = { edgeToEdge = it },
                text = "Extend content to the bottom edge",
                sub = "Applies without actions; the last item can still scroll into the safe area"
            )
            ItemSwitcher(
                state = longTitle,
                onChange = { longTitle = it },
                text = "Long title and action labels"
            )
        }
        ScreenCard(
            header = "Dismissal behavior",
            footer = "Use Close directly to exit when dismissal is disabled " +
                "or requests are ignored."
        ) {
            ItemSwitcher(
                state = dismissible,
                onChange = { dismissible = it },
                text = "Allow dismissal",
                sub = "Scrim, Back, Escape, downward swipes, and the close button"
            )
            ItemSwitcher(
                state = retainOnRequest,
                onChange = { retainOnRequest = it },
                text = "Keep open on dismissal",
                sub = "Dismissal requests leave the sheet open"
            )
            ItemSwitcher(
                state = protectDraft,
                onChange = { protectDraft = it },
                text = "Confirm unsaved changes"
            )
        }
    }

    Sheet(
        visible = visible,
        onDismissRequest = requestClose,
        title = if (!showTitle) {
            null
        } else if (longTitle) {
            "A resizable sheet with a long title and a complete action area"
        } else {
            exampleNames[example]
        },
        properties = SheetProperties(
            presentation = presentations[presentation],
            contentLayout = if (edgeToEdge) {
                SheetContentLayout.EdgeToEdge
            } else {
                SheetContentLayout.Inset
            },
            dismissible = dismissible,
            dragEnabled = dragEnabled,
            detents = sheetExampleDetents(index = heights),
            contentInteraction = if (resizeFirst) {
                SheetContentInteraction.ResizeFirst
            } else {
                SheetContentInteraction.ScrollFirst
            }
        ),
        actions = {
            if (showActions) {
                SheetExampleActions(
                    onCancel = requestClose,
                    onConfirm = {
                        if (example == 2) savedDraft = draft
                        visible = false
                    },
                    dismissible = dismissible,
                    isDraft = example == 2,
                    longText = longTitle
                )
            }
        }
    ) {
        when (example) {
            0 -> ShortSheetContent(
                contentPadding = contentPadding,
                details = details,
                onDetailsChange = { details = it },
                dismissible = dismissible,
                onDismissibleChange = { dismissible = it },
                onClose = programClose,
                onQuickReopen = quickReopen
            )
            1 -> QueueSheetContent(
                contentPadding = contentPadding,
                onClose = programClose
            )
            2 -> DraftSheetContent(
                contentPadding = contentPadding,
                draft = draft,
                onDraftChange = { draft = it },
                dismissible = dismissible,
                onDismissibleChange = { dismissible = it },
                onClose = programClose,
                onQuickReopen = quickReopen
            )
            else -> HorizontalSheetContent(
                contentPadding = contentPadding,
                dismissible = dismissible,
                onDismissibleChange = { dismissible = it },
                onClose = programClose,
                onQuickReopen = quickReopen
            )
        }
        if (confirmDiscard) {
            YesNoDialog(
                onDismissRequest = {
                    confirmDiscard = false
                },
                onConfirm = {
                    confirmDiscard = false
                    draft = savedDraft
                    visible = false
                },
                title = "Discard unsaved changes?",
                content = "Keep editing to preserve your draft, " +
                    "or discard changes and close the sheet.",
                cancelText = "Keep editing",
                confirmText = "Discard changes"
            )
        }
    }
}

private fun sheetExampleDetents(
    index: Int
): List<SheetDetent> = when (index) {
    1 -> listOf(SheetDetent.Medium, SheetDetent.Large)
    2 -> listOf(SheetDetent.Height(height = 320.dp), SheetDetent.Large)
    3 -> listOf(
        SheetDetent.Fraction(fraction = 0.25f),
        SheetDetent.Fraction(fraction = 0.75f),
        SheetDetent.Large
    )
    4 -> listOf(
        SheetDetent.Medium,
        SheetDetent.Fraction(fraction = 0.5f),
        SheetDetent.Large
    )
    else -> listOf(SheetDetent.Content)
}

@Composable
private fun RowScope.SheetExampleActions(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    dismissible: Boolean,
    isDraft: Boolean,
    longText: Boolean
) {
    Button(
        onClick = onCancel,
        text = "Cancel",
        appearance = ButtonAppearance.Subtle,
        enabled = dismissible,
        modifier = Modifier
            .weight(weight = 1f)
    )
    Button(
        onClick = onConfirm,
        text = if (longText) {
            "Save these changes and finish"
        } else if (isDraft) {
            "Save"
        } else {
            "Done"
        },
        maxLines = 2,
        modifier = Modifier
            .weight(weight = 1f)
    )
}

@Suppress("DEPRECATION")
@Composable
private fun SheetOptionItem(
    text: String,
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    val popupState = rememberPopupState()
    ItemPopup(
        state = popupState,
        text = text,
        sub = options[selected]
    ) {
        options.forEachIndexed { index, option ->
            PopupMenuItem(
                onClick = {
                    onSelect(index)
                    popupState.dismiss()
                },
                selected = index == selected,
                text = option
            )
        }
    }
}

@Composable
private fun SheetScrollableContent(
    contentPadding: PaddingValues,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(state = rememberScrollState())
            .padding(paddingValues = contentPadding),
        content = content
    )
}

@Composable
private fun ShortSheetContent(
    contentPadding: PaddingValues,
    details: Boolean,
    onDetailsChange: (Boolean) -> Unit,
    dismissible: Boolean,
    onDismissibleChange: (Boolean) -> Unit,
    onClose: () -> Unit,
    onQuickReopen: () -> Unit
) {
    SheetScrollableContent(contentPadding = contentPadding) {
        ItemOuterTip(
            text = "Short content uses its natural height. " +
                "Resize the window or switch themes to explore the layout."
        )
        RoundedColumn {
            ItemButton(
                onClick = { onDetailsChange(!details) },
                text = if (details) "Show less content" else "Show more content"
            )
        }
        if (details) {
            ItemOuterTip(
                text = "Extra content to see how the Content detent updates its height.\n".repeat(4)
            )
        }
        SheetLiveControls(
            dismissible = dismissible,
            onDismissibleChange = onDismissibleChange,
            onClose = onClose,
            onQuickReopen = onQuickReopen
        )
    }
}

@Composable
private fun QueueSheetContent(
    contentPadding: PaddingValues,
    onClose: () -> Unit
) {
    var selectedTrack by rememberSaveable { mutableIntStateOf(-1) }
    val listState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = listState,
        contentPadding = contentPadding
    ) {
        item {
            ItemOuterTip(
                text = "100 tracks. Scroll at any detent; reopening preserves your position."
            )
            RoundedColumn {
                ItemButton(
                    onClick = onClose,
                    text = "Close directly"
                )
            }
        }
        items(
            count = 100,
            key = { it }
        ) { index ->
            ItemCheck(
                state = selectedTrack == index,
                onChange = { selectedTrack = if (it) index else -1 },
                text = "Track ${index + 1}"
            )
        }
    }
}

@Composable
private fun DraftSheetContent(
    contentPadding: PaddingValues,
    draft: String,
    onDraftChange: (String) -> Unit,
    dismissible: Boolean,
    onDismissibleChange: (Boolean) -> Unit,
    onClose: () -> Unit,
    onQuickReopen: () -> Unit
) {
    SheetScrollableContent(contentPadding = contentPadding) {
        ItemOuterTip(
            text = "Edit your draft and try closing the sheet. " +
                "Keep editing preserves your input; Save closes it directly."
        )
        RoundedColumn {
            ItemEdit(
                text = draft,
                onChange = onDraftChange,
                hint = "Write your draft"
            )
        }
        repeat(times = 5) { index ->
            var value by rememberSaveable { mutableStateOf("") }
            RoundedColumn {
                ItemEdit(
                    text = value,
                    onChange = { value = it },
                    hint = "Additional detail ${index + 1} (keyboard test)"
                )
            }
        }
        ItemOuterTip(
            text = "Keep the last field and actions accessible while the keyboard is open."
        )
        SheetLiveControls(
            dismissible = dismissible,
            onDismissibleChange = onDismissibleChange,
            onClose = onClose,
            onQuickReopen = onQuickReopen
        )
    }
}

@Composable
private fun HorizontalSheetContent(
    contentPadding: PaddingValues,
    dismissible: Boolean,
    onDismissibleChange: (Boolean) -> Unit,
    onClose: () -> Unit,
    onQuickReopen: () -> Unit
) {
    val pager = rememberPagerState { 5 }
    var slider by rememberSaveable { mutableFloatStateOf(0.4f) }
    var clicks by rememberSaveable { mutableIntStateOf(0) }
    SheetScrollableContent(contentPadding = contentPadding) {
        ItemOuterTip(
            text = "Swipe pages, drag the slider, select text, " +
                "and tap buttons to test horizontal input."
        )
        HorizontalPager(
            state = pager,
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 150.dp)
                .padding(horizontal = SaltTheme.dimens.padding)
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = SaltTheme.colors.highlight.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Page ${page + 1} / 5"
                )
            }
        }
        ItemOuterTip(
            text = "Current page: ${pager.currentPage + 1}"
        )
        RoundedColumn {
            ItemSlider(
                value = slider,
                onValueChange = { slider = it },
                text = "Progress",
                sub = "${(slider * 100).toInt()}%"
            )
        }
        SelectionContainer {
            ItemOuterTip(
                text = "Long-press or drag to select this text without moving the sheet."
            )
        }
        RoundedColumn {
            ItemButton(
                onClick = { clicks++ },
                text = "Button taps: $clicks"
            )
        }
        SheetLiveControls(
            dismissible = dismissible,
            onDismissibleChange = onDismissibleChange,
            onClose = onClose,
            onQuickReopen = onQuickReopen
        )
        ItemOuterTip(
            text = "Return to the top, then drag down to move the sheet.\n".repeat(12)
        )
    }
}

@Composable
private fun SheetLiveControls(
    dismissible: Boolean,
    onDismissibleChange: (Boolean) -> Unit,
    onClose: () -> Unit,
    onQuickReopen: () -> Unit
) {
    RoundedColumn {
        ItemSwitcher(
            state = dismissible,
            onChange = onDismissibleChange,
            text = "Allow dismissal"
        )
        ItemButton(
            onClick = onClose,
            text = "Close directly"
        )
        ItemButton(
            onClick = onQuickReopen,
            text = "Close and reopen quickly",
            primary = false
        )
    }
}
