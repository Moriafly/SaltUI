package com.moriafly.salt.ui.sample.ui.screen

import androidx.compose.runtime.Composable
import com.moriafly.salt.ui.ItemCheck
import com.moriafly.salt.ui.ItemOuterTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.sample.ui.screen.basic.BasicScreenColumn
import com.moriafly.salt.ui.sample.util.AppConfig
import com.moriafly.salt.ui.screen.BasicScreenStyle

@OptIn(UnstableSaltUiApi::class)
@Composable
fun MaterialScreen() {
    BasicScreenColumn(
        title = "Material"
    ) {
        ItemOuterTitle("Title Bar Backdrop Type")
        RoundedColumn {
            BasicScreenStyle.TitleBarBackdropType.supportStyles().forEach { style ->
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
