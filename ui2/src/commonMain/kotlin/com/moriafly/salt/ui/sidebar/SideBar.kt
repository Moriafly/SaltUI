package com.moriafly.salt.ui.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.material.subMaterial

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
            .subMaterial(fallback = SaltTheme.colors.subBackground)
    ) {
        Spacer(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(1.dp)
                .background(SaltTheme.colors.stroke)
        )

        content()
    }
}
