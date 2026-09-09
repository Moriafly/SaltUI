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

package com.moriafly.salt.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, UnstableSaltUiApi::class)
class AdaptiveDialogTest {
    @Test
    fun compactSize_resolvesToDesktopWidth() {
        assertEquals(260.dp, AdaptiveDialogSize.Min.maxWidth)
        assertEquals(448.dp, AdaptiveDialogSize.Standard.maxWidth)
        assertEquals(540.dp, AdaptiveDialogSize.Max.maxWidth)
    }

    @Test
    fun compactAlert_keepsTwoActionsHorizontal() = runDesktopComposeUiTest {
        setContent {
            SaltTheme {
                YesNoDialog(
                    onDismissRequest = {},
                    onConfirm = {},
                    title = "Continue?",
                    content = "This action can be changed later",
                    cancelText = "Cancel",
                    confirmText = "Continue"
                )
            }
        }

        val dismissBounds = onNodeWithText("Cancel").getUnclippedBoundsInRoot()
        val confirmBounds = onNodeWithText("Continue").getUnclippedBoundsInRoot()

        assertEquals(dismissBounds.top, confirmBounds.top)
        assertEquals(dismissBounds.bottom, confirmBounds.bottom)
        assertTrue(dismissBounds.left < confirmBounds.left)
    }

    @Test
    fun longAlert_keepsActionsReachable() = runDesktopComposeUiTest {
        var confirmed = false
        setContent {
            SaltTheme {
                YesNoDialog(
                    onDismissRequest = {},
                    onConfirm = {
                        confirmed = true
                    },
                    title = "Long message",
                    content = "Supporting information ".repeat(100),
                    cancelText = "Cancel",
                    confirmText = "Continue"
                )
            }
        }

        onNodeWithText("Continue").performClick()

        runOnIdle {
            assertTrue(confirmed)
        }
    }
}
