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

package com.moriafly.salt.ui.window

import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * Represents the edge of the window where the pointer is located for resizing.
 */
@UnstableSaltUiApi
enum class WindowResizeEdge {
    /**
     * No resize edge (pointer is not on any resize border).
     */
    None,

    /**
     * Left edge of the window.
     */
    Left,

    /**
     * Right edge of the window.
     */
    Right,

    /**
     * Top edge of the window.
     */
    Top,

    /**
     * Bottom edge of the window.
     */
    Bottom,

    /**
     * Top-left corner of the window.
     */
    TopLeft,

    /**
     * Top-right corner of the window.
     */
    TopRight,

    /**
     * Bottom-left corner of the window.
     */
    BottomLeft,

    /**
     * Bottom-right corner of the window.
     */
    BottomRight
}
