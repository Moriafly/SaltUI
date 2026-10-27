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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.ButtonAppearance
import com.moriafly.salt.ui.ControlSize
import com.moriafly.salt.ui.ItemButton
import com.moriafly.salt.ui.ItemDropdown
import com.moriafly.salt.ui.ItemSlider
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.ItemValue
import com.moriafly.salt.ui.SaltMaterial
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.innerPadding
import com.moriafly.salt.ui.material.MaterialSource
import com.moriafly.salt.ui.material.MaterialSourceMode
import com.moriafly.salt.ui.material.MaterialType
import com.moriafly.salt.ui.material.material
import com.moriafly.salt.ui.material.subMaterial
import com.moriafly.salt.ui.popup.PopupMenuItem
import com.moriafly.salt.ui.sample.ui.screen.basic.BasicScreenBox
import com.moriafly.salt.ui.screen.ScreenCard
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class MaterialTestScene(
    val widthFraction: Float,
    val heightFraction: Float,
    val panelCount: Int
) {
    Small(0.42f, 0.4f, 1),
    Large(0.8f, 0.78f, 1),
    Shared(0.42f, 0.36f, 4)
}

@OptIn(UnstableSaltUiApi::class)
@Composable
fun MaterialTestScreen(
    modifier: Modifier = Modifier
) {
    var materialType by rememberSaveable { mutableStateOf(MaterialType.BlurryGlass) }
    var panelScene by rememberSaveable { mutableStateOf(MaterialTestScene.Large) }
    var static by rememberSaveable { mutableStateOf(false) }
    var dark by rememberSaveable { mutableStateOf(false) }
    var animate by rememberSaveable { mutableStateOf(false) }
    var wallpaper by rememberSaveable { mutableIntStateOf(0) }
    var contentKey by rememberSaveable { mutableIntStateOf(0) }
    var sourceHeight by rememberSaveable { mutableFloatStateOf(260f) }
    BasicScreenBox(
        modifier = modifier,
        title = "Material test"
    ) { contentPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            val maximumHeight = maxHeight.value * 0.55f
            val minimumHeight = minOf(240f, maximumHeight * 0.7f)
            val previewHeight = sourceHeight.coerceIn(minimumHeight, maximumHeight)
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                SaltTheme(
                    configs = SaltTheme.configs.copy(isDarkTheme = dark),
                    material = SaltMaterial(materialType)
                ) {
                    MaterialTestPreview(
                        mode = if (static) {
                            MaterialSourceMode.Static(contentKey)
                        } else {
                            MaterialSourceMode.Dynamic
                        },
                        scene = panelScene,
                        wallpaper = wallpaper,
                        animate = animate,
                        modifier = Modifier
                            .innerPadding()
                            .fillMaxWidth()
                            .height(previewHeight.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    ScreenCard(
                        header = "Source",
                        footer = "Static reuses background pixels until the key or size changes."
                    ) {
                        ItemSwitcher(
                            state = static,
                            onChange = { static = it },
                            text = "Static source",
                            sub = if (static) {
                                "Static key $contentKey"
                            } else {
                                "Dynamic"
                            }
                        )
                        ItemValue(text = "Wallpaper", sub = "Version ${wallpaper + 1}")
                        ItemButton(
                            onClick = {
                                wallpaper++
                                contentKey++
                            },
                            text = "Change wallpaper"
                        )
                        ItemButton(
                            onClick = { contentKey++ },
                            text = "Refresh cache",
                            enabled = static
                        )
                    }
                    ScreenCard(header = "Appearance") {
                        ItemDropdown(text = "Material type", value = materialType.name) {
                            for (type in MaterialType.entries) {
                                if (type != MaterialType.None) {
                                    PopupMenuItem(
                                        onClick = {
                                            materialType = type
                                            state.dismiss()
                                        },
                                        text = type.name,
                                        selected = type == materialType
                                    )
                                }
                            }
                        }
                        ItemSwitcher(
                            state = dark,
                            onChange = { dark = it },
                            text = "Dark preview"
                        )
                    }
                    ScreenCard(header = "Scene") {
                        ItemDropdown(text = "Panel layout", value = panelScene.name) {
                            for (scene in MaterialTestScene.entries) {
                                PopupMenuItem(
                                    onClick = {
                                        panelScene = scene
                                        state.dismiss()
                                    },
                                    text = scene.name,
                                    selected = scene == panelScene
                                )
                            }
                        }
                        ItemSwitcher(
                            state = animate,
                            onChange = { animate = it },
                            text = "Animate panels"
                        )
                        ItemSlider(
                            value = previewHeight,
                            onValueChange = { sourceHeight = it },
                            text = "Source height",
                            sub = "${previewHeight.roundToInt()} dp",
                            valueRange = minimumHeight..maximumHeight
                        )
                    }
                }
            }
        }
    }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun MaterialTestPreview(
    mode: MaterialSourceMode,
    scene: MaterialTestScene,
    wallpaper: Int,
    animate: Boolean,
    modifier: Modifier = Modifier
) {
    var taps by rememberSaveable { mutableIntStateOf(0) }
    var phase by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(animate) {
        phase = 0f
        if (animate) {
            val start = withFrameNanos { it }
            while (true) {
                withFrameNanos {
                    phase = sin((it - start) / 1_000_000_000.0 * 2).toFloat()
                }
            }
        }
    }
    val shared = scene == MaterialTestScene.Shared
    Layout(
        modifier = modifier
            .clip(SaltTheme.shapes.large)
            .semantics { contentDescription = "Material preview" },
        content = {
            MaterialSource(
                modifier = Modifier
                    .fillMaxSize(),
                materialSelf = false,
                mode = mode
            ) {
                MaterialTestWallpaper(
                    version = wallpaper,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
            for (index in 0 until scene.panelCount) {
                val subBackground = shared && index % 2 == 1
                val effect = if (subBackground) {
                    Modifier.subMaterial()
                } else {
                    Modifier.material()
                }
                Column(
                    modifier = Modifier
                        .clip(SaltTheme.shapes.medium)
                        .then(effect)
                        .innerPadding(),
                    verticalArrangement = Arrangement.Center
                ) {
                    if (index == 0) {
                        Button(
                            onClick = { taps++ },
                            text = "Tap count: $taps",
                            appearance = ButtonAppearance.Subtle,
                            size = ControlSize.Small
                        )
                    } else {
                        Text(
                            text = if (subBackground) { "Sub-background" } else { "Background" },
                            style = SaltTheme.textStyles.sub
                        )
                    }
                }
            }
        }
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val background = measurables.first().measure(Constraints.fixed(width, height))
        val panelConstraints = Constraints.fixed(
            width = (width * scene.widthFraction).roundToInt(),
            height = (height * scene.heightFraction).roundToInt()
        )
        val panels = List(scene.panelCount) { index ->
            measurables[index + 1].measure(panelConstraints)
        }
        layout(width, height) {
            background.placeRelative(0, 0)
            val motion = phase * 0.02f
            for (index in panels.indices) {
                val x = if (shared) {
                    0.04f + index % 2 * 0.5f
                } else {
                    (1 - scene.widthFraction) / 2
                }
                val y = if (shared) {
                    0.07f + index / 2 * 0.49f
                } else {
                    (1 - scene.heightFraction) / 2
                }
                panels[index].placeRelative(
                    x = (width * (x + motion)).roundToInt(),
                    y = (height * (y + motion)).roundToInt()
                )
            }
        }
    }
}

@Composable
private fun MaterialTestWallpaper(
    version: Int,
    modifier: Modifier = Modifier
) {
    val brush = remember(version) {
        val colors = if (version % 2 == 0) {
            listOf(Color(0xFFE44F70), Color(0xFF6546D7), Color(0xFF25C9BE))
        } else {
            listOf(Color(0xFFF6AE37), Color(0xFFDF516C), Color(0xFF2B7BC8))
        }
        Brush.linearGradient(colors)
    }
    Canvas(modifier = modifier) {
        drawRect(brush = brush)
        val cellWidth = size.width / 8
        val cellHeight = size.height / 6
        for (row in 0 until 6) {
            for (column in 0 until 8) {
                if ((row + column) % 2 == 0) {
                    drawRect(
                        color = Color.White.copy(alpha = 0.25f),
                        topLeft = Offset(column * cellWidth, row * cellHeight),
                        size = Size(cellWidth, cellHeight)
                    )
                }
            }
        }
    }
}
