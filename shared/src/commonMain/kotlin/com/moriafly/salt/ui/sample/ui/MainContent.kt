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

package com.moriafly.salt.ui.sample.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.Layer
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.bar.FloatingBottomBar
import com.moriafly.salt.ui.bar.FloatingBottomBarItem
import com.moriafly.salt.ui.ext.safeMainIgnoringVisibility
import com.moriafly.salt.ui.material.glassSource
import com.moriafly.salt.ui.material.rememberGlassSource
import com.moriafly.salt.ui.navigation.SaltNavigator
import com.moriafly.salt.ui.navigation.rememberSaltNavigator
import com.moriafly.salt.ui.pager.HorizontalPager
import com.moriafly.salt.ui.pager.rememberPagerState
import com.moriafly.salt.ui.popup.PopupMenuBackdrop
import com.moriafly.salt.ui.sample.ui.navigation.AppNavigation
import com.moriafly.salt.ui.sample.ui.navigation.LocalSaltNavigator
import com.moriafly.salt.ui.sample.ui.navigation.ScreenRoute
import com.moriafly.salt.ui.sample.ui.screen.ItemsScreen
import com.moriafly.salt.ui.sample.ui.screen.MainScreen
import com.moriafly.salt.ui.sample.ui.screen.MaterialScreen
import com.moriafly.salt.ui.screen.BasicScreenDefaults
import kotlinx.coroutines.launch
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import saltui.composeapp.generated.resources.Res
import saltui.composeapp.generated.resources.ic_nav_home
import saltui.composeapp.generated.resources.ic_nav_home_filled
import saltui.composeapp.generated.resources.ic_nav_items
import saltui.composeapp.generated.resources.ic_nav_items_filled
import saltui.composeapp.generated.resources.ic_nav_theme
import saltui.composeapp.generated.resources.ic_nav_theme_filled

private enum class BottomBarDestination(
    val route: ScreenRoute,
    val label: String,
    val icon: DrawableResource,
    val selectedIcon: DrawableResource
) {
    Home(
        route = ScreenRoute.Main,
        label = "Home",
        icon = Res.drawable.ic_nav_home,
        selectedIcon = Res.drawable.ic_nav_home_filled
    ),
    Items(
        route = ScreenRoute.Items,
        label = "Items",
        icon = Res.drawable.ic_nav_items,
        selectedIcon = Res.drawable.ic_nav_items_filled
    ),
    Theme(
        route = ScreenRoute.Material,
        label = "Theme",
        icon = Res.drawable.ic_nav_theme,
        selectedIcon = Res.drawable.ic_nav_theme_filled
    )
}

private val topLevelRoutes = BottomBarDestination.entries.map { it.route }.toSet()
private val bottomBarHeight = 56.dp
private val bottomBarMargin = 12.dp

private val navigatorConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(ScreenRoute.Main::class, ScreenRoute.Main.serializer())
            subclass(ScreenRoute.About::class, ScreenRoute.About.serializer())
            subclass(ScreenRoute.License::class, ScreenRoute.License.serializer())
            subclass(ScreenRoute.List::class, ScreenRoute.List.serializer())
            subclass(ScreenRoute.Button::class, ScreenRoute.Button.serializer())
            subclass(ScreenRoute.Component::class, ScreenRoute.Component.serializer())
            subclass(ScreenRoute.Items::class, ScreenRoute.Items.serializer())
            subclass(ScreenRoute.Layouts::class, ScreenRoute.Layouts.serializer())
            subclass(ScreenRoute.CollapsingHeader::class, ScreenRoute.CollapsingHeader.serializer())
            subclass(ScreenRoute.NestedCollapsed::class, ScreenRoute.NestedCollapsed.serializer())
            subclass(ScreenRoute.BottomSheet::class, ScreenRoute.BottomSheet.serializer())
            subclass(ScreenRoute.Dialog::class, ScreenRoute.Dialog.serializer())
            subclass(ScreenRoute.Sheet::class, ScreenRoute.Sheet.serializer())
            subclass(ScreenRoute.Material::class, ScreenRoute.Material.serializer())
            subclass(ScreenRoute.Glass::class, ScreenRoute.Glass.serializer())
            subclass(ScreenRoute.PopupMenus::class, ScreenRoute.PopupMenus.serializer())
        }
    }
}

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun MainContent(
    windowCaptionBarHeight: Dp = 0.dp,
    revealWindowBackdrop: Boolean = false
) {
    val navigator = rememberSaltNavigator(
        configuration = navigatorConfig,
        initRoute = ScreenRoute.Main,
        topLevelRoutes = topLevelRoutes
    )

    CompositionLocalProvider(
        LocalSaltNavigator provides navigator
    ) {
        Layer(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = windowCaptionBarHeight)
                .testTag("mainContentLayer"),
            decorationEnabled = OS.isDesktop()
        ) {
            PopupMenuBackdrop(
                modifier = Modifier.fillMaxSize(),
                revealWindowBackdrop = revealWindowBackdrop
            ) {
                AppNavigation(
                    navigator = navigator,
                    modifier = Modifier.fillMaxSize()
                ) {
                    TopLevelNavigation(navigator)
                }
            }
        }
    }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun TopLevelNavigation(navigator: SaltNavigator) {
    val selectedRoute = navigator.navBackStack.first()
    val selectedDestination = BottomBarDestination.entries.first { it.route == selectedRoute }
    val pagerState = rememberPagerState(initialPage = selectedDestination.ordinal) {
        BottomBarDestination.entries.size
    }
    val scope = rememberCoroutineScope()
    val homeGlassSource = rememberGlassSource()
    val bottomBarInsets = WindowInsets.safeMainIgnoringVisibility
        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
        .asPaddingValues()
    val topLevelContentPadding = BasicScreenDefaults.ContentPadding +
        PaddingValues(bottom = bottomBarHeight + bottomBarMargin)

    LaunchedEffect(navigator, pagerState) {
        var synchronizedDestination: BottomBarDestination? = null
        snapshotFlow {
            val backStack = navigator.navBackStack
            Triple(backStack.first(), backStack.size == 1, pagerState.settledPage)
        }.collect { (topLevelRoute, isTopLevelVisible, settledPage) ->
            val destination = BottomBarDestination.entries.first { it.route == topLevelRoute }
            when {
                !isTopLevelVisible || destination != synchronizedDestination -> {
                    // Follow the navigator when a route changes or the pager is behind a detail.
                    synchronizedDestination = destination
                    if (pagerState.isScrollInProgress ||
                        settledPage != destination.ordinal ||
                        pagerState.currentPageOffsetFraction != 0f
                    ) {
                        pagerState.requestScrollToPage(destination.ordinal)
                    }
                }

                settledPage != destination.ordinal -> {
                    val settledDestination = BottomBarDestination.entries[settledPage]
                    synchronizedDestination = settledDestination
                    navigator.navigate(settledDestination.route)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .glassSource(homeGlassSource),
            key = { BottomBarDestination.entries[it].name }
        ) { page ->
            when (BottomBarDestination.entries[page]) {
                BottomBarDestination.Home -> MainScreen(contentPadding = topLevelContentPadding)
                BottomBarDestination.Items -> ItemsScreen(contentPadding = topLevelContentPadding)
                BottomBarDestination.Theme -> MaterialScreen(
                    contentPadding = topLevelContentPadding
                )
            }
        }

        FloatingBottomBar(
            glassSource = homeGlassSource,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottomBarInsets)
                .padding(
                    start = bottomBarMargin,
                    end = bottomBarMargin,
                    bottom = bottomBarMargin
                )
                .height(bottomBarHeight)
        ) {
            BottomBarDestination.entries.forEach { destination ->
                val selected = selectedDestination == destination
                val icon = if (selected) destination.selectedIcon else destination.icon
                FloatingBottomBarItem(
                    selected = selected,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(destination.ordinal)
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = null
                        )
                    }
                ) {
                    Text(text = destination.label)
                }
            }
        }
    }
}
