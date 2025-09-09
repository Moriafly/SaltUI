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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.icons.Check
import com.moriafly.salt.ui.icons.SaltIcons
import com.moriafly.salt.ui.icons.Success
import com.moriafly.salt.ui.icons.Uncheck
import com.moriafly.salt.ui.popup.PopupMenu
import com.moriafly.salt.ui.popup.PopupState
import org.jetbrains.compose.resources.painterResource
import saltui.ui2.generated.resources.Res
import saltui.ui2.generated.resources.ic_closed_eye
import saltui.ui2.generated.resources.ic_error
import saltui.ui2.generated.resources.ic_eye
import saltui.ui2.generated.resources.ic_warning

/**
 * Draw an arrow icon in [Item].
 *
 * This can facilitate the implementation of fully customizable [Item].
 *
 * @param arrowType [ItemArrowType].
 */
@Composable
expect fun ItemArrow(arrowType: ItemArrowType)

/**
 * Build content interface title text.
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
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .innerPadding(),
        color = SaltTheme.colors.highlight,
        fontWeight = FontWeight.Bold,
        style = SaltTheme.textStyles.sub
    )
}

/**
 * Build content interface tip text.
 *
 * @param text text.
 */
@Composable
fun ItemTip(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .innerPadding(),
        color = SaltTheme.colors.subText,
        style = SaltTheme.textStyles.sub
    )
}

/**
 * Build content interface instruction text.
 *
 * @param text text.
 */
@Deprecated(
    message = "Use ItemTip instead. In actual development, an ItemSpacer is almost always needed " +
        "above and below the ItemText, so this padding is more suitable to be included within " +
        "the component. Note that after the replacement, remove the ItemSpacer above and below.",
    replaceWith = ReplaceWith(
        expression = "ItemTip(text = text)",
        imports = arrayOf("com.moriafly.salt.ui.ItemText")
    ),
    level = DeprecationLevel.WARNING
)
@Composable
fun ItemText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .innerPadding(vertical = false),
        color = SaltTheme.colors.subText,
        style = SaltTheme.textStyles.sub
    )
}

enum class ItemArrowType {
    Arrow,
    None,
    Link
}

/**
 * Builds a list item for content interfaces.
 *
 * This composable provides a standard list item layout including main text, optional sub text,
 * icon, tag, and an arrow indicator.
 *
 * @param onClick Callback invoked when the user clicks on the item.
 * @param text The main text content.
 * @param modifier [Modifier] to apply to this layout node.
 * @param enabled Controls the enabled state of the item. When `false`, this component will not
 * respond to user input.
 * @param iconPainter Optional icon painter displayed at the start of the item.
 * @param iconPaddingValues Padding values applied around the icon.
 * @param iconColor Tint color for the icon. If `null`, the original color of the [iconPainter] is
 * used.
 * @param textColor Color of the main [text].
 * @param sub Optional sub text displayed below the main text.
 * @param subColor Color of the [sub] text.
 * @param subContent Optional composable content to replace or complement the [sub] text area.
 * @param tag Optional tag text displayed at the end of the item, typically used for status or
 * metadata.
 * @param arrowType Type of arrow indicator displayed at the end of the item. Defaults to
 * [ItemArrowType.Arrow].
 */
@OptIn(UnstableSaltUiApi::class)
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
    tag: String? = null,
    arrowType: ItemArrowType = ItemArrowType.Arrow
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(SaltTheme.dimens.item)
            .enabledAlpha(enabled)
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
        JustifiedRow(
            startContent = {
                Column {
                    Text(
                        text = text,
                        color = textColor
                    )
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
            },
            endContent = {
                tag?.let {
                    Text(
                        text = tag,
                        color = SaltTheme.colors.subText,
                        style = SaltTheme.textStyles.sub
                    )
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .innerPadding(horizontal = false),
            verticalAlignment = Alignment.CenterVertically,
            spaceBetween = if (tag != null) {
                SaltTheme.dimens.subPadding
            } else {
                0.dp
            }
        )
        ItemArrow(
            arrowType = arrowType
        )
    }
}

/**
 * Build a switcher in the content interface.
 *
 * @param state the state of the switcher.
 * @param onChange called when state changed.
 * @param text main text.
 * @param enabled enabled.
 * @param iconPainter icon.
 * @param iconPaddingValues iconPaddingValues.
 * @param iconColor color of [iconPainter], if this value is null, will use the paint original
 *   color.
 * @param sub sub text.
 */
@Composable
fun ItemSwitcher(
    state: Boolean,
    onChange: (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = SaltTheme.colors.text,
    sub: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(SaltTheme.dimens.item)
            .enabledAlpha(enabled)
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
                text = text
            )
            sub?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sub,
                    color = SaltTheme.colors.subText,
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
 * Draw an arrow icon in [ItemPopup].
 *
 * This can facilitate the implementation of fully customizable [ItemPopup].
 */
@Composable
expect fun ItemPopupArrow()

/**
 * Popup a [PopupMenu] where many selectable or common items can be added.
 *
 * TODO: Replace [ItemPopup].
 *
 * @param state [PopupMenu].
 * @param text text.
 * @param value text of value.
 * @param enabled enabled.
 * @param iconPainter icon.
 * @param iconPaddingValues padding values of [iconPainter].
 * @param iconColor color of [iconPainter], if this value is null, will use the paint original
 *   color.
 * @param content composable content.
 */
@Deprecated("Use ItemDropdown")
@UnstableSaltUiApi
@Composable
fun ItemSelect(
    state: PopupState,
    text: String,
    value: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = SaltTheme.colors.text,
    content: @Composable ColumnScope.() -> Unit
) {
    @Suppress("DEPRECATION")
    ItemPopup(
        state = state,
        text = text,
        sub = value,
        modifier = modifier,
        enabled = enabled,
        iconPainter = iconPainter,
        iconPaddingValues = iconPaddingValues,
        iconColor = iconColor,
        content = content
    )
}

/**
 * the popup item.
 *
 * @param state the state of popup.
 */
@Deprecated("Use ItemDropdown")
@UnstableSaltUiApi
@Composable
fun ItemPopup(
    state: PopupState,
    text: String,
    sub: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = SaltTheme.colors.text,
    content: @Composable ColumnScope.() -> Unit
) {
    ItemDropdown(
        state = state,
        text = text,
        value = sub,
        modifier = modifier,
        enabled = enabled,
        iconPainter = iconPainter,
        iconPaddingValues = iconPaddingValues,
        iconColor = iconColor,
        sub = null,
        content = content
    )
}

/**
 * A customizable checkable item component.
 *
 * @param state whether the item is currently checked or unchecked.
 * @param onChange Callback invoked when the user clicks to change the checked state. The updated
 * boolean value is provided as a parameter.
 * @param text The label text displayed.
 * @param modifier [Modifier] to be applied to the container layout.
 * @param enabled The enabled state.
 * @param sub The subtext displayed below the main text.
 */
@UnstableSaltUiApi
@Composable
fun ItemCheck(
    state: Boolean,
    onChange: (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    sub: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(SaltTheme.dimens.item)
            .enabledAlpha(enabled)
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
            painter = if (state) {
                rememberVectorPainter(SaltIcons.Check)
            } else {
                rememberVectorPainter(SaltIcons.Uncheck)
            },
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
                text = text
            )
            sub?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sub,
                    color = SaltTheme.colors.subText,
                    style = SaltTheme.textStyles.sub
                )
            }
        }
    }
}

/**
 * the value item.
 *
 * @param text text.
 * @param sub value.
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
            color = SaltTheme.colors.subText,
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
 * the edit item.
 *
 * @param text text.
 * @param onChange called when text changed.
 * @param modifier modifier.
 * @param hint hint.
 * @param hintColor color of [hint] text.
 * @param readOnly readOnly.
 * @param keyboardOptions keyboardOptions.
 * @param keyboardActions keyboardActions.
 * @param visualTransformation visualTransformation.
 * @param actionContent actionContent.
 */
@UnstableSaltUiApi
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
    ItemEditImpl(
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
 * the password edit item.
 *
 * @param text text.
 * @param onChange called when text changed.
 * @param hint hint.
 * @param hintColor color of [hint] text.
 * @param readOnly readOnly.
 * @param keyboardOptions keyboardOptions.
 * @param keyboardActions keyboardActions.
 */
@UnstableSaltUiApi
@Composable
fun ItemEditPassword(
    text: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
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
        modifier = modifier,
        hint = hint,
        hintColor = hintColor,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = if (hidden) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
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

@UnstableSaltUiApi
@Composable
fun ItemSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = SaltTheme.colors.text,
    sub: String? = null,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(true) { }
            .innerPadding(vertical = false)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .enabledAlpha(enabled),
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
                    .innerPadding(horizontal = false)
            )
            Spacer(modifier = Modifier.width(SaltTheme.dimens.subPadding))
            sub?.let {
                Text(
                    text = sub
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
 * the button item.
 *
 * @param primary Indicates whether the button is primary and prominent, emphasized with a highlight
 *   color.
 */
@UnstableSaltUiApi
@Composable
fun ItemButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = true,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = SaltTheme.colors.highlight
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(SaltTheme.dimens.item)
            .enabledAlpha(enabled)
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
            color = if (enabled && primary) {
                SaltTheme.colors.highlight
            } else {
                SaltTheme.colors.subText
            },
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Build vertical spacing [SaltDimens.subPadding] for the content interface.
 */
@Composable
fun ItemSpacer(
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier
            .height(SaltTheme.dimens.subPadding)
    )
}

/**
 * Build a container with internal margins in the content interface, making it easy to add custom
 * elements such as buttons internally.
 */
@Composable
fun ItemContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .innerPadding()
    ) {
        content()
    }
}

/**
 * Build a divider in the content interface.
 *
 * @param color color of divider.
 */
@Composable
fun ItemDivider(
    modifier: Modifier = Modifier,
    color: Color = SaltTheme.colors.stroke,
    startIndent: Dp = SaltTheme.dimens.padding
) {
    // Dp.Hairline.
    val thickness = (1f / LocalDensity.current.density).dp
    Box(
        modifier = modifier
            .padding(start = startIndent)
            .fillMaxWidth()
            .height(thickness)
            .background(color = color)
    )
}

@UnstableSaltUiApi
enum class ItemInfoType {
    Success,
    Warning,
    Error
}

@UnstableSaltUiApi
@Composable
fun ItemInfo(
    text: String,
    infoType: ItemInfoType,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                when (infoType) {
                    ItemInfoType.Success -> if (SaltTheme.configs.isDarkTheme) {
                        SaltPalette.SuccessDarkBackground
                    } else {
                        SaltPalette.SuccessLightBackground
                    }
                    ItemInfoType.Warning -> if (SaltTheme.configs.isDarkTheme) {
                        SaltPalette.WarningDarkBackground
                    } else {
                        SaltPalette.WarningLightBackground
                    }
                    ItemInfoType.Error -> if (SaltTheme.configs.isDarkTheme) {
                        SaltPalette.ErrorDarkBackground
                    } else {
                        SaltPalette.ErrorLightBackground
                    }
                }
            )
            .semantics(true) { }
            .innerPadding(vertical = false),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .size(SaltTheme.dimens.itemIcon),
            painter = when (infoType) {
                ItemInfoType.Success -> rememberVectorPainter(SaltIcons.Success)
                ItemInfoType.Warning -> painterResource(Res.drawable.ic_warning)
                ItemInfoType.Error -> painterResource(Res.drawable.ic_error)
            },
            contentDescription = null,
            colorFilter = ColorFilter.tint(
                when (infoType) {
                    ItemInfoType.Success -> if (SaltTheme.configs.isDarkTheme) {
                        SaltPalette.SuccessDarkIcon
                    } else {
                        SaltPalette.SuccessLightIcon
                    }
                    ItemInfoType.Warning -> if (SaltTheme.configs.isDarkTheme) {
                        SaltPalette.WarningDarkIcon
                    } else {
                        SaltPalette.WarningLightIcon
                    }
                    ItemInfoType.Error -> if (SaltTheme.configs.isDarkTheme) {
                        SaltPalette.ErrorDarkIcon
                    } else {
                        SaltPalette.ErrorLightIcon
                    }
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
