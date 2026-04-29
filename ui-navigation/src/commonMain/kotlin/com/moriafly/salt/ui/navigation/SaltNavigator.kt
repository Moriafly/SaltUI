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
import androidx.compose.runtime.toMutableStateList
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import com.moriafly.salt.ui.UnstableSaltUiApi
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * A stateful navigator that manages a bidirectional navigation stack.
 *
 * [SaltNavigator] maintains two cooperative stacks:
 * - **Back stack** — the primary history of visited routes. The last element represents the
 *   currently visible screen.
 * - **Forward stack** — a secondary history of routes that were previously popped. It enables
 *   browser-style forward navigation.
 *
 * When navigating to a [topLevelRoutes], the back stack collapses to a single element and the
 * root is replaced, matching the behavior of bottom-bar or sidebar navigation.
 *
 * All mutations to the forward stack are observable by Compose, allowing UI to react to
 * changes in navigation availability (for example, showing or hiding a forward button).
 *
 * @property topLevelRoutes Routes that, when navigated to, reset the back stack to a single
 * element. Typically used for primary destinations in a navigation bar.
 * @property navBackStack The primary navigation history. Managed externally by
 * [rememberNavBackStack] for SaveState support.
 * @property navForwardStack The secondary forward history. Automatically saved and restored
 * across configuration changes via [rememberSaveable].
 */
@UnstableSaltUiApi
@Stable
class SaltNavigator(
    val topLevelRoutes: Set<NavKey>,
    val navBackStack: NavBackStack<NavKey>,
    val navForwardStack: SnapshotStateList<NavKey>
) {
    /**
     * Navigates to [route].
     *
     * - If [route] is identical to the current top of the back stack, this call is a no-op.
     * - If [route] is a [topLevelRoutes], the back stack is cleared (keeping only the root)
     *   and the root is replaced with [route]. The forward stack is also cleared.
     * - Otherwise, [route] is pushed onto the back stack and the forward stack is cleared.
     */
    fun navigate(route: NavKey) {
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

    /**
     * Pops the current route from the back stack and pushes it onto the forward stack.
     *
     * Has no effect if the back stack contains only one element (the initial route).
     */
    fun back() {
        if (navBackStack.size > 1) {
            val poppedRoute = navBackStack.removeLastOrNull()
            if (poppedRoute != null) {
                navForwardStack.add(poppedRoute)
            }
        }
    }

    /**
     * Pops the most recent route from the forward stack and pushes it back onto the back stack.
     *
     * Has no effect if the forward stack is empty.
     */
    fun forward() {
        if (navForwardStack.isNotEmpty()) {
            val route = navForwardStack.removeLastOrNull()
            if (route != null) {
                navBackStack.add(route)
            }
        }
    }
}

/**
 * Creates and remembers a [SaltNavigator] that survives configuration changes.
 *
 * The back stack is automatically saved and restored via [rememberNavBackStack], while the
 * forward stack is persisted through [rememberSaveable] using a custom [NavKeyListSaver].
 *
 * @param configuration The [SavedStateConfiguration] used for both the back stack and the
 * forward stack serialization. Must include polymorphic serializers for all [NavKey]
 * implementations that will be pushed onto either stack.
 * @param initRoute The initial route. Must be present in [topLevelRoutes].
 * @param topLevelRoutes The set of routes that should reset the back stack when navigated to.
 * @throws IllegalArgumentException if [initRoute] is not in [topLevelRoutes].
 */
@UnstableSaltUiApi
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

/**
 * A [Saver] that serializes a list of [NavKey] elements to a JSON string.
 *
 * Uses the [SavedStateConfiguration] to ensure consistent polymorphic serialization with
 * [rememberNavBackStack]. Both the [SavedStateConfiguration.serializersModule] and
 * [SavedStateConfiguration.encodeDefaults] settings are replicated so that the forward stack
 * encodes and decodes in exactly the same way as the back stack.
 */
internal class NavKeyListSaver(
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
