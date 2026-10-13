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
 * The host adds the bottom safe-area or keyboard inset to Bottom presentation.
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

@UnstableSaltUiApi
enum class SheetPresentation { Adaptive, Bottom, Centered }

@UnstableSaltUiApi
enum class SheetContentInteraction { ResizeFirst, ScrollFirst }

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
 * [dragEnabled] controls pointer resizing and drag dismissal in Bottom presentation.
 * The drag handle is shown automatically when resizing or drag dismissal is available.
 * Accessibility height actions remain available when pointer dragging is disabled.
 * [presentation] defaults to Bottom in narrow portrait windows and Centered elsewhere.
 * Centered heights are capped by the safe viewport, the panel width and an internal maximum.
 * [contentLayout] only affects an empty action slot; actions always retain their safe area.
 */
@UnstableSaltUiApi
data class SheetProperties(
    val presentation: SheetPresentation = SheetPresentation.Adaptive,
    val dismissible: Boolean = true,
    val dragEnabled: Boolean = true,
    val detents: List<SheetDetent> = listOf(SheetDetent.Content),
    val initialDetent: SheetDetent = detents.first(),
    val contentInteraction: SheetContentInteraction = SheetContentInteraction.ResizeFirst,
    val contentLayout: SheetContentLayout = SheetContentLayout.Inset
) {
    init {
        require(detents.isNotEmpty()) { "Sheet requires at least one detent" }
        require(initialDetent in detents) { "initialDetent must belong to detents" }
    }
}
