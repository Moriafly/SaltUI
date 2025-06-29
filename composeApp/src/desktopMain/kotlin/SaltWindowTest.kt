import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.SaltDialogWindow
import com.moriafly.salt.ui.window.SaltWindow
import com.moriafly.salt.ui.window.SaltWindowProperties

@OptIn(ExperimentalComposeUiApi::class, UnstableSaltUiApi::class)
fun main() {
    application {
        SaltWindow(
            onCloseRequest = ::exitApplication,
            // decoration = WindowDecoration.Undecorated(),
            alwaysOnTop = true,
            properties = SaltWindowProperties(
                minSize = DpSize(200.dp, 200.dp)
            )
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

        SaltDialogWindow(
            onCloseRequest = ::exitApplication,
            // decoration = WindowDecoration.Undecorated(),
            alwaysOnTop = true,
            properties = SaltWindowProperties(
                minSize = DpSize(200.dp, 200.dp)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.Green)
                )
            }
        }
    }
}
