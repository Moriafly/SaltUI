/*
 * SaltUI
 * Copyright (C) 2026 Moriafly
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

package com.moriafly.salt.ui.button

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import com.moriafly.salt.ui.ButtonDefaults
import com.moriafly.salt.ui.ControlSize
import com.moriafly.salt.ui.LocalControlSize
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.enabledAlpha
import com.moriafly.salt.ui.material.subMaterial
import com.moriafly.salt.ui.thenIf

/**
 * A button that can be rendered as either a perfect circle or a pill (capsule) shape.
 *
 * - **Circle button**: When [text] is `null`, the button has equal width and height resolved from
 *   [size], rendering as a perfect circle containing only the [icon].
 * - **Pill button**: When [text] is provided, the button expands horizontally into a capsule
 *   shape with the [icon] on the leading side and [text] on the trailing side.
 *
 * @param onClick Called when the button is clicked.
 * @param modifier [Modifier] to be applied to the button.
 * @param enabled Whether the button is enabled and responds to click events.
 * @param text Optional text content displayed to the right of the [icon].
 * When provided, the button becomes a pill shape; when `null`, it is a perfect circle.
 * @param size The control size, inherited from [LocalControlSize] unless specified explicitly.
 * @param icon Icon content displayed inside the button.
 */
@UnstableSaltUiApi
@Composable
fun PillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: (@Composable () -> Unit)? = null,
    size: ControlSize = LocalControlSize.current,
    icon: @Composable () -> Unit
) {
    val containerHeight = PillButtonDefaults.containerHeight(size)
    BasicPillButton(
        modifier = modifier
            .run {
                if (text != null) {
                    height(containerHeight)
                } else {
                    size(containerHeight)
                }
            }
            .clip(CircleShape)
            .subMaterial(fallback = SaltTheme.colors.subBackground)
            .border(Dp.Hairline, SaltTheme.colors.stroke, CircleShape)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .thenIf(text != null) {
                padding(PillButtonDefaults.contentPadding(size))
            }
            .enabledAlpha(enabled),
        text = text,
        size = size,
        icon = icon
    )
}

/**
 * Low-level layout for pill/circle buttons.
 *
 * Contains only the content arrangement logic without any background, border, or click handling.
 * For a fully styled button, use [PillButton].
 *
 * @param modifier [Modifier] to be applied to the container.
 * @param text Optional text content displayed to the right of the [icon].
 * @param size The control size used for the icon and spacing, inherited from [LocalControlSize].
 * @param icon Icon content displayed inside the button.
 */
@UnstableSaltUiApi
@Composable
fun BasicPillButton(
    modifier: Modifier = Modifier,
    text: (@Composable () -> Unit)? = null,
    size: ControlSize = LocalControlSize.current,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(PillButtonDefaults.iconSize(size)),
            contentAlignment = Alignment.Center,
            propagateMinConstraints = true
        ) {
            icon()
        }

        if (text != null) {
            Spacer(Modifier.width(PillButtonDefaults.iconSpacing(size)))
            text()
        }
    }
}

/**
 * Retains the original entry point for previously compiled callers.
 */
@Deprecated(
    message = "Retained for binary compatibility. Use the overload with size.",
    level = DeprecationLevel.HIDDEN
)
@UnstableSaltUiApi
@Composable
fun PillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: (@Composable () -> Unit)? = null,
    icon: @Composable () -> Unit
) {
    PillButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        text = text,
        size = LocalControlSize.current,
        icon = icon
    )
}

/**
 * Retains the original entry point for previously compiled callers.
 */
@Deprecated(
    message = "Retained for binary compatibility. Use the overload with size.",
    level = DeprecationLevel.HIDDEN
)
@UnstableSaltUiApi
@Composable
fun BasicPillButton(
    modifier: Modifier = Modifier,
    text: (@Composable () -> Unit)? = null,
    icon: @Composable () -> Unit
) {
    BasicPillButton(
        modifier = modifier,
        text = text,
        size = LocalControlSize.current,
        icon = icon
    )
}

/**
 * Default values used by [PillButton] and [BasicPillButton].
 */
@UnstableSaltUiApi
object PillButtonDefaults {
    /**
     * The platform-resolved height, shared with standard buttons of the same [size].
     */
    fun containerHeight(size: ControlSize): Dp = ButtonDefaults.containerHeight(size)

    /**
     * The content padding for a pill-shaped button of the given [size].
     */
    fun contentPadding(
        size: ControlSize
    ): PaddingValues = ButtonDefaults.contentPadding(size)

    /**
     * The icon size for the given control [size].
     */
    fun iconSize(size: ControlSize): Dp = ButtonDefaults.iconSize(size)

    /**
     * The spacing between the icon and text for the given control [size].
     */
    fun iconSpacing(size: ControlSize): Dp = ButtonDefaults.iconSpacing(size)
}
