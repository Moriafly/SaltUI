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

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.moriafly.salt.ui.UnstableSaltUiApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class, UnstableSaltUiApi::class)
class RememberSaltNavigatorTest {
    @OptIn(ExperimentalSerializationApi::class)
    private val configuration = SavedStateConfiguration {
        serializersModule = SerializersModule {
            polymorphic(NavKey::class) {
                subclassesOfSealed<TestRoute>()
            }
        }
    }

    @Test
    fun initialState_hasSingleInitRoute() = runComposeUiTest {
        lateinit var navigator: SaltNavigator
        setContent {
            navigator = rememberSaltNavigator(
                configuration = configuration,
                initRoute = TestRoute.Home,
                topLevelRoutes = setOf(TestRoute.Home, TestRoute.Settings)
            )
        }
        assertEquals(1, navigator.navBackStack.size)
        assertEquals(TestRoute.Home, navigator.navBackStack[0])
        assertEquals(0, navigator.navForwardStack.size)
    }

    @Test
    fun navigate_updatesBackStack() = runComposeUiTest {
        lateinit var navigator: SaltNavigator
        setContent {
            navigator = rememberSaltNavigator(
                configuration = configuration,
                initRoute = TestRoute.Home,
                topLevelRoutes = setOf(TestRoute.Home, TestRoute.Settings)
            )
        }
        navigator.navigate(TestRoute.Detail("1"))
        assertEquals(2, navigator.navBackStack.size)
        assertEquals(TestRoute.Home, navigator.navBackStack[0])
        assertEquals(TestRoute.Detail("1"), navigator.navBackStack[1])
    }

    @Test
    fun navigateToTopLevelRoute_clearsForwardStack() = runComposeUiTest {
        lateinit var navigator: SaltNavigator
        setContent {
            navigator = rememberSaltNavigator(
                configuration = configuration,
                initRoute = TestRoute.Home,
                topLevelRoutes = setOf(TestRoute.Home, TestRoute.Settings)
            )
        }
        navigator.navigate(TestRoute.Detail("1"))
        navigator.back()
        navigator.navigate(TestRoute.Settings)
        assertEquals(1, navigator.navBackStack.size)
        assertEquals(TestRoute.Settings, navigator.navBackStack[0])
        assertEquals(0, navigator.navForwardStack.size)
    }

    @Test
    fun back_movesRouteToForwardStack() = runComposeUiTest {
        lateinit var navigator: SaltNavigator
        setContent {
            navigator = rememberSaltNavigator(
                configuration = configuration,
                initRoute = TestRoute.Home,
                topLevelRoutes = setOf(TestRoute.Home, TestRoute.Settings)
            )
        }
        navigator.navigate(TestRoute.Detail("1"))
        navigator.back()
        assertEquals(1, navigator.navBackStack.size)
        assertEquals(TestRoute.Home, navigator.navBackStack[0])
        assertEquals(1, navigator.navForwardStack.size)
        assertEquals(TestRoute.Detail("1"), navigator.navForwardStack[0])
    }

    @Test
    fun forward_restoresRoute() = runComposeUiTest {
        lateinit var navigator: SaltNavigator
        setContent {
            navigator = rememberSaltNavigator(
                configuration = configuration,
                initRoute = TestRoute.Home,
                topLevelRoutes = setOf(TestRoute.Home, TestRoute.Settings)
            )
        }
        navigator.navigate(TestRoute.Detail("1"))
        navigator.back()
        navigator.forward()
        assertEquals(2, navigator.navBackStack.size)
        assertEquals(TestRoute.Home, navigator.navBackStack[0])
        assertEquals(TestRoute.Detail("1"), navigator.navBackStack[1])
        assertEquals(0, navigator.navForwardStack.size)
    }
}
