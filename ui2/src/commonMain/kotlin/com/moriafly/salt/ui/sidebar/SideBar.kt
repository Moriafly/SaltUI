package com.moriafly.salt.ui.sidebar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.UnstableSaltUiApi

@UnstableSaltUiApi
@Composable
fun SideBar(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(220.dp)
    ) {
        content()
    }
}
