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

@file:Suppress("unused")

package com.moriafly.salt.ui

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.ExperimentalExtendedContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Conditionally applies additional [Modifier].
 *
 * @param condition The boolean condition.
 * @param block The block to apply if the condition is true.
 * @return The modified [Modifier] or the original one.
 */
@OptIn(ExperimentalContracts::class, ExperimentalExtendedContracts::class)
inline fun Modifier.thenIf(condition: Boolean, block: Modifier.() -> Modifier): Modifier {
    contract {
        // Declares that the lambda runs at most once
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        // Declares that the condition is assumed to be true inside the lambda
        // See:
        // - https://kotlinlang.org/docs/whatsnew2220.html#improved-kotlin-contracts
        // - [Contract to specify that a function parameter is always true inside lambda](https://youtrack.jetbrains.com/issue/KT-32993)
        condition holdsIn block
    }
    return if (condition) block() else this
}

/**
 * Enabled alpha modifier.
 */
@Stable
fun Modifier.enabledAlpha(enabled: Boolean): Modifier = if (enabled) this else alpha(0.5f)

/**
 * **Desktop Only**
 *
 * Compatibility version of Modifier.onPointerEvent.
 */
@UnstableSaltUiApi
expect fun Modifier.onPointerEventCompat(
    eventType: PointerEventType,
    pass: PointerEventPass = PointerEventPass.Main,
    onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit
): Modifier
