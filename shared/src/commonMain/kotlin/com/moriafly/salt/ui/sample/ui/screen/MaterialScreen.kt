package com.moriafly.salt.ui.sample.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moriafly.salt.adaptive.window.LocalSaltAdaptiveWindow
import com.moriafly.salt.adaptive.window.UnstableSaltAdaptiveWindowApi
import com.moriafly.salt.ui.ItemCheck
import com.moriafly.salt.ui.ItemContainer
import com.moriafly.salt.ui.ItemOuterTitle
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.ItemValue
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.sample.ui.screen.basic.BasicScreenColumn
import com.moriafly.salt.ui.sample.util.AppConfig
import com.moriafly.salt.ui.screen.BasicScreenStyle

@OptIn(UnstableSaltUiApi::class, UnstableSaltAdaptiveWindowApi::class)
@Composable
fun MaterialScreen() {
    BasicScreenColumn(
        title = "Theme and material"
    ) {
        ItemOuterTitle("Appearance")
        RoundedColumn {
            ItemSwitcher(
                state = AppConfig.isDarkTheme,
                onChange = { AppConfig.isDarkTheme = it },
                text = "Dark theme"
            )
        }

        ItemOuterTitle("Title Bar Backdrop Type")
        RoundedColumn {
            BasicScreenStyle.TitleBarBackdropType.supportedEntries().forEach { style ->
                ItemCheck(
                    state = style == AppConfig.titleBarBackdropType,
                    onChange = {
                        AppConfig.titleBarBackdropType = style
                    },
                    text = style.name
                )
            }
        }

        ItemOuterTitle("Theme colors")
        RoundedColumn {
            val colors = SaltTheme.colors
            listOf(
                "Highlight" to colors.highlight,
                "Text" to colors.text,
                "Secondary text" to colors.subText,
                "Background" to colors.background,
                "Secondary background" to colors.subBackground,
                "Popup" to colors.popup,
                "Stroke" to colors.stroke,
                "On highlight" to colors.onHighlight,
                "Success" to colors.success,
                "Warning" to colors.warning,
                "Error" to colors.error
            ).forEach { (name, color) ->
                ItemContainer {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(name)
                    }
                }
            }
        }

        ItemOuterTitle("Typography")
        RoundedColumn {
            ItemContainer { Text("Large title", style = SaltTheme.textStyles.largeTitle) }
            ItemContainer { Text("Main text", style = SaltTheme.textStyles.main) }
            ItemContainer { Text("Secondary text", style = SaltTheme.textStyles.sub) }
            ItemContainer { Text("Paragraph text", style = SaltTheme.textStyles.paragraph) }
        }

        ItemOuterTitle("Adaptive window")
        val window = LocalSaltAdaptiveWindow.current
        RoundedColumn {
            ItemValue(text = "Form factor", sub = window.formFactor.name)
            ItemValue(text = "Size class", sub = window.sizeClass.name)
            ItemValue(text = "Window size", sub = window.windowSize.toString())
        }
    }
}
