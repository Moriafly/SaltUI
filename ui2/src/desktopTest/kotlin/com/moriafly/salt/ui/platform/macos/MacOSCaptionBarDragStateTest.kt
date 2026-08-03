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

package com.moriafly.salt.ui.platform.macos

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MacOSCaptionBarDragStateTest {
    @Test
    fun dragStateClearsPendingGesture() {
        val state = MacOSCaptionBarDragState()

        state.onPress(canStartDrag = true)
        state.onRelease()
        assertFalse(state.startPendingDrag { true })

        state.onPress(canStartDrag = true)
        assertTrue(state.startPendingDrag { true })
        assertFalse(state.startPendingDrag { true })

        state.onPress(canStartDrag = true)
        state.onPress(canStartDrag = false)
        assertFalse(state.startPendingDrag { true })

        state.onPress(canStartDrag = true)
        assertFalse(state.startPendingDrag { false })
        assertTrue(state.startPendingDrag { true })
    }
}
