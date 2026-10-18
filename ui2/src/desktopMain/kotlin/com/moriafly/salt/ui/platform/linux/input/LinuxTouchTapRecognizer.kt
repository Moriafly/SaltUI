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

import androidx.compose.ui.platform.ViewConfiguration
import java.awt.Point
import java.awt.Window

/**
 * Recognizes caption touch taps using Compose's timing and distance thresholds.
 *
 * The gesture owner supplies the captured contact and cancels recognition on another contact,
 * mouse press or gesture cancellation. This recognizer never captures or moves a window.
 */
internal class LinuxTouchTapRecognizer(
    private val window: Window
) {
    private lateinit var viewConfiguration: ViewConfiguration
    private var press: Press? = null
    private var previousTap: Tap? = null

    fun configure(viewConfiguration: ViewConfiguration) {
        this.viewConfiguration = viewConfiguration
    }

    fun begin(position: Point, timeMillis: Long) {
        press = Press(position, timeMillis)
    }

    /**
     * Completes the current contact and returns whether it completes a double tap.
     */
    fun finish(position: Point, timeMillis: Long): Boolean {
        val current = press
        press = null
        if (current == null ||
            timeMillis - current.timeMillis !in 0 until viewConfiguration.longPressTimeoutMillis ||
            !isWithinSlop(current.position, position)
        ) {
            cancel()
            return false
        }
        val doubleTap = previousTap?.let { previous ->
            val interval = current.timeMillis - previous.upTimeMillis
            val withinTimeout = interval in
                viewConfiguration.doubleTapMinTimeMillis..viewConfiguration.doubleTapTimeoutMillis
            withinTimeout && isWithinSlop(previous.position, current.position)
        } ?: false
        previousTap = if (doubleTap) null else Tap(current.position, timeMillis)
        return doubleTap
    }

    fun isWithinSlop(first: Point, second: Point): Boolean {
        // AWT screen coordinates are logical units; Compose's touch slop is in physical pixels.
        val transform = window.graphicsConfiguration.defaultTransform
        val dx = (second.x - first.x) * transform.scaleX
        val dy = (second.y - first.y) * transform.scaleY
        val slop = viewConfiguration.touchSlop
        return dx * dx + dy * dy <= slop * slop
    }

    fun cancel() {
        press = null
        previousTap = null
    }

    private class Press(
        val position: Point,
        val timeMillis: Long
    )

    private class Tap(
        val position: Point,
        val upTimeMillis: Long
    )
}
