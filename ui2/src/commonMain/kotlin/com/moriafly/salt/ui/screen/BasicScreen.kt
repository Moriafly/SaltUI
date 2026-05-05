/*
 * Salt UI
 * Copyright (C) 2025 Moriafly
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

package com.moriafly.salt.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.button.PillButton
import com.moriafly.salt.ui.button.PillButtonDefaults
import com.moriafly.salt.ui.ext.safeMainIgnoringVisibility
import com.moriafly.salt.ui.icons.Back
import com.moriafly.salt.ui.icons.SaltIcons
import com.moriafly.salt.ui.verticalEdge
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.blur.HazeProgressive
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState

/**
 * A basic screen layout with a title bar and a back button.
 *
 * This is a convenience overload that automatically provides a default back button.
 *
 * @param onBack Callback invoked when the back button is clicked.
 * @param modifier Modifier to be applied to the screen.
 * @param title Optional title text displayed in the title bar.
 * @param toolButtons Optional composable for trailing action buttons in the title bar.
 * @param contentPadding Padding values applied to the outer layout.
 * @param content The main content of the screen, receiving inner padding values.
 */
@UnstableSaltUiApi
@Composable
fun BasicScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    toolButtons: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = BasicScreenDefaults.ContentPadding,
    content: @Composable BoxScope.(PaddingValues) -> Unit
) {
    BasicScreen(
        actionButton = {
            BasicScreenDefaults.BackButton(
                onBack = onBack
            )
        },
        modifier = modifier,
        title = title,
        toolButtons = toolButtons,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * A basic screen layout with a customizable title bar.
 *
 * @param actionButton Optional composable lambda for placing a leading action (e.g., back button).
 * @param modifier Modifier to be applied to the screen.
 * @param title Optional title text displayed in the title bar.
 * @param toolButtons Optional composable for trailing action buttons in the title bar.
 * @param contentPadding Padding values applied to the outer layout.
 * @param content The main content of the screen, receiving inner padding values.
 */
@UnstableSaltUiApi
@Composable
fun BasicScreen(
    actionButton: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    title: String? = null,
    toolButtons: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = BasicScreenDefaults.ContentPadding,
    content: @Composable BoxScope.(PaddingValues) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        val hazeState = rememberHazeState()

        val boxContentPaddingTop =
            contentPadding.calculateTopPadding() +
                PillButtonDefaults.Height +
                BasicScreenDefaults.TitleBarInsideVerticalPadding * 2

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    onDrawWithContent {
                        clipRect(
                            top = boxContentPaddingTop.toPx()
                        ) {
                            this@onDrawWithContent.drawContent()
                        }
                    }
                }
                .hazeSource(hazeState)
        ) {
            val boxContentPaddingValues =
                PaddingValues(
                    top = boxContentPaddingTop
                )
            content(boxContentPaddingValues)
        }

        TitleBarBackdrop(
            height = boxContentPaddingTop,
            hazeState = hazeState
        )

        TitleBar(
            actionButton = actionButton,
            modifier = Modifier,
            title = title,
            toolButtons = toolButtons,
            contentPadding = contentPadding
        )
    }
}

/**
 * A blur backdrop placed behind the title bar in [BasicScreen].
 */
@UnstableSaltUiApi
@Composable
private fun TitleBarBackdrop(
    height: Dp,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .verticalEdge(top = height)
            .hazeEffect(hazeState) {
                blurEffect {
                    noiseFactor = 0f
                    inputScale = HazeInputScale.Auto
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = 1f,
                        endIntensity = 0f
                    )
                }
            }
    )
}

/**
 * Internal title bar component used by [BasicScreen].
 *
 * Arranges an optional [actionButton], an optional [title], and optional [toolButtons]
 * horizontally with default padding and height constraints.
 */
@UnstableSaltUiApi
@Composable
private fun TitleBar(
    actionButton: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    title: String? = null,
    toolButtons: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val layoutDirection = LocalLayoutDirection.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                PaddingValues(
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    top = contentPadding.calculateTopPadding(),
                    end = contentPadding.calculateEndPadding(layoutDirection)
                )
            )
            .padding(
                horizontal = 16.dp,
                vertical = BasicScreenDefaults.TitleBarInsideVerticalPadding
            )
            // Same as PillButtonDefaults.Height
            .height(PillButtonDefaults.Height)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            actionButton?.invoke()

            if (actionButton != null && title != null) {
                Spacer(Modifier.width(8.dp))
            }

            if (title != null) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        Row(
            modifier = Modifier
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            toolButtons?.invoke()
        }
    }
}

/**
 * Default values and components for [BasicScreen].
 */
@UnstableSaltUiApi
object BasicScreenDefaults {
    /**
     * Vertical padding inside the title bar.
     *
     * Uses a larger value on desktop platforms for better visual balance.
     */
    internal val TitleBarInsideVerticalPadding: Dp =
        if (OS.isDesktop()) 16.dp else 8.dp

    /**
     * Default content padding that respects the safe area insets at the top.
     */
    val ContentPadding: PaddingValues
        @Composable
        get() {
            val topPadding = WindowInsets.safeMainIgnoringVisibility
                .asPaddingValues()
                .calculateTopPadding()
            return PaddingValues(top = topPadding)
        }

    /**
     * A default back button using a pill-shaped container and the Salt back icon.
     *
     * @param onBack Callback invoked when the button is clicked.
     * @param modifier Modifier to be applied to the button.
     * @param enabled Whether the button is enabled.
     */
    @Composable
    fun BackButton(
        onBack: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true
    ) {
        PillButton(
            onClick = onBack,
            modifier = modifier,
            enabled = enabled
        ) {
            Icon(
                painter = rememberVectorPainter(SaltIcons.Back),
                contentDescription = null
            )
        }
    }
}
