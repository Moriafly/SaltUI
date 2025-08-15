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

package com.moriafly.salt.ui.window

import androidx.compose.ui.unit.DpSize
import com.moriafly.salt.ui.UnstableSaltUiApi
import java.awt.Window

/**
 * # Properties for SaltWindow
 *
 * @property minSize The minimum size of the window.
 * @property onVisibleChanged The callback to be invoked when the visibility of the window changes.
 * To replace obtaining the window isVisible state in Composable.
 *
 * Do **not** use this:
 * ```
 * SaltWindow(
 *     // ...
 * ) {
 *     LaunchEffect(window.isVisible) {
 *         // Do something
 *     }
 * }
 * ```
 *
 * Please use:
 * ```
 * SaltWindow(
 *     // ...,
 *     properties = SaltWindowProperties(
 *         onVisibleChanged = { window, visible ->
 *             // Do something
 *         }
 *     )
 * ) {
 *     // ...
 * }
 */
@UnstableSaltUiApi
data class SaltWindowProperties<T : Window>(
    val minSize: DpSize = DpSize.Zero,
    val onVisibleChanged: (T, Boolean) -> Unit = { _, _ -> }
)
