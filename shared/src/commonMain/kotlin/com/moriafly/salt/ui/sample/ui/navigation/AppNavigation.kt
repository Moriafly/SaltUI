/*
 * Salt UI
 * Copyright (C) 2026 Moriafly
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package com.moriafly.salt.ui.sample.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEvent.SwipeEdge
import com.moriafly.salt.ui.sample.ui.screen.AboutScreen
import com.moriafly.salt.ui.sample.ui.screen.LicenseScreen
import com.moriafly.salt.ui.sample.ui.screen.ListScreen
import com.moriafly.salt.ui.sample.ui.screen.MainScreen

val LocalNavBackStack = compositionLocalOf<NavBackStack<NavKey>> {
    error("LocalNavBackStack is not provided")
}

@Composable
fun AppNavigation(
    navBackStack: NavBackStack<NavKey>
) {
    NavDisplay(
        backStack = navBackStack,
        onBack = {
            navBackStack.removeLastOrNull()
        },
        transitionSpec = defaultTransitionSpec(),
        popTransitionSpec = defaultPopTransitionSpec(),
        predictivePopTransitionSpec = defaultPredictivePopTransitionSpec(),
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
