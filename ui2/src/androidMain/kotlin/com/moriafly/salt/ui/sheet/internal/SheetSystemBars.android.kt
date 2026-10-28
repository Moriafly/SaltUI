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

@file:Suppress("ktlint:standard:filename")

package com.moriafly.salt.ui.sheet.internal

import android.view.View
import android.view.Window
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Initializes one Sheet dialog window with the owner's opening system bar visibility.
 *
 * Call [inheritOwnerVisibility] from the host's SideEffect after a successful composition.
 * Only the dialog's controller is changed; the owner keeps its own requests, so dismissal requires
 * no restoration. Further visibility changes belong to the developer controlling the dialog.
 */
internal class SheetSystemBars(
    window: Window,
    private val ownerVisibility: Visibility
) {
    private val controller = WindowCompat.getInsetsController(window, window.decorView)
    private var initialized = false

    /**
     * Applies the opening state once per dialog. Later recompositions must not override explicit
     * changes from the Sheet's content or bars temporarily revealed by system gestures or the IME.
     */
    fun inheritOwnerVisibility() {
        if (initialized) {
            return
        }
        setVisible(WindowInsetsCompat.Type.statusBars(), ownerVisibility.statusBarVisible)
        setVisible(WindowInsetsCompat.Type.navigationBars(), ownerVisibility.navigationBarVisible)
        initialized = true
    }

    private fun setVisible(bar: Int, visible: Boolean) {
        if (visible) {
            controller.show(bar)
        } else {
            controller.hide(bar)
        }
    }

    data class Visibility(
        val statusBarVisible: Boolean,
        val navigationBarVisible: Boolean
    )

    companion object {
        /**
         * Captures actual visibility before the dialog takes focus. Later owner insets reflect the
         * active dialog and cannot reveal pending requests on the unfocused owner. Without insets,
         * uses the platform's default visible bars.
         */
        fun readOwnerVisibility(ownerView: View): Visibility {
            val insets = ViewCompat.getRootWindowInsets(ownerView)
            return Visibility(
                statusBarVisible = insets?.isVisible(WindowInsetsCompat.Type.statusBars()) != false,
                navigationBarVisible =
                    insets?.isVisible(WindowInsetsCompat.Type.navigationBars()) != false
            )
        }
    }
}
