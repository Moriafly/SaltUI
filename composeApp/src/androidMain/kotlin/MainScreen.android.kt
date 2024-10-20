import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.moriafly.salt.ui.ItemOuterTitle
import com.moriafly.salt.ui.ItemValue
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.util.RomUtil

@OptIn(UnstableSaltApi::class)
@Composable
actual fun RomUtilColumn() {
    ItemOuterTitle(text = "RomUtil")
    RoundedColumn {
        ItemValue(
            text = "ro.product.system.manufacturer",
            sub = remember { RomUtil.getSystemProperty("ro.product.system.manufacturer") }
        )
        ItemValue(
            text = "ro.mi.os.version.name",
            sub = remember { RomUtil.getSystemProperty("ro.mi.os.version.name") }
        )
        ItemValue(
            text = "isXiaomi",
            sub = RomUtil.isXiaomi.toString()
        )
    }
}