/*
 * Salt UI
 * Copyright (C) 2025 Moriafly
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

package com.moriafly.salt.ui.gestures.snapping

import androidx.compose.foundation.gestures.Orientation
import com.moriafly.salt.ui.lazy.grid.LazyGridItemInfo
import com.moriafly.salt.ui.lazy.grid.LazyGridLayoutInfo

internal val LazyGridLayoutInfo.singleAxisViewportSize: Int
    get() =
        if (orientation == Orientation.Vertical) {
            viewportSize.height
        } else {
            viewportSize.width
        }

internal fun LazyGridItemInfo.sizeOnMainAxis(orientation: Orientation): Int =
    if (orientation == Orientation.Vertical) {
        size.height
    } else {
        size.width
    }

internal fun LazyGridItemInfo.offsetOnMainAxis(orientation: Orientation): Int =
    if (orientation == Orientation.Vertical) {
        offset.y
    } else {
        offset.x
    }
