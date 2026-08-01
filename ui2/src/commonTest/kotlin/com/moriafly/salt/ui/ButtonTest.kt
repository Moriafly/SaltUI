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

package com.moriafly.salt.ui

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
    fun subtleButton_usesQuietNeutralContainer() = runComposeUiTest {
        var normalColors: ButtonColors? = null
        var destructiveColors: ButtonColors? = null
        var textColor = Color.Unspecified
        var subTextColor = Color.Unspecified
        var errorColor = Color.Unspecified

        setContent {
            SaltTheme {
                val currentNormalColors = ButtonDefaults.colors(ButtonAppearance.Subtle)
                val currentDestructiveColors = ButtonDefaults.colors(
                    appearance = ButtonAppearance.Subtle,
                    intent = ButtonIntent.Destructive
                )
                val themeColors = SaltTheme.colors
                SideEffect {
                    normalColors = currentNormalColors
                    destructiveColors = currentDestructiveColors
                    textColor = themeColors.text
                    subTextColor = themeColors.subText
                    errorColor = themeColors.error
                }
            }
        }

        runOnIdle {
            val normal = requireNotNull(normalColors)
            val destructive = requireNotNull(destructiveColors)
            assertEquals(textColor.copy(alpha = 0.08f), normal.containerColor)
            assertEquals(textColor, normal.contentColor)
            assertEquals(
                subTextColor.copy(alpha = 0.05f),
                normal.disabledContainerColor
            )
            assertEquals(
                subTextColor.copy(alpha = subTextColor.alpha * 0.55f),
                normal.disabledContentColor
            )
            assertEquals(errorColor.copy(alpha = 0.08f), destructive.containerColor)
            assertEquals(errorColor, destructive.contentColor)
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
                    .colors(ButtonAppearance.Subtle)
                    .contentColor
                expectedTextStyle = ButtonDefaults.textStyle

                Button(
                    onClick = {},
                    appearance = ButtonAppearance.Subtle
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

    @Test
    fun contentSlot_exposesContentAndButtonClick() = runComposeUiTest {
        var clickCount = 0
        setContent {
            SaltTheme {
                Button(
                    onClick = { clickCount++ },
                    modifier = Modifier.testTag(BUTTON_TAG),
                    appearance = ButtonAppearance.Subtle
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
