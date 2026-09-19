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
import androidx.compose.ui.input.pointer.PointerKeyboardModifiers
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.platform.windows.structure.POINTER_INFO
import com.moriafly.salt.ui.window.internal.SaltWindowExceptionHandler
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinDef.WPARAM
import java.awt.Component
import java.awt.Window
import java.awt.event.HierarchyEvent
import java.awt.event.HierarchyListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.SwingUtilities

/**
 * Receives WM_POINTER on the existing Skia canvas window procedure, before DefWindowProc
 * promotes touch to mouse input. Native data must be read on the window thread; Compose input
 * must be delivered on the EDT. No process-wide EnableMouseInPointer or extra subclass is needed.
 *
 * Based on the input pipeline in JetBrains/compose-multiplatform-core#3218.
 */
@OptIn(InternalComposeUiApi::class)
@UnstableSaltUiApi
internal class WindowsTouchInput private constructor(
    private val window: Window,
    private val canvas: Component,
    private val hwnd: HWND,
    private val target: WindowsTouchScene
) {
    // Accessed only by the native window thread. Keep consuming an accepted sequence even when
    // Compose cancels it, so a remaining finger cannot turn into a synthetic mouse mid-gesture.
    private val acceptedPointers = mutableSetOf<Int>()
    private val pointerState = WindowsTouchPointerState()

    @Volatile
    private var disposed = false

    private val windowListener = object : WindowAdapter() {
        override fun windowLostFocus(event: WindowEvent) = cancelOnEdt()
    }

    private val hierarchyListener = HierarchyListener { event ->
        if (event.changeFlags and HierarchyEvent.SHOWING_CHANGED.toLong() != 0L &&
            !canvas.isShowing
        ) {
            cancelOnEdt()
        }
    }

    init {
        window.addWindowFocusListener(windowListener)
        canvas.addHierarchyListener(hierarchyListener)
    }

    fun handleMessage(message: Int, wParam: WPARAM): Boolean {
        if (disposed) return false
        val id = wParam.toInt() and 0xFFFF
        return when (message) {
            WM_POINTERDOWN, WM_POINTERUPDATE, WM_POINTERUP -> onPointer(message, id)
            WM_POINTERCAPTURECHANGED, WM_POINTERLEAVE -> if (acceptedPointers.remove(id)) {
                dispatch { cancelOnEdt() }
                true
            } else {
                false
            }
            WM_CANCELMODE -> {
                dispatch { cancelOnEdt() }
                false
            }
            WM_NCDESTROY -> {
                disposed = true
                acceptedPointers.clear()
                SwingUtilities.invokeLater {
                    cancelOnEdt()
                    window.removeWindowFocusListener(windowListener)
                    canvas.removeHierarchyListener(hierarchyListener)
                }
                false
            }
            else -> false
        }
    }

    private fun onPointer(message: Int, id: Int): Boolean {
        val accepted = id in acceptedPointers
        if (!accepted && message != WM_POINTERDOWN) return false

        val info = POINTER_INFO()
        if (!User32Ex.INSTANCE.GetPointerInfo(id, info)) {
            // GetPointerInfo can fail after capture has changed. Never promote a partial sequence.
            if (accepted) {
                if (message == WM_POINTERUP) acceptedPointers.remove(id)
                dispatch { cancelOnEdt() }
            }
            return accepted
        }
        if (info.pointerType != PT_TOUCH) return false

        if (info.pointerFlags and POINTER_FLAG_CANCELED != 0 ||
            (message != WM_POINTERUP && info.pointerFlags and POINTER_FLAG_INCONTACT == 0)
        ) {
            if (accepted) {
                if (message == WM_POINTERUP) acceptedPointers.remove(id)
                dispatch { cancelOnEdt() }
            }
            return accepted
        }

        // Both coordinates are physical pixels on the native window thread. In particular, do
        // not multiply AWT locationOnScreen by density: that fails across mixed-DPI monitors.
        val point = info.ptPixelLocation
        if (!User32Ex.INSTANCE.ScreenToClient(hwnd, point)) {
            if (accepted) {
                if (message == WM_POINTERUP) acceptedPointers.remove(id)
                dispatch { cancelOnEdt() }
            }
            return accepted
        }

        if (message == WM_POINTERDOWN) acceptedPointers.add(id)
        if (message == WM_POINTERUP) acceptedPointers.remove(id)
        val position = Offset(point.x.toFloat(), point.y.toFloat())
        val eventType = when (message) {
            WM_POINTERDOWN -> PointerEventType.Press
            WM_POINTERUP -> PointerEventType.Release
            else -> PointerEventType.Move
        }
        val timeMillis = System.nanoTime() / 1_000_000
        val modifiers = PointerKeyboardModifiers(
            isShiftPressed = keyPressed(0x10),
            isCtrlPressed = keyPressed(0x11),
            isAltPressed = keyPressed(0x12),
            isMetaPressed = keyPressed(0x5B) || keyPressed(0x5C)
        )
        dispatch {
            if (!canvas.isShowing || !window.isEnabled || target.isDisposed) {
                cancelOnEdt()
            } else {
                val pointers = pointerState.update(
                    id, target.positionInScene(canvas, position), eventType
                )
                if (pointers != null) {
                    if (eventType == PointerEventType.Press) canvas.requestFocusInWindow()
                    target.scene.sendPointerEvent(
                        eventType = eventType,
                        pointers = pointers,
                        keyboardModifiers = modifiers,
                        timeMillis = timeMillis
                    )
                }
            }
        }
        return true
    }

    private fun keyPressed(key: Int): Boolean = User32Ex.INSTANCE.GetKeyState(key).toInt() < 0

    private fun dispatch(action: () -> Unit) {
        SwingUtilities.invokeLater {
            if (!disposed) {
                try {
                    action()
                } catch (exception: Throwable) {
                    SaltWindowExceptionHandler.onException(exception)
                }
            }
        }
    }

    private fun cancelOnEdt() {
        if (pointerState.clear() && !target.isDisposed) target.scene.cancelPointerInput()
    }

    companion object {
        fun create(window: Window, canvas: Component, hwnd: HWND): WindowsTouchInput? =
            WindowsTouchScene.create(window)?.let { WindowsTouchInput(window, canvas, hwnd, it) }

        private const val WM_POINTERUPDATE = 0x0245
        private const val WM_POINTERDOWN = 0x0246
        private const val WM_POINTERUP = 0x0247
        private const val WM_POINTERLEAVE = 0x024A
        private const val WM_POINTERCAPTURECHANGED = 0x024C
        private const val WM_CANCELMODE = 0x001F
        private const val WM_NCDESTROY = 0x0082
        private const val PT_TOUCH = 2
        private const val POINTER_FLAG_INCONTACT = 0x00000004
        private const val POINTER_FLAG_CANCELED = 0x00008000
    }
}