import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.ext.safeMainCompatPadding
import com.moriafly.salt.ui.outerPadding
import com.moriafly.salt.ui.screen.BasicScreen

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun BasicScreenSample() {
    com.moriafly.salt.ui.SaltTheme {
        BasicScreen(
            modifier = Modifier
                .background(SaltTheme.colors.background)
                .safeMainCompatPadding(),
            topBar = {
                Column {
                    Text(
                        text = "TopScreenBar",
                        modifier = Modifier
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
                LazyColumn(
                    contentPadding = innerPadding,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val list = (0..75).map { it.toString() }
                    items(count = list.size) {
                        Text(
                            text = list[it],
                            modifier = Modifier
                                .fillMaxWidth()
                                .outerPadding()
                        )
                    }
                }
            }
        )
    }
}
