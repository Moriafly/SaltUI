/*
 * Salt UI
 * Copyright (C) 2025 Moriafly
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package window

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.application
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.outerPadding
import com.moriafly.salt.ui.window.SaltWindow

fun main() {
    application {
        TextWindow(
            onCloseRequest = ::exitApplication
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, UnstableSaltUiApi::class)
@Composable
private fun TextWindow(
    onCloseRequest: () -> Unit,
) {
    SaltWindow(
        onCloseRequest = onCloseRequest,
        title = "Transparent Window"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Text(
                text = "Word",
                modifier = Modifier
                    .outerPadding(),
                color = Color.Black,
                fontSize = 36.sp
            )
            Text(
                text = buildAnnotatedString {
                    append("Word")
                    addStyle(
                        style = SpanStyle(
                            color = Color.Black
                        ),
                        start = 0,
                        end = 4
                    )
                    addStyle(
                        style = SpanStyle(
                            color = Color.Red
                        ),
                        start = 1,
                        end = 2
                    )
                },
                modifier = Modifier
                    .outerPadding(),
                color = Color.Black,
                fontSize = 36.sp
            )
        }
    }
}