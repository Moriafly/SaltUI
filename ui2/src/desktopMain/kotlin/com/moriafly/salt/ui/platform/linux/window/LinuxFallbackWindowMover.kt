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

package com.moriafly.salt.ui.platform.linux.window

import com.moriafly.salt.ui.UnstableSaltUiApi
import java.awt.Cursor
import java.awt.Point
import java.awt.Window

/**
 * Fallback window movement in AWT screen coordinates, shared by mouse and touch.
 *
 * From `androidx.compose.foundation.window.StandardMoveHandler`
 */
@UnstableSaltUiApi
internal class LinuxFallbackWindowMover(
    private val window: Window
) {
    private var dragStartScreenPos: Point? = null
    private var dragStartWindowPos: Point? = null
    private var isDragging = false

    fun startDrag(
        screenPosition: Point
    ) {
        dragStartScreenPos = Point(screenPosition)
        dragStartWindowPos = window.location
        isDragging = true
    }

    fun updateDrag(
        screenPosition: Point
    ) {
        if (!isDragging) return

        val startScreen = dragStartScreenPos ?: return
        val startWindow = dragStartWindowPos ?: return

        val newX = startWindow.x + (screenPosition.x - startScreen.x)
        val newY = startWindow.y + (screenPosition.y - startScreen.y)

        window.location = Point(newX, newY)
    }

    fun endDrag() {
        if (isDragging) {
            window.cursor = Cursor.getDefaultCursor()
        }
        isDragging = false
        dragStartScreenPos = null
        dragStartWindowPos = null
    }
}
