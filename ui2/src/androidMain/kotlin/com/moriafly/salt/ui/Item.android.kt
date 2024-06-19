package com.moriafly.salt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import saltui.ui2.generated.resources.Res
import saltui.ui2.generated.resources.ic_chevron_right
import saltui.ui2.generated.resources.ic_item_link_android

@Composable
internal actual fun ItemArrow(arrowType: ItemArrowType) {
    if (arrowType != ItemArrowType.None) {
        Spacer(modifier = Modifier.width(SaltTheme.dimens.contentPadding))
        Icon(
            modifier = Modifier
                .size(20.dp),
            painter = when (arrowType) {
                ItemArrowType.Arrow -> painterResource(Res.drawable.ic_chevron_right)
                ItemArrowType.Link -> painterResource(Res.drawable.ic_item_link_android)
                else -> painterResource(Res.drawable.ic_chevron_right)
            },
            contentDescription = null,
            tint = SaltTheme.colors.subText
        )
    }
}

@Composable
@UnstableSaltApi
actual fun ItemValue(
    text: String,
    sub: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = SaltTheme.dimens.item)
            .padding(vertical = SaltTheme.dimens.innerVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .sizeIn(
                    maxWidth = 80.dp
                )
                .weight(1f)
                .padding(start = SaltTheme.dimens.innerHorizontalPadding),
            text = text
        )

        Row(
            modifier = Modifier
                .weight(3f)
                .padding(start = SaltTheme.dimens.contentPadding, end = SaltTheme.dimens.innerHorizontalPadding),
            horizontalArrangement = Arrangement.End
        ) {
            SelectionContainer {
                Text(
                    text = sub,
                    color = SaltTheme.colors.subText,
                    textAlign = TextAlign.End,
                    style = SaltTheme.textStyles.main
                )
            }
        }
    }
}