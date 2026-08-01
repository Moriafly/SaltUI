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

package com.moriafly.salt.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ButtonTest {
    @Test
    fun buttonColors_copyTreatsUnspecifiedAsSourceValue() {
        val colors = ButtonColors(
            containerColor = Color.Red,
            contentColor = Color.White,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.DarkGray
        )

        assertEquals(
            expected = colors,
            actual = colors.copy(
                containerColor = Color.Unspecified,
                contentColor = Color.Unspecified,
                disabledContainerColor = Color.Unspecified,
                disabledContentColor = Color.Unspecified
            )
        )
    }

    @Test
    fun outlinedBorder_usesVisibleSemanticColor() = runComposeUiTest {
        var normalBorder = BorderStroke(0.dp, Color.Unspecified)
        var destructiveBorder = BorderStroke(0.dp, Color.Unspecified)
        var disabledBorder = BorderStroke(0.dp, Color.Unspecified)
        var strokeColor = Color.Unspecified
        var errorColor = Color.Unspecified

        setContent {
            SaltTheme {
                normalBorder = requireNotNull(
                    ButtonDefaults.border(ButtonAppearance.Outlined)
                )
                destructiveBorder = requireNotNull(
                    ButtonDefaults.border(
                        appearance = ButtonAppearance.Outlined,
                        intent = ButtonIntent.Destructive
                    )
                )
                disabledBorder = requireNotNull(
                    ButtonDefaults.border(
                        appearance = ButtonAppearance.Outlined,
                        enabled = false
                    )
                )
                strokeColor = SaltTheme.colors.stroke
                errorColor = SaltTheme.colors.error
            }
        }

        runOnIdle {
            assertEquals(
                BorderStroke(1.dp, strokeColor),
                normalBorder
            )
            assertEquals(
                BorderStroke(1.dp, errorColor.copy(alpha = 0.4f)),
                destructiveBorder
            )
            assertEquals(
                BorderStroke(
                    1.dp,
                    strokeColor.copy(alpha = strokeColor.alpha * 0.5f)
                ),
                disabledBorder
            )
        }
    }

    @Test
    fun outlinedButton_usesSubBackgroundContainer() = runComposeUiTest {
        var outlinedColors = ButtonColors(
            containerColor = Color.Unspecified,
            contentColor = Color.Unspecified,
            disabledContainerColor = Color.Unspecified,
            disabledContentColor = Color.Unspecified
        )
        var subBackground = Color.Unspecified

        setContent {
            SaltTheme {
                outlinedColors = ButtonDefaults.colors(ButtonAppearance.Outlined)
                subBackground = SaltTheme.colors.subBackground
            }
        }

        runOnIdle {
            assertEquals(subBackground, outlinedColors.containerColor)
            assertEquals(subBackground, outlinedColors.disabledContainerColor)
        }
    }

    @Test
    fun plainButton_usesTransparentContainerAndHighlightContent() = runComposeUiTest {
        var plainColors = ButtonColors(
            containerColor = Color.Unspecified,
            contentColor = Color.Unspecified,
            disabledContainerColor = Color.Unspecified,
            disabledContentColor = Color.Unspecified
        )
        var highlight = Color.Unspecified

        setContent {
            SaltTheme {
                plainColors = ButtonDefaults.colors(ButtonAppearance.Plain)
                highlight = SaltTheme.colors.highlight
            }
        }

        runOnIdle {
            assertEquals(Color.Transparent, plainColors.containerColor)
            assertEquals(highlight, plainColors.contentColor)
            assertEquals(Color.Transparent, plainColors.disabledContainerColor)
        }
    }

    @Test
    fun saltTheme_providesRootContentColorAndTextStyle() = runComposeUiTest {
        var contentColor = Color.Unspecified
        var themeColor = Color.Unspecified
        var textStyle = TextStyle.Default
        var themeTextStyle = TextStyle.Default

        setContent {
            SaltTheme {
                val currentContentColor = LocalContentColor.current
                val currentThemeColor = SaltTheme.colors.text
                val currentTextStyle = LocalTextStyle.current
                val currentThemeTextStyle = SaltTheme.textStyles.main
                SideEffect {
                    contentColor = currentContentColor
                    themeColor = currentThemeColor
                    textStyle = currentTextStyle
                    themeTextStyle = currentThemeTextStyle
                }
            }
        }

        runOnIdle {
            assertEquals(themeColor, contentColor)
            assertTypographyEquals(themeTextStyle, textStyle)
        }
    }

    @Test
    fun button_providesResolvedContentColorAndTextStyle() = runComposeUiTest {
        var contentColor = Color.Unspecified
        var expectedContentColor = Color.Unspecified
        var textStyle = TextStyle.Default
        var expectedTextStyle = TextStyle.Default
        val customTextStyle = TextStyle(
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        setContent {
            SaltTheme(
                textStyles = saltTextStyles(main = customTextStyle)
            ) {
                expectedContentColor = ButtonDefaults
                    .colors(ButtonAppearance.Outlined)
                    .contentColor
                expectedTextStyle = ButtonDefaults.textStyle

                Button(
                    onClick = {},
                    appearance = ButtonAppearance.Outlined
                ) {
                    val currentContentColor = LocalContentColor.current
                    val currentTextStyle = LocalTextStyle.current
                    SideEffect {
                        contentColor = currentContentColor
                        textStyle = currentTextStyle
                    }
                    Text("Inherited content")
                }
            }
        }

        runOnIdle {
            assertEquals(expectedContentColor, contentColor)
            assertTypographyEquals(expectedTextStyle, textStyle)
        }
    }

    @Test
    fun inheritedControlSize_resolvesPlatformHeight() = runComposeUiTest {
        setContent {
            SaltTheme {
                ProvideControlSize(ControlSize.Small) {
                    Button(
                        onClick = {},
                        text = "Compact",
                        modifier = Modifier.testTag(BUTTON_TAG)
                    )
                }
            }
        }

        val bounds = onNodeWithTag(BUTTON_TAG).getUnclippedBoundsInRoot()
        assertEquals(
            expected = ButtonDefaults.containerHeight(ControlSize.Small),
            actual = bounds.bottom - bounds.top
        )
    }

    @Test
    fun explicitControlSize_overridesInheritedSize() = runComposeUiTest {
        setContent {
            SaltTheme {
                ProvideControlSize(ControlSize.Small) {
                    Button(
                        onClick = {},
                        text = "Large",
                        modifier = Modifier.testTag(BUTTON_TAG),
                        size = ControlSize.Large
                    )
                }
            }
        }

        val bounds = onNodeWithTag(BUTTON_TAG).getUnclippedBoundsInRoot()
        assertEquals(
            expected = ButtonDefaults.containerHeight(ControlSize.Large),
            actual = bounds.bottom - bounds.top
        )
    }

    @Suppress("DEPRECATION")
    @Test
    fun legacyButtonType_remainsSourceCompatible() = runComposeUiTest {
        setContent {
            SaltTheme {
                Button(
                    onClick = {},
                    text = "Legacy",
                    type = ButtonType.Sub,
                    maxLines = 2
                )
            }
        }

        onNodeWithText("Legacy").assertExists()
    }

    @Test
    fun contentSlot_exposesContentAndButtonClick() = runComposeUiTest {
        var clickCount = 0
        setContent {
            SaltTheme {
                Button(
                    onClick = { clickCount++ },
                    modifier = Modifier.testTag(BUTTON_TAG),
                    appearance = ButtonAppearance.Outlined
                ) {
                    Text("Custom content")
                }
            }
        }

        onNodeWithText("Custom content").assertExists()
        onNodeWithTag(BUTTON_TAG).performClick()
        assertEquals(1, clickCount)
    }

    private companion object {
        const val BUTTON_TAG = "button"
    }
}

private fun assertTypographyEquals(
    expected: TextStyle,
    actual: TextStyle
) {
    assertEquals(expected.fontSize, actual.fontSize, "fontSize")
    assertEquals(expected.fontWeight, actual.fontWeight, "fontWeight")
    assertEquals(expected.fontFamily, actual.fontFamily, "fontFamily")
    assertEquals(expected.letterSpacing, actual.letterSpacing, "letterSpacing")
    assertEquals(expected.lineHeight, actual.lineHeight, "lineHeight")
}
