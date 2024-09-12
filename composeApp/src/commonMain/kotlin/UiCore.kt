import androidx.compose.runtime.Composable
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.darkSaltColors
import com.moriafly.salt.ui.saltConfigs

@Composable
fun UiCore() {
    SaltTheme(
        configs = saltConfigs(
            isDarkTheme = true
        ),
        colors = darkSaltColors()
    ) {
        MainUI()
    }
}