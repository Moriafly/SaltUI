/*
 * Salt UI
 * Copyright (C) 2025 Moriafly
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

@file:Suppress("unused", "ktlint:standard:property-naming")

package com.moriafly.salt.ui.material

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.thenIf
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

/**
 * Material effect applied by [Modifier.material] and [Modifier.subMaterial].
 */
@UnstableSaltUiApi
enum class MaterialType {
    /**
     * Without material.
     */
    None,

    /**
     * Blurred glass effect with theme-aware tinting and subtle noise.
     */
    BlurryGlass,

    /**
     * Fluent acrylic effect using base and default styles for the material layers.
     */
    Acrylic,

    /**
     * Fluent mica effect using alternate and regular styles for the material layers.
     */
    Mica,

    /**
     * Strongly blurred effect with layered color blending and subtle noise on the sub-background.
     */
    Premium
}

/**
 * Background layer used to select the styling of a [MaterialType].
 */
@UnstableSaltUiApi
enum class MaterialLayer {
    /**
     * Primary background layer used by [Modifier.material].
     */
    Background,

    /**
     * Secondary background layer used by [Modifier.subMaterial].
     */
    SubBackground
}

/**
 * Creates a Material background layer for implementing [Modifier.material] and
 * [Modifier.subMaterial].
 *
 * Note: Do not use [Modifier.material] or [Modifier.subMaterial] inside [content], as [content] is
 * typically the wallpaper image.
 *
 * Additionally, the hierarchy of MicaSource needs to be placed before elements using
 * [Modifier.material] or [Modifier.subMaterial] to achieve the correct effect.
 *
 * Using a Composable instead of a Modifier here is intentional - it encourages developers to
 * explicitly specify the hierarchy of MicaSource and use it only once.
 */
@UnstableSaltUiApi
@Composable
fun MaterialSource(
    modifier: Modifier = Modifier,
    materialSelf: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val hazeState = LocalHazeState.current
    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .thenIf(hazeState != null) {
                    hazeSource(hazeState!!)
                },
            content = content
        )

        if (materialSelf) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .material()
            )
        }
    }
}

/**
 * Disables material effects for child components, commonly used in Dialogs.
 */
@UnstableSaltUiApi
@Composable
fun DisableMaterial(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalHazeState provides null
    ) {
        content()
    }
}

@UnstableSaltUiApi
@Composable
fun Modifier.material(
    isDarkTheme: Boolean = SaltTheme.configs.isDarkTheme,
    fallback: Color = Color.Unspecified
): Modifier = basicMaterial(
    type = SaltTheme.material.type,
    layer = MaterialLayer.Background,
    isDarkTheme = isDarkTheme,
    fallback = fallback
)

@UnstableSaltUiApi
@Composable
fun Modifier.subMaterial(
    isDarkTheme: Boolean = SaltTheme.configs.isDarkTheme,
    fallback: Color = Color.Unspecified
): Modifier = basicMaterial(
    type = SaltTheme.material.type,
    layer = MaterialLayer.SubBackground,
    isDarkTheme = isDarkTheme,
    fallback = fallback
)

@UnstableSaltUiApi
@Composable
internal fun Modifier.basicMaterial(
    type: MaterialType,
    layer: MaterialLayer,
    isDarkTheme: Boolean,
    fallback: Color
): Modifier {
    val hazeState = LocalHazeState.current

    return if (hazeState != null) {
        val blurStyle = when (type) {
            MaterialType.None -> HazeBlurStyle.Unspecified
            MaterialType.BlurryGlass -> SaltHazeStyles.blurryGlass(layer, isDarkTheme)
            MaterialType.Acrylic -> SaltHazeStyles.acrylic(layer, isDarkTheme)
            MaterialType.Mica -> SaltHazeStyles.mica(layer, isDarkTheme)
            MaterialType.Premium -> SaltHazeStyles.premium(layer, isDarkTheme)
        }
        hazeEffect(state = hazeState) {
            blurEffect {
                style = blurStyle
            }
            inputScale = HazeInputScale.Fixed(InputScale)
        }
    } else {
        background(fallback)
    }
}

internal val LocalHazeState = compositionLocalOf<HazeState?> { null }

private const val InputScale = 0.67f
