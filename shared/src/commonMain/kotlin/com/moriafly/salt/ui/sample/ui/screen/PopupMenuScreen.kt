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

package com.moriafly.salt.ui.sample.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.popup.PopupActionMenu
import com.moriafly.salt.ui.popup.popupMenuAnchor
import com.moriafly.salt.ui.popup.rememberPopupMenuAnchor
import com.moriafly.salt.ui.sample.ui.screen.basic.BasicScreenColumn
import com.moriafly.salt.ui.screen.ScreenCard
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import saltui.composeapp.generated.resources.Res
import saltui.composeapp.generated.resources.ic_menu_add
import saltui.composeapp.generated.resources.ic_menu_heart
import saltui.composeapp.generated.resources.ic_menu_history
import saltui.composeapp.generated.resources.ic_menu_person
import saltui.composeapp.generated.resources.ic_menu_text_field
import saltui.composeapp.generated.resources.ic_nav_items

@OptIn(UnstableSaltUiApi::class)
@Composable
internal fun PopupMenuScreen() {
    BasicScreenColumn(title = "Popup menus") {
        LibraryMenuExample()
        TrackContextMenuExample()
        SearchableMenuExample()
        Spacer(Modifier.height(16.dp))
    }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun LibraryMenuExample() {
    var sort by rememberSaveable { mutableStateOf(LibrarySort.Title) }
    var ascending by rememberSaveable { mutableStateOf(true) }
    var showArtist by rememberSaveable { mutableStateOf(true) }
    var showTrackNumbers by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .heightIn(min = 320.dp)
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0x687B8BFA),
                        Color(0x378849BF),
                        Color(0x507CCCB9)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Library", style = SaltTheme.textStyles.largeTitle)

        Box {
            var expanded by rememberSaveable { mutableStateOf(false) }
            val anchor = rememberPopupMenuAnchor()
            Button(
                text = "Sort by ${sort.label.lowercase()}",
                modifier = Modifier.popupMenuAnchor(anchor),
                onClick = { anchor.open { expanded = true } }
            )
            PopupActionMenu(
                expanded = expanded,
                anchor = anchor,
                onDismissRequest = { expanded = false }
            ) {
                LibrarySort.entries.forEach { option ->
                    radioItem(
                        text = { Text(option.label) },
                        selected = sort == option,
                        onClick = { sort = option },
                        leadingIcon = { MenuExampleIcon(option.icon) },
                        enabled = showArtist || option != LibrarySort.Artist
                    )
                }
                divider()
                submenu(
                    text = { Text("Order") },
                    leadingIcon = { MenuExampleIcon(Res.drawable.ic_nav_items) }
                ) {
                    radioItem(
                        text = { Text("Ascending") },
                        selected = ascending,
                        onClick = { ascending = true }
                    )
                    radioItem(
                        text = { Text("Descending") },
                        selected = !ascending,
                        onClick = { ascending = false }
                    )
                }
                toggleItem(
                    text = { Text("Show artist") },
                    checked = showArtist,
                    onCheckedChange = { checked ->
                        showArtist = checked
                        if (!checked && sort == LibrarySort.Artist) {
                            sort = LibrarySort.Title
                        }
                    },
                    leadingIcon = { MenuExampleIcon(Res.drawable.ic_menu_person) },
                    closeOnClick = false
                )
                toggleItem(
                    text = { Text("Show track numbers") },
                    checked = showTrackNumbers,
                    onCheckedChange = { showTrackNumbers = it },
                    closeOnClick = false
                )
            }
        }

        val records = when (sort) {
            LibrarySort.Title -> libraryTracks.sortedBy { it.title }
            LibrarySort.Artist -> libraryTracks.sortedBy { it.artist }
            LibrarySort.Recent -> libraryTracks
        }
        val displayRecords = if (ascending) records else records.asReversed()
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            displayRecords.forEachIndexed { index, record ->
                Column {
                    val title = if (showTrackNumbers) {
                        "${index + 1}. ${record.title}"
                    } else {
                        record.title
                    }
                    Text(title)
                    if (showArtist) {
                        Text(
                            record.artist,
                            color = SaltTheme.colors.subText,
                            style = SaltTheme.textStyles.sub
                        )
                    }
                }
            }
        }
    }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun TrackContextMenuExample() {
    val desktop = OS.isDesktop()
    var expanded by remember { mutableStateOf(false) }
    var queued by rememberSaveable { mutableIntStateOf(0) }
    var favorite by rememberSaveable { mutableStateOf(false) }
    var playlist by rememberSaveable { mutableStateOf<String?>(null) }
    val anchor = rememberPopupMenuAnchor()

    fun openFromTrack() {
        anchor.open { expanded = true }
    }

    ScreenCard(header = "Track actions") {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .popupMenuAnchor(anchor)
                    .pointerInput(anchor) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (event.type == PointerEventType.Press &&
                                    event.buttons.isSecondaryPressed
                                ) {
                                    openFromTrack()
                                    event.changes.forEach { it.consume() }
                                }
                            }
                        }
                    }
                    .combinedClickable(
                        onClick = ::openFromTrack,
                        onLongClick = if (desktop) null else ::openFromTrack
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Blue Hour")
                val gestureHint = if (desktop) {
                    "Right-click for track actions"
                } else {
                    "Touch and hold for track actions"
                }
                Text(
                    text = gestureHint,
                    color = SaltTheme.colors.subText,
                    style = SaltTheme.textStyles.sub
                )
                Text("Up next: $queued", color = SaltTheme.colors.subText)
                if (favorite) {
                    Text("In favorites", color = SaltTheme.colors.highlight)
                }
                playlist?.let { Text("Playlist: $it", color = SaltTheme.colors.subText) }
            }
            PopupActionMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                anchor = anchor
            ) {
                item(
                    text = { Text("Add to queue") },
                    onClick = { queued++ },
                    leadingIcon = { MenuExampleIcon(Res.drawable.ic_menu_add) }
                )
                toggleItem(
                    text = { Text("Favorite") },
                    checked = favorite,
                    onCheckedChange = { favorite = it },
                    leadingIcon = { MenuExampleIcon(Res.drawable.ic_menu_heart) },
                    closeOnClick = false
                )
                divider()
                submenu(
                    text = { Text("Add to playlist") },
                    leadingIcon = { MenuExampleIcon(Res.drawable.ic_nav_items) }
                ) {
                    item(
                        text = { Text("Evening mix") },
                        onClick = { playlist = "Evening mix" },
                        leadingIcon = { MenuExampleIcon(Res.drawable.ic_nav_items) }
                    )
                    item(
                        text = { Text("On the road") },
                        onClick = { playlist = "On the road" }
                    )
                }
            }
        }
    }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun SearchableMenuExample() {
    val desktop = OS.isDesktop()
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selected by rememberSaveable { mutableStateOf("Living room") }
    val anchor = rememberPopupMenuAnchor()

    ScreenCard(header = "Output device") {
        Box(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Button(
                text = selected,
                modifier = Modifier.popupMenuAnchor(anchor),
                onClick = {
                    query = ""
                    anchor.open { expanded = true }
                }
            )
            PopupActionMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 340.dp),
                anchor = anchor,
                header = {
                    val colors = SaltTheme.colors
                    val fill = colors.text.copy(
                        alpha = if (SaltTheme.configs.isDarkTheme) 0.065f else 0.035f
                    )
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = SaltTheme.textStyles.main.copy(color = colors.text),
                        cursorBrush = SolidColor(colors.highlight),
                        decorationBox = { input ->
                            Box(
                                Modifier
                                    .background(fill)
                                    .padding(
                                        horizontal = if (desktop) 6.dp else 12.dp,
                                        vertical = 8.dp
                                    ),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (query.isEmpty()) {
                                    Text("Search devices", color = colors.subText)
                                }
                                input()
                            }
                        }
                    )
                }
            ) {
                outputDevices.filter { it.contains(query, ignoreCase = true) }.forEach { device ->
                    radioItem(
                        text = { Text(device) },
                        selected = selected == device,
                        onClick = { selected = device }
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuExampleIcon(resource: DrawableResource) {
    Icon(
        painter = painterResource(resource),
        contentDescription = null
    )
}

private val outputDevices = listOf(
    "Living room",
    "Headphones",
    "Studio monitors",
    "Desk speakers",
    "Kitchen",
    "Bedroom",
    "Office",
    "Game room",
    "TV",
    "USB audio",
    "Bluetooth speaker",
    "Line out",
    "Digital out",
    "Display audio",
    "Portable DAC",
    "Network audio",
    "Car audio",
    "Built-in speakers"
)

private enum class LibrarySort(
    val label: String,
    val icon: DrawableResource
) {
    Title("Title", Res.drawable.ic_menu_text_field),
    Artist("Artist", Res.drawable.ic_menu_person),
    Recent("Recently added", Res.drawable.ic_menu_history)
}

private data class LibraryTrack(
    val title: String,
    val artist: String
)

private val libraryTracks = listOf(
    LibraryTrack("Soft Current", "North Pier"),
    LibraryTrack("Blue Hour", "Juniper"),
    LibraryTrack("First Light", "Fields")
)
