/*
 * Copyright 2020 The Android Open Source Project
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
 *
 * Source: https://repo.maven.apache.org/maven2/org/jetbrains/compose/material/material-ripple/1.12.2/material-ripple-1.12.2-sources.jar
 * Modified for SaltUI: keep the modifier-node path and use the Material 3 1.4.0 state opacities
 * and SaltUI's content color.
 */

package com.moriafly.salt.ui.indication.ripple

import androidx.collection.mutableObjectListOf
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.requireDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.toSize
import com.moriafly.salt.ui.LocalContentColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal const val RipplePressedAlpha = 0.10f

/**
 * Creates the platform-specific [RippleNode] implementation.
 */
internal expect fun createPlatformRippleNode(
    interactionSource: InteractionSource,
    bounded: Boolean,
    radius: Dp,
    color: Color
): DelegatableNode

/**
 * Abstract [Modifier.Node] that provides common functionality used by ripple node implementations.
 * This node handles state overlays. Subclasses draw the animated press ripple.
 */
internal abstract class RippleNode(
    private val interactionSource: InteractionSource,
    protected val bounded: Boolean,
    private val radius: Dp,
    private val color: Color
) : Modifier.Node(),
    CompositionLocalConsumerModifierNode,
    DrawModifierNode,
    LayoutAwareModifierNode {
    final override val shouldAutoInvalidate: Boolean = false

    private var stateLayer: StateLayer? = null

    // The following are calculated inside onRemeasured(). These must be initialized before adding
    // a ripple.

    protected var targetRadius: Float = 0f
    // The size is needed for Android to update ripple bounds if the size changes
    protected var rippleSize: Size = Size.Zero
        private set

    protected val rippleColor: Color
        get() = color.takeOrElse { currentValueOf(LocalContentColor) }

    // Track interactions that were emitted before we have been placed - we need to wait until we
    // have a valid size in order to set the radius and size correctly.
    private var hasValidSize = false
    private val pendingInteractions = mutableObjectListOf<PressInteraction>()

    override fun onRemeasured(size: IntSize) {
        hasValidSize = true
        val density = requireDensity()
        rippleSize = size.toSize()
        targetRadius = with(density) {
            if (radius.isUnspecified) {
                // Use the same default radius on all platforms, including Android.
                getRippleEndRadius(bounded, rippleSize)
            } else {
                radius.toPx()
            }
        }
        // Flush any pending interactions that were waiting for measurement
        pendingInteractions.forEach { handlePressInteraction(it) }
        pendingInteractions.clear()
    }

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction -> {
                        if (hasValidSize) {
                            handlePressInteraction(interaction)
                        } else {
                            // Handle these later when we have a valid size
                            pendingInteractions += interaction
                        }
                    }
                    else -> updateStateLayer(interaction, this)
                }
            }
        }
    }

    override fun onDetach() {
        stateLayer = null
        pendingInteractions.clear()
    }

    private fun handlePressInteraction(pressInteraction: PressInteraction) {
        when (pressInteraction) {
            is PressInteraction.Press -> addRipple(pressInteraction, rippleSize, targetRadius)
            is PressInteraction.Release -> removeRipple(pressInteraction.press)
            is PressInteraction.Cancel -> removeRipple(pressInteraction.press)
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        stateLayer?.run { drawStateLayer(targetRadius, rippleColor) }
        drawRipples()
    }

    abstract fun DrawScope.drawRipples()

    abstract fun addRipple(interaction: PressInteraction.Press, size: Size, targetRadius: Float)

    abstract fun removeRipple(interaction: PressInteraction.Press)

    private fun updateStateLayer(interaction: Interaction, scope: CoroutineScope) {
        val stateLayer =
            stateLayer
                ?: StateLayer(bounded).also { instance ->
                    // Invalidate when adding the state layer so we can start drawing it
                    invalidateDraw()
                    stateLayer = instance
                }
        stateLayer.handleInteraction(interaction, scope)
    }
}

/**
 * Represents the layer underneath the press ripple, that displays an overlay for states such as
 * [DragInteraction.Start].
 *
 * Typically, there should be both an 'incoming' and an 'outgoing' layer, so that when transitioning
 * between two states, the incoming of the new state, and the outgoing of the old state can be
 * displayed. However, because:
 *
 * a) the duration of these outgoing transitions are so short (mostly 15ms, which is less than 1
 * frame at 60fps), and hence are barely noticeable if they happen at the same time as an incoming
 * transition b) two layers cause a lot of extra work, and related performance concerns
 *
 * We skip managing two layers, and instead only show one layer. The details for the
 * [AnimationSpec]s used are as follows:
 *
 * No state -> a state = incoming transition for the new state A state -> a different state =
 * incoming transition for the new state A state -> no state = outgoing transition for the old state
 *
 * @see incomingStateLayerAnimationSpecFor
 * @see outgoingStateLayerAnimationSpecFor
 */
private class StateLayer(
    private val bounded: Boolean
) {
    private val animatedAlpha = Animatable(0f)

    private val interactions: MutableList<Interaction> = mutableListOf()
    private var currentInteraction: Interaction? = null

    internal fun handleInteraction(interaction: Interaction, scope: CoroutineScope) {
        when (interaction) {
            is HoverInteraction.Enter -> {
                interactions.add(interaction)
            }
            is HoverInteraction.Exit -> {
                interactions.remove(interaction.enter)
            }
            is FocusInteraction.Focus -> {
                interactions.add(interaction)
            }
            is FocusInteraction.Unfocus -> {
                interactions.remove(interaction.focus)
            }
            is DragInteraction.Start -> {
                interactions.add(interaction)
            }
            is DragInteraction.Stop -> {
                interactions.remove(interaction.start)
            }
            is DragInteraction.Cancel -> {
                interactions.remove(interaction.start)
            }
            else -> return
        }

        // The most recent interaction is the one we want to show
        val newInteraction = interactions.lastOrNull()

        if (currentInteraction != newInteraction) {
            if (newInteraction != null) {
                val targetAlpha =
                    when (newInteraction) {
                        is HoverInteraction.Enter -> HoveredAlpha
                        is FocusInteraction.Focus -> FocusedAlpha
                        is DragInteraction.Start -> DraggedAlpha
                        else -> 0f
                    }
                val incomingAnimationSpec = incomingStateLayerAnimationSpecFor(newInteraction)

                scope.launch { animatedAlpha.animateTo(targetAlpha, incomingAnimationSpec) }
            } else {
                val outgoingAnimationSpec = outgoingStateLayerAnimationSpecFor(currentInteraction)

                scope.launch { animatedAlpha.animateTo(0f, outgoingAnimationSpec) }
            }
            currentInteraction = newInteraction
        }
    }

    fun DrawScope.drawStateLayer(radius: Float, color: Color) {
        val alpha = animatedAlpha.value

        if (alpha > 0f) {
            val modulatedColor = color.copy(alpha = alpha)

            if (bounded) {
                clipRect { drawCircle(modulatedColor, radius) }
            } else {
                drawCircle(modulatedColor, radius)
            }
        }
    }

    private companion object {
        const val HoveredAlpha = 0.08f
        const val FocusedAlpha = 0.10f
        const val DraggedAlpha = 0.16f
    }
}

/**
 * @return the [AnimationSpec] used when transitioning to [interaction], either from a previous
 *   state, or no state.
 */
private fun incomingStateLayerAnimationSpecFor(interaction: Interaction): AnimationSpec<Float> {
    return when (interaction) {
        is HoverInteraction.Enter -> DefaultTweenSpec
        is FocusInteraction.Focus -> TweenSpec(durationMillis = 45, easing = LinearEasing)
        is DragInteraction.Start -> TweenSpec(durationMillis = 45, easing = LinearEasing)
        else -> DefaultTweenSpec
    }
}

/** @return the [AnimationSpec] used when transitioning away from [interaction], to no state. */
private fun outgoingStateLayerAnimationSpecFor(interaction: Interaction?): AnimationSpec<Float> {
    return when (interaction) {
        is HoverInteraction.Enter -> DefaultTweenSpec
        is FocusInteraction.Focus -> DefaultTweenSpec
        is DragInteraction.Start -> TweenSpec(durationMillis = 150, easing = LinearEasing)
        else -> DefaultTweenSpec
    }
}

/** Default / fallback [AnimationSpec]. */
private val DefaultTweenSpec = TweenSpec<Float>(durationMillis = 15, easing = LinearEasing)
