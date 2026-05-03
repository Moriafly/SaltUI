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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemOuterLargeTitle
import com.moriafly.salt.ui.ItemOuterTip
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.button.PillButton
import com.moriafly.salt.ui.rememberScrollState
import com.moriafly.salt.ui.sample.ui.icons.Like
import com.moriafly.salt.ui.sample.ui.icons.SimpleIcons
import com.moriafly.salt.ui.sample.ui.icons.Star
import com.moriafly.salt.ui.sample.ui.navigation.LocalNavBackStack
import com.moriafly.salt.ui.sample.ui.navigation.ScreenRoute
import com.moriafly.salt.ui.screen.BasicScreen
import com.moriafly.salt.ui.verticalScroll

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun AboutScreen() {
    val navBackStack = LocalNavBackStack.current
    BasicScreen(
        onBack = {
            navBackStack.removeLastOrNull()
        },
        title = "About",
        toolButtons = {
            PillButton(
                onClick = {}
            ) {
                Icon(
                    painter = rememberVectorPainter(SimpleIcons.Like),
                    contentDescription = null
                )
            }
            PillButton(
                onClick = {}
            ) {
                Icon(
                    painter = rememberVectorPainter(SimpleIcons.Star),
                    contentDescription = null
                )
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(contentPadding.calculateTopPadding()))

            ItemOuterLargeTitle(
                text = "Salt UI 3",
                sub = "UI Components for Compose Multiplatform (Android/Desktop/iOS)"
            )

            RoundedColumn {
                Item(
                    onClick = {
                        navBackStack.add(ScreenRoute.License)
                    },
                    text = "License"
                )
            }
        }
    }
}
