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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.shadow.ShadowContext
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.internal.SmoothRoundedRectangleShape
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
internal fun Modifier.menuMaterial(
    shape: SmoothRoundedRectangleShape,
    style: PopupMenuStyle,
    backdrop: PopupBackdrop?,
    isDarkTheme: Boolean
): Modifier {
    val surface = menuEdge(shape = shape, color = style.borderColor)
    val material = style.material.withAndroidFallback()
    if (backdrop == null || material == PopupMenuMaterial.Solid) {
        return surface.thenIf(style.containerColor.isSpecified) { background(style.containerColor) }
    }

    val backdropColor = style.backdropColor.takeOrElse { Color.Transparent }
    val tintColor = style.tintColor.takeOrElse { Color.Transparent }
    val sampledSurface = surface
        .graphicsLayer()
        .then(backdrop.cutout(shape))
        .thenIf(backdropColor.alpha == 1f) { background(backdropColor) }

    return if (material == PopupMenuMaterial.Blur) {
        sampledSurface.menuBlur(
            input = backdrop.input,
            backdropColor = backdropColor,
            tintColor = tintColor
        )
    } else {
        sampledSurface.menuGlass(
            input = backdrop.input,
            backdropColor = backdropColor,
            tintColor = tintColor,
            cornerRadius = shape.radius,
            isDarkTheme = isDarkTheme
        )
    }
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

@Composable
private fun Modifier.menuBlur(
    input: HazeInput.Sources,
    backdropColor: Color,
    tintColor: Color
): Modifier {
    val style = remember(backdropColor, tintColor) {
        HazeBlurStyle {
            backgroundColor(backdropColor)
            colorEffects(listOf(HazeColorEffect.tint(tintColor)))
            fallbackColorEffect(HazeColorEffect.tint(backdropColor))
            blurRadius(12.dp)
            noiseFactor(0f)
            alpha(1f)
        }
    }
    return hazeBlur(input = input, style = style)
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun Modifier.menuGlass(
    input: HazeInput.Sources,
    backdropColor: Color,
    tintColor: Color,
    cornerRadius: Dp,
    isDarkTheme: Boolean
): Modifier {
    val style = remember(cornerRadius, isDarkTheme, backdropColor, tintColor) {
        GlassStyles.regular(isDarkTheme).then {
            // The optical mask is round-rect; the popup still clips to its smooth outline.
            shape(RoundedCornerShape(cornerRadius))
            tint(tintColor)
            if (isDarkTheme) {
                // Match the source-over tint used where the background shows through.
                contrast(0f)
                whitePoint(0f)
                chromaMultiplier(1f)
            }
            backgroundColor(backdropColor)
            alpha(1f)
        }
    }
    return hazeGlass(input = input, style = style)
}
