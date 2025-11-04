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

package com.moriafly.salt.ui.gestures

import androidx.compose.animation.core.generateDecayAnimationSpec
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.moriafly.salt.ui.gestures.cupertino.CupertinoFlingBehavior
import com.moriafly.salt.ui.gestures.cupertino.CupertinoScrollDecayAnimationSpec

// TODO FlingBehavior.shouldBeTriggeredByMouseWheel
internal fun platformScrollableDefaultFlingBehavior(): FlingBehavior =
    CupertinoFlingBehavior(CupertinoScrollDecayAnimationSpec().generateDecayAnimationSpec())

@Composable
internal actual fun rememberPlatformDefaultFlingBehavior(): FlingBehavior =
    // Unlike other platforms, we don't need to remember it based on density,
    // because it's density independent
    remember {
        platformScrollableDefaultFlingBehavior()
    }
