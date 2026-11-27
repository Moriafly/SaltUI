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

package com.moriafly.salt.ui.material

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.snapshots.Snapshot
import kotlin.test.Test

class GlassLayerOpacityTest {
    @Test
    fun opacityInvalidationCanOverlapWithRecomposition() {
        val layerOpacity = mutableFloatStateOf(1f)

        GlassLayerOpacity.observe().use {
            Snapshot.withMutableSnapshot {
                // A nested scene can send global notifications during recomposition.
                Snapshot.global { layerOpacity.floatValue = 0.9f }
                Snapshot.sendApplyNotifications()

                // Separate global updates leave a different revision from the nested callback.
                Snapshot.global {
                    layerOpacity.floatValue = 0.8f
                    Snapshot.sendApplyNotifications()
                    layerOpacity.floatValue = 0.7f
                    Snapshot.sendApplyNotifications()
                }
            }
        }
    }
}
