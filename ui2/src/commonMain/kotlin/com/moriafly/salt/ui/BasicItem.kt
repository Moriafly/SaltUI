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

@file:Suppress("unused")

package com.moriafly.salt.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
internal fun BasicItem(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = SaltTheme.colors.text,
    textColor: Color = SaltTheme.colors.text,
    arrowType: ItemArrowType = ItemArrowType.Arrow,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(SaltTheme.dimens.item)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled) {
                onClick()
            }
            .innerPadding(vertical = false),
        verticalAlignment = Alignment.CenterVertically
    ) {
        iconPainter?.let {
            Image(
                modifier = Modifier
                    .size(SaltTheme.dimens.itemIcon)
                    .padding(iconPaddingValues),
                painter = iconPainter,
                contentDescription = null,
                colorFilter = iconColor?.let { ColorFilter.tint(iconColor) }
            )
            Spacer(modifier = Modifier.width(SaltTheme.dimens.subPadding))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .innerPadding(horizontal = false)
        ) {
            Text(
                text = text,
                color = if (enabled) textColor else SaltTheme.colors.subText
            )
            content()
        }

        ItemArrow(
            arrowType = arrowType
        )
    }
}

@UnstableSaltUiApi
@Composable
internal fun BasicItemEdit(
    text: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: SaltPaddingValues,
    hint: String? = null,
    hintColor: Color = SaltTheme.colors.subText,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    actionContent: (@Composable () -> Unit)? = null
) {
    BasicTextField(
        value = text,
        onValueChange = onChange,
        modifier = modifier,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = SaltTheme.textStyles.main,
        visualTransformation = visualTransformation,
        cursorBrush = SolidColor(SaltTheme.colors.highlight),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = SaltTheme.dimens.item)
                    .padding(start = paddingValues.start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = paddingValues.top, bottom = paddingValues.bottom)
                ) {
                    innerTextField()
                    if (hint != null && text.isEmpty()) {
                        Text(
                            text = hint,
                            color = hintColor
                        )
                    }
                }
                if (actionContent != null) {
                    actionContent()
                } else {
                    Spacer(modifier = Modifier.width(paddingValues.end))
                }
            }
        }
    )
}