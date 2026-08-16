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

package com.moriafly.salt.ui

import kotlin.jvm.JvmField

/**
 * Salt UI Flags.
 */
@UnstableSaltUiApi
object SaltUiFlags {
    /**
     * Disable default window exception handler.
     */
    @Suppress("MutableBareField")
    @JvmField
    var isDisableDefaultWindowExceptionHandler: Boolean = true

    /**
     * Always dispatch scroll and fling input through the configured overscroll effect.
     *
     * Jetpack Compose 1.12.0-alpha03 dispatches overscroll when the scrollable state can scroll in
     * either direction, or when the overscroll effect reports that it is already in progress. Salt
     * UI intentionally uses a broader default: when this flag is enabled, overscroll is also
     * dispatched when the content has no scroll range and the effect is not currently in progress.
     * This allows an overscroll effect to start from that otherwise non-scrollable state.
     *
     * Because the enabled flag short-circuits the upstream conditions, `canScrollForward`,
     * `canScrollBackward`, and `OverscrollEffect.isInProgress` do not affect this dispatch decision
     * while it remains `true`.
     *
     * @see com.moriafly.salt.ui.gestures.ScrollingLogic.shouldDispatchOverscroll
     */
    @Suppress("MutableBareField")
    @JvmField
    var isAlwaysShouldDispatchOverscrollEnabled: Boolean = true
}
