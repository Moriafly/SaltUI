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

package com.moriafly.salt.ui.platform.linux

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerKeyboardModifiers
import androidx.compose.ui.unit.IntOffset
import com.moriafly.salt.ui.platform.input.DesktopTouchScene
import com.moriafly.salt.ui.platform.input.TouchPointerState
import java.awt.Component
import java.awt.Dialog
import java.awt.Window
import kotlin.math.roundToInt

/**
 * EDT-confined adaptation from native contact samples to Compose and window gestures.
 * Native connections, registration and mouse routing belong to LinuxWindowInput.
 */
@OptIn(InternalComposeUiApi::class)
internal class LinuxTouchInput(
    private val window: Window,
    private val canvas: Component,
    private val target: DesktopTouchScene,
    private val gestures: LinuxWindowGestures
) {
    private val pointers = TouchPointerState()

    fun onTouch(sample: LinuxXInputSession.Touch): Boolean {
        if (!sample.directTouch) return false
        if (!canReceiveTouch()) {
            cancel()
            return false
        }
        val type = when (sample.phase) {
            LinuxXInputSession.TouchPhase.Down -> PointerEventType.Press
            LinuxXInputSession.TouchPhase.Move -> PointerEventType.Move
            LinuxXInputSession.TouchPhase.Up -> PointerEventType.Release
        }
        val position = Offset(sample.x.toFloat(), sample.y.toFloat())
        val transform = canvas.graphicsConfiguration.defaultTransform
        val touchEvent = LinuxTouchEvent(
            id = sample.id,
            type = type,
            screenPosition = IntOffset(
                (sample.rootX / transform.scaleX).roundToInt(),
                (sample.rootY / transform.scaleY).roundToInt()
            ),
            contentPosition = position
        )
        if (type == PointerEventType.Press) canvas.requestFocusInWindow()
        gestures.dispatchTouch(
            event = touchEvent,
            dispatchToCompose = {
                // XI coordinates include system scale; extraDisplayScale only affects density.
                pointers.update(
                    id = sample.id,
                    position = target.positionInScene(canvas, position),
                    eventType = type
                )?.let { snapshot ->
                    target.scene.sendPointerEvent(
                        eventType = type,
                        pointers = snapshot,
                        keyboardModifiers = PointerKeyboardModifiers(
                            isShiftPressed = sample.modifiers and 1 != 0,
                            isCtrlPressed = sample.modifiers and 4 != 0,
                            isAltPressed = sample.modifiers and 8 != 0,
                            isMetaPressed = sample.modifiers and 64 != 0
                        ),
                        timeMillis = sample.timeMillis,
                        nativeEvent = touchEvent
                    )
                }
            },
            cancelCompose = ::cancelCompose
        )
        return true
    }

    fun cancel() {
        gestures.resetTouchInput()
        cancelCompose()
    }

    private fun cancelCompose() {
        if (pointers.clear() && !target.isDisposed) target.scene.cancelPointerInput()
    }

    private fun canReceiveTouch(): Boolean = canvas.isShowing && window.isEnabled &&
        !target.isDisposed && Window.getWindows().none { candidate ->
            candidate is Dialog && candidate.isShowing && candidate.isModal &&
                generateSequence(window) { it.owner }.none { it === candidate } &&
                when (candidate.modalityType) {
                    Dialog.ModalityType.DOCUMENT_MODAL ->
                        generateSequence(window) { it.owner }.last() ===
                            generateSequence(candidate as Window) { it.owner }.last()

                    Dialog.ModalityType.APPLICATION_MODAL ->
                        window.modalExclusionType == Dialog.ModalExclusionType.NO_EXCLUDE

                    Dialog.ModalityType.TOOLKIT_MODAL ->
                        window.modalExclusionType != Dialog.ModalExclusionType.TOOLKIT_EXCLUDE

                    else -> false
                }
        }
}
