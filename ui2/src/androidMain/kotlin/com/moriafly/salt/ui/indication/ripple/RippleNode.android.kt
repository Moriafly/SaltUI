/*
 * Copyright 2021 The Android Open Source Project
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
 * Source: https://dl.google.com/dl/android/maven2/androidx/compose/material/material-ripple-android/1.12.1/material-ripple-android-1.12.1-sources.jar
 * Modified for SaltUI: keep the modifier-node implementation and use SaltUI's ripple configuration.
 */

package com.moriafly.salt.ui.indication.ripple

import android.graphics.drawable.RippleDrawable
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt

internal actual fun createPlatformRippleNode(
    interactionSource: InteractionSource,
    bounded: Boolean,
    radius: Dp,
    color: Color
): DelegatableNode = AndroidRippleNode(
    interactionSource = interactionSource,
    bounded = bounded,
    radius = radius,
    color = color
)

/**
 * Uses a [RippleHostView] to draw press animations through Android's [RippleDrawable].
 * The shared [RippleNode] draws the state layer in Compose.
 */
private class AndroidRippleNode(
    interactionSource: InteractionSource,
    bounded: Boolean,
    radius: Dp,
    color: Color
) : RippleNode(
    interactionSource = interactionSource,
    bounded = bounded,
    radius = radius,
    color = color
), RippleHostKey {
    /**
     * [RippleContainer] attached to the nearest [ViewGroup]. If it hasn't already been created by a
     * another ripple, we will create it and attach it to the hierarchy.
     */
    private var rippleContainer: RippleContainer? = null

    /**
     * Backing [RippleHostView] used to draw press ripples.
     */
    private var rippleHostView: RippleHostView? = null
        set(value) {
            field = value
            invalidateDraw()
        }

    override fun DrawScope.drawRipples() {
        drawIntoCanvas { canvas ->
            rippleHostView?.run {
                // We set these inside addRipple() already, but they may change during the ripple
                // animation, so update them here too.
                // Note that changes to color / alpha will not be reflected in any
                // currently drawn ripples if the ripples are being drawn on the RenderThread,
                // since only the software paint is updated, not the hardware paint used in
                // RippleForeground.

                // For radius:
                // - On R and below, updates will not take effect until the next ripple, so if the
                // size changes the only way to update the calculated radius is by using
                // RippleDrawable.RADIUS_AUTO to calculate the radius from the bounds automatically.
                // But in this case, if the bounds change, the animation will switch to the UI
                // thread instead of render thread, so this isn't clearly desired either.
                // b/183019123
                // - On S and above, when hotspot bounds change mid-ripple, the radius / bounds /
                // origin will be updated for the ongoing ripple, even for explicitly set radii.
                // Note that for this to work the radius _must_ be set before we update bounds, as
                // changing the radius on its own won't do anything.
                setRippleProperties(
                    size = rippleSize,
                    radius = targetRadius.roundToInt(),
                    color = rippleColor,
                    alpha = RipplePressedAlpha,
                )

                draw(canvas.nativeCanvas)
            }
        }
    }

    override fun addRipple(interaction: PressInteraction.Press, size: Size, targetRadius: Float) {
        rippleHostView =
            with(getOrCreateRippleContainer()) {
                getRippleHostView().apply {
                    addRipple(
                        interaction = interaction,
                        bounded = bounded,
                        size = size,
                        radius = targetRadius.roundToInt(),
                        color = rippleColor,
                        alpha = RipplePressedAlpha,
                        onInvalidateRipple = { invalidateDraw() },
                    )
                }
            }
    }

    override fun removeRipple(interaction: PressInteraction.Press) {
        rippleHostView?.removeRipple()
    }

    override fun onDetach() {
        super.onDetach()
        rippleContainer?.run { disposeRippleIfNeeded() }
        rippleContainer = null
    }

    override fun onResetRippleHostView() {
        rippleHostView = null
    }

    private fun getOrCreateRippleContainer(): RippleContainer {
        val existingContainer = rippleContainer
        if (existingContainer != null) {
            return existingContainer
        }
        val view = findNearestViewGroup(currentValueOf(LocalView))
        val container = createAndAttachRippleContainerIfNeeded(view)
        rippleContainer = container
        return container
    }
}

private fun createAndAttachRippleContainerIfNeeded(view: ViewGroup): RippleContainer {
    // Try to find existing RippleContainer in the view hierarchy
    for (index in 0 until view.childCount) {
        val child = view.getChildAt(index)
        if (child is RippleContainer) {
            return child
        }
    }

    // Create a new RippleContainer if needed and add to the hierarchy
    return RippleContainer(view.context).apply { view.addView(this) }
}

/**
 * Returns [initialView] if it is a [ViewGroup], otherwise the nearest parent [ViewGroup] that we
 * will add a [RippleContainer] to.
 *
 * In all normal scenarios this should just be [LocalView], but since [LocalView] is public API
 * theoretically its value can be overridden with a non-[ViewGroup], so we walk up the tree to be
 * safe.
 */
private fun findNearestViewGroup(initialView: View): ViewGroup {
    var view: View = initialView
    while (view !is ViewGroup) {
        val parent = view.parent
        // We should never get to a ViewParent that isn't a View, without finding a ViewGroup
        // first - throw an exception if we do.
        require(parent is View) {
            "Couldn't find a valid parent for $view. Are you overriding LocalView and " +
                "providing a View that is not attached to the view hierarchy?"
        }
        view = parent
    }
    return view
}
