@file:Suppress("UNUSED")

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

package com.moriafly.salt.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.popup.PopupMenu
import com.moriafly.salt.ui.popup.PopupMenuItem
import com.moriafly.salt.ui.popup.PopupMenuItemPosition
import com.moriafly.salt.ui.popup.PopupState

/**
 * Build content interface title text.
 */
@Composable
fun ItemTitle(
    text: String
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.innerHorizontalPadding, vertical = Dimens.innerVerticalPadding),
        color = SaltTheme.colors.highlight,
        fontWeight = FontWeight.Bold,
        style = SaltTheme.textStyles.sub
    )
}

/**
 * Build content interface instruction text.
 *
 * @param text text
 */
@Composable
fun ItemText(
    text: String
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.innerHorizontalPadding),
        style = SaltTheme.textStyles.sub
    )
}

/**
 * Build item for the content interface.
 *
 * @param onClick will be called when user clicks on the element
 * @param enabled enabled
 * @param iconPainter icon
 * @param iconPaddingValues iconPaddingValues
 * @param iconColor color of [iconPainter], if this value is null, will use the paint original color
 * @param text main text
 * @param sub sub text
 * @param subColor color of [sub] text
 */
@Composable
fun Item(
    onClick: () -> Unit,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = null,
    text: String,
    sub: String? = null,
    subColor: Color = SaltTheme.colors.subText
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled) {
                onClick()
            }
            .padding(horizontal = Dimens.innerHorizontalPadding, vertical = Dimens.innerVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        iconPainter?.let {
            Image(
                modifier = Modifier
                    .size(24.dp)
                    .padding(iconPaddingValues),
                painter = iconPainter,
                contentDescription = null,
                colorFilter = iconColor?.let { ColorFilter.tint(iconColor) }
            )
            Spacer(modifier = Modifier.width(Dimens.contentPadding))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Text(
                text = text,
                color = if (enabled) SaltTheme.colors.text else SaltTheme.colors.subText,
                style = SaltTheme.textStyles.main
            )
            sub?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sub,
                    color = subColor,
                    style = SaltTheme.textStyles.sub
                )
            }
        }
        Spacer(modifier = Modifier.width(Dimens.contentPadding))
        Icon(
            modifier = Modifier
                .size(20.dp),
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = SaltTheme.colors.subText
        )
    }
}

/**
 * Build a switcher in the content interface
 *
 * @param state the state of the switcher
 * @param onChange called when state changed
 * @param enabled
 * @param iconPainter icon
 * @param iconPaddingValues iconPaddingValues
 * @param iconColor color of [iconPainter], if this value is null, will use the paint original color
 * @param text main text
 * @param sub sub text
 */
@Composable
fun ItemSwitcher(
    state: Boolean,
    onChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = null,
    text: String,
    sub: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled) {
                onChange(!state)
            }
            .padding(horizontal = Dimens.innerHorizontalPadding, vertical = Dimens.innerVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        iconPainter?.let {
            Image(
                modifier = Modifier
                    .size(24.dp)
                    .padding(iconPaddingValues),
                painter = iconPainter,
                contentDescription = null,
                colorFilter = iconColor?.let { ColorFilter.tint(iconColor) }
            )
            Spacer(modifier = Modifier.width(Dimens.contentPadding))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Text(
                text = text,
                color = if (enabled) SaltTheme.colors.text else SaltTheme.colors.subText,
                style = SaltTheme.textStyles.main
            )
            sub?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sub,
                    style = SaltTheme.textStyles.sub
                )
            }
        }
        Spacer(modifier = Modifier.width(Dimens.contentPadding))
        val backgroundColor by animateColorAsState(
            targetValue = if (state) SaltTheme.colors.highlight else SaltTheme.colors.subText.copy(alpha = 0.1f),
            animationSpec = spring(),
            label = "backgroundColor"
        )
        Box(
            modifier = Modifier
                .size(46.dp, 26.dp)
                .clip(CircleShape)
                .drawBehind {
                    drawRect(color = backgroundColor)
                }
                .padding(5.dp)
        ) {
            val translationX by animateDpAsState(
                targetValue = if (state) 20.dp else 0.dp,
                animationSpec = spring(),
                label = "startPadding"
            )
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        this.translationX = translationX.toPx()
                    }
                    .size(16.dp)
                    .border(width = 4.dp, color = Color.White, shape = CircleShape)
            )
        }
    }
}

/**
 * Popup Item
 *
 * @param state the state of popup
 */
@UnstableSaltApi
@Composable
fun ItemPopup(
    state: PopupState,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = null,
    text: String,
    sub: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .alpha(if (enabled) 1f else 0.5f)
                .clickable(enabled = enabled) {
                    state.expend()
                }
                .padding(horizontal = Dimens.innerHorizontalPadding, vertical = Dimens.innerVerticalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            iconPainter?.let {
                Image(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(iconPaddingValues),
                    painter = iconPainter,
                    contentDescription = null,
                    colorFilter = iconColor?.let { ColorFilter.tint(iconColor) }
                )
                Spacer(modifier = Modifier.width(Dimens.contentPadding))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(
                    text = text,
                    color = if (enabled) SaltTheme.colors.text else SaltTheme.colors.subText,
                    style = SaltTheme.textStyles.main
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sub,
                    style = SaltTheme.textStyles.sub
                )
            }
            Spacer(modifier = Modifier.width(Dimens.contentPadding))
            Icon(
                modifier = Modifier
                    .size(20.dp),
                painter = painterResource(id = R.drawable.ic_arrow_drop_down),
                contentDescription = null,
                tint = SaltTheme.colors.subText
            )


        }
        PopupMenu(
            expanded = state.expend,
            onDismissRequest = {
                state.dismiss()
            }
        ) {
            content()
        }
    }
}

/**
 * Build a switcher in the content interface
 *
 * @param state the state of the switcher
 * @param onChange called when state changed
 * @param enabled
 * @param text main text
 */
@UnstableSaltApi
@Composable
fun ItemCheck(
    state: Boolean,
    onChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled) {
                onChange(!state)
            }
            .padding(horizontal = Dimens.innerHorizontalPadding, vertical = Dimens.innerVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .size(24.dp),
            painter = if (state) painterResource(id = R.drawable.ic_check) else painterResource(id = R.drawable.ic_uncheck),
            contentDescription = null,
            tint = SaltTheme.colors.highlight
        )
        Spacer(modifier = Modifier.width(Dimens.contentPadding))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Text(
                text = text,
                color = if (enabled && state) SaltTheme.colors.text else SaltTheme.colors.subText,
                style = SaltTheme.textStyles.main
            )
        }
    }
}

/**
 * Value
 */
@UnstableSaltApi
@Composable
fun ItemValue(
    text: String,
    sub: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 48.dp)
            .padding(horizontal = Dimens.innerHorizontalPadding, vertical = Dimens.innerVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .weight(1f),
            text = text,
            style = SaltTheme.textStyles.main
        )
        Spacer(modifier = Modifier.width(Dimens.contentPadding))
        SelectionContainer(
            modifier = Modifier
                .weight(1f),
        ) {
            Text(
                text = sub,
                color = SaltTheme.colors.subText,
                fontSize = 15.sp,
                textAlign = TextAlign.End,
                style = SaltTheme.textStyles.main
            )
        }
    }
}

/**
 * Edit
 *
 * @param text
 * @param onChange
 * @param hint
 * @param readOnly
 * @param contentPaddingValues padding in this, beautiful for IME
 * @param keyboardOptions
 * @param keyboardActions
 */
@UnstableSaltApi
@Composable
fun ItemEdit(
    text: String,
    onChange: (String) -> Unit,
    hint: String? = null,
    readOnly: Boolean = false,
    contentPaddingValues: PaddingValues = PaddingValues(vertical = Dimens.innerVerticalPadding),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    BasicTextField(
        value = text,
        onValueChange = onChange,
        modifier = Modifier
            .padding(horizontal = Dimens.innerHorizontalPadding),
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = SaltTheme.textStyles.main,
        cursorBrush = SolidColor(SaltTheme.colors.highlight),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPaddingValues)
                    .clip(RoundedCornerShape(SaltTheme.dimens.corner))
                    .background(color = SaltTheme.colors.subText.copy(alpha = 0.1f))
                    .padding(Dimens.contentPadding),
                contentAlignment = Alignment.CenterStart
            ) {
                innerTextField()
                if (hint != null && text.isEmpty()) {
                    Text(
                        text = hint,
                        color = SaltTheme.colors.subText.copy(alpha = 0.5f),
                        style = SaltTheme.textStyles.main
                    )
                }
            }
        }
    )
}

/**
 * Build vertical spacing for the content interface.
 */
@Composable
fun ItemSpacer() {
    Spacer(
        modifier = Modifier
            .height(Dimens.innerVerticalPadding)
    )
}

/**
 * Build a container with internal margins in the content interface, making it easy to add custom elements such as buttons internally.
 */
@Composable
fun ItemContainer(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = Dimens.innerHorizontalPadding, vertical = Dimens.innerVerticalPadding)
    ) {
        content()
    }
}