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
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi

@UnstableSaltUiApi
enum class MaterialType {
    /**
     * Without material.
     */
    None,
    BlurryGlass,
    Acrylic,
    Mica,
    Premium
}

@UnstableSaltUiApi
enum class MaterialLayer {
    Background,
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
@OptIn(ExperimentalHazeMaterialsApi::class)
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
@OptIn(ExperimentalHazeMaterialsApi::class)
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
@OptIn(ExperimentalHazeApi::class, ExperimentalHazeMaterialsApi::class)
@Composable
internal fun Modifier.basicMaterial(
    type: MaterialType,
    layer: MaterialLayer,
    isDarkTheme: Boolean,
    fallback: Color
): Modifier {
    val hazeState = LocalHazeState.current

    return if (hazeState != null) {
        hazeEffect(
            state = hazeState,
            style = when (type) {
                MaterialType.None -> HazeStyle.Unspecified
                MaterialType.BlurryGlass -> SaltHazeStyles.blurryGlass(layer, isDarkTheme)
                MaterialType.Acrylic -> SaltHazeStyles.acrylic(layer, isDarkTheme)
                MaterialType.Mica -> SaltHazeStyles.mica(layer, isDarkTheme)
                MaterialType.Premium -> SaltHazeStyles.premium(layer, isDarkTheme)
            }
        ) {
            inputScale = HazeInputScale.Fixed(InputScale)
        }
    } else {
        background(fallback)
    }
}

internal val LocalHazeState = compositionLocalOf<HazeState?> { null }

private const val InputScale = 0.67f
