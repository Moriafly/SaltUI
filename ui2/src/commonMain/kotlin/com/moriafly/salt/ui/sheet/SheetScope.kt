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

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Stable
import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * Content layout information for a Sheet.
 */
@UnstableSaltUiApi
@Stable
interface SheetScope : ColumnScope {
    /**
     * Trailing space to apply with [SheetContentLayout.EdgeToEdge] and an empty action slot.
     * Use it as a lazy container's contentPadding or inside a verticalScroll modifier so the final
     * item can scroll clear of the navigation bar. Insets and actions are handled by the Sheet in
     * other configurations, where this value is zero. Reading it never changes the layout policy.
     */
    val contentPadding: PaddingValues
}
