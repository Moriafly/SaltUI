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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.thenIf
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

@UnstableSaltUiApi
enum class MultiBlurLevel(
    internal val level: Float
) {
    Window(0f),
    Item(1f),
    Bar(2f),
    Popup(3f),
    Popup2(4f),
    Popup3(5f)
}

/**
 * # Multi Blur Layer
 *
 * @param enabled Whether to enable the Multi Blur.
 * @param content The main content to be displayed.
 */
@UnstableSaltUiApi
@Composable
fun MultiBlurLayer(
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val hazeState = remember(enabled) {
        if (enabled) HazeState() else null
    }
    CompositionLocalProvider(
        LocalMultiBlur provides hazeState
    ) {
        content()
    }
}

/**
 * Elevates the Multi Blur level.
 */
@UnstableSaltUiApi
@Composable
fun MultiBlurLevelUp(
    content: @Composable () -> Unit
) {
    val multiBlurLevelUp = LocalMultiBlurLevelUp.current
    CompositionLocalProvider(
        LocalMultiBlurLevelUp provides multiBlurLevelUp + LevelUp
    ) {
        content()
    }
}

/**
 * Creates a Multi Blur background.
 *
 * If the [MultiBlurLayer]'s background is not enabled, it will use the [elseColor] as the
 * background color.
 */
@OptIn(ExperimentalHazeApi::class)
@UnstableSaltUiApi
@Composable
fun Modifier.multiBlurBackground(
    level: MultiBlurLevel,
    elseColor: Color = Color.Unspecified
): Modifier {
    val multiBlur = LocalMultiBlur.current
    val multiBlurLevelUp = LocalMultiBlurLevelUp.current

    return if (multiBlur != null) {
        val zIndex = level.level + multiBlurLevelUp
        this
            .hazeSource(multiBlur, zIndex)
            // If zIndex is not 0, apply hazeEffect
            .thenIf(zIndex != 0f) {
                hazeEffect(
                    state = multiBlur,
                    style = HazeStyle(
                        backgroundColor = SaltTheme.colors.popup,
                        tint = null,
                        blurRadius = BlurRadius,
                        noiseFactor = NoiseFactor
                    )
                ) {
                    inputScale = HazeInputScale.Fixed(InputScale)
                }
            }
    } else {
        this
            .background(elseColor)
    }
}

internal val LocalMultiBlur = compositionLocalOf<HazeState?> { null }

internal val LocalMultiBlurLevelUp = compositionLocalOf { 0f }

private const val LevelUp = 100f

private const val InputScale = 0.67f
private val BlurRadius = 50.dp
private const val NoiseFactor = 0.02f
