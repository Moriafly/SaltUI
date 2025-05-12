package com.moriafly.salt.ui

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent

/**
 * **Desktop Only**
 *
 * Compatibility version of Modifier.onPointerEvent.
 */
@UnstableSaltUiApi
@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.onPointerEventCompat(
    eventType: PointerEventType,
    pass: PointerEventPass,
    onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit
): Modifier = onPointerEvent(eventType, pass, onEvent)
