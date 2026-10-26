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
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.window.layout.WindowMetricsCalculator

internal fun View.currentWindowMetricsSize(configuration: Configuration): DpSize {
    val state =
        getTag(R.id.salt_adaptive_window_metrics_state) as? AndroidWindowMetricsState
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
// AndroidView interop also dispatches global-layout callbacks during Compose measure/layout:
// https://github.com/androidx/androidx/commit/e09149a90454b9faabd1d2b41fed4d7d61f4b129
// Observe root geometry and the actual metrics source independently of the pending callback flag.
private class AndroidWindowMetricsState(private val view: View) :
    ComponentCallbacks,
    View.OnAttachStateChangeListener,
    ViewTreeObserver.OnGlobalLayoutListener,
    ViewTreeObserver.OnPreDrawListener {
    private val currentConfiguration = Configuration(view.resources.configuration)

    // Configuration callbacks can lead the Resources used by WindowMetrics. Keep an independent
    // copy of that source; Resources mutates its Configuration in place.
    private val windowMetricsConfiguration =
        Configuration((tryUnwrapContext(view.context) ?: view.context).resources.configuration)
    private var observer: ViewTreeObserver? = null
    private var pendingWindowInfoUpdate = false
    private var lastRootViewSize = IntSize.Zero
    private var deferOrientationDraw = false

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
        refreshSizeIfNeeded()
    }

    override fun onPreDraw(): Boolean {
        // Bounds can reach Resources after the last layout, without another public configuration
        // callback. This only compares cached inputs; unchanged frames do not query metrics.
        refreshSizeIfNeeded()
        if (deferOrientationDraw) {
            deferOrientationDraw = false
            // Layout has already finished. Publish the pending snapshot changes and cancel this
            // draw so the next frame can recompose and lay out with the new orientation.
            Snapshot.sendApplyNotifications()
            return false
        }
        return true
    }

    private fun refreshSizeIfNeeded() {
        val rootView = view.rootView
        val rootViewSize = IntSize(rootView.width, rootView.height)
        val windowMetricsContext = tryUnwrapContext(view.context)
        val configuration = (windowMetricsContext ?: view.context).resources.configuration
        // On API 30-32, Configuration comparison includes the bounds used by WindowMetrics.
        // Keep this source snapshot separate from configuration callbacks that may arrive earlier.
        val metricsSourceChanged = windowMetricsConfiguration != configuration
        if (metricsSourceChanged) {
            windowMetricsConfiguration.setTo(configuration)
        }
        // Root dimensions invalidate the cache but never replace the native window bounds.
        if (pendingWindowInfoUpdate || rootViewSize != lastRootViewSize || metricsSourceChanged) {
            pendingWindowInfoUpdate = false
            lastRootViewSize = rootViewSize
            updateSize(readSize(windowMetricsContext))
        }
    }

    private fun updateSize(newSize: DpSize) {
        val wasLandscape = size.width > size.height
        val isLandscape = newSize.width > newSize.height
        if (wasLandscape != isLandscape) {
            // Keep the flag until pre-draw: global layout may publish the change first.
            // Restrict draw cancellation to orientation changes so continuous resizing can draw.
            deferOrientationDraw = true
        }
        size = newSize
    }

    override fun onConfigurationChanged(configuration: Configuration) {
        val changedFlags = currentConfiguration.updateFrom(configuration)
        if (changedFlags and MASK_FOR_NON_WINDOW_METRICS_CHANGES.inv() != 0) {
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
        observer = view.viewTreeObserver.also {
            it.addOnGlobalLayoutListener(this)
            it.addOnPreDrawListener(this)
        }
        updateSize(readSize())
    }

    private fun stopObservingLayout() {
        observer?.let {
            if (it.isAlive) {
                it.removeOnGlobalLayoutListener(this)
                it.removeOnPreDrawListener(this)
            }
            view.context.unregisterComponentCallbacks(this)
        }
        observer = null
        pendingWindowInfoUpdate = false
        lastRootViewSize = IntSize.Zero
        deferOrientationDraw = false
    }

    // Match AndroidWindowInfo.calculateWindowSize, including pre-30 window metrics and the
    // unsupported-context fallback. Display metrics alone would break split-screen windows.
    private fun readSize(unwrapped: Context? = tryUnwrapContext(view.context)): DpSize {
        val context = view.context
        return if (unwrapped != null) {
            val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(
                unwrapped
            )
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
private const val MASK_FOR_NON_WINDOW_METRICS_CHANGES =
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
