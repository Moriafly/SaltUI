@file:Suppress("UNUSED")

/**
 * SaltUI
 * Copyright (C) 2023 Moriafly
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

package com.moriafly.salt.ui

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter

/**
 * FadeClickable
 */
@UnstableSaltApi
@SuppressLint("UnnecessaryComposedModifier")
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.fadeClickable(
    onClick: () -> Unit
): Modifier = this.composed {
    var pressed by remember { mutableStateOf(false) }
    return@composed Modifier
        .pointerInteropFilter {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> pressed = true
                MotionEvent.ACTION_UP -> {
                    pressed = false
                    onClick()
                }

                MotionEvent.ACTION_CANCEL -> pressed = false
                else -> return@pointerInteropFilter false
            }
            return@pointerInteropFilter true
        }
        .graphicsLayer {
            alpha = if (pressed) 0.35f else 1f
        }
}