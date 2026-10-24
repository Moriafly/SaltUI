/*
 * Copyright 2024-2025 The Android Open Source Project
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

package com.moriafly.salt.adaptive.window

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.window.layout.WindowMetricsCalculator

internal fun View.currentWindowMetricsSize(configuration: Configuration): DpSize {
    val state =
        (getTag(R.id.salt_adaptive_window_metrics_state) as? AndroidWindowMetricsState)
            ?: AndroidWindowMetricsState(this).also {
                setTag(R.id.salt_adaptive_window_metrics_state, it)
            }
    state.onConfigurationChanged(configuration)
    return state.size
}

// Backport of the pending-update/global-layout mechanism from AndroidX ComposeViewContext:
// https://android.googlesource.com/platform/frameworks/support/+/48413c3f127790e362bd05cc72dbc1ac22628a3b
// The View tag replaces ComposeViewContext ownership; listeners are removed on detach and restored
// on reattach. No global references retain the View or its Activity.
private class AndroidWindowMetricsState(private val view: View) :
    ComponentCallbacks,
    View.OnAttachStateChangeListener,
    ViewTreeObserver.OnGlobalLayoutListener {
    private val currentConfiguration = Configuration(view.resources.configuration)
    private var observer: ViewTreeObserver? = null
    private var pendingWindowInfoUpdate = false

    var size by mutableStateOf(readSize())
        private set

    init {
        view.addOnAttachStateChangeListener(this)
        if (view.isAttachedToWindow) observeLayout()
    }

    override fun onViewAttachedToWindow(view: View) {
        observeLayout()
    }

    override fun onViewDetachedFromWindow(view: View) {
        stopObservingLayout()
    }

    override fun onGlobalLayout() {
        if (pendingWindowInfoUpdate) {
            pendingWindowInfoUpdate = false
            size = readSize()
        }
    }

    override fun onConfigurationChanged(configuration: Configuration) {
        val changedFlags = currentConfiguration.updateFrom(configuration)
        if (changedFlags and MaskForNonWindowMetricsChanges.inv() != 0) {
            // Do not query bounds here: API <= 32 can still report the old window frame.
            pendingWindowInfoUpdate = true
        }
    }

    @Deprecated("This callback is superseded by onTrimMemory")
    override fun onLowMemory() {
        // This observer owns no resource caches.
    }

    private fun observeLayout() {
        stopObservingLayout()
        view.context.registerComponentCallbacks(this)
        observer = view.viewTreeObserver.also { it.addOnGlobalLayoutListener(this) }
        size = readSize()
    }

    private fun stopObservingLayout() {
        observer?.let {
            if (it.isAlive) it.removeOnGlobalLayoutListener(this)
            view.context.unregisterComponentCallbacks(this)
        }
        observer = null
        pendingWindowInfoUpdate = false
    }

    // Match AndroidWindowInfo.calculateWindowSize, including pre-30 window metrics and the
    // unsupported-context fallback. Display metrics alone would break split-screen windows.
    private fun readSize(): DpSize {
        val context = view.context
        val unwrapped = tryUnwrapContext(context)
        return if (unwrapped != null) {
            val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(unwrapped)
            with(Density(unwrapped)) {
                IntSize(metrics.bounds.width(), metrics.bounds.height()).toSize().toDpSize()
            }
        } else {
            val configuration = context.resources.configuration
            DpSize(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp)
        }
    }
}

// Copied from AndroidWindowInfo: unwrap supported contexts before calling WindowMetricsCalculator
// to retain its workarounds for b/449386176 and b/449389108.
private fun tryUnwrapContext(context: Context): Context? {
    var iterator = context
    while (iterator is ContextWrapper) {
        if (iterator is Activity || iterator is InputMethodService || iterator is Application) {
            return iterator
        } else if (iterator.baseContext == null) {
            return null
        }
        iterator = iterator.baseContext
    }
    return null
}

// Copied from ComposeViewContext: changes excluded here cannot affect window metrics.
private const val MaskForNonWindowMetricsChanges =
    ActivityInfo.CONFIG_MCC or
        ActivityInfo.CONFIG_MNC or
        ActivityInfo.CONFIG_LOCALE or
        ActivityInfo.CONFIG_TOUCHSCREEN or
        ActivityInfo.CONFIG_KEYBOARD or
        ActivityInfo.CONFIG_KEYBOARD_HIDDEN or
        ActivityInfo.CONFIG_NAVIGATION or
        ActivityInfo.CONFIG_UI_MODE or
        ActivityInfo.CONFIG_LAYOUT_DIRECTION or
        ActivityInfo.CONFIG_FONT_SCALE or
        ActivityInfo.CONFIG_COLOR_MODE or
        ActivityInfo.CONFIG_GRAMMATICAL_GENDER or
        ActivityInfo.CONFIG_FONT_WEIGHT_ADJUSTMENT or
        ActivityInfo.CONFIG_ASSETS_PATHS
