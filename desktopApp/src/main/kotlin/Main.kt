/*
 * Salt UI
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
import com.moriafly.salt.ui.window.LocalSaltWindowInfo
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
                backgroundType = when {
                    SaltWindowBackgroundType.Vibrancy.isSupported() ->
                        SaltWindowBackgroundType.Vibrancy

                    SaltWindowBackgroundType.Mica.isSupported() -> SaltWindowBackgroundType.Mica
                    else -> SaltWindowBackgroundType.None
                },
                backgroundIsDarkTheme = AppConfig.isDarkTheme
            )
        ) {
            MainContent(
                windowCaptionBarHeight = LocalSaltWindowInfo.current.captionBarHeight
            )

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
