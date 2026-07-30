/*
 * Salt UI
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

@file:Suppress("unused")

package com.moriafly.salt.ui.indication

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import com.moriafly.salt.ui.UnstableSaltUiApi
import kotlinx.coroutines.launch

/**
 * An [IndicationNodeFactory] that reduces the content's own alpha when pressed,
 * hovered, or focused, rather than drawing an overlay on top.
 *
 * Unlike [com.moriafly.salt.ui.AlphaIndication] which paints a background rect,
 * this indication modifies the opacity of the underlying composable itself.
 */
@UnstableSaltUiApi
object AlphaContentIndication : IndicationNodeFactory {
    override fun create(
        interactionSource: InteractionSource
    ): DelegatableNode = AlphaContentIndicationInstance(interactionSource)

    override fun hashCode(): Int = -1

    override fun equals(other: Any?) = other === this

    private class AlphaContentIndicationInstance(
        private val interactionSource: InteractionSource
    ) : Modifier.Node(),
        DrawModifierNode {
        private var isPressed = false
        private var isHovered = false
        private var isFocused = false

        private val paint = Paint()

        override fun onAttach() {
            coroutineScope.launch {
                var pressCount = 0
                var hoverCount = 0
                var focusCount = 0
                interactionSource.interactions.collect { interaction ->
                    when (interaction) {
                        is PressInteraction.Press -> pressCount++
                        is PressInteraction.Release -> pressCount--
                        is PressInteraction.Cancel -> pressCount--
                        is HoverInteraction.Enter -> hoverCount++
                        is HoverInteraction.Exit -> hoverCount--
                        is FocusInteraction.Focus -> focusCount++
                        is FocusInteraction.Unfocus -> focusCount--
                    }
                    val pressed = pressCount > 0
                    val hovered = hoverCount > 0
                    val focused = focusCount > 0
                    var invalidateNeeded = false
                    if (isPressed != pressed) {
                        isPressed = pressed
                        invalidateNeeded = true
                    }
                    if (isHovered != hovered) {
                        isHovered = hovered
                        invalidateNeeded = true
                    }
                    if (isFocused != focused) {
                        isFocused = focused
                        invalidateNeeded = true
                    }
                    if (invalidateNeeded) invalidateDraw()
                }
            }
        }

        override fun ContentDrawScope.draw() {
            val alpha = when {
                isPressed -> PressedAlpha
                isHovered || isFocused -> HoveredAlpha
                else -> DefaultAlpha
            }

            if (alpha < 1f) {
                paint.alpha = alpha
                drawContext.canvas.saveLayer(
                    Rect(Offset.Zero, size),
                    paint
                )
                drawContent()
                drawContext.canvas.restore()
            } else {
                drawContent()
            }
        }

        @Suppress("ConstPropertyName")
        companion object {
            private const val PressedAlpha = 0.3f
            private const val HoveredAlpha = 0.6f
            private const val DefaultAlpha = 1f
        }
    }
}
