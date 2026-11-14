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

package com.moriafly.salt.ui

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import com.moriafly.salt.ui.material.GlassEffect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(ExperimentalTestApi::class, UnstableSaltUiApi::class)
class SaltThemeTest {
    @Test
    fun defaultGlassEffectIsInheritedWhenMaterialIsSpecified() = runComposeUiTest {
        val material = SaltMaterial.default()
        val effect = GlassEffect.Transparent
        var observedMaterial: SaltMaterial? = null
        var observedEffect: GlassEffect? = null

        setContent {
            SaltTheme(glassEffect = effect) {
                SaltTheme(material = material) {
                    val currentMaterial = SaltTheme.material
                    val currentEffect = SaltTheme.glassEffect
                    SideEffect {
                        observedMaterial = currentMaterial
                        observedEffect = currentEffect
                    }
                }
            }
        }

        runOnIdle {
            assertSame(material, observedMaterial)
            assertEquals(effect, observedEffect)
        }
    }
}
