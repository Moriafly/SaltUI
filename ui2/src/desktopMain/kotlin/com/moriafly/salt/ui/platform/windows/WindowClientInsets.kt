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

package com.moriafly.salt.ui.platform.windows

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

internal class WindowClientInsets(
    val leftVal: Int,
    val topVal: Int,
    val rightVal: Int,
    val bottomVal: Int,
) : WindowInsets {
    override fun getLeft(density: Density, layoutDirection: LayoutDirection): Int = leftVal

    override fun getTop(density: Density): Int = topVal

    override fun getRight(density: Density, layoutDirection: LayoutDirection): Int = rightVal

    override fun getBottom(density: Density): Int = bottomVal

    override fun toString(): String = "Insets(left=$leftVal, top=$topVal, right=$rightVal, " +
        "bottom=$bottomVal)"

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is WindowClientInsets) {
            return false
        }

        return leftVal == other.leftVal &&
            topVal == other.topVal &&
            rightVal == other.rightVal &&
            bottomVal == other.bottomVal
    }

    override fun hashCode(): Int {
        var result = leftVal
        result = 31 * result + topVal
        result = 31 * result + rightVal
        result = 31 * result + bottomVal
        return result
    }
}
