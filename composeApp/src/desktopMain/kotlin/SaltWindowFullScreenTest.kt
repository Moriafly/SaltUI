import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.CaptionBarHitTest
import com.moriafly.salt.ui.window.SaltWindow

@OptIn(ExperimentalComposeUiApi::class, UnstableSaltUiApi::class)
fun main() = application {
    SaltWindow(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(
            placement = WindowPlacement.Fullscreen
        )
    ) {
        CaptionBarHitTest()
    }
}
