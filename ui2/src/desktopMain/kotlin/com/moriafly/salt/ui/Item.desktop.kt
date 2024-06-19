package com.moriafly.salt.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import saltui.ui2.generated.resources.Res
import saltui.ui2.generated.resources.ic_item_arrow
import saltui.ui2.generated.resources.ic_item_link

@Composable
internal actual fun ItemArrow(
    arrowType: ItemArrowType
) {
    if (arrowType != ItemArrowType.None) {
        Spacer(modifier = Modifier.width(SaltTheme.dimens.contentPadding))
        Icon(
            modifier = Modifier
                .size(16.dp)
                .padding(
                    when (arrowType) {
                        ItemArrowType.Arrow -> PaddingValues(2.dp)
                        ItemArrowType.Link -> PaddingValues(0.dp)
                        else -> PaddingValues(0.dp)
                    }
                ),
            painter = when (arrowType) {
                ItemArrowType.Arrow -> painterResource(Res.drawable.ic_item_arrow)
                ItemArrowType.Link -> painterResource(Res.drawable.ic_item_link)
                else -> painterResource(Res.drawable.ic_item_arrow)
            },
            contentDescription = null,
            tint = SaltTheme.colors.text
        )
    }
}