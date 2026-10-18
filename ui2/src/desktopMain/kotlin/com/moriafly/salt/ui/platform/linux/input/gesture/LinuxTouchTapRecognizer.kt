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

import com.moriafly.salt.ui.platform.linux.input.gesture.LinuxWindowGestureState.Input
import com.moriafly.salt.ui.platform.linux.input.gesture.LinuxWindowGestureState.Position

/**
 * Recognizes caption taps from supplied timestamps, scale and thresholds, without a platform clock.
 */
internal class LinuxTouchTapRecognizer {
    data class Configuration(
        val touchSlop: Float,
        val longPressTimeoutMillis: Long,
        val doubleTapMinTimeMillis: Long,
        val doubleTapTimeoutMillis: Long
    )

    private lateinit var configuration: Configuration
    private var press: Press? = null
    private var previousTap: Tap? = null

    fun configure(
        configuration: Configuration
    ) {
        this.configuration = configuration
    }

    fun begin(
        position: Position,
        timeMillis: Long
    ) {
        press = Press(position, timeMillis)
    }

    fun finish(
        input: Input
    ): Boolean {
        val current = press
        press = null
        if (current == null ||
            input.timeMillis - current.timeMillis !in
            0 until configuration.longPressTimeoutMillis ||
            !isWithinSlop(
                first = current.position,
                second = input.position,
                scaleX = input.scaleX,
                scaleY = input.scaleY
            )
        ) {
            cancel()
            return false
        }
        val doubleTap = previousTap?.let { previous ->
            val interval = current.timeMillis - previous.upTimeMillis
            val withinTimeout = interval in
                configuration.doubleTapMinTimeMillis..configuration.doubleTapTimeoutMillis
            withinTimeout && isWithinSlop(
                first = previous.position,
                second = current.position,
                scaleX = input.scaleX,
                scaleY = input.scaleY
            )
        } ?: false
        previousTap = if (doubleTap) null else Tap(current.position, input.timeMillis)
        return doubleTap
    }

    fun isWithinSlop(
        first: Position,
        second: Position,
        scaleX: Double,
        scaleY: Double
    ): Boolean {
        val dx = (second.x - first.x) * scaleX
        val dy = (second.y - first.y) * scaleY
        val slop = configuration.touchSlop
        return dx * dx + dy * dy <= slop * slop
    }

    fun cancel() {
        press = null
        previousTap = null
    }

    private class Press(
        val position: Position,
        val timeMillis: Long
    )

    private class Tap(
        val position: Position,
        val upTimeMillis: Long
    )
}
