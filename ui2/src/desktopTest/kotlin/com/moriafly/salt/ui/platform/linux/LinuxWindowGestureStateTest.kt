/*
 * SaltUI
 * Copyright (C) 2026 Moriafly
 * Licensed under the Apache License, Version 2.0
 */

package com.moriafly.salt.ui.platform.linux

import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.platform.linux.input.gesture.LinuxTouchTapRecognizer
import com.moriafly.salt.ui.platform.linux.input.gesture.LinuxWindowGestureState
import com.moriafly.salt.ui.platform.linux.input.gesture.LinuxWindowGestureState.CaptionHit
import com.moriafly.salt.ui.platform.linux.input.gesture.LinuxWindowGestureState.Command
import com.moriafly.salt.ui.platform.linux.input.gesture.LinuxWindowGestureState.Hit
import com.moriafly.salt.ui.platform.linux.input.gesture.LinuxWindowGestureState.Input
import com.moriafly.salt.ui.platform.linux.input.gesture.LinuxWindowGestureState.Phase
import com.moriafly.salt.ui.platform.linux.input.gesture.LinuxWindowGestureState.Pointer
import com.moriafly.salt.ui.platform.linux.input.gesture.LinuxWindowGestureState.Position
import com.moriafly.salt.ui.window.WindowResizeEdge
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(UnstableSaltUiApi::class)
class LinuxWindowGestureStateTest {
    @Test
    fun mouseCaptionReleaseDuringTouchHoldFinishesMoveAndAllowsNextTouch() {
        val state = configuredState()
        state.onInput(mouse(Phase.Press), Hit(caption = CaptionHit.Caption))
        state.onInput(mouse(Phase.Move, 160))
        state.onInput(touch(1, Phase.Press))
        assertEquals(
            listOf(Command.Move(Position(170, 100))),
            state.onInput(mouse(Phase.Move, 170)).commands
        )

        val released = state.onInput(mouse(Phase.Release, 180))
        assertTrue(released.consumed)
        assertTrue(Command.EndMove in released.commands, "Mouse release must finish its move")
        assertTrue(Command.ShowEdge(WindowResizeEdge.None) in released.commands)
        assertTouchQuarantineEndsWithLastContact(state)
    }

    @Test
    fun mouseResizeReleaseDuringTouchHoldAppliesPendingBoundsAndAllowsNextTouch() {
        val state = configuredState()
        state.onInput(mouse(Phase.Press), Hit(edge = WindowResizeEdge.Right))
        state.onInput(mouse(Phase.Move, 160))
        state.onInput(touch(1, Phase.Press))
        assertEquals(
            listOf(Command.Resize(Position(170, 100))),
            state.onInput(mouse(Phase.Move, 170)).commands
        )

        val released = state.onInput(mouse(Phase.Release, 180))
        assertTrue(released.consumed)
        assertTrue(
            Command.EndResize(applyPending = true) in released.commands,
            "The owning mouse release must commit its last resize position"
        )
        assertTrue(Command.ShowEdge(WindowResizeEdge.None) in released.commands)
        assertTouchQuarantineEndsWithLastContact(state)
    }

    @Test
    fun mouseReleaseCannotFinishATouchOwnedResize() {
        val state = configuredState()
        state.onInput(touch(1, Phase.Press), Hit(edge = WindowResizeEdge.Right))
        val mouseRelease = state.onInput(mouse(Phase.Release))
        assertTrue(mouseRelease.consumed)
        assertFalse(mouseRelease.commands.any { it is Command.EndResize })
        val touchRelease = state.onInput(touch(1, Phase.Release))
        assertTrue(Command.EndResize(applyPending = true) in touchRelease.commands)
    }

    @Test
    fun touchHoldRejectsNewMousePressAndConsumesItsClickWithoutRetainingSuppression() {
        val state = configuredState()
        state.onInput(mouse(Phase.Press), Hit(caption = CaptionHit.Caption))
        state.onInput(touch(1, Phase.Press))
        assertTrue(state.onInput(mouse(Phase.Press), Hit(edge = WindowResizeEdge.Right)).commands.isEmpty())
        val secondaryRelease = state.onInput(mouse(Phase.Release).copy(primary = false))
        assertTrue(secondaryRelease.consumed)
        assertTrue(secondaryRelease.commands.isEmpty())
        assertTrue(Command.EndMove in state.onInput(mouse(Phase.Release)).commands)

        val click = state.onInput(mouse(Phase.Click).copy(clickCount = 2))
        assertTrue(click.consumed)
        assertFalse(Command.ToggleMaximize in click.commands)
        assertTrue(state.onInput(mouse(Phase.Press), Hit(edge = WindowResizeEdge.Right)).commands.isEmpty())
        state.onInput(touch(1, Phase.Release))
        assertFalse(state.onInput(mouse(Phase.Click).copy(clickCount = 2)).consumed)
        assertEquals(listOf(Command.DeliverTouch), state.onInput(touch(4, Phase.Press)).commands)
        state.onInput(touch(4, Phase.Release))
    }

    private fun assertTouchQuarantineEndsWithLastContact(state: LinuxWindowGestureState) {
        // A second contact during the existing hold remains captured even after mouse release.
        assertFalse(Command.DeliverTouch in state.onInput(touch(2, Phase.Press)).commands)
        state.onInput(touch(1, Phase.Release))
        assertFalse(Command.DeliverTouch in state.onInput(touch(2, Phase.Move)).commands)
        state.onInput(touch(2, Phase.Release))
        assertEquals(listOf(Command.DeliverTouch), state.onInput(touch(3, Phase.Press)).commands)
        state.onInput(touch(3, Phase.Release))
    }

    private fun configuredState() = LinuxWindowGestureState().apply {
        configure(
            LinuxWindowGestureState.Configuration(
                moveable = true,
                resizable = true,
                canDrag = true,
                canMaximize = true,
                taps = LinuxTouchTapRecognizer.Configuration(
                    touchSlop = 8f,
                    longPressTimeoutMillis = 500,
                    doubleTapMinTimeMillis = 40,
                    doubleTapTimeoutMillis = 300
                )
            )
        )
    }

    private fun mouse(phase: Phase, x: Int = 100) = Input(
        pointer = Pointer.Mouse,
        phase = phase,
        position = Position(x, 100)
    )

    private fun touch(id: Long, phase: Phase) = Input(
        pointer = Pointer.Touch(id),
        phase = phase,
        position = Position(300, 200)
    )
}
