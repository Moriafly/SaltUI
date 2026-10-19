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

package com.moriafly.salt.ui.platform.linux.window

import java.awt.Dialog
import java.awt.Dialog.ModalExclusionType
import java.awt.Dialog.ModalityType
import java.awt.Window

/**
 * Checks native input eligibility on the EDT, using AWT's exact blocker when accessible.
 * The public fallback covers the current AppContext and may over-block overlapping modal
 * dialogs because AWT does not expose their blocker ordering through public methods.
 */
internal object LinuxWindowModality {
    private val modalBlocked = runCatching {
        Window::class.java.getDeclaredMethod("isModalBlocked")
            .takeIf { it.trySetAccessible() }
    }.getOrNull()

    fun isBlocked(
        window: Window
    ): Boolean {
        val blocked = modalBlocked?.let { method ->
            runCatching { method.invoke(window) as Boolean }.getOrNull()
        }
        if (blocked != null) return blocked

        val owners = generateSequence(window) { it.owner }.toList()
        val exclusion = owners.maxOf { it.modalExclusionType }
        val dialogs = Window.getWindows().filterIsInstance<Dialog>().filter {
            it.isShowing && it.isModal && it !in owners
        }
        return dialogs.any { dialog ->
            when (dialog.modalityType) {
                ModalityType.MODELESS -> false
                ModalityType.DOCUMENT_MODAL -> {
                    val dialogOwners = generateSequence<Window>(dialog) { it.owner }
                    if (exclusion >= ModalExclusionType.APPLICATION_EXCLUDE) {
                        // Document-modal children still block their excluded owners.
                        dialogOwners.any { it === window }
                    } else {
                        dialogOwners.last() === owners.last()
                    }
                }

                ModalityType.APPLICATION_MODAL -> exclusion < ModalExclusionType.APPLICATION_EXCLUDE
                ModalityType.TOOLKIT_MODAL -> exclusion < ModalExclusionType.TOOLKIT_EXCLUDE
            }
        }
    }
}
