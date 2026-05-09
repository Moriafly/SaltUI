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

import androidx.compose.animation.core.EaseInOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.ext.safeMainIgnoringVisibility
import com.moriafly.salt.ui.icons.Back
import com.moriafly.salt.ui.icons.SaltIcons
import com.moriafly.salt.ui.verticalEdge
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeProgressive
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

/**
 * A basic screen layout with a title bar and a back button.
 *
 * This is a convenience overload that automatically provides a default back button.
 *
 * @param onBack Callback invoked when the back button is clicked.
 * @param modifier Modifier to be applied to the screen.
 * @param title Optional title text displayed in the title bar.
 * @param subtitle Optional subtitle text displayed below the title.
 * @param toolButtons Optional composable for trailing action buttons in the title bar.
 * @param contentPadding Padding values applied to the outer layout.
 * @param style Screen-level visual properties such as title bar backdrop type.
 * @param content The main content of the screen, receiving inner padding values.
 */
@UnstableSaltUiApi
@Composable
fun BasicScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    toolButtons: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = BasicScreenDefaults.ContentPadding,
    style: BasicScreenStyle = BasicScreenStyle.default(),
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
        subtitle = subtitle,
        toolButtons = toolButtons,
        contentPadding = contentPadding,
        style = style,
        content = content
    )
}

/**
 * A basic screen layout with a customizable title bar.
 *
 * @param actionButton Optional composable lambda for placing a leading action (e.g., back button).
 * @param modifier Modifier to be applied to the screen.
 * @param title Optional title text displayed in the title bar.
 * @param subtitle Optional subtitle text displayed below the title.
 * @param toolButtons Optional composable for trailing action buttons in the title bar.
 * @param contentPadding Padding values applied to the outer layout.
 * @param style Screen-level visual properties such as title bar backdrop type.
 * @param content The main content of the screen, receiving inner padding values.
 */
@UnstableSaltUiApi
@Composable
fun BasicScreen(
    actionButton: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    toolButtons: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = BasicScreenDefaults.ContentPadding,
    style: BasicScreenStyle = SaltTheme.basicScreenStyle,
    content: @Composable BoxScope.(PaddingValues) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        val hazeState = rememberHazeState()

        val boxContentPaddingTop =
            contentPadding.calculateTopPadding() + BasicScreenDefaults.TitleBarHeight

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
            hazeState = hazeState,
            backdropType = style.titleBarBackdropType
        )

        TitleBar(
            actionButton = actionButton,
            modifier = Modifier,
            title = title,
            subtitle = subtitle,
            toolButtons = toolButtons,
            contentPadding = contentPadding
        )
    }
}

/**
 * Visual properties for configuring [BasicScreen] appearance.
 *
 * @param titleBarBackdropType The type of backdrop effect applied behind the title bar.
 */
@UnstableSaltUiApi
data class BasicScreenStyle(
    val titleBarBackdropType: TitleBarBackdropType
) {
    /**
     * Available backdrop effect types for the title bar.
     */
    enum class TitleBarBackdropType {
        /**
         * No backdrop effect.
         */
        None,

        /**
         * Uniform mask backdrop.
         */
        Mask,

        /**
         * Progressive blur that fades toward the bottom.
         */
        Progressive
    }

    companion object {
        /**
         * Returns default properties based on the current platform.
         */
        fun default(
            titleBarBackdropType: TitleBarBackdropType =
                when (val os = OS.current) {
                    is OS.Android ->
                        when {
                            os.versionSdk >= OS.Android.ANDROID_13 ->
                                TitleBarBackdropType.Progressive
                            os.versionSdk >= OS.Android.ANDROID_12 ->
                                TitleBarBackdropType.Mask
                            else -> TitleBarBackdropType.None
                        }
                    is OS.IOS -> TitleBarBackdropType.Progressive
                    else -> TitleBarBackdropType.Mask
                }
        ): BasicScreenStyle =
            BasicScreenStyle(
                titleBarBackdropType = titleBarBackdropType
            )
    }
}

/**
 * A blur backdrop placed behind the title bar in [BasicScreen].
 *
 * @param height Height of the backdrop area.
 * @param hazeState Haze state for blur calculation.
 * @param backdropType Type of blur effect to apply.
 * @param modifier Modifier to be applied to the backdrop.
 */
@UnstableSaltUiApi
@Composable
private fun TitleBarBackdrop(
    height: Dp,
    hazeState: HazeState,
    backdropType: BasicScreenStyle.TitleBarBackdropType,
    modifier: Modifier = Modifier
) {
    val backdropModifier = when (backdropType) {
        BasicScreenStyle.TitleBarBackdropType.None ->
            Modifier

        BasicScreenStyle.TitleBarBackdropType.Mask ->
            Modifier
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .verticalEdge(top = height)
                .hazeEffect(hazeState) {
                    blurEffect {
                        blurRadius = 8.dp
                        noiseFactor = 0f
                        inputScale = HazeInputScale.Auto
                        mask = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black)
                        )
                    }
                }

        BasicScreenStyle.TitleBarBackdropType.Progressive ->
            Modifier
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()

                        drawRect(
                            brush = Brush.verticalGradient(
                                0f to Color.Transparent,
                                size.height to Color.Black
                            ),
                            blendMode = BlendMode.DstIn
                        )
                    }
                }
                .hazeEffect(hazeState) {
                    blurEffect {
                        noiseFactor = 0f
                        inputScale = HazeInputScale.Auto
                        progressive = HazeProgressive.verticalGradient(
                            easing = EaseInOut,
                            startIntensity = 1f,
                            endIntensity = 0f
                        )
                    }
                }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .then(backdropModifier)
    )
}

/**
 * Internal title bar component used by [BasicScreen].
 *
 * Arranges an optional [actionButton], an optional [title] with [subtitle],.
 * and optional [toolButtons] with default padding and height constraints.
 *
 * @param actionButton Optional leading action composable.
 * @param modifier Modifier to be applied to the title bar.
 * @param title Optional title text.
 * @param subtitle Optional subtitle text displayed below the title.
 * @param toolButtons Optional trailing action buttons.
 * @param contentPadding Padding values for the title bar edges.
 */
@UnstableSaltUiApi
@Composable
private fun TitleBar(
    actionButton: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    toolButtons: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    if (OS.isDesktop()) {
        DesktopTitleBar(
            actionButton = actionButton,
            modifier = modifier,
            title = title,
            subtitle = subtitle,
            toolButtons = toolButtons,
            contentPadding = contentPadding
        )
    } else {
        MobileTitleBar(
            actionButton = actionButton,
            modifier = modifier,
            title = title,
            subtitle = subtitle,
            toolButtons = toolButtons,
            contentPadding = contentPadding
        )
    }
}

@UnstableSaltUiApi
@Composable
private fun DesktopTitleBar(
    actionButton: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    toolButtons: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val layoutDirection = LocalLayoutDirection.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                PaddingValues(
                    start = contentPadding.calculateStartPadding(layoutDirection) + 12.dp,
                    top = contentPadding.calculateTopPadding(),
                    end = contentPadding.calculateEndPadding(layoutDirection) + 12.dp
                )
            )
            .height(BasicScreenDefaults.TitleBarHeight)
            .pointerInput(Unit) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (actionButton != null) {
                actionButton()
                Spacer(Modifier.width(8.dp))
            } else {
                Spacer(Modifier.width(4.dp))
            }

            if (title != null) {
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            if (subtitle != null) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = subtitle,
                    color = SaltTheme.colors.subText,
                    fontSize = 14.sp,
                    style = SaltTheme.textStyles.sub
                )
            }
        }

        Row(
            modifier = Modifier
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.End
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            toolButtons?.invoke()
        }
    }
}

@UnstableSaltUiApi
@Composable
private fun MobileTitleBar(
    actionButton: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    toolButtons: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val layoutDirection = LocalLayoutDirection.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                PaddingValues(
                    start = contentPadding.calculateStartPadding(layoutDirection) + 8.dp,
                    top = contentPadding.calculateTopPadding(),
                    end = contentPadding.calculateEndPadding(layoutDirection) + 8.dp
                )
            )
            .height(BasicScreenDefaults.TitleBarHeight)
            .pointerInput(Unit) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        actionButton?.invoke()

        Spacer(Modifier.width(8.dp))

        Column {
            if (title != null) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 17.sp
                )
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = SaltTheme.colors.subText,
                    style = SaltTheme.textStyles.sub
                )
            }
        }

        Row(
            modifier = Modifier
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(
                space = 4.dp,
                alignment = Alignment.End
            ),
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
     * Default height of the title bar.
     */
    internal val TitleBarHeight: Dp = 56.dp

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
     * A default back button using the Salt back icon.
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
        TitleBarButton(
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
