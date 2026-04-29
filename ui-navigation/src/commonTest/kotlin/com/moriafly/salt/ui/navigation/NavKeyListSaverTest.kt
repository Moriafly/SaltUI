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

package com.moriafly.salt.ui.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.toMutableStateList
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class NavKeyListSaverTest {
    @OptIn(ExperimentalSerializationApi::class)
    private val configuration = SavedStateConfiguration {
        serializersModule = SerializersModule {
            polymorphic(NavKey::class) {
                subclassesOfSealed<TestRoute>()
            }
        }
    }

    private val saver = NavKeyListSaver(configuration)

    private fun save(value: List<NavKey>): String =
        with(saver) {
            SaverScope { true }.save(value.toMutableStateList())
        }

    @Test
    fun saveAndRestore_preservesOrder() {
        val original = mutableStateListOf<NavKey>(TestRoute.Home, TestRoute.Settings)
        val saved = save(original)
        val restored = saver.restore(saved)
        assertNotNull(restored)
        assertEquals(2, restored.size)
        assertEquals(TestRoute.Home, restored[0])
        assertEquals(TestRoute.Settings, restored[1])
    }

    @Test
    fun saveAndRestore_preservesPolymorphicTypes() {
        val original = mutableStateListOf<NavKey>(TestRoute.Home, TestRoute.Detail("42"))
        val saved = save(original)
        val restored = saver.restore(saved)
        assertNotNull(restored)
        assertEquals(TestRoute.Home, restored[0])
        assertEquals(TestRoute.Detail("42"), restored[1])
    }

    @Test
    fun restoreEmptyList_returnsEmptyList() {
        val original = mutableStateListOf<NavKey>()
        val saved = save(original)
        val restored = saver.restore(saved)
        assertNotNull(restored)
        assertEquals(0, restored.size)
    }

    @Test
    fun restoreCorruptedString_throws() {
        assertFailsWith<Exception> {
            saver.restore("invalid-json")
        }
    }
}
