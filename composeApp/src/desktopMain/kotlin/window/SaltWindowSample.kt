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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.application
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.sample.ui.theme.AppTheme
import com.moriafly.salt.ui.window.CaptionBarHitTest
import com.moriafly.salt.ui.window.CaptionButtonsAlign
import com.moriafly.salt.ui.window.LocalSaltWindowInfo
import com.moriafly.salt.ui.window.SaltWindow

@OptIn(ExperimentalComposeUiApi::class, UnstableSaltUiApi::class)
fun main() {
    application {
        AppTheme {
            SaltWindow(
                onCloseRequest = ::exitApplication
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
                }
            }
        }
    }
}
