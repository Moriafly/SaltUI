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

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.internal.SmoothRoundedRectangleShape
import com.moriafly.salt.ui.saltTextStyles
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DialogTest {
    @Test
    fun adaptiveDialogMaxSize_clampsToWindowAndRequestedWidth() {
        assertEquals(
            DpSize(260.dp, 688.dp),
            calculateAdaptiveDialogMaxSize(
                windowSize = DpSize(960.dp, 720.dp),
                requestedMaxWidth = 260.dp
            )
        )
        assertEquals(
            DpSize(0.dp, 0.dp),
            calculateAdaptiveDialogMaxSize(
                windowSize = DpSize(20.dp, 20.dp),
                requestedMaxWidth = 260.dp
            )
        )
        assertEquals(
            DpSize(0.dp, 68.dp),
            calculateAdaptiveDialogMaxSize(
                windowSize = DpSize(100.dp, 100.dp),
                requestedMaxWidth = (-1).dp
            )
        )
    }

    @Test
    fun defaults_resolveFromPlatformMetrics() {
        val metrics = platformDialogMetrics()

        assertEquals(metrics.shape, DialogDefaults.shape)
        assertEquals(metrics.contentPadding, DialogDefaults.contentPadding)
        assertEquals(metrics.controlSize, DialogDefaults.controlSize)
        assertEquals(metrics.ambientShadow, DialogDefaults.ambientShadow)
        assertEquals(metrics.keyShadow, DialogDefaults.keyShadow)
    }

    @Test
    fun dialogShape_usesCalibratedContinuousCorner() {
        val shape = assertIs<SmoothRoundedRectangleShape>(platformDialogMetrics().shape)

        assertEquals(0.65f, shape.smoothing)
    }

    @Test
    fun typography_inheritsThemeMainStyle() = runComposeUiTest {
        val mainStyle = TextStyle(
            fontSize = 19.sp,
            letterSpacing = 0.4.sp
        )
        var titleStyle = TextStyle.Default
        var messageStyle = TextStyle.Default

        setContent {
            SaltTheme(
                textStyles = saltTextStyles(main = mainStyle)
            ) {
                val currentTitleStyle = DialogDefaults.titleTextStyle
                val currentMessageStyle = DialogDefaults.messageTextStyle
                SideEffect {
                    titleStyle = currentTitleStyle
                    messageStyle = currentMessageStyle
                }
            }
        }

        runOnIdle {
            assertEquals(mainStyle.fontSize, titleStyle.fontSize)
            assertEquals(mainStyle.letterSpacing, titleStyle.letterSpacing)
            assertEquals(platformDialogMetrics().titleFontWeight, titleStyle.fontWeight)
            assertEquals(mainStyle, messageStyle)
        }
    }

    @Test
    fun smoothShape_createsGenericOutline() {
        val shape = SmoothRoundedRectangleShape(
            radius = 28.dp,
            smoothing = 0.5f
        )

        val outline = shape.createOutline(
            size = Size(320f, 200f),
            layoutDirection = LayoutDirection.Ltr,
            density = Density(1f)
        )

        assertIs<Outline.Generic>(outline)
    }

    @Test
    fun smoothShape_rejectsInvalidSmoothing() {
        assertFailsWith<IllegalArgumentException> {
            SmoothRoundedRectangleShape(
                radius = 28.dp,
                smoothing = 1.1f
            )
        }
    }

    @Test
    fun yesNoDialog_dispatchesBothActions() = runComposeUiTest {
        var dismissed = false
        var confirmed = false

        setContent {
            SaltTheme {
                YesNoDialog(
                    onDismissRequest = {
                        dismissed = true
                    },
                    onConfirm = {
                        confirmed = true
                    },
                    title = "Continue?",
                    content = "This action can be changed later",
                    cancelText = "Cancel",
                    confirmText = "Continue"
                )
            }
        }

        onNodeWithText("Cancel").performClick()
        onNodeWithText("Continue").performClick()

        runOnIdle {
            assertTrue(dismissed)
            assertTrue(confirmed)
        }
    }
}
