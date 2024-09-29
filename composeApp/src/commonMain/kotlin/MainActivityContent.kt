import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.darkSaltColors
import com.moriafly.salt.ui.lightSaltColors
import com.moriafly.salt.ui.saltConfigs

@Composable
fun MainActivityContent() {
    val isDarkTheme = isSystemInDarkTheme()
    SaltTheme(
        configs = saltConfigs(
            isDarkTheme = isDarkTheme
        ),
        colors = if (isDarkTheme) {
            darkSaltColors()
        } else {
            lightSaltColors()
        }
    ) {
        MainScreen()
    }
}