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

package com.moriafly.salt.ui.platform.linux.input

import androidx.compose.ui.geometry.Offset
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.util.findSkiaLayer
import java.awt.AWTEvent
import java.awt.Component
import java.awt.EventQueue
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.MouseEvent
import java.awt.event.WindowEvent
import javax.swing.SwingUtilities

/**
 * Routes AWT and native mouse input through window handlers before Compose sees it.
 * Consuming a MouseEvent in an AWT listener alone does not stop other listeners receiving it.
 */
internal object LinuxWindowMouseInput {
    interface Handler {
        fun onMouseEvent(event: MouseEvent): Boolean

        fun afterMouseEvent(event: MouseEvent) {}

        fun cancel() {}
    }

    private data class Registration(
        val handler: Handler,
        val priority: Int
    )

    private val handlers = mutableMapOf<Window, MutableList<Registration>>()
    private val sources = mutableMapOf<Window, LinuxMouseInput>()
    private var queue: WindowEventQueue? = null

    fun register(
        window: Window,
        handler: Handler,
        priority: Int = 0
    ): () -> Unit {
        check(EventQueue.isDispatchThread())
        if (queue == null) {
            queue = WindowEventQueue().also {
                Toolkit.getDefaultToolkit().systemEventQueue.push(it)
            }
        }
        val registration = Registration(handler, priority)
        handlers.getOrPut(window) { mutableListOf() }.apply {
            add(registration)
            sortByDescending { it.priority }
        }
        return {
            check(EventQueue.isDispatchThread())
            handler.cancel()
            handlers[window]?.let {
                it.remove(registration)
                if (it.isEmpty()) handlers.remove(window)
            }
            if (handlers.isEmpty() && sources.isEmpty()) queue?.uninstallIfCurrent()
        }
    }

    fun registerSource(window: Window, source: LinuxMouseInput): () -> Unit {
        check(EventQueue.isDispatchThread())
        check(window in handlers && window !in sources)
        sources[window] = source
        return {
            check(EventQueue.isDispatchThread())
            source.cancel()
            sources.remove(window)
            if (handlers.isEmpty() && sources.isEmpty()) queue?.uninstallIfCurrent()
        }
    }

    fun dispatchToWindow(window: Window, event: MouseEvent) {
        check(EventQueue.isDispatchThread())
        // Input has already passed source selection. Deliver directly to this window's handlers
        // and AWT component, without queue re-entry or a second source selection pass.
        checkNotNull(queue).deliverToWindow(window, event)
    }

    @OptIn(UnstableSaltUiApi::class)
    fun positionInContent(event: MouseEvent, window: Window): Offset {
        // Compose coordinates start at its canvas, which can be inset by AWT decoration.
        val canvas = window.findSkiaLayer()?.canvas ?: window
        val point = SwingUtilities.convertPoint(event.component, event.point, canvas)
        val transform = window.graphicsConfiguration.defaultTransform
        return Offset(
            (point.x * transform.scaleX).toFloat(),
            (point.y * transform.scaleY).toFloat()
        )
    }

    fun positionInWindow(event: MouseEvent, window: Window): Offset {
        val transform = window.graphicsConfiguration.defaultTransform
        return Offset(
            ((event.xOnScreen - window.x) * transform.scaleX).toFloat(),
            ((event.yOnScreen - window.y) * transform.scaleY).toFloat()
        )
    }

    private class WindowEventQueue : EventQueue() {
        public override fun dispatchEvent(event: AWTEvent) {
            if (event is WindowEvent &&
                (
                    event.id == WindowEvent.WINDOW_DEACTIVATED ||
                        event.id == WindowEvent.WINDOW_CLOSED
                )
            ) {
                handlers[event.window]?.toList()?.forEach { it.handler.cancel() }
                sources[event.window]?.cancel()
            }
            val mouseEvent = event as? MouseEvent
            val component = mouseEvent?.source as? Component
            val window = component as? Window ?: component?.let(SwingUtilities::getWindowAncestor)
            if (mouseEvent == null || window == null) {
                super.dispatchEvent(event)
                return
            }
            val source = sources[window]
            if (source == null) {
                deliverToWindow(window, mouseEvent)
            } else {
                source.dispatchAwt(mouseEvent) { deliverToWindow(window, it) }
            }
        }

        fun deliverToWindow(window: Window, mouseEvent: MouseEvent) {
            val registrations = handlers[window]?.toList().orEmpty()
            if (!window.isShowing || !window.isEnabled) {
                registrations.forEach { it.handler.cancel() }
                sources[window]?.cancel()
                super.dispatchEvent(mouseEvent)
                return
            }
            if (registrations.any { it.handler.onMouseEvent(mouseEvent) }) {
                mouseEvent.consume()
            } else {
                super.dispatchEvent(mouseEvent)
            }
            registrations.forEach { it.handler.afterMouseEvent(mouseEvent) }
        }

        fun uninstallIfCurrent() {
            // Do not pop an event queue installed above ours by another library.
            if (Toolkit.getDefaultToolkit().systemEventQueue === this) {
                pop()
                queue = null
            }
        }
    }
}
