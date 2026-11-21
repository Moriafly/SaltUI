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
    val edge = Modifier.thenIf(style.borderColor.isSpecified) {
        border(Dp.Hairline, style.borderColor, shape)
    }
    val android = OS.current as? OS.Android
    if (backdrop == null || (android != null && android.versionSdk < OS.Android.ANDROID_12)) {
        return Modifier
            .thenIf(style.containerColor.isSpecified) { background(style.containerColor) }
            .then(edge)
    }

    val baseFill = if (revealWindowBackdrop) {
        style.windowBackdropColor
    } else {
        style.containerColor
    }
    val base = Modifier
        .thenIf(baseFill.isSpecified && (revealWindowBackdrop || baseFill.alpha == 1f)) {
            background(baseFill)
        }
        .then(edge)
    val effectBackground = if (revealWindowBackdrop || !style.containerColor.isSpecified) {
        Color.Transparent
    } else {
        style.containerColor
    }
    val effectOpacity = if (revealWindowBackdrop) style.windowBackdropOpacity else 1f
    val blur = style.material == PopupMenuMaterial.Blur ||
        (android != null && android.versionSdk < OS.Android.ANDROID_13)
    return if (blur) {
        val blurStyle = remember(effectBackground, effectOpacity, style.tintColor) {
            HazeBlurStyle {
                backgroundColor(effectBackground)
                colorEffects(listOf(HazeColorEffect.tint(style.tintColor)))
                fallbackColorEffect(HazeColorEffect.tint(effectBackground))
                blurRadius(12.dp)
                noiseFactor(0f)
                alpha(effectOpacity)
            }
        }
        base.hazeBlur(input = backdrop.input, style = blurStyle)
    } else {
        val glassStyle = remember(
            cornerRadius,
            dark,
            effectBackground,
            effectOpacity,
            style.tintColor
        ) {
            GlassStyles.regular(dark).then {
                // The optical mask is round-rect; the popup still clips to its smooth outline.
                shape(RoundedCornerShape(cornerRadius))
                tint(style.tintColor)
                if (dark) {
                    // Preserve the theme's elevated color when sampling a dark page.
                    whitePoint(0f)
                }
                backgroundColor(effectBackground)
                alpha(effectOpacity)
            }
        }
        base.hazeGlass(input = backdrop.input, style = glassStyle)
    }
}
