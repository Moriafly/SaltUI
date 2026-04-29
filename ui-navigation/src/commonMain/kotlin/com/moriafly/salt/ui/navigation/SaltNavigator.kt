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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * # Salt Navigator
 */
@Stable
class SaltNavigator(
    val topLevelRoutes: Set<NavKey>,
    val navBackStack: NavBackStack<NavKey>,
    val navForwardStack: SnapshotStateList<NavKey>
) {
    fun navigate(route: NavKey) {
        // 跳转的页面和当前页面相同则返回
        if (route == navBackStack.lastOrNull()) {
            return
        }

        if (navForwardStack.isNotEmpty()) {
            navForwardStack.clear()
        }

        if (route in topLevelRoutes) {
            navBackStack.subList(1, navBackStack.size).clear()
            navBackStack[0] = route
        } else {
            navBackStack.add(route)
        }
    }

    fun back() {
        if (navBackStack.size > 1) {
            val poppedRoute = navBackStack.removeLastOrNull()
            if (poppedRoute != null) {
                navForwardStack.add(poppedRoute)
            }
        }
    }

    fun forward() {
        if (navForwardStack.isNotEmpty()) {
            val route = navForwardStack.removeLastOrNull()
            if (route != null) {
                navBackStack.add(route)
            }
        }
    }
}

@Composable
fun rememberSaltNavigator(
    configuration: SavedStateConfiguration,
    initRoute: NavKey,
    topLevelRoutes: Set<NavKey>
): SaltNavigator {
    require(initRoute in topLevelRoutes) {
        "initRoute must be in topLevelRoutes"
    }

    val navBackStack = rememberNavBackStack(configuration, initRoute)
    val navForwardStack =
        rememberSaveable(
            saver = NavKeyListSaver(configuration)
        ) {
            mutableStateListOf()
        }
    return remember(navBackStack) {
        SaltNavigator(
            topLevelRoutes = topLevelRoutes,
            navBackStack = navBackStack,
            navForwardStack = navForwardStack
        )
    }
}

private class NavKeyListSaver(
    private val configuration: SavedStateConfiguration
) : Saver<SnapshotStateList<NavKey>, String> {
    private val json = Json {
        serializersModule = configuration.serializersModule
        encodeDefaults = configuration.encodeDefaults
    }

    private val serializer = ListSerializer(PolymorphicSerializer(NavKey::class))

    override fun restore(value: String): SnapshotStateList<NavKey> =
        json.decodeFromString(serializer, value).toMutableStateList()

    override fun SaverScope.save(value: SnapshotStateList<NavKey>): String =
        json.encodeToString(serializer, value.toList())
}
