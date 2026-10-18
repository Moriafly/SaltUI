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

import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.internal.SaltWindowExceptionHandler
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.NativeLong
import com.sun.jna.platform.unix.X11
import com.sun.jna.ptr.IntByReference
import javax.swing.SwingUtilities
import kotlin.concurrent.thread

/**
 * One XI2 connection. The reader exclusively owns its display, subscriptions, grabs and cookies.
 * Listeners receive copied samples on the EDT; no native structure escapes the reader.
 * requestStop invalidates queued deliveries immediately. onClosed runs only after XCloseDisplay,
 * so the owner can keep a stopping session alive without blocking the EDT or overlapping grabs.
 */
@OptIn(UnstableSaltUiApi::class)
internal class LinuxXInputSession(
    private val handle: Long,
    private val requestTouch: Boolean,
    private val listener: Listener
) {
    interface Listener {
        fun onReady(capabilities: Capabilities)

        fun onTouch(event: Touch): Boolean

        fun onHover(event: Hover)

        fun onDevicesChanged()

        fun onClosed()
    }

    data class Capabilities(
        val touch: Boolean,
        val hover: Boolean
    )

    enum class TouchPhase { Down, Move, Up }

    enum class HoverPhase { Move, Enter, Exit }

    enum class State { Starting, Active, Stopping, Closed }

    data class Touch(
        val id: Long,
        val phase: TouchPhase,
        val x: Double,
        val y: Double,
        val rootX: Double,
        val rootY: Double,
        val modifiers: Int,
        val timeMillis: Long,
        val x11Timestamp: Long,
        val directTouch: Boolean
    )

    data class Hover(
        val phase: HoverPhase,
        val x: Double,
        val y: Double,
        val rootX: Double,
        val rootY: Double,
        val modifiers: Int,
        val directTouch: Boolean,
        val emulated: Boolean,
        val crossingMode: Int = 0
    )

    // Lifecycle state is EDT-confined; stop and touch cancellation signals cross into the reader.
    var state = State.Starting
        private set

    @Volatile
    private var stopRequested = false

    @Volatile
    private var touchEpoch = 0L

    // The reader updates only a still-queued Move from the same input stream. The EDT takes
    // its latest sample before calling listeners, without holding the producer lock in Compose.
    private class HoverMove(
        val source: Int,
        var sample: Hover
    )

    private val hoverLock = Any()
    private var pendingHoverMove: HoverMove? = null

    fun start() {
        check(SwingUtilities.isEventDispatchThread() && state == State.Starting)
        thread(name = "SaltUI-XI2-$handle", isDaemon = true) {
            try {
                if (!stopRequested) readConnection()
            } catch (exception: Exception) {
                logUnavailable(exception)
            } catch (error: LinkageError) {
                logUnavailable(error)
            } finally {
                stopRequested = true
                SwingUtilities.invokeLater {
                    state = State.Closed
                    listener.onClosed()
                }
            }
        }
    }

    fun requestStop() {
        check(SwingUtilities.isEventDispatchThread())
        if (state == State.Closed) return
        state = State.Stopping
        stopRequested = true
        sealHoverMove()
    }

    fun invalidateTouches() {
        check(SwingUtilities.isEventDispatchThread())
        touchEpoch++
    }

    private fun readConnection() {
        val x11 = LinuxTouchX11.instance
        val xi = LinuxXInput2.instance
        val display = x11.XOpenDisplay(null) ?: return
        try {
            val opcode = IntByReference()
            if (!x11.XQueryExtension(
                    display,
                    "XInputExtension",
                    opcode,
                    IntByReference(),
                    IntByReference()
                )
            ) {
                return
            }
            val major = IntByReference(2)
            val minor = IntByReference(2)
            if (xi.XIQueryVersion(display, major, minor) != 0 || major.value < 2) return
            val xid = X11.Window(handle)
            // Both channels need an up-to-date device inventory. Neither depends on the other
            // channel's registration succeeding; XI 2.0 can still supply hover without touch.
            if (xi.XISelectEvents(
                    display,
                    x11.XDefaultRootWindow(display),
                    LinuxXInput2.EventMask(
                        LinuxXInput2.ALL_DEVICES,
                        LinuxXInput2.DEVICE_CHANGED,
                        LinuxXInput2.HIERARCHY_CHANGED
                    ),
                    1
                ) != 0
            ) {
                return
            }
            val supportsTouch = major.value > 2 || minor.value >= 2
            val touch = requestTouch && supportsTouch && grabTouches(display, xid)
            val hover = selectHover(display, xid)
            if (!touch && !hover) return
            x11.XFlush(display)
            val directDevices = queryDirectDevices(display)
            dispatchAndWait {
                if (!stopRequested) {
                    state = State.Active
                    listener.onReady(Capabilities(touch, hover))
                }
            }
            readEvents(
                display = display,
                xid = xid,
                opcode = opcode.value,
                capabilities = Capabilities(touch, hover),
                initialDirectDevices = directDevices
            )
        } finally {
            // This removes every registration and grab, including partial initialization.
            x11.XCloseDisplay(display)
        }
    }

    private fun grabTouches(display: X11.Display, xid: X11.Window): Boolean = try {
        val modifiers = LinuxXInput2.GrabModifiers()
        val result = LinuxXInput2.instance.XIGrabTouchBegin(
            display,
            LinuxXInput2.ALL_MASTER_DEVICES,
            xid,
            0,
            LinuxXInput2.EventMask(
                LinuxXInput2.ALL_MASTER_DEVICES,
                LinuxXInput2.TOUCH_BEGIN,
                LinuxXInput2.TOUCH_UPDATE,
                LinuxXInput2.TOUCH_END
            ),
            1,
            modifiers
        )
        result == 0 && modifiers.status == 0
    } catch (error: LinkageError) {
        // A client libXi without touch-grab support can still provide XI2 hover.
        System.getLogger(LinuxXInputSession::class.java.name).log(
            System.Logger.Level.WARNING,
            "XI2 touch grab unavailable",
            error
        )
        false
    }

    private fun selectHover(display: X11.Display, xid: X11.Window): Boolean =
        LinuxXInput2.instance.XISelectEvents(
            display,
            xid,
            LinuxXInput2.EventMask(
                LinuxXInput2.ALL_MASTER_DEVICES,
                LinuxXInput2.MOTION,
                LinuxXInput2.ENTER,
                LinuxXInput2.LEAVE
            ),
            1
        ) == 0

    private fun queryDirectDevices(display: X11.Display): Set<Int> {
        val xi = LinuxXInput2.instance
        val count = IntByReference()
        val data = xi.XIQueryDevice(display, LinuxXInput2.ALL_DEVICES, count)
            ?: error("Cannot query XI2 input devices")
        try {
            val size = LinuxXInput2.DeviceInfo(data).size()
            return (0 until count.value).mapNotNull { index ->
                val info = LinuxXInput2.DeviceInfo(data.share(index.toLong() * size))
                info.device.takeIf { info.isDirectTouch }
            }.toSet()
        } finally {
            xi.XIFreeDeviceInfo(data)
        }
    }

    private fun readEvents(
        display: X11.Display,
        xid: X11.Window,
        opcode: Int,
        capabilities: Capabilities,
        initialDirectDevices: Set<Int>
    ) {
        val x11 = LinuxTouchX11.instance
        val event = X11.XEvent()
        var directDevices = initialDirectDevices
        Memory(8).use { fd ->
            fd.clear()
            fd.setInt(0, x11.XConnectionNumber(display))
            fd.setShort(4, 1) // POLLIN
            while (!stopRequested) {
                if (x11.XPending(display) == 0) {
                    // Bounded stop latency, with no EDT join that could deadlock touch acceptance.
                    val result = LinuxTouchPoll.instance.poll(fd, NativeLong(1), 100)
                    if (result < 0) {
                        if (Native.getLastError() == 4) continue // EINTR
                        error("XI2 connection polling failed")
                    }
                    if (result > 0 && fd.getShort(6).toInt() and 0x38 != 0) {
                        error("XI2 connection closed while polling")
                    }
                    continue
                }
                x11.XNextEvent(display, event)
                if (event.type != 35) continue // GenericEvent
                val cookie = LinuxTouchX11.EventCookie(event.pointer)
                if (cookie.extension != opcode || x11.XGetEventData(display, cookie) == 0) continue
                try {
                    when (cookie.eventType) {
                        LinuxXInput2.DEVICE_CHANGED, LinuxXInput2.HIERARCHY_CHANGED -> {
                            directDevices = queryDirectDevices(display)
                            // Finish cancellation before a new press can be accepted.
                            dispatchAndWait {
                                if (!stopRequested) listener.onDevicesChanged()
                            }
                        }

                        LinuxXInput2.TOUCH_BEGIN,
                        LinuxXInput2.TOUCH_UPDATE,
                        LinuxXInput2.TOUCH_END -> if (capabilities.touch) {
                            val native = LinuxXInput2.DeviceEvent(requireNotNull(cookie.data))
                            onTouch(
                                display = display,
                                xid = xid,
                                native = native,
                                directTouch = native.source in directDevices
                            )
                        }

                        LinuxXInput2.MOTION -> if (capabilities.hover) {
                            val native = LinuxXInput2.DeviceEvent(requireNotNull(cookie.data))
                            val sample = Hover(
                                phase = HoverPhase.Move,
                                x = native.eventX,
                                y = native.eventY,
                                rootX = native.rootX,
                                rootY = native.rootY,
                                modifiers = native.modifiers.effective,
                                directTouch = native.source in directDevices,
                                emulated = native.flags and LinuxXInput2.POINTER_EMULATED != 0
                            )
                            dispatchHover(sample, native.source)
                        }

                        LinuxXInput2.ENTER, LinuxXInput2.LEAVE -> if (capabilities.hover) {
                            val native = LinuxXInput2.CrossingEvent(requireNotNull(cookie.data))
                            val sample = Hover(
                                phase = if (cookie.eventType == LinuxXInput2.ENTER) {
                                    HoverPhase.Enter
                                } else {
                                    HoverPhase.Exit
                                },
                                x = native.eventX,
                                y = native.eventY,
                                rootX = native.rootX,
                                rootY = native.rootY,
                                modifiers = native.modifiers.effective,
                                directTouch = native.source in directDevices,
                                emulated = false,
                                crossingMode = native.mode
                            )
                            dispatchHover(sample, native.source)
                        }
                    }
                } finally {
                    x11.XFreeEventData(display, cookie)
                }
            }
        }
    }

    private fun onTouch(
        display: X11.Display,
        xid: X11.Window,
        native: LinuxXInput2.DeviceEvent,
        directTouch: Boolean
    ) {
        val epoch = touchEpoch
        val sample = Touch(
            id = (native.source.toLong() shl 32) or (native.detail.toLong() and 0xffffffffL),
            phase = when (native.eventType) {
                LinuxXInput2.TOUCH_BEGIN -> TouchPhase.Down
                LinuxXInput2.TOUCH_END -> TouchPhase.Up
                else -> TouchPhase.Move
            },
            x = native.eventX,
            y = native.eventY,
            rootX = native.rootX,
            rootY = native.rootY,
            modifiers = native.modifiers.effective,
            timeMillis = System.nanoTime() / 1_000_000,
            x11Timestamp = native.time.toLong() and 0xffffffffL,
            directTouch = directTouch
        )
        if (sample.phase == TouchPhase.Down) {
            var accept = false
            dispatchAndWait {
                if (!stopRequested) {
                    // A stale direct-touch press is still claimed, so cancellation cannot
                    // promote it into an AWT click. It must not reach Compose or request focus.
                    accept = if (epoch == touchEpoch) {
                        listener.onTouch(sample)
                    } else {
                        sample.directTouch
                    }
                }
            }
            LinuxXInput2.instance.XIAllowTouchEvents(
                display,
                native.device,
                native.detail,
                xid,
                if (accept) LinuxXInput2.ACCEPT_TOUCH else LinuxXInput2.REJECT_TOUCH
            )
            LinuxTouchX11.instance.XFlush(display)
        } else {
            dispatch {
                if (epoch == touchEpoch) listener.onTouch(sample)
            }
        }
    }

    private fun dispatchHover(sample: Hover, source: Int) {
        if (sample.phase != HoverPhase.Move) {
            dispatch { listener.onHover(sample) }
            return
        }
        val delivery = synchronized(hoverLock) {
            val pending = pendingHoverMove
            val previous = pending?.sample
            val sameStream = pending?.source == source &&
                previous?.modifiers == sample.modifiers &&
                previous.directTouch == sample.directTouch &&
                previous.emulated == sample.emulated
            if (pending != null && sameStream) {
                pending.sample = sample
                null
            } else {
                HoverMove(source, sample).also { pendingHoverMove = it }
            }
        } ?: return
        post {
            val latest = synchronized(hoverLock) {
                if (pendingHoverMove === delivery) pendingHoverMove = null
                delivery.sample
            }
            listener.onHover(latest)
        }
    }

    private fun sealHoverMove() = synchronized(hoverLock) {
        // Keep the already-queued sample, but prevent later Move events crossing this boundary.
        pendingHoverMove = null
    }

    private fun dispatchAndWait(action: () -> Unit) {
        sealHoverMove()
        SwingUtilities.invokeAndWait { action() }
    }

    private fun dispatch(action: () -> Unit) {
        sealHoverMove()
        post(action)
    }

    private fun post(action: () -> Unit) {
        SwingUtilities.invokeLater {
            if (!stopRequested) {
                try {
                    action()
                } catch (exception: Throwable) {
                    SaltWindowExceptionHandler.onException(exception)
                }
            }
        }
    }

    private fun logUnavailable(exception: Throwable) {
        System.getLogger(LinuxXInputSession::class.java.name).log(
            System.Logger.Level.WARNING,
            "Native XI2 input stopped; restoring AWT input",
            exception
        )
    }
}
