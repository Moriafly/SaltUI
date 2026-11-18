/*
 * Copyright 2021 The Android Open Source Project
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
 *
 * Source: https://repo.maven.apache.org/maven2/org/jetbrains/compose/material/material-ripple/1.12.2/material-ripple-1.12.2-sources.jar
 * Modified for SaltUI: use the shared SaltUI ripple renderer.
 */

package com.moriafly.salt.ui.indication.ripple

import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.unit.Dp

internal actual fun createPlatformRippleNode(
    interactionSource: InteractionSource,
    bounded: Boolean,
    radius: Dp,
    color: Color
): DelegatableNode = CommonRippleNode(
    interactionSource = interactionSource,
    bounded = bounded,
    radius = radius,
    color = color
)
