/*
 * Salt UI
 * Copyright (C) 2026 Moriafly
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

package com.moriafly.salt.ui.sample.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.ext.safeMainCompat
import com.moriafly.salt.ui.rememberScrollState
import com.moriafly.salt.ui.sample.ui.navigation.LocalNavBackStack
import com.moriafly.salt.ui.verticalScroll

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun BasicScreenColumn(
    title: String,
    showBackBtn: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeMainCompat
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
            )
    ) {
        val navBackStack = LocalNavBackStack.current
        TitleBar(
            onBack = {
                if (navBackStack.size > 1) {
                    navBackStack.removeLastOrNull()
                }
            },
            text = title,
            showBackBtn = showBackBtn
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            content()

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeMainCompat))
        }
    }
}

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun BasicScreenBox(
    title: String,
    showBackBtn: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeMainCompat
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
            )
    ) {
        val navBackStack = LocalNavBackStack.current
        TitleBar(
            onBack = {
                if (navBackStack.size > 1) {
                    navBackStack.removeLastOrNull()
                }
            },
            text = title,
            showBackBtn = showBackBtn
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            content()
        }
    }
}
