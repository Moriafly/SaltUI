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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.sample.ui.MainContent
import com.moriafly.salt.ui.sample.ui.component.ComposeIcon
import com.moriafly.salt.ui.sample.ui.theme.AppTheme
import com.moriafly.salt.ui.sample.util.AppConfig
import com.moriafly.salt.ui.window.CaptionBarHitTest
import com.moriafly.salt.ui.window.DesktopCaptionBar
import com.moriafly.salt.ui.window.SaltWindow
import com.moriafly.salt.ui.window.SaltWindowBackgroundType
import com.moriafly.salt.ui.window.SaltWindowProperties

@OptIn(ExperimentalComposeUiApi::class, UnstableSaltUiApi::class)
fun main() = application {
    AppTheme {
        SaltWindow(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(
                size = DpSize(960.dp, 720.dp),
                position = WindowPosition.Aligned(Alignment.Center)
            ),
            title = "Salt UI",
            resizable = true,
            properties = SaltWindowProperties.default(
                captionButtonIsDarkTheme = AppConfig.isDarkTheme,
                backgroundType = SaltWindowBackgroundType.Mica,
                backgroundIsDarkTheme = AppConfig.isDarkTheme
            )
        ) {
            MainContent()

            DesktopCaptionBar {
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(12.dp))
                    ComposeIcon()
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Salt UI"
                    )
                }
            }

            CaptionBarHitTest()
        }
    }
}
