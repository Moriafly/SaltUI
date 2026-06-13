package com.moriafly.salt.ui.sample.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.dialog.AdaptiveDialogSize
import com.moriafly.salt.ui.dialog.BasicAdaptiveDialog
import com.moriafly.salt.ui.sample.ui.navigation.LocalNavBackStack
import com.moriafly.salt.ui.screen.BasicScreen

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun DialogScreen() {
    val navBackStack = LocalNavBackStack.current
    BasicScreen(
        onBack = {
            navBackStack.removeLastOrNull()
        },
        title = "Dialog"
    ) { contentPadding ->
        BasicAdaptiveDialog(
            onDismissRequest = {
            },
            size = AdaptiveDialogSize.Min
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.Blue)
            ) {
            }
        }
    }
}
