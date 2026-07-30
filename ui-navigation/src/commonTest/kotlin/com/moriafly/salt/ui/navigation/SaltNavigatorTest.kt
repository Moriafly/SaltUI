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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.moriafly.salt.ui.UnstableSaltUiApi
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(UnstableSaltUiApi::class)
class SaltNavigatorTest {
    private fun createNavigator(
        backStack: NavBackStack<NavKey> = NavBackStack(mutableStateListOf(TestRoute.Home)),
        forwardStack: SnapshotStateList<NavKey> = mutableStateListOf()
    ): SaltNavigator = SaltNavigator(
        topLevelRoutes = setOf(TestRoute.Home, TestRoute.Settings),
        navBackStack = backStack,
        navForwardStack = forwardStack
    )

    @Test
    fun navigateToSameRoute_doesNothing() {
        val navigator = createNavigator()
        navigator.navigate(TestRoute.Home)
        assertEquals(1, navigator.navBackStack.size)
        assertEquals(TestRoute.Home, navigator.navBackStack[0])
    }

    @Test
    fun navigateToTopLevelRoute_clearsBackStackAndReplacesRoot() {
        val navigator = createNavigator(
            backStack = NavBackStack(
                mutableStateListOf(TestRoute.Home, TestRoute.Detail("1"))
            )
        )
        navigator.navigate(TestRoute.Settings)
        assertEquals(1, navigator.navBackStack.size)
        assertEquals(TestRoute.Settings, navigator.navBackStack[0])
    }

    @Test
    fun navigateToTopLevelRoute_clearsForwardStack() {
        val navigator = createNavigator(
            backStack = NavBackStack(
                mutableStateListOf(TestRoute.Home, TestRoute.Detail("1"))
            ),
            forwardStack = mutableStateListOf(TestRoute.Detail("2"))
        )
        navigator.navigate(TestRoute.Settings)
        assertEquals(0, navigator.navForwardStack.size)
    }

    @Test
    fun navigateToNonTopLevelRoute_pushesRoute() {
        val navigator = createNavigator()
        navigator.navigate(TestRoute.Detail("1"))
        assertEquals(2, navigator.navBackStack.size)
        assertEquals(TestRoute.Home, navigator.navBackStack[0])
        assertEquals(TestRoute.Detail("1"), navigator.navBackStack[1])
    }

    @Test
    fun navigateToNonTopLevelRoute_clearsForwardStack() {
        val navigator = createNavigator(
            backStack = NavBackStack(
                mutableStateListOf(TestRoute.Home, TestRoute.Detail("1"))
            ),
            forwardStack = mutableStateListOf(TestRoute.Detail("2"))
        )
        navigator.navigate(TestRoute.Detail("3"))
        assertEquals(0, navigator.navForwardStack.size)
    }

    @Test
    fun back_movesRouteToForwardStack() {
        val navigator = createNavigator(
            backStack = NavBackStack(
                mutableStateListOf(TestRoute.Home, TestRoute.Detail("1"))
            )
        )
        navigator.back()
        assertEquals(1, navigator.navBackStack.size)
        assertEquals(TestRoute.Home, navigator.navBackStack[0])
        assertEquals(1, navigator.navForwardStack.size)
        assertEquals(TestRoute.Detail("1"), navigator.navForwardStack[0])
    }

    @Test
    fun backWhenOnlyInitRoute_doesNothing() {
        val navigator = createNavigator()
        navigator.back()
        assertEquals(1, navigator.navBackStack.size)
        assertEquals(0, navigator.navForwardStack.size)
    }

    @Test
    fun forward_movesRouteToBackStack() {
        val navigator = createNavigator(
            backStack = NavBackStack(mutableStateListOf(TestRoute.Home)),
            forwardStack = mutableStateListOf(TestRoute.Detail("1"))
        )
        navigator.forward()
        assertEquals(2, navigator.navBackStack.size)
        assertEquals(TestRoute.Home, navigator.navBackStack[0])
        assertEquals(TestRoute.Detail("1"), navigator.navBackStack[1])
        assertEquals(0, navigator.navForwardStack.size)
    }

    @Test
    fun forwardWhenEmpty_doesNothing() {
        val navigator = createNavigator()
        navigator.forward()
        assertEquals(1, navigator.navBackStack.size)
        assertEquals(0, navigator.navForwardStack.size)
    }
}
