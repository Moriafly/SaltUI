/**
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
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.FluentMaterials

@UnstableSaltUiApi
enum class MaterialType {
    /**
     * Without material.
     */
    None,
    BlurryGlass,
    Acrylic,
    Mica
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .material()
        )
    }
}

@UnstableSaltUiApi
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun Modifier.material(
    fallback: Color = Color.Unspecified
): Modifier = basicMica(
    type = SaltTheme.material.type,
    layer = MaterialLayer.Background,
    fallback = fallback
)

@UnstableSaltUiApi
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun Modifier.subMaterial(
    fallback: Color = Color.Unspecified
): Modifier = basicMica(
    type = SaltTheme.material.type,
    layer = MaterialLayer.SubBackground,
    fallback = fallback
)

/**
 * This implementation takes inspiration from Windows 11's Mica material,
 * with key differences in scope and application:
 *
 * - Windows applies Material effects system-wide to app backgrounds
 * - Salt UI implements localized effects for specific components:
 *     - `com.moriafly.salt.ui.RoundedColumn` uses MicaAlt
 *     - `com.moriafly.salt.ui.dialog.BasicDialog` uses standard Mica
 *
 * Both variants are rendered relative to the application's own wallpaper layer,
 * not the system wallpaper as in Windows.
 */
@UnstableSaltUiApi
@OptIn(ExperimentalHazeApi::class, ExperimentalHazeMaterialsApi::class)
@Composable
internal fun Modifier.basicMica(
    type: MaterialType,
    layer: MaterialLayer,
    fallback: Color = Color.Unspecified
): Modifier {
    val hazeState = LocalHazeState.current

    return if (hazeState != null) {
        hazeEffect(
            state = hazeState,
            style = when (type) {
                MaterialType.None -> HazeStyle.Unspecified

                MaterialType.BlurryGlass -> when (layer) {
                    MaterialLayer.Background ->
                        CupertinoMaterials.ultraThin(SaltTheme.colors.background)
                    MaterialLayer.SubBackground ->
                        CupertinoMaterials.regular(SaltTheme.colors.background)
                }

                MaterialType.Acrylic -> when (layer) {
                    MaterialLayer.Background ->
                        FluentMaterials.acrylicBase(SaltTheme.configs.isDarkTheme)
                    MaterialLayer.SubBackground ->
                        FluentMaterials.acrylicDefault(SaltTheme.configs.isDarkTheme)
                }

                MaterialType.Mica -> when (layer) {
                    MaterialLayer.Background ->
                        FluentMaterials.micaAlt(SaltTheme.configs.isDarkTheme)
                    MaterialLayer.SubBackground ->
                        FluentMaterials.mica(SaltTheme.configs.isDarkTheme)
                }
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
