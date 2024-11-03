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

@file:Suppress("UNUSED")

package com.moriafly.salt.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.popup.PopupMenu
import com.moriafly.salt.ui.popup.PopupState
import org.jetbrains.compose.resources.painterResource
import saltui.ui2.generated.resources.Res
import saltui.ui2.generated.resources.ic_check
import saltui.ui2.generated.resources.ic_closed_eye
import saltui.ui2.generated.resources.ic_error
import saltui.ui2.generated.resources.ic_eye
import saltui.ui2.generated.resources.ic_uncheck
import saltui.ui2.generated.resources.ic_warning

/**
 * Draw an arrow icon in [Item]
 *
 * This can facilitate the implementation of fully customizable [Item]
 *
 * @param arrowType [ItemArrowType]
 */
@Composable
expect fun ItemArrow(
    arrowType: ItemArrowType
)

/**
 * Build content interface title text
 */
@Deprecated(
    message = "Use ItemOuterTitle instead",
    replaceWith = ReplaceWith(
        expression = "ItemOuterTitle(text = text)",
        imports = arrayOf("com.moriafly.salt.ui.ItemOuterTitle")
    ),
    level = DeprecationLevel.WARNING
)
@Composable
fun ItemTitle(
    text: String
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .innerPadding(),
        color = SaltTheme.colors.highlight,
        fontWeight = FontWeight.Bold,
        style = SaltTheme.textStyles.sub
    )
}

/**
 * Build content interface tip text
 *
 * @param text text
 */
@Composable
fun ItemTip(
    text: String
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .innerPadding(),
        style = SaltTheme.textStyles.sub
    )
}

/**
 * Build content interface instruction text
 *
 * @param text text
 */
@Deprecated(
    message = "Use ItemTip instead. In actual development, an ItemSpacer is almost always needed above and below the ItemText, so this padding is more suitable to be included within the component. " +
            "Note that after the replacement, remove the ItemSpacer above and below",
    replaceWith = ReplaceWith(
        expression = "ItemTip(text = text)",
        imports = arrayOf("com.moriafly.salt.ui.ItemText")
    ),
    level = DeprecationLevel.WARNING
)
@Composable
fun ItemText(
    text: String
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .innerPadding(vertical = false),
        style = SaltTheme.textStyles.sub
    )
}

enum class ItemArrowType {
    Arrow,
    None,
    Link
}

/**
 * Build item for the content interface
 *
 * @param onClick will be called when user clicks on the element
 * @param text main text
 * @param modifier [Modifier] to apply to this layout node
 * @param enabled enabled
 * @param iconPainter icon
 * @param iconPaddingValues iconPaddingValues
 * @param iconColor color of [iconPainter], if this value is null, will use the paint original color
 * @param textColor color of [text] text, you can set highlight to replace ItemTextButton
 * @param sub sub text
 * @param subColor color of [sub] text
 * @param subContent Allow customizing the region of existing sub text
 * @param arrowType type of arrow
 */
@Composable
fun Item(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = SaltTheme.colors.text,
    textColor: Color = SaltTheme.colors.text,
    sub: String? = null,
    subColor: Color = SaltTheme.colors.subText,
    subContent: (@Composable () -> Unit)? = null,
    arrowType: ItemArrowType = ItemArrowType.Arrow
) {
    BasicItem(
        onClick = onClick,
        text = text,
        modifier = modifier,
        enabled = enabled,
        iconPainter = iconPainter,
        iconPaddingValues = iconPaddingValues,
        iconColor = iconColor,
        textColor = textColor,
        arrowType = arrowType
    ) {
        sub?.let {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sub,
                color = subColor,
                style = SaltTheme.textStyles.sub
            )
        }

        subContent?.let {
            Spacer(modifier = Modifier.height(2.dp))
            subContent()
        }
    }
}

/**
 * Build a switcher in the content interface
 *
 * @param state the state of the switcher
 * @param onChange called when state changed
 * @param text main text
 * @param enabled
 * @param iconPainter icon
 * @param iconPaddingValues iconPaddingValues
 * @param iconColor color of [iconPainter], if this value is null, will use the paint original color
 * @param sub sub text
 */
@Composable
fun ItemSwitcher(
    state: Boolean,
    onChange: (Boolean) -> Unit,
    text: String,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = SaltTheme.colors.text,
    sub: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(SaltTheme.dimens.item)
            .alpha(if (enabled) 1f else 0.5f)
            .toggleable(
                value = state,
                enabled = enabled,
                role = Role.Switch
            ) {
                onChange(!state)
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
                color = if (enabled) SaltTheme.colors.text else SaltTheme.colors.subText
            )
            sub?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sub,
                    style = SaltTheme.textStyles.sub
                )
            }
        }
        Spacer(modifier = Modifier.width(SaltTheme.dimens.subPadding))
        Switcher(
            state = state
        )
    }
}

/**
 * Draw an arrow icon in [ItemPopup]
 *
 * This can facilitate the implementation of fully customizable [ItemPopup]
 */
@Composable
expect fun ItemPopupArrow()

/**
 * Popup a [PopupMenu] where many selectable or common items can be added
 *
 * TODO Replace [ItemPopup]
 *
 * @param state [PopupMenu]
 * @param text Text
 * @param value Text of value
 * @param enabled Enabled
 * @param iconPainter Icon
 * @param iconPaddingValues Padding values of [iconPainter]
 * @param iconColor Color of [iconPainter], if this value is null, will use the paint original color
 * @param content Composable content
 */
@UnstableSaltApi
@Composable
fun ItemSelect(
    state: PopupState,
    text: String,
    value: String,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = SaltTheme.colors.text,
    content: @Composable ColumnScope.() -> Unit
) {
    ItemPopup(
        state = state,
        text = text,
        sub = value,
        enabled = enabled,
        iconPainter = iconPainter,
        iconPaddingValues = iconPaddingValues,
        iconColor = iconColor,
        content = content
    )
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
    text: String,
    sub: String,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = SaltTheme.colors.text,
    content: @Composable ColumnScope.() -> Unit
) {
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(SaltTheme.dimens.item)
                .alpha(if (enabled) 1f else 0.5f)
                .clickable(enabled = enabled) {
                    state.expend()
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
                    color = if (enabled) SaltTheme.colors.text else SaltTheme.colors.subText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sub,
                    style = SaltTheme.textStyles.sub
                )
            }
            Spacer(modifier = Modifier.width(SaltTheme.dimens.subPadding))

            ItemPopupArrow()
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
    text: String,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(SaltTheme.dimens.item)
            .alpha(if (enabled) 1f else 0.5f)
            .toggleable(
                value = state,
                enabled = enabled,
                role = Role.Checkbox
            ) {
                onChange(!state)
            }
            .innerPadding(vertical = false),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .size(SaltTheme.dimens.itemIcon),
            painter = if (state) painterResource(Res.drawable.ic_check) else painterResource(Res.drawable.ic_uncheck),
            contentDescription = null,
            tint = SaltTheme.colors.highlight
        )
        Spacer(modifier = Modifier.width(SaltTheme.dimens.subPadding))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .innerPadding(horizontal = false)
        ) {
            Text(
                text = text,
                color = if (enabled) SaltTheme.colors.text else SaltTheme.colors.subText
            )
        }
    }
}

/**
 * Item Value
 *
 * @param text text
 * @param sub value
 */
@Composable
fun ItemValue(
    text: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(true) { }
            .innerPadding()
    ) {
        Text(
            text = text,
            style = SaltTheme.textStyles.sub
        )
        Spacer(modifier = Modifier.height(2.dp))
        SelectionContainer {
            Text(
                text = sub
            )
        }
    }
}

/**
 * Edit
 *
 * @param text text
 * @param onChange called when text changed
 * @param modifier modifier
 * @param hint hint
 * @param hintColor color of [hint] text
 * @param readOnly readOnly
 * @param keyboardOptions keyboardOptions
 * @param keyboardActions keyboardActions
 * @param visualTransformation visualTransformation
 * @param actionContent actionContent
 */
@UnstableSaltApi
@Composable
fun ItemEdit(
    text: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
    hintColor: Color = SaltTheme.colors.subText,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    actionContent: (@Composable () -> Unit)? = null
) {
    BasicItemEdit(
        text = text,
        onChange = onChange,
        modifier = modifier,
        paddingValues = SaltTheme.dimens.innerPaddingValues,
        hint = hint,
        hintColor = hintColor,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        actionContent = actionContent
    )
}

/**
 * Password Edit
 *
 * @param text text
 * @param onChange called when text changed
 * @param hint hint
 * @param hintColor color of [hint] text
 * @param readOnly readOnly
 * @param keyboardOptions keyboardOptions
 * @param keyboardActions keyboardActions
 */
@UnstableSaltApi
@Composable
fun ItemEditPassword(
    text: String,
    onChange: (String) -> Unit,
    hint: String? = null,
    hintColor: Color = SaltTheme.colors.subText,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Password
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var hidden by remember { mutableStateOf(true) }

    ItemEdit(
        text = text,
        onChange = onChange,
        hint = hint,
        hintColor = hintColor,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = if (hidden) PasswordVisualTransformation() else VisualTransformation.None,
        actionContent = {
            Icon(
                painter = painterResource(
                    if (hidden) {
                        Res.drawable.ic_closed_eye
                    } else {
                        Res.drawable.ic_eye
                    }
                ),
                contentDescription = null,
                modifier = Modifier
                    .toggleable(
                        value = hidden,
                        role = Role.Switch
                    ) {
                        hidden = !hidden
                    }
                    .padding(
                        start = SaltTheme.dimens.subPadding,
                        top = SaltTheme.dimens.subPadding,
                        end = SaltTheme.dimens.padding,
                        bottom = SaltTheme.dimens.subPadding
                    )
                    .size(20.dp),
                tint = SaltTheme.colors.subText
            )
        }
    )
}

@UnstableSaltApi
@Composable
fun ItemSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    text: String,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = null,
    sub: String? = null,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    /*@IntRange(from = 0)*/
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(true) { }
            .innerPadding(vertical = false)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else 0.5f),
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
            Text(
                text = text,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .innerPadding(horizontal = false),
                color = if (enabled) SaltTheme.colors.text else SaltTheme.colors.subText
            )
            Spacer(modifier = Modifier.width(SaltTheme.dimens.subPadding))
            sub?.let {
                Text(
                    text = sub,
                    color = if (enabled) SaltTheme.colors.text else SaltTheme.colors.subText
                )
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth(),
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            interactionSource = interactionSource
        )
        ItemSpacer()
    }
}

/**
 * Item Button
 *
 * @param primary Indicates whether the button is primary and prominent, emphasized with a highlight color
 */
@UnstableSaltApi
@Composable
fun ItemButton(
    onClick: () -> Unit,
    text: String,
    enabled: Boolean = true,
    primary: Boolean = true,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = SaltTheme.colors.highlight
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(SaltTheme.dimens.item)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClickLabel = text
            ) {
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
        Text(
            text = text,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .innerPadding(horizontal = false),
            color = if (enabled && primary) SaltTheme.colors.highlight else SaltTheme.colors.subText,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Build vertical spacing [SaltDimens.subPadding] for the content interface
 */
@Composable
fun ItemSpacer() {
    Spacer(
        modifier = Modifier
            .height(SaltTheme.dimens.subPadding)
    )
}

/**
 * Build a container with internal margins in the content interface, making it easy to add custom elements such as buttons internally
 */
@Composable
fun ItemContainer(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .innerPadding()
    ) {
        content()
    }
}

/**
 * Build a divider in the content interface
 *
 * @param color color of divider
 */
@Composable
fun ItemDivider(
    modifier: Modifier = Modifier,
    color: Color = SaltTheme.colors.stroke,
    startIndent: Dp = SaltTheme.dimens.padding
) {
    Divider(
        modifier = modifier,
        color = color,
        thickness = Dp.Hairline,
        startIndent = startIndent
    )
}

@UnstableSaltApi
enum class ItemInfoType {
    Warning,
    Error
}

@UnstableSaltApi
@Composable
fun ItemInfo(
    text: String,
    infoType: ItemInfoType
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                when (infoType) {
                    ItemInfoType.Warning -> if (SaltTheme.configs.isDarkTheme) SaltPalette.WarningDarkBackground else SaltPalette.WarningLightBackground
                    ItemInfoType.Error -> if (SaltTheme.configs.isDarkTheme) SaltPalette.ErrorDarkBackground else SaltPalette.ErrorLightBackground
                }
            )
            .semantics(true) { }
            .innerPadding(vertical = false),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .size(SaltTheme.dimens.itemIcon),
            painter = painterResource(
                when (infoType) {
                    ItemInfoType.Warning -> Res.drawable.ic_warning
                    ItemInfoType.Error -> Res.drawable.ic_error
                }
            ),
            contentDescription = null,
            colorFilter = ColorFilter.tint(
                when (infoType) {
                    ItemInfoType.Warning -> if (SaltTheme.configs.isDarkTheme) SaltPalette.WarningDarkIcon else SaltPalette.WarningLightIcon
                    ItemInfoType.Error -> if (SaltTheme.configs.isDarkTheme) SaltPalette.ErrorDarkIcon else SaltPalette.ErrorLightIcon
                }
            )
        )
        Spacer(modifier = Modifier.width(SaltTheme.dimens.subPadding))
        Text(
            text = text,
            modifier = Modifier
                .innerPadding(horizontal = false),
            color = SaltTheme.colors.text
        )
    }
}