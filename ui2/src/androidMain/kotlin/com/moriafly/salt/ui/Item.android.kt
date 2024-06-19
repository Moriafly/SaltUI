package com.moriafly.salt.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import saltui.ui2.generated.resources.Res
import saltui.ui2.generated.resources.ic_chevron_right

@Composable
internal actual fun ItemArrow(arrowType: ItemArrowType) {
    if (arrowType != ItemArrowType.None) {
        Spacer(modifier = Modifier.width(SaltTheme.dimens.contentPadding))
        Icon(
            modifier = Modifier
                .size(20.dp),
            painter = painterResource(Res.drawable.ic_chevron_right),
            contentDescription = null,
            tint = if (arrowType == ItemArrowType.Link) SaltTheme.colors.highlight else SaltTheme.colors.subText
        )
    }
}