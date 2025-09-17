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

@file:Suppress("unused")

package com.moriafly.salt.ui.blur

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.thenIf
import dev.chrisbanes.haze.HazeState
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
 * The Multi Blur Layer itself serves as a [MultiBlurLevel.Window] level, ultimately forming a
 * hierarchical structure.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param background Background composable, typically used to set the wallpaper background for the
 * entire app.
 * Can also be used in smaller scopes like a single Screen in Navigation or even smaller areas if
 * not used as app background.
 * Set to `null` to disable this feature.
 * @param content The main content to be displayed.
 */
@UnstableSaltUiApi
@Composable
fun MultiBlurLayer(
    modifier: Modifier = Modifier,
    background: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
    ) {
        val hazeState = remember(background) {
            background?.let { HazeState() }
        }
        CompositionLocalProvider(
            LocalMultiBlur provides hazeState
        ) {
            background?.let {
                Box(
                    modifier = Modifier
                        .multiBlurBackground(MultiBlurLevel.Window)
                ) {
                    it.invoke(this)
                }
            }

            content()
        }
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
        LocalMultiBlurLevelUp provides multiBlurLevelUp + LEVEL_UP
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
                hazeEffect(multiBlur)
            }
    } else {
        this
            .background(elseColor)
    }
}

internal val LocalMultiBlur = compositionLocalOf<HazeState?> { null }

internal val LocalMultiBlurLevelUp = compositionLocalOf { 0f }

private const val LEVEL_UP = 100f
