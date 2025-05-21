import androidx.compose.runtime.Composable
import com.moriafly.salt.core.UnstableSaltCoreApi
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.ItemValue
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.UnstableSaltUiApi

@OptIn(UnstableSaltUiApi::class, UnstableSaltCoreApi::class)
@Composable
actual fun RomUtilColumn() {
    RoundedColumn {
        val version = when (OS.os) {
            OS.Windows -> OS.windowsBuild.toString()
            OS.MacOS -> OS.macOSVersion
            else -> "Unknown"
        }

        ItemValue(text = "Version", sub = version)
    }
}
