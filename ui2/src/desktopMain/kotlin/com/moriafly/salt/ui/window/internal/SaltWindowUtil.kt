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

package com.moriafly.salt.ui.window.internal

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowDecoration
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * Resolves the appropriate window decoration based on the target platform.
 *
 * On Linux environments, the window is always forced to [WindowDecoration.Undecorated]
 * regardless of the requested decoration, because [WindowDecoration.SystemDefault]
 * implicitly renders the native desktop environment's title bar, which conflicts
 * with the unified custom window appearance across all platforms.
 *
 * On other platforms, the original decoration is returned as-is.
 */
@UnstableSaltUiApi
@OptIn(ExperimentalComposeUiApi::class)
internal fun WindowDecoration.resolveForPlatform(): WindowDecoration {
    val requiresUndecorated = OS.isLinux()
    return if (requiresUndecorated) WindowDecoration.Undecorated(0.dp) else this
}
