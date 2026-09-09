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

package com.moriafly.salt.ui.sample.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEvent.SwipeEdge
import com.moriafly.salt.ui.sample.ui.screen.AboutScreen
import com.moriafly.salt.ui.sample.ui.screen.ButtonScreen
import com.moriafly.salt.ui.sample.ui.screen.ComponentScreen
import com.moriafly.salt.ui.sample.ui.screen.DialogScreen
import com.moriafly.salt.ui.sample.ui.screen.LicenseScreen
import com.moriafly.salt.ui.sample.ui.screen.ListScreen
import com.moriafly.salt.ui.sample.ui.screen.MainScreen
import com.moriafly.salt.ui.sample.ui.screen.MaterialScreen

val LocalNavBackStack = compositionLocalOf<NavBackStack<NavKey>> {
    error("LocalNavBackStack is not provided")
}

@Composable
fun AppNavigation(
    navBackStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier
) {
    NavDisplay(
        backStack = navBackStack,
        modifier = modifier
            .clipToBounds(),
        onBack = {
            navBackStack.removeLastOrNull()
        },
        transitionSpec = {
            ContentTransform(
                targetContentEnter = EnterTransition.None,
                initialContentExit = ExitTransition.None
            )
        },
        popTransitionSpec = {
            ContentTransform(
                targetContentEnter = EnterTransition.None,
                initialContentExit = ExitTransition.None
            )
        },
        predictivePopTransitionSpec = {
            ContentTransform(
                targetContentEnter = EnterTransition.None,
                initialContentExit = ExitTransition.None
            )
        },
        entryProvider = entryProvider {
            entry<ScreenRoute.Main>(
                metadata =
                    NavDisplay.transitionSpec {
                        ContentTransform(
                            targetContentEnter = EnterTransition.None,
                            initialContentExit = ExitTransition.None
                        )
                    } +
                        NavDisplay.popTransitionSpec {
                            ContentTransform(
                                targetContentEnter = EnterTransition.None,
                                initialContentExit = ExitTransition.None
                            )
                        } +
                        NavDisplay.predictivePopTransitionSpec {
                            ContentTransform(
                                targetContentEnter = EnterTransition.None,
                                initialContentExit = ExitTransition.None
                            )
                        }
            ) {
                MainScreen()
            }
            entry<ScreenRoute.About>(
                metadata =
                    NavDisplay.transitionSpec {
                        ContentTransform(
                            targetContentEnter = EnterTransition.None,
                            initialContentExit = ExitTransition.None
                        )
                    } +
                        NavDisplay.popTransitionSpec {
                            ContentTransform(
                                targetContentEnter = EnterTransition.None,
                                initialContentExit = ExitTransition.None
                            )
                        } +
                        NavDisplay.predictivePopTransitionSpec {
                            ContentTransform(
                                targetContentEnter = EnterTransition.None,
                                initialContentExit = ExitTransition.None
                            )
                        }
            ) {
                AboutScreen()
            }
            entry<ScreenRoute.License> { LicenseScreen() }
            entry<ScreenRoute.List> { ListScreen() }
            entry<ScreenRoute.Button> { ButtonScreen() }
            entry<ScreenRoute.Component> { ComponentScreen() }
            entry<ScreenRoute.Dialog> { DialogScreen() }
            entry<ScreenRoute.Material> { MaterialScreen() }
        }
    )
}

private fun <T : Any> defaultTransitionSpec():
    AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    ContentTransform(
        targetContentEnter = slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(300)
        ),
        initialContentExit = slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(300)
        )
    )
}

private fun <T : Any> defaultPopTransitionSpec():
    AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    ContentTransform(
        targetContentEnter = slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(300)
        ),
        initialContentExit = slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(300)
        )
    )
}

private fun <T : Any> defaultPredictivePopTransitionSpec():
    AnimatedContentTransitionScope<Scene<T>>.(@SwipeEdge Int) -> ContentTransform = {
    ContentTransform(
        targetContentEnter = slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(300)
        ),
        initialContentExit = slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(300)
        )
    )
}
