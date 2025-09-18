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
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltConfigs
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.blur.MicaSource
import com.moriafly.salt.ui.dialog.BasicDialog
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
            configs = SaltConfigs.default(
                isDarkTheme = true,
                mica = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SaltTheme.colors.background)
            ) {
                MicaSource {
                    Image(
                        painter = painterResource(Res.drawable.bg_wallpaper),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Content()
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
        BasicDialog(
            onDismissRequest = {
                dialog = false
            }
        ) {
            RoundedColumn {
                Item(
                    onClick = {
                        dialog = false
                    },
                    text = "关闭"
                )
            }
        }
    }

//    val hazeState = remember { HazeState() }
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .hazeSource(hazeState, 0f)
//        ) {
//            Image(
//                painter = painterResource(Res.drawable.bg_wallpaper),
//                contentDescription = null,
//                modifier = Modifier
//                    .fillMaxSize(),
//                contentScale = ContentScale.Crop
//            )
//        }
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .hazeSource(hazeState, 1f, "bg")
//        ) {
//            Column(
//                modifier = Modifier
//                    .padding(32.dp)
//                    .fillMaxSize()
//                    .hazeSource(hazeState, 2f)
//                    .hazeEffect(hazeState) {
//                        canDrawArea = { area ->
//                            area.zIndex != 1f
//                        }
//                    }
//            ) {
//                repeat(100) {
//                    Text(
//                        text = "测试测试测试测试测试测试 1 测试 1 测试 1 测试 1 测试 1",
//                        color = Color.Red
//                    )
//                }
//            }
//
//            Column(
//                modifier = Modifier
//                    .padding(64.dp)
//                    .fillMaxSize()
//                    // 绘制小于同级，即绘制 1f
//                    .hazeSource(hazeState, 3f)
//                    .hazeEffect(hazeState) {
//                        canDrawArea = { area ->
//                            area.zIndex != 1f
//                        }
//                    }
//            ) {
//                repeat(100) {
//                    Text(
//                        text = "测试测试测试测试测试测试 2 测试 2 测试 2 测试 2 测试 2",
//                        color = Color.Green
//                    )
//                }
//            }
//
//            Dialog(
//                onDismissRequest = {}
//            ) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .hazeSource(hazeState, 4f)
//                        .hazeEffect(hazeState)
//                )
//            }
//
//            Column(
//                modifier = Modifier
//                    .padding(128.dp)
//                    .fillMaxSize()
//                    .background(Color.White)
//            ) {
//            }
//        }
//    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(56.dp))

        RoundedColumn {
//            ItemDropdown(
//                text = "测试",
//                value = "测试结果"
//            ) {
//                PopupMenuItem(
//                    onClick = {
//                    },
//                    text = "菜单 1",
//                )
//                PopupMenuItem(
//                    onClick = {
//                    },
//                    text = "菜单 2",
//                )
//            }
        }

        RoundedColumn {
            repeat(100) {
                Item(
                    onClick = {
                        dialog = true
                    },
                    text = "测试 Blur 测试 Blur 测试 Blur 测试 Blur 测试 Blur 测试 Blur 测试 Blur 测试 Blur",
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    )
}
