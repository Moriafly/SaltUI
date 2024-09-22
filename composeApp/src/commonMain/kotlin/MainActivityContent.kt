import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.darkSaltColors
import com.moriafly.salt.ui.saltConfigs

@Composable
fun MainActivityContent() {
    SaltTheme(
        configs = saltConfigs(
            isDarkTheme = isSystemInDarkTheme()
        ),
        colors = darkSaltColors()
    ) {
        MainScreen()
    }
}