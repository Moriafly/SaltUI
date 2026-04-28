package com.moriafly.salt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.moriafly.salt.core.os.OS

@UnstableSaltUiApi
@Composable
fun Layer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDarkTheme = SaltTheme.configs.isDarkTheme
    Surface(
        modifier = modifier
            .clip(IslandDefaults.IslandShape)
            .border(
                width = 1.dp,
                color = Color(0x09000000),
                shape = IslandDefaults.IslandShape
            )
            .background(
                if (isDarkTheme) {
                    Color(0x1E3A3A3A)
                } else {
                    Color(0x64FFFFFF)
                }
            ),
        content = content
    )
}

object IslandDefaults {
    val IslandShape: Shape =
        // TODO
        when (OS.current) {
            else -> RoundedCornerShape(topStart = 12.dp)
        }
}
