import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemOuterLargeTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.ext.safeMainCompatPadding
import com.moriafly.salt.ui.screen.BasicScreen

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun BasicScreenSample() {
    SaltTheme {
        BasicScreen(
            modifier = Modifier
                .background(SaltTheme.colors.background)
                .safeMainCompatPadding(),
            collapsedTopBar = {
                TitleBar(
                    onBack = {
                    },
                    text = "关于",
                    showBackBtn = false
                )
            },
            expandedTopBar = {
                ItemOuterLargeTitle(
                    text = "关于",
                    sub = "Moriafly"
                )
            },
            content = { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                ) {
                    val state = rememberLazyListState()
                    LazyColumn(
                        state = state
                    ) {
                        val list = (0..75).map { it.toString() }
                        items(count = list.size) {
                            RoundedColumn {
                                Item(
                                    onClick = {
                                    },
                                    text = "Item $it"
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}
