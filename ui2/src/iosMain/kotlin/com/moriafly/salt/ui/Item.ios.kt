/*
 * Salt UI
 * Copyright (C) 2024 Moriafly
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

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import saltui.ui2.generated.resources.Res
import saltui.ui2.generated.resources.ic_arrow_drop_down
import saltui.ui2.generated.resources.ic_chevron_right
import saltui.ui2.generated.resources.ic_item_link_android

@Composable
internal actual fun ItemArrow(arrowType: ItemArrowType) {
    if (arrowType != ItemArrowType.None) {
        Spacer(modifier = Modifier.width(SaltTheme.dimens.subPadding))
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
internal actual fun ItemPopupArrow() {
    Icon(
        modifier = Modifier
            .size(20.dp),
        painter = painterResource(Res.drawable.ic_arrow_drop_down),
        contentDescription = null,
        tint = SaltTheme.colors.subText
    )
}