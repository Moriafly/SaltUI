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

@file:OptIn(com.moriafly.salt.ui.UnstableSaltUiApi::class)

package com.moriafly.salt.ui.sheet.internal

import androidx.compose.animation.core.animate
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.AnchoredDraggableState
import com.moriafly.salt.ui.DraggableAnchors
import com.moriafly.salt.ui.sheet.SheetDetent
import com.moriafly.salt.ui.sheet.SheetProperties
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.math.abs

internal sealed interface SheetAnchor {
    data object Hidden : SheetAnchor

    data class Detent(
        val value: SheetDetent
    ) : SheetAnchor
}

internal class SheetTransitionState(
    private var density: Density,
    private val preferredDetent: () -> SheetDetent,
    private val onDetentChanged: (SheetDetent) -> Unit
) {
    val position = AnchoredDraggableState<SheetAnchor>(
        initialValue = SheetAnchor.Hidden,
        positionalThreshold = { it * 0.5f },
        velocityThreshold = { with(density) { 600.dp.toPx() } },
        animationSpec = SheetDefaults.MotionSpec
    )
    var viewportHeight by mutableFloatStateOf(0f)
        private set
    var points by mutableStateOf<List<Pair<SheetDetent, Float>>>(emptyList())
        private set
    private var contentHeight = 0f
    private var scrimReferenceHeight by mutableFloatStateOf(0f)
    private var presentationHeight by mutableFloatStateOf(0f)
    private var presentationJob: Job? = null
    var selected by mutableStateOf<SheetDetent>(preferredDetent())
        private set
    private var animationJob: Job? = null
    private var gestureOrigin: SheetDetent? = null
    private var visibleRequested = false
    private var presentationStarted = false

    val ready: Boolean get() = points.isNotEmpty()
    val selectedHeight: Float get() = points.firstOrNull { it.first == selected }?.second
        ?: viewportHeight
    val visibleHeight: Float get() = (
        viewportHeight -
            position.offset.takeUnless { it.isNaN() }.orZero()
    )
        .coerceIn(0f, viewportHeight)

    // Freeze the presentation size so animated offsets only invalidate the layer.
    val surfaceHeight: Float by derivedStateOf(structuralEqualityPolicy()) {
        val minimumHeight = points.firstOrNull()?.second ?: 0f
        if (presentationHeight > 0f) {
            presentationHeight.coerceIn(minimumHeight, viewportHeight)
        } else {
            visibleHeight.coerceAtLeast(minimumHeight)
        }
    }

    // Use the live offset so an interrupted floating transition keeps its current motion.
    val presentationProgress: Float get() = (
        viewportHeight - position.offset.takeUnless { it.isNaN() }.orZero()
    ) / selectedHeight.coerceAtLeast(1f)

    // Detent resizing keeps full dimming; dragging below the smallest detent fades it out.
    val scrimProgress: Float
        get() {
            val reference = scrimReferenceHeight.takeIf { it > 0f }
                ?: points.firstOrNull()?.second
                ?: return 0f
            return (visibleHeight / reference.coerceAtLeast(1f)).coerceIn(0f, 1f)
        }

    fun resetGeometry() {
        points = emptyList()
        contentHeight = 0f
    }

    fun updateGeometry(
        density: Density,
        height: Int,
        chromeHeight: Int,
        measuredContent: Int?,
        properties: SheetProperties,
        bottomInset: Int = 0
    ) {
        if (height <= 0) return
        val densityScale = density.density / this.density.density
        val previousHeight = points.firstOrNull {
            it.first == selected
        }?.second?.times(densityScale)
        val oldProgress = presentationProgress
        val oldScrimProgress = scrimProgress
        val oldSelectedHeight = selectedHeight
        // Keep the live position while a replacement waits for the incoming surface's measurement.
        val preservePosition = ready && presentationStarted
        this.density = density
        viewportHeight = height.toFloat()
        val inset = bottomInset.coerceIn(0, height).toFloat()
        contentHeight = if (measuredContent != null) {
            (measuredContent - inset).coerceAtLeast(0f)
        } else {
            contentHeight * densityScale
        }
        val geometry = resolveSheetGeometry(
            density = density,
            height = height,
            chromeHeight = chromeHeight,
            bottomInset = bottomInset,
            contentHeight = contentHeight,
            preferred = preferredDetent(),
            previousHeight = previousHeight,
            properties = properties
        )
        selected = geometry.selected
        points = geometry.points
        val anchors = DraggableAnchors {
            SheetAnchor.Hidden at viewportHeight
            points.forEach { (detent, resolvedHeight) ->
                SheetAnchor.Detent(detent) at
                    viewportHeight - resolvedHeight
            }
        }
        val anchorsChanged = anchors != position.anchors
        if (presentationHeight > 0f && anchorsChanged) {
            val heightScale = selectedHeight / oldSelectedHeight.coerceAtLeast(1f)
            presentationHeight = (presentationHeight * heightScale).coerceAtMost(viewportHeight)
        }
        position.updateAnchors(
            newAnchors = anchors,
            newTarget = if (!visibleRequested || !presentationStarted) {
                SheetAnchor.Hidden
            } else {
                SheetAnchor.Detent(selected)
            }
        )
        if (preservePosition && anchorsChanged) {
            val mappedOffset = (viewportHeight - selectedHeight * oldProgress)
                .coerceIn(anchors.minPosition(), anchors.maxPosition())
            position.dispatchRawDelta(mappedOffset - position.requireOffset())
            if (scrimReferenceHeight > 0f && oldScrimProgress > 0f) {
                scrimReferenceHeight = visibleHeight / oldScrimProgress
            }
        }
    }

    fun updateVisibility(visible: Boolean) {
        visibleRequested = visible
        if (!visible) gestureOrigin = null
    }

    suspend fun animateVisible(visible: Boolean) {
        val job = currentCoroutineContext().job
        presentationJob = job
        // Measure the complete panel once, then reveal or hide it with a layer translation.
        presentationHeight = if (visible) selectedHeight else surfaceHeight
        updateVisibility(visible = visible)
        if (visible) presentationStarted = true
        try {
            if (!visible) {
                // Capture the current opacity so release after a drag does not make the scrim jump.
                val progress = scrimProgress
                if (progress > 0f) scrimReferenceHeight = visibleHeight / progress
            }
            animate(if (visible) SheetAnchor.Detent(selected) else SheetAnchor.Hidden)
            scrimReferenceHeight = 0f
            if (!visible) presentationStarted = false
        } finally {
            if (presentationJob === job) {
                presentationJob = null
                presentationHeight = 0f
            }
        }
    }

    private suspend fun animate(
        target: SheetAnchor,
        velocity: Float = position.lastVelocity
    ) {
        val job = currentCoroutineContext().job
        animationJob = job
        try {
            var firstRun = true
            position.anchoredDrag(targetValue = target) { anchors, latestTarget ->
                val targetOffset = anchors.positionOf(latestTarget)
                if (!targetOffset.isNaN()) {
                    val initialVelocity = if (firstRun) velocity else position.lastVelocity
                    firstRun = false
                    coroutineScope {
                        if (latestTarget is SheetAnchor.Detent && scrimReferenceHeight > 0f) {
                            launch { restoreScrimReference() }
                        }
                        animate(
                            initialValue = position.requireOffset(),
                            targetValue = targetOffset,
                            initialVelocity = initialVelocity,
                            animationSpec = position.animationSpec
                        ) { value, currentVelocity ->
                            dragTo(
                                newOffset = value.coerceIn(
                                    anchors.minPosition(),
                                    anchors.maxPosition()
                                ),
                                lastKnownVelocity = currentVelocity
                            )
                        }
                    }
                }
            }
        } finally {
            if (animationJob === job) animationJob = null
        }
    }

    private suspend fun restoreScrimReference() {
        val minimumHeight = points.firstOrNull()?.second ?: return
        animate(
            initialValue = scrimReferenceHeight,
            targetValue = minimumHeight,
            animationSpec = position.animationSpec
        ) { value, _ ->
            scrimReferenceHeight = value
        }
        scrimReferenceHeight = 0f
    }

    fun beginGesture() {
        if (!visibleRequested || !ready) return
        animationJob?.cancel()
        if (gestureOrigin == null) gestureOrigin = selected
    }

    fun drag(
        delta: Float,
        dismissible: Boolean
    ): Float {
        if (!visibleRequested || !ready) return 0f
        val lowest = points.first().second
        val maximumOffset = if (dismissible) viewportHeight else viewportHeight - lowest
        val old = position.requireOffset()
        val next = (old + delta).coerceIn(
            viewportHeight - points.last().second,
            maximumOffset.coerceAtLeast(old)
        )
        if (next == old) return 0f
        beginGesture()
        return position.dispatchRawDelta(next - old)
    }

    suspend fun settle(
        velocity: Float,
        dismissible: Boolean,
        onDismissRequest: () -> Unit
    ) {
        if (!visibleRequested || !ready) return
        val origin = gestureOrigin ?: selected
        gestureOrigin = null
        val lowest = points.first()
        val offset = position.requireOffset()
        val lowestOffset = viewportHeight - lowest.second
        val threshold = with(density) { 48.dp.toPx() }.coerceAtMost(lowest.second * 0.25f)
        val close = dismissible &&
            (
                offset - lowestOffset >= threshold ||
                    (offset > lowestOffset && velocity > with(density) { 1000.dp.toPx() })
            )
        if (close) {
            onDismissRequest()
            // A request is not permission to hide. A later visible=false interrupts this rollback.
            val restore = points.firstOrNull { it.first == origin }?.first ?: selected
            if (visibleRequested) animate(SheetAnchor.Detent(restore), velocity)
            return
        }
        val currentHeight = viewportHeight - offset
        val target = if (abs(velocity) >= with(density) { 600.dp.toPx() }) {
            if (velocity < 0f) {
                points.firstOrNull { it.second > currentHeight + 0.5f } ?: points.last()
            } else {
                points.lastOrNull { it.second < currentHeight - 0.5f } ?: points.first()
            }
        } else {
            points.minBy { abs(viewportHeight - it.second - offset) }
        }
        selected = target.first
        onDetentChanged(selected)
        animate(SheetAnchor.Detent(selected), velocity)
    }

    suspend fun changeHeight(direction: Int) {
        if (!visibleRequested || !ready) return
        val index = points.indexOfFirst { it.first == selected }.coerceAtLeast(0)
        selected = points[(index + direction).coerceIn(points.indices)].first
        onDetentChanged(selected)
        animate(SheetAnchor.Detent(selected))
    }
}

private fun Float?.orZero() = this ?: 0f

internal val SheetDetentSaver = Saver<SheetDetent, List<Any>>(
    save = {
        when (it) {
            SheetDetent.Content -> listOf("content")
            SheetDetent.Medium -> listOf("medium")
            SheetDetent.Large -> listOf("large")
            is SheetDetent.Height -> listOf("height", it.height.value)
            is SheetDetent.Fraction -> listOf("fraction", it.fraction)
        }
    },
    restore = {
        when (it.first()) {
            "content" -> SheetDetent.Content
            "medium" -> SheetDetent.Medium
            "large" -> SheetDetent.Large
            "height" -> SheetDetent.Height((it[1] as Float).dp)
            else -> SheetDetent.Fraction(it[1] as Float)
        }
    }
)
