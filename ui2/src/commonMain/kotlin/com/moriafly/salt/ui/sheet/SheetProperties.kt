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

package com.moriafly.salt.ui.sheet

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * Heights include the header, body, action padding or the empty-action bottom inset.
 * The host adds the bottom safe-area or keyboard inset in [SheetDisplayMode.Bottom].
 */
@UnstableSaltUiApi
@Immutable
sealed interface SheetDetent {
    /**
     * Measures the content within the available viewport, including lazy content.
     */
    data object Content : SheetDetent

    data object Medium : SheetDetent

    data object Large : SheetDetent

    data class Height(
        val height: Dp
    ) : SheetDetent {
        init {
            require(height.value.isFinite() && height.value > 0f) {
                "Sheet height must be finite and positive"
            }
        }
    }

    data class Fraction(
        val fraction: Float
    ) : SheetDetent {
        init {
            require(fraction.isFinite() && fraction > 0f && fraction <= 1f) {
                "Sheet fraction must be in (0, 1]"
            }
        }
    }
}

/**
 * Determines whether a [Sheet] attaches to the bottom or floats within its owning window.
 *
 * [Auto] selects a mode from the owning window's width and height. [Bottom] and [Floating]
 * override that selection without changing the window's adaptive information.
 */
@UnstableSaltUiApi
enum class SheetDisplayMode {
    /**
     * Uses [Floating] when the owning window is at least 600 dp wide and 480 dp tall,
     * and [Bottom] otherwise. Adapts as the window is resized or rotated, using its dimensions
     * before safe-area and keyboard insets are applied.
     */
    Auto,

    /**
     * Attaches to the bottom edge and stays horizontally centered within the owning window.
     * Width is capped, so the Sheet can leave space on either side in a wide window.
     * Supports height detents, pointer resizing and drag dismissal when enabled.
     */
    Bottom,

    /**
     * Floats in the center of the available safe area, with limited width and height.
     * The selected detent caps the content height rather than providing a draggable stop.
     * Does not show a drag handle or support pointer resizing or drag dismissal.
     */
    Floating
}

/**
 * Coordinates vertical pointer drags between a scrollable body and the Sheet's height detents.
 *
 * Applies only in [SheetDisplayMode.Bottom] with [SheetProperties.dragEnabled] enabled.
 * In either mode, downward drags scroll the body back to its start before collapsing the Sheet
 * or requesting dismissal. Does not affect direct header or handle drags, or mouse-wheel scrolling.
 */
@UnstableSaltUiApi
enum class SheetScrollBehavior {
    /**
     * An upward drag expands the Sheet to its largest detent before scrolling the body.
     * Once the Sheet reaches that height, the remaining drag movement scrolls the body.
     */
    ExpandFirst,

    /**
     * The body scrolls first in either direction. The Sheet resizes only when the body leaves
     * unconsumed movement at a scroll boundary. The header and handle can still resize it directly.
     */
    ScrollFirst
}

/**
 * Determines who applies the bottom safe area when the action slot is empty.
 */
@UnstableSaltUiApi
enum class SheetContentLayout {
    /**
     * Keeps the body above the bottom safe area and adds the default trailing space.
     */
    Inset,

    /**
     * Extends the body to the bottom edge. Apply [SheetScope.contentPadding] inside scrolling content.
     * The Sheet continues to protect the viewport from the keyboard.
     */
    EdgeToEdge
}

/**
 * Declarative Sheet configuration. Use a new list to change [detents].
 * [initialDetent] seeds the height once; reopening the same Sheet preserves the user's height.
 * [dismissible] gates user requests, and never prevents programmatic `visible = false`.
 * The header close button is hidden when dismissal is disabled.
 * [dragEnabled] controls pointer resizing and drag dismissal in [SheetDisplayMode.Bottom].
 * The drag handle is shown automatically when resizing or drag dismissal is available.
 * Accessibility height actions remain available when pointer dragging is disabled.
 * [displayMode] defaults to [SheetDisplayMode.Auto] and follows the owning window's dimensions.
 * Floating heights are capped by the safe viewport, panel width and internal maximum.
 * [scrollBehavior] defaults to expanding the Sheet before scrolling its body upward.
 * [contentLayout] only affects an empty action slot; actions always retain their safe area.
 */
@UnstableSaltUiApi
data class SheetProperties(
    val displayMode: SheetDisplayMode = SheetDisplayMode.Auto,
    val dismissible: Boolean = true,
    val dragEnabled: Boolean = true,
    val detents: List<SheetDetent> = listOf(SheetDetent.Content),
    val initialDetent: SheetDetent = detents.first(),
    val scrollBehavior: SheetScrollBehavior = SheetScrollBehavior.ExpandFirst,
    val contentLayout: SheetContentLayout = SheetContentLayout.Inset
) {
    init {
        require(detents.isNotEmpty()) { "Sheet requires at least one detent" }
        require(initialDetent in detents) { "initialDetent must belong to detents" }
    }
}
