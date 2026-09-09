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

@file:Suppress("unused")

package com.moriafly.salt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.icons.ArrowBack
import com.moriafly.salt.ui.icons.SaltIcons
import com.moriafly.salt.ui.internal.stringResourceBack

/**
 * Title bar.
 */
@Suppress("ktlint:compose:modifier-missing-check")
@UnstableSaltUiApi
@Composable
fun TitleBar(
    onBack: () -> Unit,
    text: String,
    showBackBtn: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        if (showBackBtn) {
            val backButtonContentDescription = stringResourceBack()
            Icon(
                modifier = Modifier
                    .size(56.dp)
                    .semantics {
                        this.role = Role.Button
                        this.contentDescription = backButtonContentDescription
                    }
                    .noRippleClickable {
                        onBack()
                    }
                    .padding(18.dp),
                painter = rememberVectorPainter(SaltIcons.ArrowBack),
                contentDescription = stringResourceBack(),
                tint = SaltTheme.colors.text
            )
        }
        Text(
            text = text,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 56.dp),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

/**
 * the bottom bar.
 */
@UnstableSaltUiApi
@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = SaltTheme.colors.subBackground,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(color = backgroundColor)
    ) {
        content()
    }
}

/**
 * The item in [BottomBar].
 */
@Suppress("ktlint:compose:modifier-missing-check")
@UnstableSaltUiApi
@Composable
fun RowScope.BottomBarItem(
    state: Boolean,
    onClick: () -> Unit,
    painter: Painter,
    text: String
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .noRippleClickable {
                onClick()
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val color = if (state) {
            SaltTheme.colors.highlight
        } else {
            SaltTheme.colors.subText.copy(
                alpha = 0.5f
            )
        }
        Icon(
            modifier = Modifier
                .size(24.dp),
            painter = painter,
            contentDescription = null,
            tint = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            style = SaltTheme.textStyles.sub
        )
    }
}
