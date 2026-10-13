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

package com.moriafly.salt.ui.sheet.internal

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.moriafly.salt.ui.sheet.SheetPresentation
import androidx.compose.ui.graphics.Color as ComposeColor

@Composable
@OptIn(com.moriafly.salt.ui.UnstableSaltUiApi::class)
internal actual fun SheetHost(
    inputEnabled: Boolean,
    presentation: SheetPresentation,
    backgroundColor: ComposeColor,
    onDismissRequest: () -> Unit,
    content: @Composable (SheetHostEnvironment) -> Unit
) {
    val ownerSize = LocalWindowInfo.current.containerDpSize
    val ownerView = LocalView.current
    val bottom = presentation.isBottom(windowSize = ownerSize)
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
        val keyboard = LocalSoftwareKeyboardController.current
        val imeInsets = WindowInsets.ime
        val density = LocalDensity.current
        val dialogView = LocalView.current
        val accessibilityImportance = remember(dialogView) { dialogView.importantForAccessibility }
        SideEffect {
            dialogView.importantForAccessibility = if (inputEnabled) {
                accessibilityImportance
            } else {
                View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            }
            window?.configureSheetWindow(
                inputEnabled = inputEnabled,
                ownerView = ownerView,
                bottom = bottom,
                lightBackground = backgroundColor.luminance() > 0.179f
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
                    windowSize = ownerSize
                )
            )
        }
    }
}

private fun Window.configureSheetWindow(
    inputEnabled: Boolean,
    ownerView: View,
    bottom: Boolean,
    lightBackground: Boolean
) {
    // Native dialog transitions would also transform the full-window scrim.
    setWindowAnimations(0)
    setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT
    )
    clearFlags(
        WindowManager.LayoutParams.FLAG_DIM_BEHIND or
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
    )
    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    val inputFlags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
    setFlags(if (inputEnabled) 0 else inputFlags, inputFlags)
    @Suppress("DEPRECATION")
    val ownerController = ViewCompat.getWindowInsetsController(ownerView)
    WindowCompat.getInsetsController(this, decorView).apply {
        isAppearanceLightStatusBars = !inputEnabled &&
            ownerController?.isAppearanceLightStatusBars == true
        isAppearanceLightNavigationBars = if (inputEnabled && bottom) {
            lightBackground
        } else {
            ownerController?.isAppearanceLightNavigationBars == true
        }
    }
    @Suppress("DEPRECATION")
    statusBarColor = Color.TRANSPARENT
    @Suppress("DEPRECATION")
    navigationBarColor = Color.TRANSPARENT
    if (Build.VERSION.SDK_INT >= 28) {
        attributes = attributes.apply {
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }
    if (Build.VERSION.SDK_INT >= 29) {
        @Suppress("DEPRECATION")
        isStatusBarContrastEnforced = false
        isNavigationBarContrastEnforced = false
    }
}
