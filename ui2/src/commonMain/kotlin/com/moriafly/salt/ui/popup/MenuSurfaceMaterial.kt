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

package com.moriafly.salt.ui.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.shadow.ShadowContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.material.GlassStyles
import com.moriafly.salt.ui.thenIf
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.glass.hazeGlass

internal fun Modifier.menuShadow(
    shape: Shape,
    dark: Boolean,
    desktop: Boolean,
    shadowContext: ShadowContext
): Modifier = drawWithCache {
    val silhouette = Path().apply {
        addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache))
    }
    val opacity = if (desktop) {
        if (dark) 0.24f else 0.075f
    } else {
        if (dark) 0.20f else 0.045f
    }
    val shadow = shadowContext.createDropShadowPainter(
        shape = shape,
        shadow = Shadow(
            radius = if (desktop) 9.dp else 7.dp,
            color = Color.Black.copy(alpha = opacity),
            offset = DpOffset(0.dp, if (desktop) 2.dp else 1.dp)
        )
    )
    onDrawBehind {
        clipPath(silhouette, ClipOp.Difference) {
            shadow.apply { draw(size) }
        }
    }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
internal fun menuMaterial(
    shape: Shape,
    cornerRadius: Dp,
    style: PopupMenuStyle,
    backdrop: PopupBackdrop?,
    revealWindowBackdrop: Boolean,
    dark: Boolean
): Modifier {
    val material = style.material.withAndroidFallback()
    if (backdrop == null || material == PopupMenuMaterial.Solid) {
        return Modifier
            .thenIf(style.containerColor.isSpecified) { background(style.containerColor) }
            .menuEdge(shape = shape, color = style.borderColor)
    }
    return menuSampledMaterial(
        shape = shape,
        cornerRadius = cornerRadius,
        style = style,
        input = backdrop.input,
        revealWindowBackdrop = revealWindowBackdrop,
        blur = material == PopupMenuMaterial.Blur,
        dark = dark
    )
}

@OptIn(UnstableSaltUiApi::class)
private fun PopupMenuMaterial.withAndroidFallback(): PopupMenuMaterial {
    val android = OS.current as? OS.Android ?: return this
    return when {
        android.versionSdk < OS.Android.ANDROID_12 -> PopupMenuMaterial.Solid
        this == PopupMenuMaterial.Glass && android.versionSdk < OS.Android.ANDROID_13 ->
            PopupMenuMaterial.Blur
        else -> this
    }
}

private fun Modifier.menuEdge(shape: Shape, color: Color): Modifier = thenIf(color.isSpecified) {
    border(Dp.Hairline, color, shape)
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun menuSampledMaterial(
    shape: Shape,
    cornerRadius: Dp,
    style: PopupMenuStyle,
    input: HazeInput.Sources,
    revealWindowBackdrop: Boolean,
    blur: Boolean,
    dark: Boolean
): Modifier {
    val baseFill = if (revealWindowBackdrop) {
        style.windowBackdropColor
    } else {
        style.containerColor
    }
    val base = Modifier
        .thenIf(baseFill.isSpecified && (revealWindowBackdrop || baseFill.alpha == 1f)) {
            background(baseFill)
        }
        .menuEdge(shape = shape, color = style.borderColor)
    val effectBackground = if (revealWindowBackdrop || !style.containerColor.isSpecified) {
        Color.Transparent
    } else {
        style.containerColor
    }
    val effectOpacity = if (revealWindowBackdrop) style.windowBackdropOpacity else 1f
    return if (blur) {
        base.menuBlur(
            input = input,
            background = effectBackground,
            opacity = effectOpacity,
            tintColor = style.tintColor
        )
    } else {
        base.menuGlass(
            input = input,
            background = effectBackground,
            opacity = effectOpacity,
            tintColor = style.tintColor,
            cornerRadius = cornerRadius,
            dark = dark
        )
    }
}

@Composable
private fun Modifier.menuBlur(
    input: HazeInput.Sources,
    background: Color,
    opacity: Float,
    tintColor: Color
): Modifier {
    val style = remember(background, opacity, tintColor) {
        HazeBlurStyle {
            backgroundColor(background)
            colorEffects(listOf(HazeColorEffect.tint(tintColor)))
            fallbackColorEffect(HazeColorEffect.tint(background))
            blurRadius(12.dp)
            noiseFactor(0f)
            alpha(opacity)
        }
    }
    return hazeBlur(input = input, style = style)
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun Modifier.menuGlass(
    input: HazeInput.Sources,
    background: Color,
    opacity: Float,
    tintColor: Color,
    cornerRadius: Dp,
    dark: Boolean
): Modifier {
    val style = remember(cornerRadius, dark, background, opacity, tintColor) {
        GlassStyles.regular(dark).then {
            // The optical mask is round-rect; the popup still clips to its smooth outline.
            shape(RoundedCornerShape(cornerRadius))
            tint(tintColor)
            if (dark) {
                // Preserve the theme's elevated color when sampling a dark page.
                whitePoint(0f)
            }
            backgroundColor(background)
            alpha(opacity)
        }
    }
    return hazeGlass(input = input, style = style)
}
