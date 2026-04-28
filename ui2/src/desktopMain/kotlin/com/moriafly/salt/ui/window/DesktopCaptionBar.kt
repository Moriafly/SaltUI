package com.moriafly.salt.ui.window

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.moriafly.salt.ui.UnstableSaltUiApi

@UnstableSaltUiApi
@Composable
fun DesktopCaptionBar(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val saltWindowInfo = LocalSaltWindowInfo.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(saltWindowInfo.captionBarHeight),
        content = content
    )
}
