/*
 * Salt UI
 * Copyright (C) 2025 Moriafly
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

package window

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.sample.ui.theme.AppTheme
import com.moriafly.salt.ui.sample.util.AppConfig
import com.moriafly.salt.ui.window.CaptionBarHitTest
import com.moriafly.salt.ui.window.CaptionButtonsAlign
import com.moriafly.salt.ui.window.LocalSaltWindowInfo
import com.moriafly.salt.ui.window.SaltWindow
import com.moriafly.salt.ui.window.SaltWindowProperties

@OptIn(ExperimentalComposeUiApi::class, UnstableSaltUiApi::class)
fun main() {
    application {
        AppTheme(
            isDarkTheme = AppConfig.isDarkTheme
        ) {
            MainWindow(
                onCloseRequest = ::exitApplication
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, UnstableSaltUiApi::class)
@Composable
private fun MainWindow(
    onCloseRequest: () -> Unit
) {
    SaltWindow(
        onCloseRequest = onCloseRequest,
        properties = SaltWindowProperties.default(
            minSize = DpSize(
                width = 600.dp,
                height = 400.dp
            )
        )
    ) {
        val saltWindowInfo = LocalSaltWindowInfo.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SaltTheme.colors.background)
        ) {
            CaptionBarHitTest()

            Spacer(
                modifier = Modifier
                    .align(
                        when (saltWindowInfo.captionButtonsAlign) {
                            CaptionButtonsAlign.Start -> Alignment.TopStart
                            CaptionButtonsAlign.End -> Alignment.TopEnd
                        }
                    )
                    .width(saltWindowInfo.captionButtonsFullWidth)
                    .height(saltWindowInfo.captionBarHeight)
                    .background(SaltTheme.colors.highlight)
            )

            Content()
        }
    }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun Content() {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val saltWindowInfo = LocalSaltWindowInfo.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(saltWindowInfo.captionBarHeight)
        ) {
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            RoundedColumn {
                ItemSwitcher(
                    state = AppConfig.isDarkTheme,
                    onChange = {
                        AppConfig.updateIsDarkTheme(it)
                    },
                    text = "Dark Theme"
                )
            }
        }
    }
}
