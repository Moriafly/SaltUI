import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.moriafly.salt.ui.SaltDynamicColors
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.saltConfigs

@Composable
fun MainActivityContent() {
    val isDarkTheme = isSystemInDarkTheme()
    SaltTheme(
        configs = saltConfigs(
            isDarkTheme = isDarkTheme
        ),
        dynamicColors = SaltDynamicColors.default()
    ) {
        MainScreen()
    }
}
