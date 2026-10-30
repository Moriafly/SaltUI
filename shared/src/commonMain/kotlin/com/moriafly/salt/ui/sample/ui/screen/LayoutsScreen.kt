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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.ItemOuterTitle
import com.moriafly.salt.ui.BottomBar
import com.moriafly.salt.ui.BottomBarItem
import com.moriafly.salt.ui.Card
import com.moriafly.salt.ui.JustifiedRow
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.innerPadding
import com.moriafly.salt.ui.horizontalScroll
import com.moriafly.salt.ui.lazy.grid.GridCells
import com.moriafly.salt.ui.lazy.LazyRow
import com.moriafly.salt.ui.lazy.grid.LazyHorizontalGrid
import com.moriafly.salt.ui.lazy.grid.LazyVerticalGrid
import com.moriafly.salt.ui.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import com.moriafly.salt.ui.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import com.moriafly.salt.ui.lazy.staggeredgrid.StaggeredGridCells
import com.moriafly.salt.ui.outerPadding
import com.moriafly.salt.ui.pager.HorizontalPager
import com.moriafly.salt.ui.pager.VerticalPager
import com.moriafly.salt.ui.pager.rememberPagerState
import com.moriafly.salt.ui.rememberScrollState
import com.moriafly.salt.ui.sample.ui.screen.basic.BasicScreenColumn
import com.moriafly.salt.ui.sample.ui.icons.Like
import com.moriafly.salt.ui.sample.ui.icons.SimpleIcons
import com.moriafly.salt.ui.sample.ui.icons.Star
import com.moriafly.salt.ui.screen.ScreenCard

@OptIn(UnstableSaltUiApi::class)
@Composable
fun LayoutsScreen() {
    val horizontalPager = rememberPagerState { 5 }
    val verticalPager = rememberPagerState { 5 }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    BasicScreenColumn(title = "Layouts and scrolling") {
        ScreenCard(header = "Justified row") {
            JustifiedRow(
                startContent = { Text("Start content") },
                endContent = { Text("End content") },
                modifier = Modifier.innerPadding()
            )
        }

        ItemOuterTitle("Card")
        Card(modifier = Modifier.outerPadding()) {
            Text(text = "A standalone SaltUI card", modifier = Modifier.innerPadding())
        }

        ItemOuterTitle("Bottom bar")
        BottomBar(modifier = Modifier.outerPadding()) {
            BottomBarItem(
                state = selectedTab == 0,
                onClick = { selectedTab = 0 },
                painter = rememberVectorPainter(SimpleIcons.Star),
                text = "Favorites"
            )
            BottomBarItem(
                state = selectedTab == 1,
                onClick = { selectedTab = 1 },
                painter = rememberVectorPainter(SimpleIcons.Like),
                text = "Liked"
            )
        }

        ItemOuterTitle("Horizontal pager · ${horizontalPager.currentPage + 1} / 5")
        HorizontalPager(
            state = horizontalPager,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .outerPadding(),
            pageSpacing = 8.dp
        ) { page ->
            LayoutTile(text = "Page ${page + 1}", modifier = Modifier.fillMaxSize())
        }

        ItemOuterTitle("Vertical pager · ${verticalPager.currentPage + 1} / 5")
        VerticalPager(
            state = verticalPager,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .outerPadding(),
            pageSpacing = 8.dp
        ) { page ->
            LayoutTile(text = "Page ${page + 1}", modifier = Modifier.fillMaxSize())
        }

        ItemOuterTitle("Lazy grid")
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .outerPadding(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(count = 30) { index ->
                LayoutTile(text = "${index + 1}", modifier = Modifier.height(72.dp))
            }
        }

        ItemOuterTitle("Lazy row")
        LazyRow(
            modifier = Modifier.outerPadding(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(count = 30) { index ->
                LayoutTile(
                    text = "${index + 1}",
                    modifier = Modifier
                        .width(96.dp)
                        .height(72.dp)
                )
            }
        }

        ItemOuterTitle("Horizontal scroll")
        Row(
            modifier = Modifier
                .outerPadding()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(8) { index ->
                LayoutTile(
                    text = "${index + 1}",
                    modifier = Modifier
                        .width(96.dp)
                        .height(72.dp)
                )
            }
        }

        ItemOuterTitle("Horizontal grid")
        LazyHorizontalGrid(
            rows = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .outerPadding(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(count = 30) { index ->
                LayoutTile(
                    text = "${index + 1}",
                    modifier = Modifier
                        .width(88.dp)
                        .fillMaxSize()
                )
            }
        }

        ItemOuterTitle("Staggered grid")
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .outerPadding(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp
        ) {
            items(count = 30) { index ->
                LayoutTile(
                    text = "${index + 1}",
                    modifier = Modifier.height(if (index % 2 == 0) 96.dp else 64.dp)
                )
            }
        }

        ItemOuterTitle("Horizontal staggered grid")
        LazyHorizontalStaggeredGrid(
            rows = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .outerPadding(),
            horizontalItemSpacing = 8.dp,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(count = 30) { index ->
                LayoutTile(
                    text = "${index + 1}",
                    modifier = Modifier
                        .width(if (index % 2 == 0) 112.dp else 72.dp)
                        .fillMaxSize()
                )
            }
        }
    }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun LayoutTile(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SaltTheme.colors.highlight.copy(alpha = 0.12f))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text)
    }
}
