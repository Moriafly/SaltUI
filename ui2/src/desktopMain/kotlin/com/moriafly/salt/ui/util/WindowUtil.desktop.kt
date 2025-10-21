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

import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.awt.ComposeWindow
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef
import org.jetbrains.skiko.SkiaLayer
import java.awt.Container
import java.awt.Window
import javax.swing.JComponent

/**
 * Get the handle of the window, only works on Windows platform.
 */
val ComposeWindow.hwnd: WinDef.HWND
    get() = WinDef.HWND(Pointer(windowHandle))

/**
 * Get the handle of the window, only works on Windows platform.
 */
val ComposeDialog.hwnd: WinDef.HWND
    get() = WinDef.HWND(Pointer(windowHandle))

/**
 * Get the handle of the window, only works on Windows platform.
 */
@UnstableSaltUiApi
val Window.hwnd: WinDef.HWND
    get() = when (this) {
        is ComposeWindow -> hwnd
        is ComposeDialog -> hwnd
        else -> throw IllegalArgumentException(
            "Unsupported window type: ${this::class.simpleName}"
        )
    }

/**
 * Get whether the window is undecorated, only works on Windows platform.
 */
@UnstableSaltUiApi
val Window.isUndecorated: Boolean
    get() = when (this) {
        is ComposeWindow -> isUndecorated
        is ComposeDialog -> isUndecorated
        else -> throw IllegalArgumentException(
            "Unsupported window type: ${this::class.simpleName}"
        )
    }

/**
 * Extension on [ComposeWindow] to find its underlying [SkiaLayer].
 */
fun ComposeWindow.findSkiaLayer(): SkiaLayer? = findComponent<SkiaLayer>()

/**
 * Extension on [ComposeDialog] to find its underlying [SkiaLayer].
 */
@UnstableSaltUiApi
fun ComposeDialog.findSkiaLayer(): SkiaLayer? = findComponent<SkiaLayer>()

/**
 * Extension on [Window] to find its underlying [SkiaLayer].
 */
@UnstableSaltUiApi
fun Window.findSkiaLayer(): SkiaLayer? = when (this) {
    is ComposeWindow -> findSkiaLayer()
    is ComposeDialog -> findSkiaLayer()
    else -> throw IllegalArgumentException(
        "Unsupported window type: ${this::class.simpleName}"
    )
}

/**
 * Recursively finds the first JComponent of a specific type in a container (depth-first).
 *
 * @param container The container to search in.
 * @param klass The component class to find.
 * @return The found component, or null if not found.
 */
private fun <T : JComponent> findComponent(container: Container, klass: Class<T>): T? {
    for (component in container.components) {
        if (klass.isInstance(component)) {
            @Suppress("UNCHECKED_CAST")
            return component as T
        }
        if (component is Container) {
            val found = findComponent(component, klass)
            if (found != null) {
                return found
            }
        }
    }
    return null
}

/**
 * Convenience extension to call [findComponent] with a reified type.
 */
private inline fun <reified T : JComponent> Container.findComponent(): T? =
    findComponent(this, T::class.java)
