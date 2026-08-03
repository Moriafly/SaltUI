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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.sample.ui.MainContent
import com.moriafly.salt.ui.sample.ui.theme.AppTheme
import com.moriafly.salt.ui.window.CaptionBarHitTest
import com.moriafly.salt.ui.window.LocalSaltWindowInfo
import com.moriafly.salt.ui.window.SaltWindow
import com.moriafly.salt.ui.window.SaltWindowProperties
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalTestApi::class,
    UnstableSaltUiApi::class
)
class MainContentTest {
    @Test
    fun layerStartsAtCaptionBarBottom() = runDesktopComposeUiTest {
        setContent {
            AppTheme {
                SaltWindow(
                    onCloseRequest = {},
                    state = rememberWindowState(
                        size = DpSize(960.dp, 720.dp)
                    ),
                    title = "Caption Bar Layout Test",
                    properties = SaltWindowProperties.default(
                        captionBarHeight = 64.dp
                    )
                ) {
                    MainContent(
                        windowCaptionBarHeight = LocalSaltWindowInfo.current.captionBarHeight
                    )
                    CaptionBarHitTest(
                        modifier = Modifier.testTag("captionBarHitTest")
                    )
                }
            }
        }

        val captionBarBounds = onNodeWithTag("captionBarHitTest")
            .fetchSemanticsNode()
            .boundsInRoot
        val layerBounds = onNodeWithTag("mainContentLayer")
            .fetchSemanticsNode()
            .boundsInRoot

        assertEquals(captionBarBounds.bottom, layerBounds.top)
    }
}
