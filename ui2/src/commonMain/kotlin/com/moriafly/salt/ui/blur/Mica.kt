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

package com.moriafly.salt.ui.blur

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.FluentMaterials

/**
 * Creates a Multi Blur background layer for implementing [Modifier.mica] and [Modifier.micaAlt]
 *
 * Note: Do not use [Modifier.mica] or [Modifier.micaAlt] inside [content], as [content] is
 * typically the wallpaper image.
 *
 * Additionally, the hierarchy of MicaSource needs to be placed before elements using
 * [Modifier.mica] or [Modifier.micaAlt] to achieve the correct effect.
 *
 * Using a Composable instead of a Modifier here is intentional - it encourages developers to
 * explicitly specify the hierarchy of MicaSource and use it only once.
*/
@UnstableSaltUiApi
@Composable
fun MicaSource(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val hazeState = LocalHazeState.current
    Box(
        modifier = modifier
            .thenIf(hazeState != null) {
                hazeSource(hazeState!!)
            },
        content = content
    )
}

@UnstableSaltUiApi
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun Modifier.mica(
    fallback: Color = Color.Unspecified
): Modifier = basicMica(
    style = FluentMaterials.mica(SaltTheme.configs.isDarkTheme),
    fallback = fallback
)

@UnstableSaltUiApi
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun Modifier.micaAlt(
    fallback: Color = Color.Unspecified
): Modifier = basicMica(
    style = FluentMaterials.micaAlt(SaltTheme.configs.isDarkTheme),
    fallback = fallback
)

/**
 * This implementation takes inspiration from Windows 11's Mica material,
 * with key differences in scope and application:
 *
 * - Windows applies Mica effects system-wide to app backgrounds
 * - Salt UI implements localized effects for specific components:
 *     - `com.moriafly.salt.ui.RoundedColumn` uses MicaAlt
 *     - `com.moriafly.salt.ui.dialog.BasicDialog` uses standard Mica
 *
 * Both variants are rendered relative to the application's own wallpaper layer,
 * not the system wallpaper as in Windows.
 */
@UnstableSaltUiApi
@OptIn(ExperimentalHazeApi::class)
@Composable
internal fun Modifier.basicMica(
    style: HazeStyle,
    fallback: Color = Color.Unspecified
): Modifier {
    val hazeState = LocalHazeState.current

    return if (hazeState != null) {
        hazeEffect(
            state = hazeState,
            style = style
        ) {
            inputScale = HazeInputScale.Fixed(InputScale)
        }
    } else {
        background(fallback)
    }
}

internal val LocalHazeState = compositionLocalOf<HazeState?> { null }

private const val InputScale = 0.67f
