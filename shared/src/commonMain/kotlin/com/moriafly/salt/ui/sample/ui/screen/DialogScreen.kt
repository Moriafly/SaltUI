package com.moriafly.salt.ui.sample.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.ItemButton
import com.moriafly.salt.ui.ItemValue
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.dialog.AdaptiveDialogSize
import com.moriafly.salt.ui.dialog.BasicAdaptiveDialog
import com.moriafly.salt.ui.dialog.InputDialog
import com.moriafly.salt.ui.dialog.YesDialog
import com.moriafly.salt.ui.dialog.YesNoDialog
import com.moriafly.salt.ui.popup.rememberPopupState
import com.moriafly.salt.ui.sample.ui.screen.basic.BasicScreenColumn

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun DialogScreen() {
    var inputVisible by rememberSaveable { mutableStateOf(false) }
    var draft by rememberSaveable { mutableStateOf("") }
    var savedText by rememberSaveable { mutableStateOf("") }

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
        if (inputVisible) {
            InputDialog(
                onDismissRequest = { inputVisible = false },
                onConfirm = {
                    savedText = draft
                    inputVisible = false
                },
                onChange = { draft = it },
                title = "Edit name",
                text = draft,
                hint = "Enter a name",
                confirmText = "Save"
            )
        }

        RoundedColumn {
            ItemButton(
                onClick = {
                    basicAdaptiveDialogPopupState.expend()
                },
                text = "BasicAdaptiveDialog"
            )
            ItemButton(
                onClick = {
                    yesDialogPopupState.expend()
                },
                text = "YesDialog"
            )
            ItemButton(
                onClick = {
                    yesNoDialogPopupState.expend()
                },
                text = "YesNoDialog"
            )
            ItemButton(
                onClick = {
                    draft = savedText
                    inputVisible = true
                },
                text = "InputDialog"
            )
            ItemValue(text = "Saved name", sub = savedText.ifBlank { "None" })
        }
    }
}
