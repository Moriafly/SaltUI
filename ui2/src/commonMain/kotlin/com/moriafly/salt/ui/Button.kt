/*
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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp

/** The visual treatment of a [Button]. */
enum class ButtonAppearance {
    /** A contrasting filled container for the most prominent action in a surface. */
    Filled,

    /** A transparent container with an outline for actions that need a visible boundary. */
    Outlined,

    /** A transparent, borderless container for actions already framed by their parent surface. */
    Plain
}

/** Semantic intent that can affect the colors of a [Button]. */
enum class ButtonIntent {
    Normal,
    Destructive
}

/**
 * Legacy button emphasis retained for source and binary migration.
 */
@Deprecated(
    message = "Use ButtonAppearance instead"
)
enum class ButtonType {
    Highlight,
    Sub
}

/**
 * Colors used by a button in enabled and disabled states.
 *
 * Interaction feedback such as hover, focus, and press remains the responsibility of the current
 * [androidx.compose.foundation.Indication].
 */
@Immutable
class ButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color
) {
    @Stable
    internal fun containerColor(enabled: Boolean): Color =
        if (enabled) containerColor else disabledContainerColor

    @Stable
    internal fun contentColor(enabled: Boolean): Color =
        if (enabled) contentColor else disabledContentColor

    fun copy(
        containerColor: Color = this.containerColor,
        contentColor: Color = this.contentColor,
        disabledContainerColor: Color = this.disabledContainerColor,
        disabledContentColor: Color = this.disabledContentColor
    ): ButtonColors = ButtonColors(
        containerColor = containerColor.takeOrElse { this.containerColor },
        contentColor = contentColor.takeOrElse { this.contentColor },
        disabledContainerColor = disabledContainerColor.takeOrElse {
            this.disabledContainerColor
        },
        disabledContentColor = disabledContentColor.takeOrElse {
            this.disabledContentColor
        }
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is ButtonColors) return false

        if (containerColor != other.containerColor) return false
        if (contentColor != other.contentColor) return false
        if (disabledContainerColor != other.disabledContainerColor) return false
        if (disabledContentColor != other.disabledContentColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = containerColor.hashCode()
        result = 31 * result + contentColor.hashCode()
        result = 31 * result + disabledContainerColor.hashCode()
        result = 31 * result + disabledContentColor.hashCode()
        return result
    }
}

/**
 * A button with a structured text label and an optional leading icon.
 *
 * Use the content-slot overload when the label needs rich text, a trailing icon, progress, or a
 * custom content arrangement.
 *
 * [maxLines] defaults to one because standard action labels should be concise. Prefer the
 * content-slot overload when a button needs a more complex layout.
 */
@Composable
fun Button(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    appearance: ButtonAppearance = ButtonAppearance.Filled,
    intent: ButtonIntent = ButtonIntent.Normal,
    size: ControlSize = LocalControlSize.current,
    leadingIcon: (@Composable () -> Unit)? = null,
    maxLines: Int = 1
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        appearance = appearance,
        intent = intent,
        size = size
    ) {
        if (leadingIcon != null) {
            Box(
                modifier = Modifier.size(ButtonDefaults.iconSize(size)),
                contentAlignment = Alignment.Center,
                propagateMinConstraints = true
            ) {
                leadingIcon()
            }
            Spacer(Modifier.width(ButtonDefaults.iconSpacing(size)))
        }
        Text(
            text = text,
            overflow = TextOverflow.Ellipsis,
            maxLines = maxLines
        )
    }
}

/**
 * Legacy [ButtonType] overload retained for migration to [ButtonAppearance].
 *
 * [type] is intentionally required for source overload resolution. It remains nullable so binaries
 * compiled against the former default value can resolve a missing value as [ButtonType.Highlight].
 */
@Suppress("DEPRECATION")
@Deprecated(
    message = "Use Button with ButtonAppearance instead",
    replaceWith = ReplaceWith(
        expression = "Button(onClick = onClick, text = text, modifier = modifier, " +
            "enabled = enabled, appearance = if (type == ButtonType.Sub) " +
            "ButtonAppearance.Plain else ButtonAppearance.Filled, maxLines = maxLines)",
        imports = arrayOf(
            "com.moriafly.salt.ui.Button",
            "com.moriafly.salt.ui.ButtonAppearance",
            "com.moriafly.salt.ui.ButtonType"
        )
    )
)
@Composable
fun Button(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    type: ButtonType?,
    maxLines: Int = Int.MAX_VALUE
) {
    Button(
        onClick = onClick,
        text = text,
        modifier = modifier,
        enabled = enabled,
        appearance = when (type ?: ButtonType.Highlight) {
            ButtonType.Highlight -> ButtonAppearance.Filled
            ButtonType.Sub -> ButtonAppearance.Plain
        },
        maxLines = maxLines
    )
}

/**
 * A button whose label is supplied by a flexible [RowScope] content slot.
 *
 * [appearance] describes how the button is drawn, while [intent] describes the meaning of its
 * action. [size] describes surrounding layout density and must not be used to express action
 * importance.
 */
@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    appearance: ButtonAppearance = ButtonAppearance.Filled,
    intent: ButtonIntent = ButtonIntent.Normal,
    size: ControlSize = LocalControlSize.current,
    content: @Composable RowScope.() -> Unit
) {
    BasicButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        size = size,
        colors = ButtonDefaults.colors(
            appearance = appearance,
            intent = intent
        ),
        border = ButtonDefaults.border(
            appearance = appearance,
            enabled = enabled
        ),
        content = content
    )
}

/**
 * Low-level Salt button with customizable visual tokens and a [RowScope] content slot.
 *
 * Prefer [Button] for product UI. Use this primitive for custom branded or specialized buttons
 * that still need Salt's sizing, interaction, semantics, and inherited content styling.
 */
@Composable
fun BasicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: ControlSize = LocalControlSize.current,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.colors(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.contentPadding(size),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) {
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val metrics = platformButtonMetrics(size)
    val contentColor = colors.contentColor(enabled)

    ProvideContentColorTextStyle(
        contentColor = contentColor,
        textStyle = ButtonDefaults.textStyle
    ) {
        Row(
            modifier = modifier
                .defaultMinSize(minHeight = metrics.containerHeight)
                .clip(shape)
                .background(colors.containerColor(enabled))
                .then(
                    if (border != null) {
                        Modifier.border(border, shape)
                    } else {
                        Modifier
                    }
                )
                .clickable(
                    interactionSource = resolvedInteractionSource,
                    indication = LocalIndication.current,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick
                )
                .padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/** Default visual tokens and platform-resolved metrics for [Button]. */
@Suppress("ConstPropertyName")
object ButtonDefaults {
    /** The default capsule shape shared by all standard button appearances. */
    val shape: Shape = CircleShape

    fun containerHeight(size: ControlSize): Dp = platformButtonMetrics(size).containerHeight

    fun contentPadding(size: ControlSize): PaddingValues = platformButtonMetrics(
        size
    ).contentPadding

    fun iconSize(size: ControlSize): Dp = platformButtonMetrics(size).iconSize

    fun iconSpacing(size: ControlSize): Dp = platformButtonMetrics(size).iconSpacing

    val textStyle: TextStyle
        @Composable
        get() = SaltTheme.textStyles.main

    @Composable
    fun colors(
        appearance: ButtonAppearance = ButtonAppearance.Filled,
        intent: ButtonIntent = ButtonIntent.Normal
    ): ButtonColors {
        val themeColors = SaltTheme.colors
        val destructive = intent == ButtonIntent.Destructive
        val disabledContentColor = themeColors.subText.copy(
            alpha = themeColors.subText.alpha * DisabledContentAlpha
        )

        return when (appearance) {
            ButtonAppearance.Filled -> ButtonColors(
                containerColor = if (destructive) themeColors.error else themeColors.highlight,
                contentColor = themeColors.onHighlight,
                disabledContainerColor = themeColors.subBackground,
                disabledContentColor = disabledContentColor
            )

            ButtonAppearance.Outlined,
            ButtonAppearance.Plain -> ButtonColors(
                containerColor = Color.Transparent,
                contentColor = if (destructive) themeColors.error else themeColors.text,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = disabledContentColor
            )
        }
    }

    @Composable
    fun border(
        appearance: ButtonAppearance,
        enabled: Boolean = true
    ): BorderStroke? {
        if (appearance != ButtonAppearance.Outlined) return null

        val stroke = SaltTheme.colors.stroke
        return BorderStroke(
            width = Dp.Hairline,
            color = if (enabled) {
                stroke
            } else {
                stroke.copy(alpha = stroke.alpha * DisabledBorderAlpha)
            }
        )
    }

    private const val DisabledContentAlpha = 0.55f
    private const val DisabledBorderAlpha = 0.5f
}

/**
 * Default text-content button retained for source compatibility.
 */
@Deprecated(
    message = "Use Button or BasicButton instead",
    replaceWith = ReplaceWith(
        expression = "Button(onClick = onClick, text = text, modifier = modifier, " +
            "enabled = enabled)",
        imports = arrayOf("com.moriafly.salt.ui.Button")
    )
)
@Composable
fun TextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textColor: Color = SaltTheme.colors.onHighlight,
    backgroundColor: Color = SaltTheme.colors.highlight
) {
    BasicButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.5f),
            disabledContentColor = textColor.copy(alpha = 0.55f)
        )
    ) {
        Text(
            text = text,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            maxLines = 1
        )
    }
}

@Immutable
internal data class ButtonMetrics(
    val containerHeight: Dp,
    val contentPadding: PaddingValues,
    val iconSize: Dp,
    val iconSpacing: Dp
)

internal expect fun platformButtonMetrics(size: ControlSize): ButtonMetrics
