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

package com.moriafly.salt.ui.platform.linux

import com.sun.jna.Native
import com.sun.jna.NativeLong
import com.sun.jna.platform.unix.X11
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import java.awt.MouseInfo
import java.awt.Window

/**
 * Native X11 window mover using `_NET_WM_MOVERESIZE` protocol.
 *
 * Delegates window dragging to the window manager, providing smooth movement
 * and native features like window snapping.
 */
internal object LinuxX11WindowMover {
    private const val NET_WM_MOVERESIZE_MOVE = 8L

    /**
     * Try to initiate a native X11 window move.
     *
     * @return `true` if the native move was successfully initiated, `false` otherwise
     * (e.g., on Wayland or if X11 calls fail).
     */
    fun tryMove(window: Window): Boolean {
        if (!window.isDisplayable) return false

        val x11: X11Ext
        try {
            x11 = X11Ext.INSTANCE
        } catch (_: UnsatisfiedLinkError) {
            return false
        }

        val display = x11.XOpenDisplay(null) ?: return false

        try {
            val windowId = Native.getComponentID(window)
            if (windowId == 0L) return false
            val xWindow = X11.Window(windowId)

            val rootWindow = queryRootWindow(x11, display, xWindow) ?: return false
            val netWmMoveresize = x11.XInternAtom(display, "_NET_WM_MOVERESIZE", false)

            val pointerInfo = MouseInfo.getPointerInfo() ?: return false
            val mouseX = pointerInfo.location.x
            val mouseY = pointerInfo.location.y

            val event = buildMoveResizeEvent(
                display = display,
                xWindow = xWindow,
                atom = netWmMoveresize,
                mouseX = mouseX,
                mouseY = mouseY
            )

            // 释放 AWT 对鼠标的独占锁定，让窗口管理器接管
            x11.XUngrabPointer(display, NativeLong(0))

            val mask = NativeLong(
                (X11.SubstructureRedirectMask or X11.SubstructureNotifyMask).toLong()
            )
            x11.XSendEvent(display, rootWindow, 0, mask, event)
            x11.XFlush(display)

            return true
        } catch (_: Exception) {
            return false
        } finally {
            x11.XCloseDisplay(display)
        }
    }

    private fun queryRootWindow(
        x11: X11Ext,
        display: X11.Display,
        xWindow: X11.Window
    ): X11.Window? {
        val rootRet = X11.WindowByReference()
        val parentRet = X11.WindowByReference()
        val childrenRet = PointerByReference()
        val numChildren = IntByReference()

        x11.XQueryTree(display, xWindow, rootRet, parentRet, childrenRet, numChildren)

        if (childrenRet.value != null) {
            x11.XFree(childrenRet.value)
        }

        return rootRet.value
    }

    private fun buildMoveResizeEvent(
        display: X11.Display,
        xWindow: X11.Window,
        atom: X11.Atom,
        mouseX: Int,
        mouseY: Int
    ): X11.XEvent {
        val event = X11.XEvent()
        event.type = X11.ClientMessage
        event.setType(X11.XClientMessageEvent::class.java)

        val clientMsg = event.xclient
        clientMsg.type = X11.ClientMessage
        clientMsg.serial = NativeLong(0)
        clientMsg.send_event = 1
        clientMsg.display = display
        clientMsg.window = xWindow
        clientMsg.message_type = atom
        clientMsg.format = 32

        clientMsg.data.setType("l")
        clientMsg.data.l[0] = NativeLong(mouseX.toLong()) // x_root
        clientMsg.data.l[1] = NativeLong(mouseY.toLong()) // y_root
        clientMsg.data.l[2] = NativeLong(NET_WM_MOVERESIZE_MOVE) // direction
        clientMsg.data.l[3] = NativeLong(1) // button (左键)
        clientMsg.data.l[4] = NativeLong(1) // source indication (普通应用)

        return event
    }
}

/**
 * JNA's default X11 interface is missing some methods.
 * We extend it here to bind the missing `XUngrabPointer` function.
 */
internal interface X11Ext : X11 {
    @Suppress("ktlint:standard:function-naming")
    fun XUngrabPointer(display: X11.Display?, time: NativeLong?): Int

    companion object {
        val INSTANCE: X11Ext = Native.load("X11", X11Ext::class.java)
    }
}
