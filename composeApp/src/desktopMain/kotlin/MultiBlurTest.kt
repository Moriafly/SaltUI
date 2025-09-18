import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemDropdown
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.blur.MultiBlurLayer
import com.moriafly.salt.ui.blur.MultiBlurLevel
import com.moriafly.salt.ui.blur.multiBlurBackground
import com.moriafly.salt.ui.dialog.YesDialog
import com.moriafly.salt.ui.popup.PopupMenuItem
import com.moriafly.salt.ui.saltConfigs
import org.jetbrains.compose.resources.painterResource
import saltui.composeapp.generated.resources.Res
import saltui.composeapp.generated.resources.bg_wallpaper

@OptIn(UnstableSaltUiApi::class)
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Multi Blur",
    ) {
        SaltTheme(
            configs = saltConfigs(
                isDarkTheme = true
            )
        ) {
            MultiBlurLayer(
                enabled = true
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SaltTheme.colors.background)
                ) {
                    Image(
                        painter = painterResource(Res.drawable.bg_wallpaper),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .multiBlurBackground(MultiBlurLevel.Window),
                        contentScale = ContentScale.Crop
                    )

                    Content()
                }
            }
        }
    }
}

@Suppress("UnusedReceiverParameter")
@OptIn(UnstableSaltUiApi::class)
@Composable
private fun BoxScope.Content() {
    var dialog by remember { mutableStateOf(false) }
    if (dialog) {
        YesDialog(
            onDismissRequest = {
                dialog = false
            },
            title = "提示",
            content = "是否确定退出？"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(56.dp))

        RoundedColumn {
            ItemDropdown(
                text = "测试",
                value = "测试结果"
            ) {
                PopupMenuItem(
                    onClick = {
                    },
                    text = "菜单 1",
                )
                PopupMenuItem(
                    onClick = {
                    },
                    text = "菜单 2",
                )
            }
        }

        RoundedColumn {
            repeat(100) {
                Item(
                    onClick = {
                        dialog = true
                    },
                    text = "测试 Blur 测试 Blur 测试 Blur 测试 Blur 测试 Blur 测试 Blur 测试 Blur 测试 Blur",
                    textColor = Color.Red
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .multiBlurBackground(MultiBlurLevel.Bar, SaltTheme.colors.subBackground)
    )
}
