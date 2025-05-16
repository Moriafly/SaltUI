/*
 * Salt UI
 * Copyright (C) 2024 Moriafly
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

@file:Suppress("unused")

package com.moriafly.salt.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp

/**
 * Get [SaltColors] by Material 3 [ColorScheme].
 *
 * Of course, you can also implement the corresponding color conversion yourself.
 */
@RequiresApi(Build.VERSION_CODES.S)
@UnstableSaltUiApi
fun saltColorsByColorScheme(
    colorScheme: ColorScheme
): SaltColors = SaltColors(
    highlight = colorScheme.primary,
    text = colorScheme.onSurface,
    subText = colorScheme.onSurfaceVariant,
    background = colorScheme.surface,
    subBackground = colorScheme.surfaceColorAtElevation(3.dp),
    popup = colorScheme.surfaceColorAtElevation(3.dp).compositeOver(colorScheme.surface),
    stroke = colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
    onHighlight = Color.White
)
