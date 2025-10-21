import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.application
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.util.findSkiaLayer
import com.moriafly.salt.ui.window.SaltWindow

@OptIn(ExperimentalComposeUiApi::class, UnstableSaltUiApi::class)
fun main() = application {
    SaltWindow(
        onCloseRequest = {
        },
        title = "Demo",
        init = { composeWindow ->
            composeWindow.findSkiaLayer()?.transparency = true
        }
    ) {
        val interactionSource = remember { MutableInteractionSource() }

        val isHovered by interactionSource.collectIsHoveredAsState()
        val backgroundColor = if (isHovered) {
            Color.Red
        } else {
            Color.Unspecified
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                }
        )
    }
}
