/*
 * SaltUI
 * Copyright (C) 2024 Moriafly
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

@file:Suppress("UNUSED")

package com.moriafly.salt.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Infinite rotation animation loop
 *
 * @param key Determines whether to restart the animation based on the key's value
 * @param rotating Controls whether the rotation is in progress
 * @param resetDuration Duration for resetting the position back to the starting point
 * @param repeatDuration Duration for completing one full rotation cycle
 */
@UnstableSaltApi
@Composable
fun animateInfiniteRotationState(
    key: Any,
    rotating: Boolean,
    resetDuration: Int = 250,
    repeatDuration: Int = 15_000
): State<Float> {
    var rotation by remember { mutableStateOf(Animatable(0f)) }

    LaunchedEffect(key) {
        rotation.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = resetDuration,
                easing = LinearEasing
            )
        )
    }

    LaunchedEffect(rotating) {
        if (rotating) {
            // From the last paused angle -> execute animation -> to target angle (+360)
            rotation.animateTo(
                targetValue = (rotation.value % 360f) + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = repeatDuration,
                        easing = LinearEasing
                    )
                )
            )
        } else {
            rotation.stop()
            // Taking the modulus of the initial angle ensures the target angle doesn't infinitely increase after each pause
            rotation = Animatable(rotation.value % 360f)
        }
    }
    return rotation.asState()
}