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
import com.moriafly.salt.ui.platform.windows.WinUserConst.WM_MOUSELEAVE
import com.moriafly.salt.ui.platform.windows.WinUserConst.WM_MOUSEMOVE
import com.moriafly.salt.ui.platform.windows.WinUserConst.WM_NCMOUSELEAVE
import com.moriafly.salt.ui.platform.windows.WinUserConst.WM_NCMOUSEMOVE
import com.moriafly.salt.ui.platform.windows.structure.POINTER_INFO
import com.moriafly.salt.ui.util.findSkiaLayer
import com.moriafly.salt.ui.util.hwnd
import com.moriafly.salt.ui.window.internal.SaltWindowExceptionHandler
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinDef.LPARAM
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
    private val acceptedPointers = mutableMapOf<Int, WindowsTouchEvent>()
    private val pointerState = WindowsTouchPointerState()

    private var captionPointerId: Int? = null

    val isCaptionDragInProgress: Boolean
        get() = captionPointerId != null

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

    fun handleMessage(
        message: Int,
        wParam: WPARAM,
        lParam: LPARAM
    ): Boolean {
        if (disposed) return false
        val id = wParam.toInt() and 0xFFFF
        if (id == captionPointerId && (
                message == WM_POINTERUPDATE || message == WM_POINTERUP ||
                    message == WM_NCPOINTERUPDATE || message == WM_NCPOINTERUP
            )
        ) {
            val released = message == WM_POINTERUP || message == WM_NCPOINTERUP
            sendCaptionPointer(
                message = if (released) WM_NCPOINTERUP else WM_NCPOINTERUPDATE,
                id = id,
                position = lParam
            )
            if (released) captionPointerId = null
            return true
        }
        return when (message) {
            WM_POINTERDOWN, WM_POINTERUPDATE, WM_POINTERUP -> onPointer(message, id)
            WM_NCPOINTERDOWN -> {
                val hit = wParam.toInt() ushr 16
                if (hit == HitTestResult.HTCAPTION.value ||
                    hit == HitTestResult.HTMINBUTTON.value ||
                    hit == HitTestResult.HTMAXBUTTON.value ||
                    hit == HitTestResult.HTCLOSE.value
                ) {
                    onPointer(WM_POINTERDOWN, id)
                } else {
                    false
                }
            }

            WM_NCPOINTERUPDATE -> onPointer(WM_POINTERUPDATE, id)
            WM_NCPOINTERUP -> onPointer(WM_POINTERUP, id)
            WM_POINTERCAPTURECHANGED, WM_POINTERLEAVE -> {
                if (captionPointerId == id) captionPointerId = null
                if (acceptedPointers.remove(id) != null) {
                    dispatch { cancelOnEdt() }
                    true
                } else {
                    false
                }
            }
            // Moving a window generates mouse hover events even while the mouse is stationary.
            // The AWT single-pointer path makes Compose synthesize releases for active touches.
            WM_MOUSEMOVE, WM_NCMOUSEMOVE, WM_MOUSELEAVE, WM_NCMOUSELEAVE ->
                acceptedPointers.isNotEmpty()

            WM_CANCELMODE -> {
                captionPointerId = null
                dispatch { cancelOnEdt() }
                false
            }

            WM_NCDESTROY -> {
                disposed = true
                captionPointerId = null
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
        val nativeEvent = acceptedPointers[id]
        val accepted = nativeEvent != null
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

        if (message == WM_POINTERUPDATE && nativeEvent?.isCaptionDragRequested == true &&
            info.pointerFlags and POINTER_FLAG_PRIMARY != 0 &&
            window.isEnabled && window.findSkiaLayer()?.fullscreen != true
        ) {
            acceptedPointers.remove(id)
            dispatch { cancelOnEdt() }
            // DefWindowProc must run inside a real pointer message on the native window thread.
            // Posting WM_NCPOINTERDOWN later loses its input context and cannot start touch moving.
            captionPointerId = id
            sendCaptionPointer(
                message = WM_NCPOINTERDOWN,
                id = id,
                position = LPARAM(
                    (
                        (info.ptPixelLocation.y shl 16) or
                            (info.ptPixelLocation.x and 0xFFFF)
                    ).toLong()
                )
            )
            return true
        }

        // ScreenToClient uses physical pixels here, including across mixed-DPI monitors.
        val point = info.ptPixelLocation
        if (!User32Ex.INSTANCE.ScreenToClient(hwnd, point)) {
            if (accepted) {
                if (message == WM_POINTERUP) acceptedPointers.remove(id)
                dispatch { cancelOnEdt() }
            }
            return accepted
        }

        val touchEvent = nativeEvent ?: WindowsTouchEvent()
        if (message == WM_POINTERDOWN) acceptedPointers[id] = touchEvent
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
                    id,
                    target.positionInScene(canvas, position),
                    eventType
                )
                if (pointers != null) {
                    if (eventType == PointerEventType.Press) canvas.requestFocusInWindow()
                    target.scene.sendPointerEvent(
                        eventType = eventType,
                        pointers = pointers,
                        keyboardModifiers = modifiers,
                        timeMillis = timeMillis,
                        nativeEvent = touchEvent
                    )
                }
            }
        }
        return true
    }

    private fun sendCaptionPointer(
        message: Int,
        id: Int,
        position: LPARAM
    ) {
        User32Ex.INSTANCE.DefWindowProc(
            window.hwnd,
            message,
            WPARAM((id or (HitTestResult.HTCAPTION.value shl 16)).toLong()),
            position
        )
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

        private const val WM_NCPOINTERUPDATE = 0x0241
        private const val WM_NCPOINTERDOWN = 0x0242
        private const val WM_NCPOINTERUP = 0x0243
        private const val WM_POINTERUPDATE = 0x0245
        private const val WM_POINTERDOWN = 0x0246
        private const val WM_POINTERUP = 0x0247
        private const val WM_POINTERLEAVE = 0x024A
        private const val WM_POINTERCAPTURECHANGED = 0x024C
        private const val WM_CANCELMODE = 0x001F
        private const val WM_NCDESTROY = 0x0082
        private const val PT_TOUCH = 2
        private const val POINTER_FLAG_INCONTACT = 0x00000004
        private const val POINTER_FLAG_PRIMARY = 0x00002000
        private const val POINTER_FLAG_CANCELED = 0x00008000
    }
}
