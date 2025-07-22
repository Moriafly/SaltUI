/*
 * Salt UI
 * Copyright (C) 2025 Moriafly
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

@file:Suppress("unused")

package com.moriafly.salt.ui.util

import androidx.compose.ui.input.pointer.PointerButton
import com.moriafly.salt.ui.UnstableSaltUiApi
import java.awt.event.MouseEvent

/**
 * The Compose [PointerButton] that corresponds to this AWT [MouseEvent]'s button code.
 *
 * This property is `null` if the button code is [MouseEvent.NOBUTTON] or is otherwise unrecognized.
 *
 * ### AWT to Compose Mapping
 * - **[MouseEvent.BUTTON1]** -> `PointerButton.Primary`
 * - **[MouseEvent.BUTTON2]** -> `PointerButton.Tertiary` (Middle button)
 * - **[MouseEvent.BUTTON3]** -> `PointerButton.Secondary` (Right-click)
 * - **Button `4`** -> `PointerButton.Back`
 * - **Button `5`** -> `PointerButton.Forward`
 */
@UnstableSaltUiApi
val MouseEvent.pointerButtonOrNull: PointerButton?
    get() = when (this.button) {
        MouseEvent.BUTTON1 -> PointerButton.Primary
        MouseEvent.BUTTON2 -> PointerButton.Tertiary
        MouseEvent.BUTTON3 -> PointerButton.Secondary
        4 -> PointerButton.Back
        5 -> PointerButton.Forward
        else -> null
    }
