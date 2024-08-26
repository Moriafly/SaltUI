import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import com.moriafly.salt.ui.BottomBar
import com.moriafly.salt.ui.BottomBarItem
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemArrowType
import com.moriafly.salt.ui.ItemCheck
import com.moriafly.salt.ui.ItemContainer
import com.moriafly.salt.ui.ItemEdit
import com.moriafly.salt.ui.ItemEditPassword
import com.moriafly.salt.ui.ItemOuter
import com.moriafly.salt.ui.ItemOuterLargeTitle
import com.moriafly.salt.ui.ItemOuterTitle
import com.moriafly.salt.ui.ItemPopup
import com.moriafly.salt.ui.ItemSlider
import com.moriafly.salt.ui.ItemSpacer
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.ItemTextButton
import com.moriafly.salt.ui.ItemTextButtonContainer
import com.moriafly.salt.ui.ItemTip
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.ItemValue
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TextButton
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.dialog.InputDialog
import com.moriafly.salt.ui.dialog.YesDialog
import com.moriafly.salt.ui.dialog.YesNoDialog
import com.moriafly.salt.ui.popup.PopupMenuItem
import com.moriafly.salt.ui.popup.rememberPopupState
import org.jetbrains.compose.resources.painterResource
import saltui.composeapp.generated.resources.Res
import saltui.composeapp.generated.resources.ic_qr_code
import saltui.composeapp.generated.resources.ic_verified

@OptIn(UnstableSaltApi::class)
@Composable
fun MainUI() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = SaltTheme.colors.background)
    ) {
        TitleBar(
            onBack = {

            },
            text = ""
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .background(color = SaltTheme.colors.background)
                .verticalScroll(rememberScrollState())
        ) {
            ItemOuterLargeTitle(
                text = "Hello, SaltUI 2.0",
                sub = "SaltUI（UI for Salt Player） 是提取自椒盐音乐的 UI 风格组件，用以快速生成椒盐音乐风格用户界面。本库将会广泛用以椒盐系列 App 开发，以达到快速开发目的"
            )

            RoundedColumn {
                ItemCheck(
                    state = false,
                    onChange = {

                    },
                    text = "未选中按钮"
                )
                ItemCheck(
                    state = true,
                    onChange = {

                    },
                    text = "选中按钮"
                )
                ItemTextButtonContainer {
                    ItemTextButton(
                        onClick = {

                        },
                        text = "强调默认按钮 TextButton"
                    )
                    ItemTextButton(
                        onClick = {

                        },
                        text = "强调默认按钮 TextButton 强调默认按钮 TextButton 强调默认按钮 TextButton 强调默认按钮 TextButton"
                    )
                    ItemTextButton(
                        onClick = {

                        },
                        text = "默认按钮 TextButton 默认按钮 TextButton 默认按钮 TextButton",
                        primary = false
                    )
                }
            }

            RoundedColumn {
                ItemTitle(text = "控件")
                Item(
                    onClick = {

                    },
                    iconPainter = painterResource(Res.drawable.ic_qr_code),
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
                    iconPainter = painterResource(Res.drawable.ic_verified),
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
                ItemTip(text = "测试")
                Item(
                    onClick = {

                    },
                    text = "标准 Item 控件",
                    sub = "Item 控件的副标题"
                )
                Item(
                    onClick = {

                    },
                    iconPainter = painterResource(Res.drawable.ic_qr_code),
                    iconColor = SaltTheme.colors.highlight,
                    text = "标准 Item 控件"
                )
                Item(
                    onClick = {

                    },
                    enabled = false,
                    iconPainter = painterResource(Res.drawable.ic_qr_code),
                    iconColor = SaltTheme.colors.highlight,
                    text = "标准 Item 控件",
                    sub = "已禁用"
                )
                Item(
                    onClick = {

                    },
                    text = "标准 Item 控件"
                )

                var switch by remember { mutableStateOf(false) }
                ItemSwitcher(
                    state = switch,
                    onChange = {
                        switch = it
                    },
                    text = "标准开关控件",
                    sub = "开关控件的副标题"
                )
                ItemSwitcher(
                    state = true,
                    onChange = {

                    },
                    enabled = false,
                    iconPainter = painterResource(Res.drawable.ic_verified),
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

            RoundedColumn {
                ItemTitle(text = "Edit 组件")
                var text by remember { mutableStateOf("") }
                ItemEdit(
                    text = text,
                    onChange = {
                        text = it
                    },
                    hint = "HINT 这是输入框"
                )

                var text2 by remember { mutableStateOf("") }
                ItemEditPassword(
                    text = text2,
                    onChange = {
                        text2 = it
                    },
                    hint = "HINT 这是密码输入框"
                )
            }

            RoundedColumn {
                val popupState = rememberPopupState()
                ItemPopup(
                    state = popupState,
                    text = "Popup Item",
                    sub = "Value"
                ) {
                    PopupMenuItem(
                        onClick = {
                            popupState.dismiss()
                        },
                        selected = true,
                        text = "选项一",
                        sub = "这是选项一的介绍信息"
                    )
                    PopupMenuItem(
                        onClick = {
                            popupState.dismiss()
                        },
                        selected = false,
                        text = "选项二"
                    )

                    PopupMenuItem(
                        onClick = {
                            popupState.dismiss()
                        },
                        text = "二维码二维码二维码",
                        iconPainter = painterResource(Res.drawable.ic_qr_code),
                        iconColor = SaltTheme.colors.text
                    )
                    PopupMenuItem(
                        onClick = {
                            popupState.dismiss()
                        },
                        text = "其他选项"
                    )
                    PopupMenuItem(
                        onClick = {
                            popupState.dismiss()
                        },
                        text = "其他选项"
                    )
                    PopupMenuItem(
                        onClick = {
                            popupState.dismiss()
                        },
                        text = "其他选项"
                    )
                    PopupMenuItem(
                        onClick = {
                            popupState.dismiss()
                        },
                        text = "其他选项"
                    )
                    PopupMenuItem(
                        onClick = {
                            popupState.dismiss()
                        },
                        text = "其他选项"
                    )
                    PopupMenuItem(
                        onClick = {
                            popupState.dismiss()
                        },
                        text = "其他选项"
                    )
                    PopupMenuItem(
                        onClick = {
                            popupState.dismiss()
                        },
                        text = "其他选项"
                    )
                    PopupMenuItem(
                        onClick = {
                            popupState.dismiss()
                        },
                        text = "其他选项"
                    )
                }
            }

            RoundedColumn {
                ItemTitle(text = "Dialog 对话框")
                var yesNoDialog by remember { mutableStateOf(false) }
                if (yesNoDialog) {
                    YesNoDialog(
                        onDismissRequest = { yesNoDialog = false },
                        onConfirm = { yesNoDialog = false },
                        title = "YesNoDialog",
                        content = "这是一个是否确认的对话框"
                    )
                }
                Item(
                    onClick = {
                        yesNoDialog = true
                    },
                    text = "YesNoDialog",
                    arrowType = ItemArrowType.Link
                )

                var yesDialog by remember { mutableStateOf(false) }
                if (yesDialog) {
                    YesDialog(
                        onDismissRequest = { yesDialog = false },
                        title = "YesDialog",
                        content = "这是一个是否确认的对话框"
                    )
                }
                Item(
                    onClick = {
                        yesDialog = true
                    },
                    text = "YesDialog"
                )

                var inputDialog by remember { mutableStateOf(false) }
                if (inputDialog) {
                    var inputText by remember { mutableStateOf("") }
                    InputDialog(
                        onDismissRequest = {
                            inputDialog = false
                        },
                        onConfirm = {
                            inputDialog = false
                        },
                        title = "文本输入",
                        text = inputText,
                        onChange = {
                            inputText = it
                        }
                    )
                }
                Item(
                    onClick = {
                        inputDialog = true
                    },
                    text = "InputDialog"
                )
            }

            RoundedColumn {
                var slider by remember { mutableStateOf(0f) }
                ItemSlider(
                    value = slider,
                    onValueChange = {
                        slider = it
                    },
                    iconPainter = painterResource(Res.drawable.ic_qr_code),
                    iconColor = SaltTheme.colors.text,
                    text = "Slider 滑块",
                    // sub = "滑块介绍"
                    steps = 2
                )
            }

            RoundedColumn(
                color = Color.Unspecified
            ) {
                ItemOuterTitle(text = "猜你在找")
                ItemOuter(
                    onClick = {

                    },
                    text = "你好呀"
                )
                ItemSpacer()
            }

        }
        BottomBar {
            BottomBarItem(
                state = true,
                onClick = {

                },
                painter = painterResource(Res.drawable.ic_qr_code),
                text = "二维码"
            )
            BottomBarItem(
                state = false,
                onClick = {

                },
                painter = painterResource(Res.drawable.ic_verified),
                text = "认证"
            )
        }
    }
}