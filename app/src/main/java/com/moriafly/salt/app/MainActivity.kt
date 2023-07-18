package com.moriafly.salt.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.moriafly.salt.ui.BottomBar
import com.moriafly.salt.ui.BottomBarItem
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemContainer
import com.moriafly.salt.ui.ItemSpacer
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.ItemText
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.ItemValue
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.SaltUILogo
import com.moriafly.salt.ui.TextButton
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.darkSaltColors
import com.moriafly.salt.ui.lightSaltColors

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colors = if (isSystemInDarkTheme()) {
                darkSaltColors()
            } else {
                lightSaltColors()
            }
            SaltTheme(
                colors = colors
            ) {
                MainUI()
            }
        }
    }

}

@OptIn(UnstableSaltApi::class)
@Composable
private fun MainUI() {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TitleBar(
            onBack = {

            },
            text = "SaltUI"
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .background(color = SaltTheme.colors.background)
                .verticalScroll(rememberScrollState())
        ) {
            SaltUILogo()

            RoundedColumn {
                ItemTitle(text = "Hello, SaltUI")
                ItemText(
                    text = "SaltUI（UI for Salt Player） 是提取自椒盐音乐的 UI 风格组件，用以快速生成椒盐音乐风格用户界面。本库将会广泛用以椒盐系列 App 开发，以达到快速开发目的"
                )
                ItemSpacer()
            }

            RoundedColumn {
                ItemTitle(text = "控件")
                Item(
                    onClick = {

                    },
                    iconPainter = painterResource(id = R.drawable.ic_qr_code),
                    iconColor = SaltTheme.colors.highlight,
                    text = "标准 Item 控件，带图标（可选），副标题文本（可选）",
                    sub = "Item 控件的副标题"
                )
                var switch by remember { mutableStateOf(false) }
                ItemSwitcher(
                    state = switch,
                    onChange = {
                        switch = it
                    },
                    iconPainter = painterResource(id = R.drawable.ic_verified),
                    iconColor = SaltTheme.colors.highlight,
                    text = "标准开关控件，带图标（可选），副标题文本（可选）",
                    sub = "开关控件的副标题"
                )
                ItemContainer {
                    TextButton(
                        onClick = {

                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = "默认按钮 TextButton"
                    )
                }
            }

            RoundedColumn {
                ItemTitle(text = "其他样式测试")
                Item(
                    onClick = {

                    },
                    text = "标准 Item 控件",
                    sub = "Item 控件的副标题"
                )
                Item(
                    onClick = {

                    },
                    iconPainter = painterResource(id = R.drawable.ic_qr_code),
                    iconColor = SaltTheme.colors.highlight,
                    text = "标准 Item 控件"
                )
                Item(
                    onClick = {

                    },
                    enabled = false,
                    iconPainter = painterResource(id = R.drawable.ic_qr_code),
                    iconColor = SaltTheme.colors.highlight,
                    text = "标准 Item 控件",
                    sub = "已禁用"
                )
                Item(
                    onClick = {

                    },
                    text = "标准 Item 控件"
                )
                ItemSwitcher(
                    state = true,
                    onChange = {

                    },
                    text = "标准开关控件",
                    sub = "开关控件的副标题"
                )
                ItemSwitcher(
                    state = true,
                    onChange = {

                    },
                    enabled = false,
                    iconPainter = painterResource(id = R.drawable.ic_verified),
                    iconColor = SaltTheme.colors.highlight,
                    text = "标准开关控件（禁用）",
                    sub = "此开关状态被禁用"
                )
                ItemSwitcher(
                    state = true,
                    onChange = {

                    },
                    text = "标准开关控件"
                )
            }

            RoundedColumn {
                ItemTitle(text = "Value 组件")
                ItemValue(text = "Value 标题", sub = "Value 内容")
                ItemValue(text = "Value 标题标题标题标题标题标题标题标题标题标题标题", sub = "Value 内容内容内容内容")
            }

        }
        BottomBar {
            BottomBarItem(
                state = true,
                onClick = {

                },
                painter = painterResource(id = R.drawable.ic_qr_code),
                text = "二维码"
            )
            BottomBarItem(
                state = false,
                onClick = {

                },
                painter = painterResource(id = R.drawable.ic_verified),
                text = "认证"
            )
        }
    }
}