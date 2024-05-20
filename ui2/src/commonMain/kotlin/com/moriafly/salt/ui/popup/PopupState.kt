/**
 * SaltUI
 * Copyright (C) 2023 Moriafly
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

@file:Suppress("UNUSED")

package com.moriafly.salt.ui.popup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun rememberPopupState(
    initialExpend: Boolean = false
): PopupState {
    return rememberSaveable(saver = PopupState.Saver) {
        PopupState(
            initialExpend = initialExpend
        )
    }
}

/**
 * The state of PopupMenu
 *
 * @param initialExpend initial expend state
 */
@Stable
class PopupState(
    initialExpend: Boolean
) {

    var expend by mutableStateOf(initialExpend)
        private set

    fun expend() {
        expend = true
    }

    fun dismiss() {
        expend = false
    }

    companion object {

        val Saver: Saver<PopupState, *> = Saver(
            save = { it.expend },
            restore = { PopupState(it) }
        )

    }

}