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

package com.moriafly.salt.ui.platform.windows

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.scene.ComposeScenePointer

/**
 * EDT-confined contact state. A release includes the lifted finger once, with pressed=false.
 * After cancellation, only a new press may introduce a contact again.
 */
@OptIn(InternalComposeUiApi::class)
internal class WindowsTouchPointerState {
    private val pointers = linkedMapOf<PointerId, ComposeScenePointer>()

    fun update(
        id: Int,
        position: Offset,
        eventType: PointerEventType
    ): List<ComposeScenePointer>? {
        val pointerId = PointerId(id.toLong())
        if (eventType != PointerEventType.Press && pointerId !in pointers) return null
        pointers[pointerId] = ComposeScenePointer(
            id = pointerId,
            position = position,
            pressed = eventType != PointerEventType.Release,
            type = PointerType.Touch
        )
        val snapshot = pointers.values.toList()
        if (eventType == PointerEventType.Release) pointers.remove(pointerId)
        return snapshot
    }

    fun clear(): Boolean {
        val hadPointers = pointers.isNotEmpty()
        pointers.clear()
        return hadPointers
    }
}