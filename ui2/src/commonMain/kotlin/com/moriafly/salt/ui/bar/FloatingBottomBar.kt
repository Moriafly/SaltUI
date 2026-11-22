/*
 * SaltUI
 * Copyright (C) 2026 Moriafly
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

package com.moriafly.salt.ui.bar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.ProvideContentColorTextStyle
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.material.GlassEffect
import com.moriafly.salt.ui.material.GlassSource
import com.moriafly.salt.ui.material.LocalGlassEffect
import com.moriafly.salt.ui.material.glass

/**
 * A floating navigation bar with a glass background.
 *
 * Share [glassSource] with the content behind the bar through
 * [Modifier.glassSource][com.moriafly.salt.ui.material.glassSource].
 * Add tabs with [FloatingBottomBarItem] in [content].
 */
@UnstableSaltUiApi
@Composable
fun FloatingBottomBar(
    glassSource: GlassSource,
    modifier: Modifier = Modifier,
    glassEffect: GlassEffect = LocalGlassEffect.current,
    content: @Composable FloatingBottomBarScope.() -> Unit
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .glass(
                source = glassSource,
                effect = glassEffect
            )
            .selectableGroup(),
        horizontalArrangement = Arrangement.Center
    ) {
        FloatingBottomBarScopeInstance.content()
    }
}

/**
 * The scope for declaring tabs in [FloatingBottomBar].
 */
@LayoutScopeMarker
@UnstableSaltUiApi
sealed interface FloatingBottomBarScope

@OptIn(UnstableSaltUiApi::class)
private object FloatingBottomBarScopeInstance : FloatingBottomBarScope

/**
 * A navigation tab with an [icon] above its [label].
 *
 * The caller supplies [selected] and updates it in response to [onClick].
 */
@UnstableSaltUiApi
@Suppress("UnusedReceiverParameter")
@Composable
fun FloatingBottomBarScope.FloatingBottomBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable () -> Unit
) {
    val colors = SaltTheme.colors
    val contentColor = when {
        !enabled -> colors.subText.copy(alpha = colors.subText.alpha * 0.38f)
        selected -> colors.highlight
        else -> colors.subText
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(80.dp)
            .selectable(
                selected = selected,
                interactionSource = null,
                indication = null,
                enabled = enabled,
                role = Role.Tab,
                onClick = onClick
            ),
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProvideContentColorTextStyle(
            contentColor = contentColor,
            textStyle = SaltTheme.textStyles.sub
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp),
                contentAlignment = Alignment.Center,
                propagateMinConstraints = true
            ) {
                icon()
            }

            label()
        }
    }
}
