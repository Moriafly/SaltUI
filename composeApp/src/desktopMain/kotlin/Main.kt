import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.application
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.window.CaptionBarHitTest
import com.moriafly.salt.ui.window.SaltDialogWindow
import com.moriafly.salt.ui.window.SaltWindow
import com.moriafly.salt.ui.window.SaltWindowProperties

@OptIn(ExperimentalComposeUiApi::class, UnstableSaltUiApi::class)
fun main() = application {
    val isDarkTheme = false
    SaltWindow(
        onCloseRequest = ::exitApplication,
        title = "Salt UI",
//        decoration = WindowDecoration.Undecorated(),
//        transparent = true,
        // resizable = false
        properties = SaltWindowProperties(
            captionButtonIsDarkTheme = isDarkTheme
        )
    ) {
        MainActivityContent(
            isDarkTheme = isDarkTheme
        )

        CaptionBarHitTest()

        Button(
            onClick = {
                throw Exception("Test")
            },
            text = "Throw Exception"
        )

        SaltDialogWindow(
            onCloseRequest = {
            },
            title = "Dialog"
        ) {
            CaptionBarHitTest()
        }
    }
}
