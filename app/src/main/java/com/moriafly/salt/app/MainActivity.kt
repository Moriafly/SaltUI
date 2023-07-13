package com.moriafly.salt.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemContainer
import com.moriafly.salt.ui.ItemSpacer
import com.moriafly.salt.ui.ItemText
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TextButton
import com.moriafly.salt.ui.TitleBar

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SaltTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = SaltTheme.colors.background)
                ) {
                    TitleBar(
                        onBack = { /*TODO*/ },
                        text = "SaltUI"
                    )

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
                            iconPainter = painterResource(id = R.drawable.ic_qr_code),
                            iconColor = SaltTheme.colors.highlight,
                            onClick = {

                            },
                            text = "标准 Item 控件，带图标（可选），副标题文本（可选）",
                            sub = "Item 控件的副标题"
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
                }
            }
        }
    }

}