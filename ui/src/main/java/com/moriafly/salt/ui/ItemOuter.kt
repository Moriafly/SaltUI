/**
 * SaltUI
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

@file:Suppress("UNUSED")

package com.moriafly.salt.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/**
 * Build content interface title text (Out of RoundedColumn)
 */
@Composable
fun ItemOuterTitle(
    text: String
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SaltTheme.dimens.innerHorizontalPadding, vertical = SaltTheme.dimens.innerVerticalPadding),
        fontWeight = FontWeight.Bold,
        style = SaltTheme.textStyles.sub
    )
}

/**
 * Can replace ItemTitle
 */
@UnstableSaltApi
@Composable
fun ItemOuterLargeTitle(
    text: String,
    sub: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SaltTheme.dimens.outerHorizontalPadding * 2, vertical = SaltTheme.dimens.outerVerticalPadding * 6),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            style = SaltTheme.textStyles.main
        )
        Spacer(modifier = Modifier.height(SaltTheme.dimens.contentPadding * 1.5f))
        Text(
            text = sub,
            textAlign = TextAlign.Center,
            style = SaltTheme.textStyles.paragraph
        )
    }
}

/**
 * ItemOuter
 */
@UnstableSaltApi
@Composable
fun ItemOuter(
    onClick: () -> Unit,
    text: String
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SaltTheme.dimens.outerHorizontalPadding, vertical = SaltTheme.dimens.outerVerticalPadding)
            .fadeClickable {
                onClick()
            },
        color = SaltTheme.colors.highlight,
        fontWeight = FontWeight.Bold,
        style = SaltTheme.textStyles.main
    )
}

/**
 * For Item Outer
 */
@Composable
fun ItemOuterTextButton(
    onClick: () -> Unit,
    text: String,
    textColor: Color = Color.White,
    backgroundColor: Color = SaltTheme.colors.highlight
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = SaltTheme.dimens.outerHorizontalPadding + SaltTheme.dimens.innerHorizontalPadding, vertical = SaltTheme.dimens.innerVerticalPadding),
        text = text,
        textColor = textColor,
        backgroundColor = backgroundColor
    )
}

/**
 * Build vertical spacing for the Item Outer
 */
@Composable
fun ItemOutSpacer() {
    Spacer(
        modifier = Modifier
            .height(SaltTheme.dimens.outerVerticalPadding * 2)
    )
}

/**
 * Build half vertical spacing for the Item Outer
 */
@Composable
fun ItemOutHalfSpacer() {
    Spacer(
        modifier = Modifier
            .height(SaltTheme.dimens.outerVerticalPadding)
    )
}