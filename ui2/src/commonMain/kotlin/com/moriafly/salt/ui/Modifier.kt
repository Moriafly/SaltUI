/*
 * Salt UI
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

package com.moriafly.salt.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Conditionally applies additional [Modifier]
 *
 * @param condition The boolean condition
 * @param block The block to apply if the condition is true
 * @return The modified [Modifier] or the original one
 */
@Composable
fun Modifier.thenIf(condition: Boolean, block: @Composable Modifier.() -> Modifier): Modifier = if (condition) block() else this