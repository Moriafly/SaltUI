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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemDropdown
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltConfigs
import com.moriafly.salt.ui.SaltMaterial
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.dialog.BasicDialog
import com.moriafly.salt.ui.material.MaterialSource
import com.moriafly.salt.ui.material.MaterialType
import com.moriafly.salt.ui.popup.PopupMenuItem
import org.jetbrains.compose.resources.painterResource
import saltui.composeapp.generated.resources.Res
import saltui.composeapp.generated.resources.bg_iphone
import saltui.composeapp.generated.resources.bg_wallpaper
import saltui.composeapp.generated.resources.bg_wallpaper2

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun MaterialScreen() {
    var isDarkTheme by remember { mutableStateOf(false) }
    var materialType by remember { mutableStateOf(MaterialType.BlurryGlass) }
    SaltTheme(
        configs = SaltConfigs.default(
            isDarkTheme = isDarkTheme
        ),
        material = SaltMaterial.default(
            type = materialType
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SaltTheme.colors.background)
        ) {
            MaterialSource {
                Image(
                    painter = painterResource(Res.drawable.bg_iphone),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Content(
                onIsDarkTheme = {
                    isDarkTheme = it
                },
                onMaterialType = {
                    materialType = it
                }
            )
        }
    }
}

@Suppress("UnusedReceiverParameter")
@OptIn(UnstableSaltUiApi::class)
@Composable
private fun BoxScope.Content(
    onIsDarkTheme: (Boolean) -> Unit,
    onMaterialType: (MaterialType) -> Unit,
) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(140.dp))

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
            ItemSwitcher(
                state = SaltTheme.configs.isDarkTheme,
                onChange = {
                    onIsDarkTheme(it)
                },
                text = "深色模式"
            )
        }

        RoundedColumn {
            ItemDropdown(
                text = "材质",
                value = ""
            ) {
                MaterialType.entries.forEach {
                    PopupMenuItem(
                        onClick = {
                            onMaterialType(it)
                        },
                        text = it.name
                    )
                }
            }
        }

        RoundedColumn {
            Item(
                onClick = {
                    dialog = true
                },
                text = "测试 Blur 测试 Blur 测试 Blur 测试 Blur 测试 Blur 测试 Blur 测试 Blur 测试 Blur",
            )
        }

        RoundedColumn {
            Item(
                onClick = {
                    dialog = true
                },
                text = "测试 Blur 测试 Blur 测试 Blur 测试 Blur 测试 Blur 测试 Blur 测试 Blur 测试 Blur",
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    )
}
