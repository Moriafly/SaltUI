import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.SaltWindow
import com.moriafly.salt.ui.window.SaltWindowProperties

@OptIn(ExperimentalComposeUiApi::class, UnstableSaltUiApi::class)
fun main() {
    application {
        var visible by remember { mutableStateOf(true) }
        LaunchedEffect(visible) {
            println("Out window visible: $visible")
        }

        SaltWindow(
            onCloseRequest = ::exitApplication,
            visible = visible,
            properties = SaltWindowProperties(
                minSize = DpSize(200.dp, 200.dp),
                onVisibleChanged = { window, isVisible ->
                    println("isVisible: $isVisible")
                }
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

        SaltWindow(
            onCloseRequest = ::exitApplication,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Green)
            ) {
                Button(
                    onClick = {
                        visible = !visible
                    },
                    text = "显示/隐藏"
                )
            }
        }
    }
}
