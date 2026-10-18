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

package com.moriafly.salt.ui.platform.linux.input.gesture

import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.WindowResizeEdge

/**
 * Deterministic window-gesture policy. Inputs and commands contain values only; this state machine
 * neither reads a window nor calls Compose, a clock, or a native input service.
 *
 * A captured touch sequence remains quarantined until every contact leaves. Caption queries carry
 * an identity so a reply arriving after cancellation cannot capture a new sequence.
 */
@OptIn(UnstableSaltUiApi::class)
internal class LinuxWindowGestureState {
    data class Position(
        val x: Int,
        val y: Int
    )

    sealed interface Pointer {
        data object Mouse : Pointer

        data class Touch(
            val id: Long
        ) : Pointer
    }

    enum class Phase { Press, Move, Release, Click, Enter, Exit }

    enum class CaptionHit { Content, Caption, ResolveInCompose }

    data class Hit(
        val edge: WindowResizeEdge = WindowResizeEdge.None,
        val caption: CaptionHit = CaptionHit.Content
    )

    data class Input(
        val pointer: Pointer,
        val phase: Phase,
        val position: Position,
        val timeMillis: Long = 0,
        val primary: Boolean = true,
        val clickCount: Int = 0,
        val scaleX: Double = 1.0,
        val scaleY: Double = 1.0
    )

    data class Configuration(
        val moveable: Boolean,
        val resizable: Boolean,
        val canDrag: Boolean,
        val canMaximize: Boolean,
        val taps: LinuxTouchTapRecognizer.Configuration
    )

    sealed interface Command {
        data object DeliverTouch : Command

        data class ResolveCaption(
            val request: Long
        ) : Command

        data object CancelCompose : Command

        data class StartMove(
            val pointer: Pointer,
            val start: Position,
            val current: Position
        ) : Command

        data class Move(
            val position: Position
        ) : Command

        data object EndMove : Command

        data object ToggleMaximize : Command

        data class StartResize(
            val edge: WindowResizeEdge,
            val position: Position
        ) : Command

        data class Resize(
            val position: Position
        ) : Command

        data class EndResize(
            val applyPending: Boolean
        ) : Command

        data class ShowEdge(
            val edge: WindowResizeEdge
        ) : Command

        data class EnableResize(
            val enabled: Boolean
        ) : Command
    }

    data class Decision(
        val consumed: Boolean,
        val commands: List<Command>
    )

    private sealed interface Gesture {
        val pointer: Pointer

        data class Move(
            override val pointer: Pointer,
            val start: Position,
            var pending: Boolean = true
        ) : Gesture

        data class Resize(
            override val pointer: Pointer
        ) : Gesture
    }

    private data class CaptionQuery(
        val request: Long,
        val input: Input
    )

    private enum class Click { Caption, Resize }

    private var configuration: Configuration? = null
    private var gesture: Gesture? = null
    private val contacts = mutableSetOf<Long>()
    private var capturedTouches = false
    private var suppressedClick: Click? = null
    private var captionQuery: CaptionQuery? = null
    private var nextRequest = 0L
    private val taps = LinuxTouchTapRecognizer()

    fun configure(
        value: Configuration
    ): Decision {
        val previous = configuration
        if (previous == null || previous.moveable != value.moveable ||
            previous.canDrag != value.canDrag ||
            previous.canMaximize != value.canMaximize
        ) {
            taps.cancel()
            captionQuery = null
        }
        configuration = value
        taps.configure(value.taps)
        val commands = mutableListOf<Command>(Command.EnableResize(value.resizable))
        if ((gesture is Gesture.Move && (!value.moveable || !value.canDrag)) ||
            (gesture is Gesture.Resize && !value.resizable)
        ) {
            finish(commands)
        }
        if (!value.resizable) commands += Command.ShowEdge(WindowResizeEdge.None)
        return Decision(false, commands)
    }

    fun onInput(
        input: Input,
        hit: Hit = Hit()
    ): Decision {
        checkNotNull(configuration) { "Configure gestures before dispatching input" }
        val commands = mutableListOf<Command>()
        val consumed = when (input.pointer) {
            Pointer.Mouse -> onMouse(input, hit, commands)
            is Pointer.Touch -> onTouch(input, hit, commands)
        }
        return Decision(consumed, commands)
    }

    fun resolveCaption(
        request: Long,
        hit: Boolean
    ): Decision {
        val query = captionQuery
        if (query == null || query.request != request) return Decision(true, emptyList())
        captionQuery = null
        val commands = mutableListOf<Command>()
        val pointer = query.input.pointer as Pointer.Touch
        if (hit && canMove && gesture == null && !capturedTouches && pointer.id in contacts) {
            captureCaption(query.input, commands)
        } else {
            taps.cancel()
        }
        return Decision(true, commands)
    }

    fun cancel(): Decision {
        taps.cancel()
        captionQuery = null
        suppressedClick = null
        capturedTouches = contacts.isNotEmpty()
        val commands = mutableListOf<Command>()
        finish(commands)
        return Decision(false, commands)
    }

    fun resetTouchInput(): Decision {
        taps.cancel()
        captionQuery = null
        contacts.clear()
        capturedTouches = false
        val commands = mutableListOf<Command>()
        // Device/session resets must leave an AWT mouse drag alone.
        if (gesture?.pointer is Pointer.Touch) finish(commands)
        return Decision(false, commands)
    }

    private val canMove: Boolean
        get() = configuration?.let { it.moveable && it.canDrag } == true

    private fun onTouch(
        input: Input,
        hit: Hit,
        commands: MutableList<Command>
    ): Boolean {
        val pointer = input.pointer as Pointer.Touch
        val firstContact = contacts.isEmpty()
        if (input.phase == Phase.Press) {
            if (!contacts.add(pointer.id)) return true
            captionQuery = null
            if (!firstContact) taps.cancel()
        }

        if (pointer.id !in contacts) {
            return true
        }

        try {
            if (capturedTouches || gesture?.pointer == Pointer.Mouse) {
                capturedTouches = true
                onCapturedTouch(input, commands)
                return true
            }

            if (firstContact && input.phase == Phase.Press) {
                beginTouchGesture(input, hit, commands)
            } else {
                commands += Command.DeliverTouch
            }

            return true
        } finally {
            if (input.phase == Phase.Release) {
                if (captionQuery?.input?.pointer == pointer) {
                    captionQuery = null
                }

                contacts.remove(pointer.id)

                if (contacts.isEmpty()) {
                    capturedTouches = false
                }
            }
        }
    }

    private fun onCapturedTouch(
        input: Input,
        commands: MutableList<Command>
    ) {
        if (gesture?.pointer != input.pointer) return
        when (input.phase) {
            Phase.Move -> update(input, commands)
            Phase.Release -> {
                val doubleTap = gesture is Gesture.Move && taps.finish(input)
                finish(commands, applyPendingResize = true)
                if (doubleTap) toggleMaximize(commands)
            }

            else -> Unit
        }
    }

    private fun beginTouchGesture(
        input: Input,
        hit: Hit,
        commands: MutableList<Command>
    ) {
        when {
            configuration!!.resizable && hit.edge != WindowResizeEdge.None -> {
                capturedTouches = true
                commands += Command.CancelCompose
                beginResize(
                    pointer = input.pointer,
                    edge = hit.edge,
                    position = input.position,
                    commands = commands
                )
            }

            canMove && hit.caption == CaptionHit.Caption -> captureCaption(input, commands)
            canMove && hit.caption == CaptionHit.ResolveInCompose -> {
                val query = CaptionQuery(++nextRequest, input)
                captionQuery = query
                commands += Command.ResolveCaption(query.request)
            }

            else -> {
                taps.cancel()
                commands += Command.DeliverTouch
            }
        }
    }

    private fun captureCaption(
        input: Input,
        commands: MutableList<Command>
    ) {
        capturedTouches = true
        commands += Command.CancelCompose
        taps.begin(input.position, input.timeMillis)
        gesture = Gesture.Move(input.pointer, input.position)
    }

    private fun onMouse(
        input: Input,
        hit: Hit,
        commands: MutableList<Command>
    ): Boolean {
        if (input.phase == Phase.Press) taps.cancel()

        // Touch contacts block new mouse gestures, but cannot take ownership of an existing one.
        val continuesMouseGesture = gesture?.pointer == Pointer.Mouse &&
            (input.phase == Phase.Move || input.phase == Phase.Release)

        if (contacts.isNotEmpty() && !continuesMouseGesture) {
            if (input.phase == Phase.Click && input.primary) {
                suppressedClick = null
            }

            return input.phase == Phase.Press || input.phase == Phase.Move ||
                input.phase == Phase.Release || input.phase == Phase.Click
        }
        return onMouseGesture(input, hit, commands)
    }

    private fun onMouseGesture(
        input: Input,
        hit: Hit,
        commands: MutableList<Command>
    ): Boolean {
        val edge = if (configuration!!.resizable) hit.edge else WindowResizeEdge.None
        when (input.phase) {
            Phase.Enter -> commands += Command.ShowEdge(edge)
            Phase.Exit -> if (gesture == null) commands += Command.ShowEdge(WindowResizeEdge.None)
            Phase.Press -> return beginMouseGesture(
                input = input,
                edge = edge,
                caption = hit.caption,
                commands = commands
            )

            Phase.Move -> {
                if (gesture?.pointer != Pointer.Mouse) return false
                update(input, commands)
                return true
            }

            Phase.Release -> {
                val active = gesture ?: return false
                if (input.primary) {
                    suppressedClick = if (active is Gesture.Move) Click.Caption else Click.Resize
                    finish(commands, applyPendingResize = true)
                    commands += Command.ShowEdge(edge)
                }
                return true
            }

            Phase.Click -> return onMouseClick(input, commands)
        }
        return false
    }

    private fun onMouseClick(
        input: Input,
        commands: MutableList<Command>
    ): Boolean {
        val click = suppressedClick ?: return false
        if (!input.primary) return false
        suppressedClick = null
        if (click == Click.Caption && input.clickCount == 2) toggleMaximize(commands)
        return true
    }

    private fun beginMouseGesture(
        input: Input,
        edge: WindowResizeEdge,
        caption: CaptionHit,
        commands: MutableList<Command>
    ): Boolean {
        if (!input.primary) return gesture != null
        finish(commands)
        suppressedClick = null
        if (edge != WindowResizeEdge.None) {
            beginResize(
                pointer = Pointer.Mouse,
                edge = edge,
                position = input.position,
                commands = commands
            )
            return true
        }
        if (!canMove || caption != CaptionHit.Caption) return false
        gesture = Gesture.Move(Pointer.Mouse, input.position)
        return true
    }

    private fun beginResize(
        pointer: Pointer,
        edge: WindowResizeEdge,
        position: Position,
        commands: MutableList<Command>
    ) {
        taps.cancel()
        gesture = Gesture.Resize(pointer)
        commands += Command.StartResize(edge, position)
        commands += Command.ShowEdge(edge)
    }

    private fun update(
        input: Input,
        commands: MutableList<Command>
    ) {
        when (val active = gesture) {
            is Gesture.Move -> {
                if (active.pending) {
                    if (active.pointer is Pointer.Touch) {
                        val withinSlop = taps.isWithinSlop(
                            first = active.start,
                            second = input.position,
                            scaleX = input.scaleX,
                            scaleY = input.scaleY
                        )
                        if (withinSlop) return
                        taps.cancel()
                    }
                    active.pending = false
                    commands += Command.StartMove(
                        pointer = active.pointer,
                        start = active.start,
                        current = input.position
                    )
                } else {
                    commands += Command.Move(input.position)
                }
            }

            is Gesture.Resize -> commands += Command.Resize(input.position)
            null -> Unit
        }
    }

    private fun toggleMaximize(
        commands: MutableList<Command>
    ) {
        if (canMove && configuration!!.canMaximize) commands += Command.ToggleMaximize
    }

    /**
     * Releases ownership and clears the move state and resize-edge indicator.
     *
     * @param applyPendingResize Pass `true` on mouse or touch release to commit the last queued
     * resize. Cancellation leaves it `false` so pending work cannot change the window afterward.
     */
    private fun finish(
        commands: MutableList<Command>,
        applyPendingResize: Boolean = false
    ) {
        if (gesture is Gesture.Resize) commands += Command.EndResize(applyPendingResize)
        gesture = null
        commands += Command.EndMove
        commands += Command.ShowEdge(WindowResizeEdge.None)
    }
}
