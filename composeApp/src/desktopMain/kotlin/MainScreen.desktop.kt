import androidx.compose.runtime.Composable
import com.moriafly.salt.ui.ItemValue
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.util.SystemUtil
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

@Composable
actual fun RomUtilColumn() {
    RoundedColumn {
        val version = when (hostOs) {
            OS.Windows -> SystemUtil.windowsBuild.toString()
            OS.MacOS -> SystemUtil.macOSVersion
            else -> "Unknown"
        }

        ItemValue(text = "Version", sub = version)
    }
}