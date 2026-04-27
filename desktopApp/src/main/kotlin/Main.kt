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

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.application
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.sample.ui.AppContent
import com.moriafly.salt.ui.sample.util.AppConfig
import com.moriafly.salt.ui.window.CaptionBarHitTest
import com.moriafly.salt.ui.window.SaltWindow
import com.moriafly.salt.ui.window.SaltWindowProperties

@OptIn(ExperimentalComposeUiApi::class, UnstableSaltUiApi::class)
fun main() = application {
    SaltWindow(
        onCloseRequest = ::exitApplication,
        title = "Salt UI",
        resizable = true,
        properties = SaltWindowProperties.default(
            captionButtonIsDarkTheme = AppConfig.isDarkTheme,
            backgroundIsDarkTheme = AppConfig.isDarkTheme
        )
    ) {
        CaptionBarHitTest()

        AppContent()
    }
}
