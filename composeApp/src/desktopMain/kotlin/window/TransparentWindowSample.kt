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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.application
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.outerPadding
import com.moriafly.salt.ui.window.SaltWindow

fun main() {
    application {
        TransparentWindow(
            onCloseRequest = ::exitApplication
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, UnstableSaltUiApi::class)
@Composable
private fun TransparentWindow(
    onCloseRequest: () -> Unit,
) {
    SaltWindow(
        onCloseRequest = onCloseRequest,
        title = "Transparent Window",
        decoration = WindowDecoration.Undecorated(),
        transparent = true
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Text(
                text = "Hello World",
                modifier = Modifier
                    .outerPadding()
            )
        }
    }
}