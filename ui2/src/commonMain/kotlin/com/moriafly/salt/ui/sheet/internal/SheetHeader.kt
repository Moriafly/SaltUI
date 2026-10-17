/*
 * SaltUI
 * Copyright (C) 2026 Moriafly
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

@file:OptIn(com.moriafly.salt.ui.UnstableSaltUiApi::class)

package com.moriafly.salt.ui.sheet.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.LocalControlSize
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.button.PillButton
import com.moriafly.salt.ui.button.PillButtonDefaults
import com.moriafly.salt.ui.generated.resources.Res
import com.moriafly.salt.ui.generated.resources.sheet_close
import com.moriafly.salt.ui.generated.resources.sheet_collapse
import com.moriafly.salt.ui.generated.resources.sheet_drag
import com.moriafly.salt.ui.generated.resources.sheet_expand
import com.moriafly.salt.ui.generated.resources.sheet_resize
import com.moriafly.salt.ui.icons.Close
import com.moriafly.salt.ui.icons.SaltIcons
import com.moriafly.salt.ui.sheet.SheetProperties
import com.moriafly.salt.ui.thenIf
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
internal fun SheetHeader(
    state: SheetTransitionState,
    title: String?,
    visible: Boolean,
    properties: SheetProperties,
    showHandle: Boolean,
    dragEnabled: Boolean,
    compactChrome: Boolean,
    onDismissRequest: () -> Unit
) {
    val closeButtonSize = PillButtonDefaults.containerHeight(LocalControlSize.current)
    val closeButtonInset = SaltTheme.dimens.padding
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val currentVisible by rememberUpdatedState(visible)
    val currentProperties by rememberUpdatedState(properties)
    val currentDismissRequest by rememberUpdatedState(onDismissRequest)
    val dragModifier = Modifier
        .thenIf(dragEnabled) {
            sheetDrag(
                state = state,
                scope = scope,
                visible = { currentVisible },
                dismissible = { currentProperties.dismissOnDrag },
                onDismissRequest = { currentDismissRequest() }
            )
        }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .thenIf(compactChrome) {
                verticalScroll(state = scrollState)
            }
    ) {
        if (title != null || properties.dismissOnCloseButtonClick) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = closeButtonSize + closeButtonInset * 2)
                    .padding(
                        start = closeButtonInset,
                        top = 8.dp,
                        end = if (properties.dismissOnCloseButtonClick) {
                            closeButtonInset + closeButtonSize + 12.dp
                        } else {
                            closeButtonInset
                        },
                        bottom = 8.dp
                    )
                    .thenIf(!compactChrome) {
                        then(dragModifier)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        modifier = Modifier
                            .weight(weight = 1f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        maxLines = 2
                    )
                } else {
                    Spacer(
                        modifier = Modifier
                            .weight(weight = 1f)
                    )
                }
            }
        }
        if (showHandle) {
            SheetDragHandle(
                state = state,
                enabled = dragEnabled,
                modifier = Modifier
                    .align(alignment = Alignment.TopCenter)
                    .then(dragModifier),
                onClick = {
                    scope.launch {
                        val index = state.points.indexOfFirst { it.first == state.selected }
                        val direction = if (index == state.points.lastIndex) -1 else 1
                        state.changeHeight(direction = direction)
                    }
                }
            )
        }
        if (properties.dismissOnCloseButtonClick) {
            SheetCloseButton(
                onClick = onDismissRequest,
                enabled = visible,
                modifier = Modifier
                    .align(alignment = Alignment.TopEnd)
                    .padding(
                        top = closeButtonInset,
                        end = closeButtonInset
                    )
            )
        }
    }
}

@Composable
private fun SheetDragHandle(
    state: SheetTransitionState,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val canResize = state.points.size > 1
    val dragLabel = stringResource(
        resource = if (canResize) Res.string.sheet_resize else Res.string.sheet_drag
    )
    val heightPercent = (
        state.selectedHeight / state.viewportHeight.coerceAtLeast(1f) * 100
    ).roundToInt()
    Box(
        modifier = Modifier
            .size(
                width = SheetDefaults.DragHandleTouchHeight,
                height = SheetDefaults.DragHandleTouchHeight
            )
            .then(modifier)
            .thenIf(canResize) {
                clickable(
                    enabled = enabled,
                    interactionSource = null,
                    indication = null,
                    onClick = onClick
                )
            }
            .semantics {
                contentDescription = dragLabel
                stateDescription = "$heightPercent%"
            }
            .sheetHeightActions(
                state = state,
                enabled = enabled
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .padding(top = SheetDefaults.DragHandleTopInset)
                .size(
                    width = SheetDefaults.DragHandleWidth,
                    height = SheetDefaults.DragHandleHeight
                )
                .background(
                    color = SaltTheme.colors.subText.copy(
                        alpha = SheetDefaults.DRAG_HANDLE_OPACITY
                    ),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun SheetCloseButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier
) {
    val closeLabel = stringResource(resource = Res.string.sheet_close)
    PillButton(
        onClick = onClick,
        modifier = modifier
            .semantics { contentDescription = closeLabel },
        enabled = enabled
    ) {
        Icon(
            imageVector = SaltIcons.Close,
            contentDescription = null
        )
    }
}

@Composable
internal fun Modifier.sheetHeightActions(
    state: SheetTransitionState,
    enabled: Boolean
): Modifier {
    val scope = rememberCoroutineScope()
    val expandLabel = stringResource(resource = Res.string.sheet_expand)
    val collapseLabel = stringResource(resource = Res.string.sheet_collapse)
    return semantics {
        if (enabled && state.points.size > 1) {
            val index = state.points.indexOfFirst { it.first == state.selected }
            customActions = buildList {
                if (index < state.points.lastIndex) {
                    add(
                        CustomAccessibilityAction(
                            label = expandLabel,
                            action = {
                                scope.launch { state.changeHeight(direction = 1) }
                                true
                            }
                        )
                    )
                }
                if (index > 0) {
                    add(
                        CustomAccessibilityAction(
                            label = collapseLabel,
                            action = {
                                scope.launch { state.changeHeight(direction = -1) }
                                true
                            }
                        )
                    )
                }
            }
        }
    }
}
