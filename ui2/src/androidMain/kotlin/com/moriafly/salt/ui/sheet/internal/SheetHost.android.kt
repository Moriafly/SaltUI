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

@file:Suppress("ktlint:standard:filename")
@file:OptIn(com.moriafly.salt.adaptive.window.UnstableSaltAdaptiveWindowApi::class)

package com.moriafly.salt.ui.sheet.internal

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.moriafly.salt.adaptive.window.SaltAdaptiveWindow
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.sheet.SheetDisplayMode
import com.moriafly.salt.ui.util.WindowUtil
import androidx.compose.ui.graphics.Color as ComposeColor

@Composable
@OptIn(UnstableSaltUiApi::class)
internal actual fun SheetHost(
    lease: SheetLease,
    inputEnabled: Boolean,
    displayMode: SheetDisplayMode,
    adaptiveWindow: SaltAdaptiveWindow,
    backgroundColor: ComposeColor,
    onDismissRequest: () -> Unit,
    content: @Composable (SheetHostEnvironment) -> Unit
) {
    val ownerSize = LocalWindowInfo.current.containerDpSize
    val ownerView = LocalView.current
    // Keep one owner snapshot per presentation, before the Dialog can change the visible insets.
    val ownerBarVisibility = remember(ownerView, lease) {
        SheetSystemBars.readOwnerVisibility(ownerView)
    }
    SheetOwnerScrim(ownerView = ownerView, coordinator = lease.coordinator)
    val bottom = displayMode.isBottom(
        adaptiveWindow = adaptiveWindow,
        windowSize = ownerSize
    )
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        val systemBars = remember(window, ownerBarVisibility) {
            window?.let { SheetSystemBars(window = it, ownerVisibility = ownerBarVisibility) }
        }
        val keyboard = LocalSoftwareKeyboardController.current
        val imeInsets = WindowInsets.ime
        val density = LocalDensity.current
        val layoutDirection = LocalLayoutDirection.current
        val navigationInsets = WindowInsets.navigationBars
        val darkNavigationBarIcons = backgroundColor.luminance() > 0.179f &&
            navigationInsets.getLeft(density, layoutDirection) == 0 &&
            navigationInsets.getRight(density, layoutDirection) == 0
        val dialogView = LocalView.current
        val coordinator = lease.coordinator
        val accessibilityImportance = remember(dialogView) { dialogView.importantForAccessibility }
        DisposableEffect(dialogView, lease) {
            val listener = object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(view: View) {
                    coordinator.updateWindowAttachment(lease = lease, attached = true)
                }

                override fun onViewDetachedFromWindow(view: View) {
                    coordinator.updateWindowAttachment(lease = lease, attached = false)
                }
            }
            coordinator.updateWindowAttachment(
                lease = lease,
                attached = dialogView.isAttachedToWindow
            )
            dialogView.addOnAttachStateChangeListener(listener)
            onDispose {
                dialogView.removeOnAttachStateChangeListener(listener)
                coordinator.updateWindowAttachment(lease = lease, attached = false)
            }
        }
        val hasVisibleSheet = inputEnabled || coordinator.hasVisibleSheet
        val replacementPending = hasVisibleSheet &&
            coordinator.activeLease?.windowAttached != true
        SideEffect {
            systemBars?.inheritOwnerVisibility()
            // The visual transition must not keep the closing dialog in the input path.
            window?.configureSheetInput(
                inputEnabled = inputEnabled,
                focusEnabled = inputEnabled || replacementPending
            )
            dialogView.importantForAccessibility = if (inputEnabled) {
                accessibilityImportance
            } else {
                View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            }
            window?.configureSheetWindow(
                inputEnabled = inputEnabled,
                hasVisibleSheet = hasVisibleSheet,
                ownerView = ownerView,
                bottom = bottom,
                darkNavigationBarIcons = darkNavigationBarIcons
            )
        }
        // Collection completes only on commit. Cancellation leaves the sheet unchanged.
        PredictiveBackHandler(enabled = inputEnabled) {
            it.collect { }
            if (imeInsets.getBottom(density = density) > 0 && keyboard != null) {
                keyboard.hide()
            } else {
                onDismissRequest()
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            content(
                sheetHostEnvironment(
                    windowSize = ownerSize,
                    adaptiveWindow = adaptiveWindow,
                    drawsScrim = false
                )
            )
        }
    }
}

private fun Window.configureSheetInput(
    inputEnabled: Boolean,
    focusEnabled: Boolean
) {
    val touchFlags = if (inputEnabled) {
        0
    } else {
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
    }
    val focusFlags = if (focusEnabled) 0 else WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
    val disabledFlags = touchFlags or focusFlags
    val inputMask = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
    if (attributes.flags and inputMask != disabledFlags) setFlags(disabledFlags, inputMask)
}

private data class SheetSystemBarAppearance(
    val darkStatusBarIcons: Boolean,
    val darkNavigationBarIcons: Boolean
)

@Suppress("DEPRECATION")
private fun View.readSheetSystemBarAppearance(): SheetSystemBarAppearance {
    if (Build.VERSION.SDK_INT >= 35) {
        val appearance = windowInsetsController?.systemBarsAppearance ?: 0
        return SheetSystemBarAppearance(
            darkStatusBarIcons =
                appearance and WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS != 0,
            darkNavigationBarIcons =
                appearance and WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS != 0
        )
    }

    // Compat appearance getters can mutate the owner on API 30–34, even with a Window.
    // Read the actual window's decor flags directly, matching WindowUtil's setters.
    val visibility = rootView.systemUiVisibility
    return SheetSystemBarAppearance(
        darkStatusBarIcons = visibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR != 0,
        darkNavigationBarIcons = visibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR != 0
    )
}

@Suppress("DEPRECATION")
@OptIn(UnstableSaltUiApi::class)
private fun Window.configureSheetWindow(
    inputEnabled: Boolean,
    hasVisibleSheet: Boolean,
    ownerView: View,
    bottom: Boolean,
    darkNavigationBarIcons: Boolean
) {
    // Native dialog transitions would also transform the full-window scrim.
    setWindowAnimations(0)
    setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT
    )
    clearFlags(
        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION or
            WindowManager.LayoutParams.FLAG_DIM_BEHIND
    )
    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    val ownerAppearance = ownerView.readSheetSystemBarAppearance()
    val useDarkStatusBarIcons = !hasVisibleSheet && ownerAppearance.darkStatusBarIcons
    val useDarkNavigationBarIcons = if (inputEnabled && bottom) {
        darkNavigationBarIcons
    } else {
        ownerAppearance.darkNavigationBarIcons
    }
    WindowUtil.setStatusBarForegroundColor(
        window = this,
        color = if (useDarkStatusBarIcons) {
            WindowUtil.BarColor.Black
        } else {
            WindowUtil.BarColor.White
        }
    )
    WindowUtil.setNavigationBarForegroundColor(
        window = this,
        color = if (useDarkNavigationBarIcons) {
            WindowUtil.BarColor.Black
        } else {
            WindowUtil.BarColor.White
        }
    )
    statusBarColor = Color.TRANSPARENT
    navigationBarColor = Color.TRANSPARENT
    if (Build.VERSION.SDK_INT >= 28) {
        attributes = attributes.apply {
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }
    if (Build.VERSION.SDK_INT >= 29) {
        isStatusBarContrastEnforced = false
        isNavigationBarContrastEnforced = false
    }
}
