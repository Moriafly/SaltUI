package com.moriafly.salt.ui.sample.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.lazy.LazyColumn
import com.moriafly.salt.ui.lazy.rememberLazyListState
import com.moriafly.salt.ui.nested.NestedHeaderLayout
import com.moriafly.salt.ui.nested.rememberNestedHeaderState
import com.moriafly.salt.ui.pager.HorizontalPager
import com.moriafly.salt.ui.pager.rememberPagerState

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun NestedCollapsedLayoutScreen() {
    val nestedState = rememberNestedHeaderState()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 1. 嵌套滚动布局
        NestedHeaderLayout(
            state = nestedState,
            header = {
                // --- 专辑封面区域 ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp) // 设定一个足够的高度
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("专辑封面/信息", color = Color.White, fontSize = 24.sp)
                }
            },
            content = {
                HorizontalPager(
                    state = rememberPagerState {
                        2
                    }
                ) { page ->
                    when (page) {
                        0 -> TrackPager()
                        1 -> AlbumPager()
                    }
                }
            }
        )
    }
}

@Composable
private fun TrackPager() {
    Box(
        modifier = Modifier
    ) {
        val lazyListState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState
        ) {
            items(50) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable {
                        }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("歌曲名称 $index", color = Color.Gray)
                }
            }
        }
        val scope = rememberCoroutineScope()
//        Button(
//            onClick = {
//                scope.launch {
//                    // 滚动到顶部
//                    nestedState.collapse()
//                    lazyListState.animateScrollToItem(10)
//                }
//            },
//            text = "定位",
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .absoluteOffset {
//                    IntOffset(
//                        0,
//                        (nestedState.minOffset - nestedState.offset).roundToInt()
//                    )
//                }
//        )
    }
}

@Composable
private fun AlbumPager() {
    Box(
        modifier = Modifier
    ) {
        val lazyListState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState
        ) {
            items(50) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable {
                        }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("歌曲名称 $index", color = Color.Gray)
                }
            }
        }
        val scope = rememberCoroutineScope()
//        Button(
//            onClick = {
//                scope.launch {
//                    // 滚动到顶部
//                    nestedState.collapse()
//                    lazyListState.animateScrollToItem(10)
//                }
//            },
//            text = "定位",
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .absoluteOffset {
//                    IntOffset(
//                        0,
//                        (nestedState.minOffset - nestedState.offset).roundToInt()
//                    )
//                }
//        )
    }
}
