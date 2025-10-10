/**
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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemDropdown
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltConfigs
import com.moriafly.salt.ui.SaltMaterial
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.dialog.BasicDialog
import com.moriafly.salt.ui.material.MaterialSource
import com.moriafly.salt.ui.material.MaterialType
import com.moriafly.salt.ui.popup.PopupMenuItem
import org.jetbrains.compose.resources.painterResource
import saltui.composeapp.generated.resources.Res
import saltui.composeapp.generated.resources.bg_wallpaper

@OptIn(UnstableSaltUiApi::class)
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(
            size = DpSize(
                width = 460.dp,
                height = 1000.dp
            )
        ),
        title = "Multi Blur",
    ) {
        MaterialScreen()
    }
}
