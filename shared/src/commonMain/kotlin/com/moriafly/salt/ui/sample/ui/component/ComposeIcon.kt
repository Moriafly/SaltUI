package com.moriafly.salt.ui.sample.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.Icon
import org.jetbrains.compose.resources.painterResource
import saltui.composeapp.generated.resources.Res
import saltui.composeapp.generated.resources.ic_compose_logo

@Composable
fun ComposeIcon(
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(Res.drawable.ic_compose_logo),
        contentDescription = null,
        modifier = modifier
            .size(20.dp),
        tint = Color.Unspecified
    )
}
