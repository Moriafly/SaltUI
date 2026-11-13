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

package com.moriafly.salt.ui.sample.ui.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.MeshGradientPainter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.BasicButton
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.ButtonAppearance
import com.moriafly.salt.ui.ButtonColors
import com.moriafly.salt.ui.ButtonDefaults
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.LocalControlSize
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.outerPadding
import com.moriafly.salt.ui.material.GlassEffect
import com.moriafly.salt.ui.material.GlassSource
import com.moriafly.salt.ui.material.glass
import com.moriafly.salt.ui.material.glassSource
import com.moriafly.salt.ui.material.rememberGlassSource
import com.moriafly.salt.ui.sample.ui.icons.SimpleIcons
import com.moriafly.salt.ui.sample.ui.icons.Star
import com.moriafly.salt.ui.sample.ui.screen.basic.BasicScreenBox
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(UnstableSaltUiApi::class)
@Composable
fun GlassScreen() {
    val glassSource = rememberGlassSource()

    BasicScreenBox(
        title = "Glass"
    ) { contentPadding ->
        GlassBackground(glassSource)

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .glassSource(glassSource),
                contentPadding = contentPadding,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item { GlassArticle() }
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(SaltTheme.dimens.subPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GlassDemoRow(source = glassSource, label = "Solid", effect = GlassEffect.Solid())
                GlassDemoRow(source = glassSource, label = "Blur", effect = GlassEffect.Blur)
                GlassDemoRow(source = glassSource, label = "Default")
                GlassDemoRow(
                    source = glassSource,
                    label = "Transparent",
                    effect = GlassEffect.Transparent
                )
            }
        }
    }
}

@Composable
private fun GlassArticle() {
    val textColor = SaltTheme.colors.text
    val detailColor = textColor.copy(alpha = textColor.alpha * 0.7f)
    val labelStyle = SaltTheme.textStyles.sub.copy(
        color = detailColor,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.2.sp
    )
    val bodyStyle = SaltTheme.textStyles.paragraph.copy(
        color = textColor,
        lineHeight = 26.sp
    )
    Column(modifier = Modifier.outerPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("SALT JOURNAL", style = labelStyle)
            Text("FIELD NOTES / 01", style = labelStyle)
        }
        Spacer(Modifier.height(30.dp))
        Text(
            text = "A study\nin light.",
            fontFamily = FontFamily.Serif,
            fontSize = 44.sp,
            lineHeight = 47.sp,
            letterSpacing = (-1).sp,
            color = textColor
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "On layers, movement and the quiet beauty of what lies beneath.",
            fontSize = 18.sp,
            lineHeight = 27.sp,
            color = detailColor
        )
        Spacer(Modifier.height(28.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(detailColor.copy(alpha = 0.3f)))
        Spacer(Modifier.height(14.dp))
        Text("AN ESSAY ON SEEING", style = labelStyle)
        Spacer(Modifier.height(36.dp))
        Text(
            text = "Light never stays in one place. It moves across a room, gathers at an edge, " +
                "and turns an ordinary surface into something worth noticing.",
            style = bodyStyle
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "A sheet of glass lets us see several things at once: its own delicate shape, " +
                "the words below it, and the color moving beyond the page.",
            style = bodyStyle
        )
        Spacer(Modifier.height(40.dp))
        Text("01 / THE SPACE BETWEEN", style = labelStyle)
        Spacer(Modifier.height(18.dp))
        Text(
            text = "A surface can be quiet\nwithout being still.",
            fontFamily = FontFamily.Serif,
            fontSize = 26.sp,
            lineHeight = 33.sp,
            color = textColor
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Look closely and the details begin to shift. A line bends as it reaches the " +
                "edge; a bright color softens as it passes through. The view changes, but the " +
                "story beneath remains.",
            style = bodyStyle
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "This is the pleasure of transparency: not an empty space, but a conversation " +
                "between what is in front of us and what lies behind.",
            style = bodyStyle
        )
        Spacer(Modifier.height(36.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(detailColor.copy(alpha = 0.3f)))
        Spacer(Modifier.height(14.dp))
        Text("THE END", style = labelStyle)
    }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun GlassDemoRow(
    source: GlassSource,
    label: String,
    effect: GlassEffect = SaltTheme.glassEffect
) {
    val controlSize = LocalControlSize.current
    val textColor = SaltTheme.colors.text
    val buttonColors = ButtonColors(
        containerColor = Color.Transparent,
        contentColor = textColor,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = textColor
    )
    var isFavorite by remember { mutableStateOf(false) }
    var clickCount by remember { mutableIntStateOf(0) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicButton(
            onClick = { isFavorite = !isFavorite },
            modifier = Modifier
                .size(ButtonDefaults.containerHeight(controlSize))
                .glass(source = source, shape = CircleShape, effect = effect),
            size = controlSize,
            shape = CircleShape,
            colors = buttonColors,
            contentPadding = PaddingValues(0.dp),
            indication = null
        ) {
            Icon(
                imageVector = SimpleIcons.Star,
                contentDescription = if (isFavorite) {
                    "Remove $label from favorites"
                } else {
                    "Add $label to favorites"
                },
                modifier = Modifier.size(ButtonDefaults.iconSize(controlSize)),
                tint = if (isFavorite) SaltTheme.colors.highlight else textColor
            )
        }
        Button(
            onClick = { clickCount++ },
            modifier = Modifier
                .widthIn(min = 164.dp)
                .glass(source = source, shape = CircleShape, effect = effect),
            size = controlSize,
            appearance = ButtonAppearance.Glass
        ) {
            Text("$label ($clickCount)")
        }
    }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun GlassBackground(source: GlassSource) {
    val isDarkTheme = SaltTheme.configs.isDarkTheme
    val colors = remember(isDarkTheme) {
        if (isDarkTheme) {
            listOf(
                Color(0xFF050927), Color(0xFF171454), Color(0xFF241276),
                Color(0xFF292176), Color(0xFF2541B5), Color(0xFF1456C7),
                Color(0xFF081E55), Color(0xFF173BC5), Color(0xFF071648)
            )
        } else {
            listOf(
                Color(0xFF2468B8), Color(0xFF66B7F2), Color(0xFFB6E5FF),
                Color(0xFF78C8EA), Color(0xFFFFE4A8), Color(0xFF4D98E5),
                Color(0xFF2257C8), Color(0xFF3C79F3), Color(0xFF0A2C83)
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "Glass background")
    val phase = transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing)
        ),
        label = "Flow"
    )
    val android = OS.current as? OS.Android
    val background = if (android == null || android.versionSdk >= OS.Android.ANDROID_10) {
        val painter = remember(colors) {
            MeshGradientPainter(rows = 2, columns = 2) {
                setVertex(0, 0, Offset(0f, 0f), colors[0])
                setVertex(0, 1, Offset(0.5f, 0f), colors[1])
                setVertex(0, 2, Offset(1f, 0f), colors[2])
                setVertex(1, 0, Offset(0f, 0.64f), colors[3])
                setVertex(1, 1, Offset(0.72f, 0.5f), colors[4])
                setVertex(1, 2, Offset(1f, 0.36f), colors[5])
                setVertex(2, 0, Offset(0f, 1f), colors[6])
                setVertex(2, 1, Offset(0.5f, 1f), colors[7])
                setVertex(2, 2, Offset(1f, 1f), colors[8])
            }
        }
        Modifier.paint(painter)
    } else {
        // Hardware-accelerated drawVertices is available from Android 10.
        Modifier.drawWithCache {
            val base = Brush.linearGradient(listOf(colors[0], colors[4], colors[8]))
            val radius = maxOf(size.width, size.height) * 0.7f
            val center = Offset(size.width * 0.85f, size.height * 0.5f)
            val glow = Brush.radialGradient(
                colors = listOf(colors[5], colors[5].copy(alpha = 0f)),
                center = center,
                radius = radius
            )
            onDrawBehind {
                drawRect(base)
                drawCircle(glow, radius = radius, center = center)
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .glassSource(source)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Reuse the static mesh while the separate wave layer animates.
                .graphicsLayer()
                .then(background)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val width = size.width
                    val height = size.height
                    val wave = Path()
                    val lowerWave = Path()
                    val ribbon = Path()
                    val rim = Path()
                    val rimStroke = Stroke(width = 2.dp.toPx())
                    val waveBrush = Brush.linearGradient(
                        colors = if (isDarkTheme) {
                            listOf(Color(0xFF526DFF), Color(0xFF183BC6), Color(0xFF060E3C))
                        } else {
                            listOf(Color(0xFF8FDFFF), Color(0xFF245FEA), Color(0xFF071E6B))
                        },
                        start = Offset(width * 0.1f, height * 0.2f),
                        end = Offset(width * 0.8f, height)
                    )
                    val lowerBrush = Brush.linearGradient(
                        colors = if (isDarkTheme) {
                            listOf(Color(0xAA263FCD), Color(0xEE040B2E))
                        } else {
                            listOf(Color(0x9972C6F9), Color(0xDD0A267D))
                        },
                        start = Offset.Zero,
                        end = Offset(width, height)
                    )
                    val ribbonBrush = Brush.linearGradient(
                        colors = if (isDarkTheme) {
                            listOf(Color(0x005A8BFF), Color(0xBB729DFF), Color(0x332753EC))
                        } else {
                            listOf(Color(0x008CDAFF), Color(0xCCB8F2FF), Color(0x554782F5))
                        },
                        start = Offset(0f, height),
                        end = Offset(width, 0f)
                    )
                    val rimBrush = Brush.linearGradient(
                        colors = listOf(Color(0x006BC7FF), Color(0xDDC6EFFF), Color(0x1181BDFF)),
                        start = Offset(0f, height),
                        end = Offset(width, 0f)
                    )
                    onDrawBehind {
                        val angle = phase.value
                        val drift = sin(angle)
                        val swell = cos(angle)
                        val leftY = height * (0.69f + 0.18f * drift)
                        val middleY = height * (0.44f - 0.16f * swell)
                        val rightY = height * (0.24f + 0.14f * drift)

                        rim.reset()
                        rim.moveTo(-0.1f * width, leftY)
                        rim.cubicTo(
                            0.25f * width, leftY - 0.03f * height,
                            0.44f * width, middleY,
                            1.1f * width, rightY
                        )
                        wave.reset()
                        wave.addPath(rim)
                        wave.lineTo(1.1f * width, height)
                        wave.lineTo(-0.1f * width, height)
                        wave.close()
                        drawPath(wave, waveBrush)

                        lowerWave.reset()
                        lowerWave.moveTo(-0.1f * width, height * (0.91f - 0.08f * swell))
                        lowerWave.cubicTo(
                            0.22f * width, height * (0.73f + 0.09f * drift),
                            0.58f * width, height * (1.03f - 0.1f * swell),
                            1.1f * width, height * (0.62f - 0.08f * drift)
                        )
                        lowerWave.lineTo(1.1f * width, height)
                        lowerWave.lineTo(-0.1f * width, height)
                        lowerWave.close()
                        drawPath(lowerWave, lowerBrush)

                        ribbon.reset()
                        ribbon.addPath(rim)
                        ribbon.lineTo(1.1f * width, rightY + 0.13f * height)
                        ribbon.cubicTo(
                            0.47f * width, middleY + 0.12f * height,
                            0.22f * width, leftY + 0.17f * height,
                            -0.1f * width, leftY + 0.16f * height
                        )
                        ribbon.close()
                        drawPath(ribbon, ribbonBrush)
                        drawPath(rim, rimBrush, style = rimStroke)
                    }
                }
        )
    }
}
