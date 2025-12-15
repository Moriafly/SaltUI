package com.moriafly.salt.ui.sample.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.lazy.LazyColumn
import com.moriafly.salt.ui.lazy.rememberLazyListState
import com.moriafly.salt.ui.nested.NestedHeaderLayout
import com.moriafly.salt.ui.nested.rememberNestedHeaderState
import com.moriafly.salt.ui.pager.HorizontalPager
import com.moriafly.salt.ui.pager.rememberPagerState
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun NestedCollapsedLayoutScreen() {
    val nestedState = rememberNestedHeaderState()

    // 计算 Header 的折叠进度 (0.0 -> 1.0)，用于控制 TopBar 变色
    // 假设 Header 高度约为 300dp，我们在滚动 200dp 时完全变色
    val toolbarAlpha by remember {
        derivedStateOf {
            (nestedState.offset.absoluteValue / 200f).coerceIn(0f, 1f)
        }
    }

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
                    // --- 歌曲列表区域 ---
                    // 注意：这里不需要再套 Scrollable，LazyColumn 自带滚动
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
                        Button(
                            onClick = {
                                scope.launch {
                                    // 滚动到顶部
                                    nestedState.collapse()
                                    lazyListState.animateScrollToItem(10)
                                }
                            },
                            text = "定位",
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .absoluteOffset {
                                    IntOffset(
                                        0,
                                        (nestedState.minOffset - nestedState.offset).roundToInt()
                                    )
                                }
                        )
                    }
                }
            }
        )
    }
}
