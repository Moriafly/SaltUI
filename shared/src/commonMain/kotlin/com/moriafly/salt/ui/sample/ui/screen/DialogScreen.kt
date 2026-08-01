package com.moriafly.salt.ui.sample.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemArrowType
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.dialog.AdaptiveDialogSize
import com.moriafly.salt.ui.dialog.BasicAdaptiveDialog
import com.moriafly.salt.ui.dialog.YesDialog
import com.moriafly.salt.ui.dialog.YesNoDialog
import com.moriafly.salt.ui.popup.rememberPopupState
import com.moriafly.salt.ui.sample.ui.screen.basic.BasicScreenColumn

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun DialogScreen() {
    BasicScreenColumn(
        title = "Dialog"
    ) {
        val basicAdaptiveDialogPopupState = rememberPopupState()
        val yesDialogPopupState = rememberPopupState()
        val yesNoDialogPopupState = rememberPopupState()

        if (basicAdaptiveDialogPopupState.expend) {
            BasicAdaptiveDialog(
                onDismissRequest = {
                    basicAdaptiveDialogPopupState.dismiss()
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
        if (yesDialogPopupState.expend) {
            YesDialog(
                onDismissRequest = {
                    yesDialogPopupState.dismiss()
                },
                title = "Changes saved",
                content = "Your settings have been saved successfully",
                confirmText = "Done"
            )
        }
        if (yesNoDialogPopupState.expend) {
            YesNoDialog(
                onDismissRequest = {
                    yesNoDialogPopupState.dismiss()
                },
                onConfirm = {
                    yesNoDialogPopupState.dismiss()
                },
                title = "Continue?",
                content = "Would you like to continue with this action?",
                cancelText = "Cancel",
                confirmText = "Continue"
            )
        }

        RoundedColumn {
            Item(
                onClick = {
                    basicAdaptiveDialogPopupState.expend()
                },
                text = "BasicAdaptiveDialog",
                arrowType = ItemArrowType.Link
            )
            Item(
                onClick = {
                    yesDialogPopupState.expend()
                },
                text = "YesDialog",
                arrowType = ItemArrowType.Link
            )
            Item(
                onClick = {
                    yesNoDialogPopupState.expend()
                },
                text = "YesNoDialog",
                arrowType = ItemArrowType.Link
            )
        }
    }
}
