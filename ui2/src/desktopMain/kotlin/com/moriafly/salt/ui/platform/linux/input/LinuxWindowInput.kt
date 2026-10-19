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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.platform.LocalViewConfiguration
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.platform.input.DesktopTouchScene
import com.moriafly.salt.ui.platform.linux.window.LinuxWindowFocus
import com.moriafly.salt.ui.platform.linux.x11.LinuxXInputSession
import com.moriafly.salt.ui.util.findSkiaLayer
import com.sun.jna.Native
import java.awt.Component
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.HierarchyEvent
import java.awt.event.HierarchyListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.lang.reflect.Method

@Composable
internal fun LinuxWindowInputEffect(
    window: Window,
    gestures: LinuxWindowGestures,
    moveable: Boolean,
    resizable: Boolean,
    canDrag: Boolean = true,
    canMaximize: Boolean = false
) {
    val viewConfiguration = LocalViewConfiguration.current
    SideEffect {
        gestures.configure(
            moveable = moveable,
            resizable = resizable,
            canDrag = canDrag,
            canMaximize = canMaximize,
            viewConfiguration = viewConfiguration
        )
    }
    DisposableEffect(window, gestures) {
        val uninstallGestures = gestures.install()
        val focus = LinuxWindowFocus.install(window)
        val input = LinuxWindowInput.create(
            window = window,
            gestures = gestures,
            focus = focus
        )
        onDispose {
            input?.close()
            focus?.close()
            uninstallGestures()
        }
    }
}

/**
 * Attaches the native session to the canvas lifetime and connects its independent input channels.
 * Both channels pass AWT's window eligibility before reaching gestures or Compose.
 * A stopping session retains its slot until the reader confirms that native resources are closed.
 */
@OptIn(InternalComposeUiApi::class)
internal class LinuxWindowInput private constructor(
    private val window: Window,
    private val canvas: Component,
    private val isModalBlocked: Method,
    private val gestures: LinuxWindowGestures,
    private val target: DesktopTouchScene?,
    private val touch: LinuxTouchInput?,
    private val mouse: LinuxMouseInput
) : AutoCloseable,
    LinuxXInputSession.Listener {
    private var session: LinuxXInputSession? = null
    private var restartAfterClose = false
    private var closed = false

    private val windowListener = object : WindowAdapter() {
        override fun windowLostFocus(
            event: WindowEvent
        ) = cancelInput()

        override fun windowIconified(
            event: WindowEvent
        ) = cancelInput()
    }

    private val hierarchyListener = HierarchyListener { event ->
        if (event.changeFlags and (
                HierarchyEvent.SHOWING_CHANGED or HierarchyEvent.DISPLAYABILITY_CHANGED
            ).toLong() != 0L
        ) {
            if (canvas.isShowing) attach() else detach()
        }
    }

    init {
        window.addWindowFocusListener(windowListener)
        window.addWindowListener(windowListener)
        canvas.addHierarchyListener(hierarchyListener)
        attach()
    }

    private fun attach() {
        if (closed || !canvas.isShowing) return
        val existing = session
        if (existing != null) {
            restartAfterClose = existing.state == LinuxXInputSession.State.Stopping
            return
        }
        try {
            val handle = Native.getComponentID(canvas)
            if (handle == 0L) {
                mouse.updateNativeCapabilities(
                    LinuxXInputSession.Capabilities(
                        touch = false,
                        hover = false
                    )
                )
                return
            }
            mouse.awaitNativeCapabilities()
            session = LinuxXInputSession(
                handle = handle,
                windowHandle = Native.getComponentID(window),
                requestTouch = touch != null,
                listener = this
            ).also { it.start() }
        } catch (exception: Exception) {
            mouse.updateNativeCapabilities(
                LinuxXInputSession.Capabilities(
                    touch = false,
                    hover = false
                )
            )
            logUnavailable(exception)
        } catch (error: LinkageError) {
            mouse.updateNativeCapabilities(
                LinuxXInputSession.Capabilities(
                    touch = false,
                    hover = false
                )
            )
            logUnavailable(error)
        }
    }

    private fun detach() {
        restartAfterClose = false
        cancelInput()
        session?.requestStop()
    }

    private fun cancelTouch() {
        session?.invalidateTouches()
        touch?.cancel()
    }

    private fun cancelInput() {
        gestures.cancel()
        cancelTouch()
        mouse.cancel()
        // Window lifetime cancels every Compose pointer source, including AWT compatibility input.
        if (target != null && !target.isDisposed) target.scene.cancelPointerInput()
    }

    override fun onReady(
        capabilities: LinuxXInputSession.Capabilities
    ) {
        mouse.updateNativeCapabilities(capabilities)
    }

    override fun onTouch(
        event: LinuxXInputSession.Touch,
        canMoveNatively: Boolean
    ): LinuxXInputSession.TouchAction {
        if (!acceptNativeInput()) return LinuxXInputSession.TouchAction.Reject
        return when (touch?.dispatch(event, canMoveNatively)) {
            null -> LinuxXInputSession.TouchAction.Reject
            LinuxWindowGestures.TouchResult.Handled -> LinuxXInputSession.TouchAction.Accept
            LinuxWindowGestures.TouchResult.PendingNativeMove -> LinuxXInputSession.TouchAction.Pending
            LinuxWindowGestures.TouchResult.PreparedNativeMove -> LinuxXInputSession.TouchAction.Move
        }
    }

    override fun onTouchMove(
        id: Long
    ) {
        // Ownership was rejected even if the WM request fails. Suppress its AWT replay.
        mouse.suppressTouchEmulation()
        if (acceptNativeInput()) gestures.commitNativeTouchMove(id)
    }

    override fun onMouseMoveEnd() {
        if (gestures.endNativeMouseMove()) mouse.onNativeMouseRelease()
    }

    override fun onHover(
        event: LinuxXInputSession.Hover
    ) {
        if (acceptNativeInput()) mouse.onHover(event)
    }

    private fun acceptNativeInput(): Boolean {
        // XI2 callbacks run on the EDT but bypass AWT's modal event filter. Read AWT's actual
        // blocking decision, including nested dialogs, inherited exclusions and AppContext.
        if (!closed && canvas.isShowing && window.isEnabled &&
            !(isModalBlocked.invoke(window) as Boolean)
        ) {
            return true
        }
        cancelInput()
        return false
    }

    override fun onDevicesChanged() {
        cancelTouch()
    }

    override fun onClosed() {
        // The display, grabs and registrations are gone before compatibility input is restored.
        session = null
        mouse.updateNativeCapabilities(
            LinuxXInputSession.Capabilities(
                touch = false,
                hover = false
            )
        )
        touch?.cancel()
        if (closed) {
            mouse.close()
        } else if (restartAfterClose) {
            restartAfterClose = false
            attach()
        }
    }

    override fun close() {
        if (closed) return
        closed = true
        restartAfterClose = false
        window.removeWindowFocusListener(windowListener)
        window.removeWindowListener(windowListener)
        canvas.removeHierarchyListener(hierarchyListener)
        cancelInput()
        val existing = session
        if (existing != null) existing.requestStop() else mouse.close()
    }

    companion object {
        private fun logUnavailable(
            exception: Throwable
        ) {
            System.getLogger(LinuxWindowInput::class.java.name).log(
                System.Logger.Level.WARNING,
                "Cannot attach native XI2 input; keeping AWT input",
                exception
            )
        }

        @OptIn(UnstableSaltUiApi::class)
        fun create(
            window: Window,
            gestures: LinuxWindowGestures,
            focus: LinuxWindowFocus?
        ): LinuxWindowInput? {
            // XIDs and libXi do not apply to JBR's native Wayland toolkit.
            if (Toolkit.getDefaultToolkit().javaClass.name != "sun.awt.X11.XToolkit") return null
            val canvas = window.findSkiaLayer()?.canvas ?: return null
            // Resolve access before selecting XI2 or grabbing touch. Without the existing
            // java.desktop/java.awt module open, retain AWT's complete, modality-aware path.
            val isModalBlocked = try {
                Window::class.java.getDeclaredMethod("isModalBlocked").apply {
                    check(trySetAccessible()) {
                        "Native XI2 input requires --add-opens=java.desktop/java.awt=ALL-UNNAMED"
                    }
                }
            } catch (exception: Exception) {
                logUnavailable(exception)
                return null
            }
            val target = DesktopTouchScene.create(window)
            val touch = target?.let {
                LinuxTouchInput(
                    canvas = canvas,
                    target = target,
                    gestures = gestures,
                    focus = { timestamp ->
                        canvas.requestFocusInWindow()
                        focus?.onPointerPress(canvas, timestamp)
                    }
                )
            }
            // Hover remains usable even when Compose's private scene adapter is unavailable.
            return LinuxWindowInput(
                window = window,
                canvas = canvas,
                isModalBlocked = isModalBlocked,
                gestures = gestures,
                target = target,
                touch = touch,
                mouse = LinuxMouseInput(window, canvas)
            )
        }
    }
}
