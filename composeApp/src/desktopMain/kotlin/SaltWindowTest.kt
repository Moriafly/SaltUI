import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.CaptionBarHitTest
import com.moriafly.salt.ui.window.SaltWindow
import com.moriafly.salt.ui.window.SaltWindowProperties

@OptIn(ExperimentalComposeUiApi::class, UnstableSaltUiApi::class)
fun main() {
    application {
        val state = rememberWindowState()
        SaltWindow(
            onCloseRequest = ::exitApplication,
            state = state,
            // decoration = WindowDecoration.Undecorated(),
            alwaysOnTop = true,
            properties = SaltWindowProperties(
                minSize = DpSize(200.dp, 200.dp)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                CaptionBarHitTest()

                Button(
                    onClick = {
                        if (state.placement == WindowPlacement.Fullscreen) {
                            state.placement = WindowPlacement.Floating
                        } else {
                            state.placement = WindowPlacement.Fullscreen
                        }
                    },
                    text = "Change Fullscreen"
                )
            }
        }
    }
}
