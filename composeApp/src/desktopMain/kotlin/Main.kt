import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.application
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.SaltWindow

@OptIn(ExperimentalComposeUiApi::class, UnstableSaltUiApi::class)
fun main() = application {
    SaltWindow(
        onCloseRequest = ::exitApplication,
        title = "SaltUI",
    ) {
        MainActivityContent()
        Button(
            onClick = {
                throw Exception("Test")
            },
            text = "Throw Exception"
        )
    }
}
