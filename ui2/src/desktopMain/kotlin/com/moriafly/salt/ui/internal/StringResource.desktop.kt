/*
 * SaltUI
 * Copyright (C) 2024 Moriafly
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("UNUSED")

package com.moriafly.salt.ui.internal

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import saltui.ui2.generated.resources.Res
import saltui.ui2.generated.resources.back
import saltui.ui2.generated.resources.cancel
import saltui.ui2.generated.resources.confirm

@Composable
internal actual fun stringResourceBack(): String {
    return stringResource(Res.string.back)
}

@Composable
internal actual fun stringResourceCancel(): String {
    return stringResource(Res.string.cancel)
}

@Composable
internal actual fun stringResourceConfirm(): String {
    return stringResource(Res.string.confirm)
}