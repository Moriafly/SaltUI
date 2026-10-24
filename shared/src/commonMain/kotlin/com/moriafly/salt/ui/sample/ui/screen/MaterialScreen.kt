package com.moriafly.salt.ui.sample.ui.screen

import androidx.compose.runtime.Composable
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemCheck
import com.moriafly.salt.ui.ItemOuterTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.sample.ui.navigation.LocalSaltNavigator
import com.moriafly.salt.ui.sample.ui.navigation.ScreenRoute
import com.moriafly.salt.ui.sample.ui.screen.basic.BasicScreenColumn
import com.moriafly.salt.ui.sample.util.AppConfig
import com.moriafly.salt.ui.screen.BasicScreenStyle
import com.moriafly.salt.ui.screen.ScreenCard
import com.moriafly.salt.ui.UnstableSaltUiApi

@OptIn(UnstableSaltUiApi::class)
@Composable
fun MaterialScreen() {
    BasicScreenColumn(
        title = "Material"
    ) {
        val navigator = LocalSaltNavigator.current
        ScreenCard {
            Item(
                onClick = { navigator.navigate(ScreenRoute.MaterialTest) },
                text = "Material cache test",
                sub = "Compare Dynamic and Static backgrounds"
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
    }
}
