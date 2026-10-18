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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Registers caption geometry and reports Compose touch hits to the window gesture owner.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun Modifier.linuxCaptionBarInput(): Modifier {
    val hitTest = LocalLinuxCaptionBarHitTest.current
    val modifier = if (hitTest != null) {
        val region = remember(hitTest) { LinuxCaptionBarHitTest.Region() }
        DisposableEffect(hitTest, region) {
            hitTest.regions.add(region)
            onDispose { hitTest.regions.remove(region) }
        }
        onGloballyPositioned { region.bounds = it.boundsInWindow() }
    } else {
        this
    }
    val gestures = LocalLinuxWindowGestures.current ?: return modifier
    // Compose hit testing decides whether the caption or a control above it receives the press.
    return modifier.onPointerEvent(PointerEventType.Press) { event ->
        val native = event.nativeEvent as? LinuxTouchEvent ?: return@onPointerEvent
        if (gestures.onCaptionPress(native)) {
            event.changes.forEach { it.consume() }
        }
    }
}
