/*
 * Salt UI
 * Copyright (C) 2025 Moriafly
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

package com.moriafly.salt.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.enabledAlpha
import com.moriafly.salt.ui.indication.AlphaContentIndication

@UnstableSaltUiApi
@Composable
fun TitleBarButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(TitleBarButtonDefaults.Size)
            .clickable(
                interactionSource = null,
                indication = AlphaContentIndication,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .enabledAlpha(enabled)
            .padding(8.dp),
        contentAlignment = Alignment.Center,
        propagateMinConstraints = true
    ) {
        icon()
    }
}

object TitleBarButtonDefaults {
    internal val Size: Dp = if (OS.isDesktop()) 36.dp else 40.dp
}
