import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.moriafly.salt.ui.BottomSheetScaffold
import com.moriafly.salt.ui.SaltDynamicColors
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.outerPadding
import com.moriafly.salt.ui.saltConfigs

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun MainActivityContent() {
    val isDarkTheme = false // isSystemInDarkTheme()
    SaltTheme(
        configs = saltConfigs(
            isDarkTheme = isDarkTheme
        ),
        dynamicColors = SaltDynamicColors.default()
    ) {
        BottomSheetScaffold(
            sheetContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Blue)
                ) {
//                    LazyColumn(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .background(Color.Red)
//                    ) {
//                        items(50) {
//                            Text(
//                                text = "Item $it",
//                                modifier = Modifier
//                                    .outerPadding()
//                            )
//                        }
//                    }

                    VerticalPager(
                        state = rememberPagerState {
                            2
                        }
                    ) { page ->
                        when (page) {
                            0 -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Red)
                                ) {
                                    items(50) {
                                        Text(
                                            text = "Item $it",
                                            modifier = Modifier
                                                .outerPadding()
                                        )
                                    }
                                }
                            }
                            1 -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Green)
                                ) {
                                    items(50) {
                                        Text(
                                            text = "Item $it",
                                            modifier = Modifier
                                                .outerPadding()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) {
            MainScreen(
                modifier = Modifier
                    .padding(it)
            )
        }
    }
}
