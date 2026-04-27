import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.moriafly.salt.ui.SaltConfigs
import com.moriafly.salt.ui.SaltDynamicColors
import com.moriafly.salt.ui.SaltTheme

@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    SaltTheme(
        configs = SaltConfigs.default(
            isDarkTheme = isDarkTheme
        ),
        dynamicColors = SaltDynamicColors.default(),
        content = content
    )
}
