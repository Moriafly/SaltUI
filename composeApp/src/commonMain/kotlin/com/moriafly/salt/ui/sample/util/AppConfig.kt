package com.moriafly.salt.ui.sample.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AppConfig {
    var isDarkTheme by mutableStateOf(false)
        private set

    fun updateIsDarkTheme(value: Boolean) {
        isDarkTheme = value
    }
}
