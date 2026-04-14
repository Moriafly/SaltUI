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

package com.moriafly.salt.ui.window.internal

import com.moriafly.salt.ui.UnstableSaltUiApi

/**
 * Handler for exceptions caught inside [com.moriafly.salt.ui.window.SaltWindow]
 * and [com.moriafly.salt.ui.window.SaltDialogWindow].
 *
 * By default the exception is rethrown. The caller application can override
 * [onException] to redirect exceptions to its own logging or crash-reporting
 * system. When overridden, the exception is considered handled and will not be
 * propagated to the AWT event loop.
 */
@UnstableSaltUiApi
object SaltWindowExceptionHandler {
    /**
     * Called synchronously on the UI thread (AWT Event Dispatch Thread)
     * when an uncaught exception occurs inside a Salt window.
     */
    var onException: (Throwable) -> Unit = { throw it }
}
