/*
 * Salt UI
 * Copyright (C) 2026 Moriafly
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

package com.moriafly.salt.ui.sample.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.outerPadding

@Suppress("ktlint:compose:modifier-missing-check")
@Composable
fun LicenseScreen() {
    BasicScreenColumn(
        title = "License"
    ) {
        Text(
            text =
                """
                Salt UI
                Copyright (C) 2023-2026 Moriafly

                This library is free software; you can redistribute it and/or
                modify it under the terms of the GNU Lesser General Public
                License as published by the Free Software Foundation; either
                version 2.1 of the License, or (at your option) any later version.

                This library is distributed in the hope that it will be useful,
                but WITHOUT ANY WARRANTY; without even the implied warranty of
                MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
                Lesser General Public License for more details.
                """.trimIndent(),
            modifier = Modifier
                .outerPadding()
        )
    }
}
