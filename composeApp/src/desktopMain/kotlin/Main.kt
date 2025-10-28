import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    var captionButtonsVisible by remember { mutableStateOf(true) }
    SaltWindow(
        onCloseRequest = ::exitApplication,
        title = "Salt UI",
//        decoration = WindowDecoration.Undecorated(),
//        transparent = true,
        resizable = true,
        properties = SaltWindowProperties(
            captionButtonsVisible = captionButtonsVisible,
            captionButtonIsDarkTheme = isDarkTheme
        )
    ) {
//        MainActivityContent(
//            isDarkTheme = isDarkTheme
//        )

        CaptionBarHitTest()

//        Button(
//            onClick = {
//                captionButtonsVisible = !captionButtonsVisible
//            },
//            text = "Change Caption Buttons Visible"
//        )

        BasicScreenSample()

//        SaltDialogWindow(
//            onCloseRequest = {
//            },
//            title = "Dialog",
//        ) {
//            CaptionBarHitTest()
//        }
    }
}
