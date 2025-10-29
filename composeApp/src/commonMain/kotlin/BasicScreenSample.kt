import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.ext.safeMainCompatPadding
import com.moriafly.salt.ui.outerPadding
import com.moriafly.salt.ui.screen.BasicScreen
import kotlinx.coroutines.launch

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun BasicScreenSample() {
    SaltTheme {
        BasicScreen(
            modifier = Modifier
                .background(SaltTheme.colors.background)
                .safeMainCompatPadding(),
            topBar = {
                Column {
                    Text(
                        text = "TopScreenBar",
                        modifier = Modifier
                            .background(Color.Green)
                            .outerPadding(),
                        fontSize = 48.sp
                    )
                }
            },
            collapsedTopBar = {
                TitleBar(
                    onBack = {
                    },
                    text = "TopScreenBar"
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
                            Text(
                                text = list[it],
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Blue)
                                    .outerPadding()
                            )
                        }
                    }

                    val scope = rememberCoroutineScope()
                    Button(
                        onClick = {
                            scope.launch {
                                state.animateScrollToItem(50)
                            }
                        },
                        text = "Scroll To",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                    )
                }
            }
        )
    }
}
