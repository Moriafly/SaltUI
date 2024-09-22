package com.moriafly.salt.ui.ext

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import com.moriafly.salt.ui.UnstableSaltApi

/**
 * Same as [safeMain]
 */
@UnstableSaltApi
actual val WindowInsets.Companion.safeMainIgnoringVisibility: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = WindowInsets.systemBars.union(WindowInsets.displayCutout)