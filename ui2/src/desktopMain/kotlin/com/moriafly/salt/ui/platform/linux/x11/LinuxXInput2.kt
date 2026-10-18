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

package com.moriafly.salt.ui.platform.linux.x11

import com.sun.jna.Library
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.NativeLong
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.platform.unix.X11
import com.sun.jna.ptr.IntByReference

/**
 * XI 2.2 touch bindings, matching XInput2.h. Only direct touch is accepted; touchpads
 * and pointer devices keep their existing AWT input path.
 */
@Suppress("FunctionName")
internal interface LinuxXInput2 : Library {
    fun XIQueryVersion(
        display: X11.Display,
        major: IntByReference,
        minor: IntByReference
    ): Int

    fun XIQueryDevice(
        display: X11.Display,
        device: Int,
        count: IntByReference
    ): Pointer?

    fun XIFreeDeviceInfo(
        info: Pointer
    )

    fun XISelectEvents(
        display: X11.Display,
        window: X11.Window,
        masks: EventMask,
        count: Int
    ): Int

    fun XIGrabTouchBegin(
        display: X11.Display,
        device: Int,
        window: X11.Window,
        ownerEvents: Int,
        mask: EventMask,
        count: Int,
        modifiers: GrabModifiers
    ): Int

    fun XIAllowTouchEvents(
        display: X11.Display,
        device: Int,
        touch: Int,
        window: X11.Window,
        mode: Int
    ): Int

    @Structure.FieldOrder("device", "length", "mask")
    class EventMask(
        device: Int,
        vararg events: Int
    ) : Structure() {
        @JvmField
        var device: Int = device

        @JvmField
        var length: Int = 4

        @JvmField
        var mask: Pointer = Memory(length.toLong()).apply {
            clear()
            events.forEach { event ->
                val offset = (event / 8).toLong()
                setByte(offset, (getByte(offset).toInt() or (1 shl (event % 8))).toByte())
            }
        }
    }

    @Structure.FieldOrder("modifiers", "status")
    class GrabModifiers : Structure() {
        @JvmField
        var modifiers: Int = Int.MIN_VALUE

        // XIAnyModifier
        @JvmField
        var status: Int = 0
    }

    @Structure.FieldOrder("device", "name", "use", "attachment", "enabled", "count", "classes")
    class DeviceInfo(
        pointer: Pointer
    ) : Structure(pointer) {
        @JvmField
        var device: Int = 0

        @JvmField
        var name: Pointer? = null

        @JvmField
        var use: Int = 0

        @JvmField
        var attachment: Int = 0

        @JvmField
        var enabled: Int = 0

        @JvmField
        var count: Int = 0

        @JvmField
        var classes: Pointer? = null

        init {
            read()
        }

        val isDirectTouch: Boolean
            get() = enabled != 0 && (0 until count).any { index ->
                val info = classes?.getPointer(index.toLong() * Native.POINTER_SIZE)
                // XIAnyClassInfo.type; XITouchClassInfo.mode
                info != null && info.getInt(0) == 8 && info.getInt(8) == 1
            }
    }

    @Structure.FieldOrder("length", "mask")
    class ButtonState : Structure() {
        @JvmField
        var length: Int = 0

        @JvmField
        var mask: Pointer? = null
    }

    @Structure.FieldOrder("length", "mask", "values")
    class ValuatorState : Structure() {
        @JvmField
        var length: Int = 0

        @JvmField
        var mask: Pointer? = null

        @JvmField
        var values: Pointer? = null
    }

    @Structure.FieldOrder("base", "latched", "locked", "effective")
    class ModifierState : Structure() {
        @JvmField
        var base: Int = 0

        @JvmField
        var latched: Int = 0

        @JvmField
        var locked: Int = 0

        @JvmField
        var effective: Int = 0
    }

    @Structure.FieldOrder(
        "type",
        "serial",
        "sendEvent",
        "display",
        "extension",
        "eventType",
        "time",
        "device",
        "source",
        "detail",
        "root",
        "event",
        "child",
        "rootX",
        "rootY",
        "eventX",
        "eventY",
        "flags",
        "buttons",
        "valuators",
        "modifiers",
        "group"
    )
    class DeviceEvent(
        pointer: Pointer
    ) : Structure(pointer) {
        @JvmField
        var type: Int = 0

        @JvmField
        var serial: NativeLong = NativeLong()

        @JvmField
        var sendEvent: Int = 0

        @JvmField
        var display: Pointer? = null

        @JvmField
        var extension: Int = 0

        @JvmField
        var eventType: Int = 0

        @JvmField
        var time: NativeLong = NativeLong()

        @JvmField
        var device: Int = 0

        @JvmField
        var source: Int = 0

        @JvmField
        var detail: Int = 0

        @JvmField
        var root: NativeLong = NativeLong()

        @JvmField
        var event: NativeLong = NativeLong()

        @JvmField
        var child: NativeLong = NativeLong()

        @JvmField
        var rootX: Double = 0.0

        @JvmField
        var rootY: Double = 0.0

        @JvmField
        var eventX: Double = 0.0

        @JvmField
        var eventY: Double = 0.0

        @JvmField
        var flags: Int = 0

        @JvmField
        var buttons: ButtonState = ButtonState()

        @JvmField
        var valuators: ValuatorState = ValuatorState()

        @JvmField
        var modifiers: ModifierState = ModifierState()

        @JvmField
        var group: ModifierState = ModifierState()

        init {
            read()
        }
    }

    @Structure.FieldOrder(
        "type",
        "serial",
        "sendEvent",
        "display",
        "extension",
        "eventType",
        "time",
        "device",
        "source",
        "detail",
        "root",
        "event",
        "child",
        "rootX",
        "rootY",
        "eventX",
        "eventY",
        "mode",
        "focus",
        "sameScreen",
        "buttons",
        "modifiers",
        "group"
    )
    class CrossingEvent(
        pointer: Pointer
    ) : Structure(pointer) {
        @JvmField
        var type: Int = 0

        @JvmField
        var serial: NativeLong = NativeLong()

        @JvmField
        var sendEvent: Int = 0

        @JvmField
        var display: Pointer? = null

        @JvmField
        var extension: Int = 0

        @JvmField
        var eventType: Int = 0

        @JvmField
        var time: NativeLong = NativeLong()

        @JvmField
        var device: Int = 0

        @JvmField
        var source: Int = 0

        @JvmField
        var detail: Int = 0

        @JvmField
        var root: NativeLong = NativeLong()

        @JvmField
        var event: NativeLong = NativeLong()

        @JvmField
        var child: NativeLong = NativeLong()

        @JvmField
        var rootX: Double = 0.0

        @JvmField
        var rootY: Double = 0.0

        @JvmField
        var eventX: Double = 0.0

        @JvmField
        var eventY: Double = 0.0

        @JvmField
        var mode: Int = 0

        @JvmField
        var focus: Int = 0

        @JvmField
        var sameScreen: Int = 0

        @JvmField
        var buttons: ButtonState = ButtonState()

        @JvmField
        var modifiers: ModifierState = ModifierState()

        @JvmField
        var group: ModifierState = ModifierState()

        init {
            read()
        }
    }

    companion object {
        val instance: LinuxXInput2 = Native.load("Xi", LinuxXInput2::class.java)
        const val ALL_DEVICES = 0
        const val ALL_MASTER_DEVICES = 1
        const val DEVICE_CHANGED = 1
        const val MOTION = 6
        const val ENTER = 7
        const val LEAVE = 8
        const val HIERARCHY_CHANGED = 11
        const val TOUCH_BEGIN = 18
        const val TOUCH_UPDATE = 19
        const val TOUCH_END = 20
        const val ACCEPT_TOUCH = 6
        const val REJECT_TOUCH = 7
        const val POINTER_EMULATED = 1 shl 16
    }
}

@Suppress("FunctionName")
internal interface LinuxTouchX11 : X11 {
    fun XConnectionNumber(
        display: X11.Display
    ): Int

    fun XGetEventData(
        display: X11.Display,
        cookie: EventCookie
    ): Int

    fun XFreeEventData(
        display: X11.Display,
        cookie: EventCookie
    )

    @Structure.FieldOrder(
        "type",
        "serial",
        "sendEvent",
        "display",
        "extension",
        "eventType",
        "cookie",
        "data"
    )
    class EventCookie(
        pointer: Pointer
    ) : Structure(pointer) {
        @JvmField
        var type: Int = 0

        @JvmField
        var serial: NativeLong = NativeLong()

        @JvmField
        var sendEvent: Int = 0

        @JvmField
        var display: Pointer? = null

        @JvmField
        var extension: Int = 0

        @JvmField
        var eventType: Int = 0

        @JvmField
        var cookie: Int = 0

        @JvmField
        var data: Pointer? = null

        init {
            read()
        }
    }

    companion object {
        val instance: LinuxTouchX11 = Native.load("X11", LinuxTouchX11::class.java)
    }
}

internal interface LinuxTouchPoll : Library {
    fun poll(
        fds: Pointer,
        count: NativeLong,
        timeout: Int
    ): Int

    companion object {
        val instance: LinuxTouchPoll = Native.load("c", LinuxTouchPoll::class.java)
    }
}
