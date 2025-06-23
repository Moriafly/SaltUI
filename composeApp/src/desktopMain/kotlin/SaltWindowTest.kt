import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.application
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.SaltDialogWindow

@OptIn(ExperimentalComposeUiApi::class, UnstableSaltUiApi::class)
fun main() {
    application {
        SaltDialogWindow(
            onCloseRequest = ::exitApplication,
            decoration = WindowDecoration.Undecorated(),
            minSize = DpSize(200.dp, 200.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.Blue)
                )
            }
        }
    }
}
