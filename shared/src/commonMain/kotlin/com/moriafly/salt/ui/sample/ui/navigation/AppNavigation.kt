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

package com.moriafly.salt.ui.sample.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.unveilIn
import androidx.compose.animation.veilOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEvent.Companion.EDGE_LEFT
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.navigation.SaltNavigator
import com.moriafly.salt.ui.sample.ui.screen.AboutScreen
import com.moriafly.salt.ui.sample.ui.screen.BottomSheetScreen
import com.moriafly.salt.ui.sample.ui.screen.ButtonScreen
import com.moriafly.salt.ui.sample.ui.screen.CollapsingHeaderScreen
import com.moriafly.salt.ui.sample.ui.screen.ComponentScreen
import com.moriafly.salt.ui.sample.ui.screen.DialogScreen
import com.moriafly.salt.ui.sample.ui.screen.GlassScreen
import com.moriafly.salt.ui.sample.ui.screen.LayoutsScreen
import com.moriafly.salt.ui.sample.ui.screen.LicenseScreen
import com.moriafly.salt.ui.sample.ui.screen.ListScreen
import com.moriafly.salt.ui.sample.ui.screen.NestedCollapsedScreen
import com.moriafly.salt.ui.sample.ui.screen.PopupMenuScreen
import com.moriafly.salt.ui.sample.ui.screen.SheetScreen

// Use Voxzen's iOS navigation motion on all Sample targets.
private const val NAVIGATION_TRANSITION_DURATION_MILLIS = 500
private val NavigationTransitionEasing = CubicBezierEasing(0.2833f, 0.99f, 0.31833f, 0.99f)
private val NavigationDimmingColor = Color.Black.copy(alpha = 0.1f)

@OptIn(UnstableSaltUiApi::class)
val LocalSaltNavigator = staticCompositionLocalOf<SaltNavigator> {
    error("LocalSaltNavigator is not provided")
}

@OptIn(UnstableSaltUiApi::class)
@Composable
internal fun AppNavigation(
    navigator: SaltNavigator,
    transparentTopLevelBackground: Boolean,
    modifier: Modifier = Modifier,
    topLevelContent: @Composable () -> Unit
) {
    val backStack = navigator.navBackStack
    val restoredTopLevelIndex = backStack.indexOfLast { it in navigator.topLevelRoutes }
    if (restoredTopLevelIndex > 0) {
        // Older Sample versions stored these tabs above Home.
        LaunchedEffect(navigator, restoredTopLevelIndex) {
            backStack.subList(0, restoredTopLevelIndex).clear()
        }
        return
    }

    val currentTopLevelContent by rememberUpdatedState(topLevelContent)
    // Keep the pager in one entry while the navigator replaces its logical root.
    val displayBackStack = backStack.mapIndexed { index, route ->
        if (index == 0) ScreenRoute.Main else route
    }
    val sceneDecorators = remember(transparentTopLevelBackground) {
        listOf(
            SceneDecoratorStrategy<NavKey> { scene ->
                val isTopLevel = scene.previousEntries.isEmpty()
                NavigationPageScene(
                    source = scene,
                    transparentBackground = transparentTopLevelBackground && isTopLevel
                )
            }
        )
    }

    NavDisplay(
        backStack = displayBackStack,
        modifier = modifier.clipToBounds(),
        onBack = {
            navigator.back()
        },
        sceneDecoratorStrategies = sceneDecorators,
        transitionSpec = {
            pushTransition()
        },
        popTransitionSpec = {
            popTransition(
                direction = SlideDirection.Right,
                easing = NavigationTransitionEasing
            )
        },
        predictivePopTransitionSpec = { swipeEdge ->
            val direction = if (swipeEdge == EDGE_LEFT) {
                SlideDirection.Right
            } else {
                SlideDirection.Left
            }
            popTransition(
                direction = direction,
                easing = LinearEasing
            )
        },
        entryProvider = entryProvider {
            entry<ScreenRoute.Main> { currentTopLevelContent() }
            entry<ScreenRoute.About> { AboutScreen() }
            entry<ScreenRoute.License> { LicenseScreen() }
            entry<ScreenRoute.List> { ListScreen() }
            entry<ScreenRoute.Button> { ButtonScreen() }
            entry<ScreenRoute.Component> { ComponentScreen() }
            entry<ScreenRoute.Layouts> { LayoutsScreen() }
            entry<ScreenRoute.CollapsingHeader> { CollapsingHeaderScreen() }
            entry<ScreenRoute.NestedCollapsed> { NestedCollapsedScreen() }
            entry<ScreenRoute.BottomSheet> { BottomSheetScreen() }
            entry<ScreenRoute.Dialog> { DialogScreen() }
            entry<ScreenRoute.Sheet> { SheetScreen() }
            entry<ScreenRoute.Glass> { GlassScreen() }
            entry<ScreenRoute.PopupMenus> { PopupMenuScreen() }
        }
    )
}

private data class NavigationPageScene(
    private val source: Scene<NavKey>,
    private val transparentBackground: Boolean
) : Scene<NavKey> by source {
    override val key: Any = source::class to source.key

    override val content: @Composable () -> Unit = {
        val backgroundColor = if (transparentBackground) {
            Color.Transparent
        } else {
            SaltTheme.colors.background
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            source.content()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun AnimatedContentTransitionScope<*>.pushTransition(): ContentTransform {
    val slideSpec = tween<IntOffset>(
        durationMillis = NAVIGATION_TRANSITION_DURATION_MILLIS,
        easing = NavigationTransitionEasing
    )
    return ContentTransform(
        targetContentEnter = slideIntoContainer(
            towards = SlideDirection.Left,
            animationSpec = slideSpec
        ),
        initialContentExit = slideOutOfContainer(
            towards = SlideDirection.Left,
            animationSpec = slideSpec,
            targetOffset = { it / 4 }
        ) + veilOut(
            animationSpec = tween(
                durationMillis = NAVIGATION_TRANSITION_DURATION_MILLIS,
                easing = NavigationTransitionEasing
            ),
            targetColor = NavigationDimmingColor
        )
    )
}

@OptIn(ExperimentalAnimationApi::class)
private fun AnimatedContentTransitionScope<*>.popTransition(
    direction: SlideDirection,
    easing: Easing
): ContentTransform {
    val slideSpec = tween<IntOffset>(
        durationMillis = NAVIGATION_TRANSITION_DURATION_MILLIS,
        easing = easing
    )
    return ContentTransform(
        targetContentEnter = slideIntoContainer(
            towards = direction,
            animationSpec = slideSpec,
            initialOffset = { it / 4 }
        ) + unveilIn(
            animationSpec = tween(
                durationMillis = NAVIGATION_TRANSITION_DURATION_MILLIS,
                easing = easing
            ),
            initialColor = NavigationDimmingColor
        ),
        initialContentExit = slideOutOfContainer(
            towards = direction,
            animationSpec = slideSpec
        )
    )
}
