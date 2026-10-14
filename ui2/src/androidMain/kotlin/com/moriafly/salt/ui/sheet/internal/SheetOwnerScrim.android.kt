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

@file:Suppress("ktlint:standard:filename")

package com.moriafly.salt.ui.sheet.internal

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.CanvasHolder
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Main-thread ownership follows the standard Dialog hosts, without creating another window.
private val ownerScrims = mutableMapOf<Pair<View, SheetHostCoordinator>, SheetScrimOverlay>()

/**
 * Draws one persistent scrim above the owner and below its Dialog windows.
 * A drawable overlay has no input region and survives overlapping Sheet transitions.
 */
@Composable
internal fun SheetOwnerScrim(ownerView: View, coordinator: SheetHostCoordinator) {
    val owner = ownerView.rootView
    val scope = rememberCoroutineScope()
    DisposableEffect(owner, coordinator) {
        val key = owner to coordinator
        val scrim = ownerScrims.getOrPut(key) { SheetScrimOverlay(owner) }
        scrim.acquire()
        val job = scope.launch {
            snapshotFlow { coordinator.scrimAlpha }.collect { scrim.updateAlpha(it) }
        }
        onDispose {
            job.cancel()
            if (scrim.release()) ownerScrims.remove(key)
        }
    }
}

private class SheetScrimOverlay(
    private val owner: View
) {
    private val drawable = SheetScrimDrawable()
    private var hosts = 0
    private val layoutListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
        updateBounds()
    }

    fun acquire() {
        hosts++
        if (hosts == 1) {
            updateBounds()
            owner.overlay.add(drawable)
            owner.addOnLayoutChangeListener(layoutListener)
        }
    }

    fun release(): Boolean {
        hosts--
        if (hosts > 0) return false
        owner.overlay.remove(drawable)
        owner.removeOnLayoutChangeListener(layoutListener)
        return true
    }

    fun updateAlpha(alpha: Float) {
        drawable.updateAlpha(alpha)
    }

    private fun updateBounds() {
        drawable.setBounds(0, 0, owner.width, owner.height)
    }
}

private class SheetScrimDrawable : Drawable() {
    private val canvasHolder = CanvasHolder()
    private val drawScope = CanvasDrawScope()
    private val density = Density(1f)
    private var opacity = 0f

    fun updateAlpha(alpha: Float) {
        val next = alpha.coerceIn(0f, 1f)
        if (opacity == next) return
        opacity = next
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        if (opacity <= 0f) return
        canvasHolder.drawInto(canvas) {
            drawScope.draw(
                density = density,
                layoutDirection = LayoutDirection.Ltr,
                canvas = this,
                size = Size(bounds.width().toFloat(), bounds.height().toFloat())
            ) {
                drawSheetScrim(opacity)
            }
        }
    }

    override fun setAlpha(alpha: Int) {
        updateAlpha(alpha / 255f)
    }

    override fun getAlpha(): Int = (opacity * 255).roundToInt()

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
