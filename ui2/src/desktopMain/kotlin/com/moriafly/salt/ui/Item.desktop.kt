/*
 * Salt UI
 * Copyright (C) 2023 Moriafly
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import saltui.ui2.generated.resources.ic_item_expand_arrow
import saltui.ui2.generated.resources.ic_item_link

@Composable
actual fun ItemArrow(
    arrowType: ItemArrowType
) {
    if (arrowType != ItemArrowType.None) {
        Spacer(modifier = Modifier.width(SaltTheme.dimens.subPadding))
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

@Composable
actual fun ItemPopupArrow() {
    Icon(
        modifier = Modifier
            .size(16.dp)
            .padding(2.dp),
        painter = painterResource(Res.drawable.ic_item_expand_arrow),
        contentDescription = null,
        tint = SaltTheme.colors.text
    )
}