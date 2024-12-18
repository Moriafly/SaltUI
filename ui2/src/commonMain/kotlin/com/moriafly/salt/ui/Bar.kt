/**
 * Salt UI
 * Copyright (C) 2023 Moriafly
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.internal.stringResourceBack
import org.jetbrains.compose.resources.painterResource
import saltui.ui2.generated.resources.Res
import saltui.ui2.generated.resources.ic_arrow_back

/**
 * the title bar.
 */
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
                painter = painterResource(Res.drawable.ic_arrow_back),
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
 * the item in [BottomBar].
 */
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
