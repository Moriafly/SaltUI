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
