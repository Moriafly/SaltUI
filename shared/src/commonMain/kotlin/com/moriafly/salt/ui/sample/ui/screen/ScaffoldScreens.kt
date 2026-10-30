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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.BottomSheetScaffold
import com.moriafly.salt.ui.ItemButton
import com.moriafly.salt.ui.ItemOuterTip
import com.moriafly.salt.ui.ItemOuterTitle
import com.moriafly.salt.ui.ItemValue
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.lazy.LazyColumn
import com.moriafly.salt.ui.nested.NestedHeaderLayout
import com.moriafly.salt.ui.nested.NestedCollapsed
import com.moriafly.salt.ui.rememberBottomSheetState
import com.moriafly.salt.ui.sample.ui.screen.basic.BasicScreenBox
import kotlinx.coroutines.launch

@OptIn(UnstableSaltUiApi::class)
@Composable
fun CollapsingHeaderScreen() {
    BasicScreenBox(title = "Collapsing header") { contentPadding ->
        NestedHeaderLayout(
            header = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(SaltTheme.colors.highlight.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Scroll to collapse the header")
                }
            },
            contentPadding = contentPadding
        ) { listPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = listPadding
            ) {
                items(count = 50) { index ->
                    ItemValue(text = "Item ${index + 1}", sub = "Scroll up and down")
                }
            }
        }
    }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
fun NestedCollapsedScreen() {
    BasicScreenBox(title = "Nested collapsed") { contentPadding ->
        NestedCollapsed(
            modifier = Modifier.padding(top = contentPadding.calculateTopPadding()),
            collapsed = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(SaltTheme.colors.highlight.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Scroll to collapse this content")
                }
            }
        ) { collapsedPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = collapsedPadding + androidx.compose.foundation.layout.PaddingValues(
                    bottom = contentPadding.calculateBottomPadding()
                )
            ) {
                items(count = 50) { index ->
                    ItemValue(text = "Item ${index + 1}", sub = "Scroll up and down")
                }
            }
        }
    }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
fun BottomSheetScreen() {
    val sheetState = rememberBottomSheetState()
    val scope = rememberCoroutineScope()

    BasicScreenBox(title = "Bottom sheet scaffold") { contentPadding ->
        BottomSheetScaffold(
            state = sheetState,
            sheetPeekHeight = 72.dp,
            modifier = Modifier.padding(contentPadding),
            sheetContent = {
                ItemOuterTitle("Now playing")
                ItemValue(text = "Track", sub = "Midnight Drive")
                ItemValue(text = "Artist", sub = "The Night Owls")
                ItemButton(
                    onClick = { scope.launch { sheetState.collapse() } },
                    text = "Collapse"
                )
            }
        ) { bodyPadding ->
            Column(modifier = Modifier.padding(bodyPadding)) {
                ItemOuterTip("Drag the sheet or use the button to expand it.")
                RoundedColumn {
                    ItemButton(
                        onClick = { scope.launch { sheetState.expand() } },
                        text = "Expand sheet"
                    )
                }
            }
        }
    }
}
