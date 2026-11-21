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

package com.moriafly.salt.ui.popup

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathHitTester
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.internal.SmoothRoundedRectangleShape
import com.moriafly.salt.ui.material.drawGlassInteraction
import com.moriafly.salt.ui.material.glassInteractionLight
import com.moriafly.salt.ui.material.glassPointerInput
import com.moriafly.salt.ui.material.rememberGlassInteractionState
import com.moriafly.salt.ui.rememberScrollState
import com.moriafly.salt.ui.thenIf
import com.moriafly.salt.ui.verticalScroll
import kotlin.math.roundToInt

/**
 * Hosts one menu panel in a Compose popup and manages its placement and visibility.
 */
@OptIn(UnstableSaltUiApi::class)
@Composable
internal fun PopupMenuPanel(
    expanded: Boolean,
    onDismissRequest: (() -> Unit)?,
    offset: DpOffset,
    properties: PopupProperties,
    style: PopupMenuStyle,
    anchor: PopupMenuAnchor?,
    popupPositionProvider: PopupPositionProvider?,
    scrollable: Boolean,
    modifier: Modifier = Modifier,
    useIntrinsicWidth: Boolean = true,
    dropdownSlideSelection: Boolean = false,
    isSlideSelecting: (() -> Boolean)? = null,
    visible: MutableTransitionState<Boolean> = remember { MutableTransitionState(false) },
    content: @Composable ColumnScope.() -> Unit
) {
    val desktop = OS.isDesktop()
    val pointerPosition = if (expanded && anchor != null) {
        val position = remember(anchor) { anchor.pendingPositionInWindow }
        DisposableEffect(anchor) {
            anchor.onMenuOpened()
            onDispose { anchor.onMenuClosed() }
        }
        position
    } else {
        null
    }
    visible.targetState = expanded
    if (expanded || (!desktop && (!visible.isIdle || visible.currentState))) {
        val density = LocalDensity.current
        val offsetPx = with(density) { IntOffset(offset.x.roundToPx(), offset.y.roundToPx()) }
        var placement by remember { mutableStateOf<MenuPlacement?>(null) }
        val positionOrigin = properties.menuPositionOrigin()
        val positionProvider = remember(
            pointerPosition,
            offsetPx,
            popupPositionProvider,
            density,
            desktop,
            positionOrigin
        ) {
            val pointerGap = if (desktop) 0 else with(density) { 12.dp.roundToPx() }
            val margin = with(density) { (if (desktop) 8.dp else 12.dp).roundToPx() }
            createMenuPositionProvider { bounds, windowSize, direction, popupSize ->
                val placementAnchor = if (pointerPosition != null) {
                    val position = IntOffset(
                        x = pointerPosition.x.roundToInt(),
                        y = pointerPosition.y.roundToInt()
                    ) - positionOrigin
                    IntRect(position, IntSize.Zero)
                } else {
                    bounds
                }
                val position = popupPositionProvider?.calculatePosition(
                    bounds,
                    windowSize,
                    direction,
                    popupSize
                ) ?: menuPosition(
                    anchor = placementAnchor,
                    windowSize = windowSize,
                    direction = direction,
                    popupSize = popupSize,
                    offset = offsetPx,
                    gap = if (pointerPosition != null) pointerGap else 0,
                    margin = margin
                )
                if (!desktop) {
                    placement =
                        MenuPlacement.resolve(placementAnchor, IntRect(position, popupSize))
                }
                position
            }
        }

        var lastOpenPosition by remember { mutableStateOf<PopupPositionProvider?>(null) }
        SideEffect {
            if (!desktop && expanded) {
                lastOpenPosition = positionProvider
            }
        }

        Popup(
            onDismissRequest = if (expanded) onDismissRequest else null,
            popupPositionProvider = if (expanded) {
                positionProvider
            } else {
                lastOpenPosition
                    ?: positionProvider
            },
            properties = if (expanded) properties else properties.forMenuExit()
        ) {
            MenuSurface(
                visible = visible,
                placement = { placement },
                desktop = desktop,
                style = style,
                scrollable = scrollable,
                useIntrinsicWidth = useIntrinsicWidth,
                dropdownSlideSelection = dropdownSlideSelection,
                isSlideSelecting = isSlideSelecting,
                onDismissOutside = onDismissRequest.takeIf {
                    expanded && properties.dismissOnClickOutside
                },
                modifier = modifier,
                content = content
            )
        }
    }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun MenuSurface(
    visible: MutableTransitionState<Boolean>,
    placement: () -> MenuPlacement?,
    desktop: Boolean,
    style: PopupMenuStyle,
    scrollable: Boolean,
    useIntrinsicWidth: Boolean,
    dropdownSlideSelection: Boolean,
    isSlideSelecting: (() -> Boolean)?,
    onDismissOutside: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val transition = if (desktop) null else rememberTransition(visible, label = "Popup menu")
    val progress = transition?.animateFloat(
        transitionSpec = {
            if (targetState) {
                spring(dampingRatio = 0.72f, stiffness = 700f, visibilityThreshold = 0.008f)
            } else {
                tween(105, easing = FastOutLinearInEasing)
            }
        },
        label = "Panel motion"
    ) { if (it) 1f else 0f }
    val opacity = transition?.animateFloat(
        transitionSpec = {
            if (targetState) {
                tween(120, easing = LinearOutSlowInEasing)
            } else {
                tween(90, easing = FastOutLinearInEasing)
            }
        },
        label = "Panel opacity"
    ) { if (it) 1f else 0f }

    val backdrop = LocalPopupBackdrop.current.takeIf { style.material != PopupMenuMaterial.Solid }
    val reveal = backdrop?.revealWindowBackdrop == true && popupSharesHostCanvas
    val dark = SaltTheme.configs.isDarkTheme
    val menuRadius = if (desktop) 10.dp else mobileMenuCornerRadius()
    val shape = remember(menuRadius) { SmoothRoundedRectangleShape(menuRadius) }
    val material = menuMaterial(
        shape = shape,
        cornerRadius = menuRadius,
        style = style,
        backdrop = backdrop,
        revealWindowBackdrop = reveal,
        dark = dark
    )
    val glassInteraction = if (backdrop != null) rememberGlassInteractionState() else null
    val windowSize = LocalWindowInfo.current.containerDpSize
    val maxWidth = (windowSize.width - 24.dp).coerceAtLeast(1.dp)
    val maxHeight = (windowSize.height - 24.dp).coerceAtLeast(1.dp)
    val scrollState = rememberScrollState()
    val direction = LocalLayoutDirection.current
    val currentDismiss by rememberUpdatedState(onDismissOutside)
    val dropdownDrag = if (dropdownSlideSelection && !desktop) {
        remember { MenuDragSelection<DropdownMenuItem>() }
    } else {
        null
    }
    val currentSlideSelecting by rememberUpdatedState(isSlideSelecting)
    val followsConsumedMovement = remember(dropdownDrag) {
        {
            dropdownDrag?.isSlideSelecting == true || currentSlideSelecting?.invoke() == true
        }
    }
    Box(
        modifier = modifier
            .widthIn(max = if (windowSize.width > 0.dp) maxWidth else Dp.Unspecified)
            .heightIn(max = if (windowSize.height > 0.dp) maxHeight else Dp.Unspecified)
            .thenIf(useIntrinsicWidth) { width(IntrinsicSize.Max) }
            .thenIf(reveal) { revealPopupWindowBackdrop(shape) }
            .pointerInput(shape, direction, desktop) {
                awaitEachGesture {
                    val down =
                        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    val panelSize = size.toSize()
                    val position = placement()
                    val point = when {
                        desktop -> down.position
                        position != null && (opacity?.value ?: 0f) > 0f -> {
                            // Match the panel's entrance layer before testing the outline.
                            val fraction = progress?.value ?: 1f
                            val pivot = Offset(
                                panelSize.width * position.pivot.x,
                                panelSize.height * position.pivot.y
                            )
                            val shift = position.shift(fraction, MobileMenuTravel.toPx())
                            pivot + (down.position - shift - pivot) / menuScale(fraction)
                        }
                        else -> null
                    }
                    val inside = if (point != null) {
                        val outline = shape.createOutline(panelSize, direction, this)
                        val path = Path().apply { addOutline(outline) }
                        point in PathHitTester(path)
                    } else {
                        false
                    }
                    if (!inside) {
                        down.consume()
                        currentDismiss?.invoke()
                    }
                }
            }
            .thenIf(visible.targetState && glassInteraction != null) {
                glassPointerInput(requireNotNull(glassInteraction), followsConsumedMovement)
            }
            .thenIf(!visible.targetState) {
                clearAndSetSemantics { hideFromAccessibility() }
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent(PointerEventPass.Initial).changes.forEach {
                                    it.consume()
                                }
                            }
                        }
                    }
            },
        propagateMinConstraints = true
    ) {
        Box(
            modifier = Modifier
                .thenIf(!desktop) {
                    graphicsLayer {
                        val position = placement()
                        if (position == null) {
                            alpha = 0f
                        } else {
                            val fraction = progress?.value ?: 1f
                            val scale = menuScale(fraction)
                            val shift = position.shift(fraction, MobileMenuTravel.toPx())
                            transformOrigin = TransformOrigin(position.pivot.x, position.pivot.y)
                            scaleX = scale
                            scaleY = scale
                            translationX = shift.x
                            translationY = shift.y
                            alpha = opacity?.value ?: 1f
                        }
                    }
                }
                .thenIf(!desktop && glassInteraction != null) {
                    drawGlassInteraction(requireNotNull(glassInteraction), maxTranslation = 3.dp)
                }
                .thenIf(desktop) {
                    menuShadow(
                        shape = shape,
                        dark = dark,
                        desktop = true,
                        shadowContext = LocalGraphicsContext.current.shadowContext
                    )
                }
                .clip(shape),
            propagateMinConstraints = true
        ) {
            Box(Modifier.matchParentSize().graphicsLayer().then(material))
            if (glassInteraction != null) {
                Box(
                    Modifier
                        .matchParentSize()
                        .graphicsLayer()
                        .glassInteractionLight(glassInteraction, shape, dark)
                )
            }
            val padding = if (desktop) DesktopMenuContentPadding else MobileMenuContentPadding
            val dragModifier = if (dropdownDrag != null) {
                Modifier
                    .onGloballyPositioned { dropdownDrag.coordinates = it }
                    .menuDragSelection(
                        selection = dropdownDrag,
                        enabled = visible.targetState,
                        canSlide = { scrollState.maxValue == 0 },
                        entryEnabled = { true },
                        onActivate = { it.onClick() }
                    )
            } else {
                Modifier
            }
            Column(
                modifier = Modifier
                    .padding(padding)
                    .thenIf(scrollable) {
                        verticalScroll(
                            scrollState,
                            enabled =
                                visible.targetState && scrollState.maxValue > 0
                        )
                    }
                    .then(dragModifier)
            ) {
                CompositionLocalProvider(LocalDropdownDragSelection provides dropdownDrag) {
                    content()
                }
            }
        }
    }
}

internal val DesktopMenuContentPadding = 4.dp
private val MobileMenuTravel = 4.dp

internal expect fun PopupProperties.forMenuExit(): PopupProperties

@Composable
internal expect fun PopupProperties.menuPositionOrigin(): IntOffset

internal fun createMenuPositionProvider(
    calculate: (IntRect, IntSize, LayoutDirection, IntSize) -> IntOffset
): PopupPositionProvider = object : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset = calculate(anchorBounds, windowSize, layoutDirection, popupContentSize)
}

private fun menuPosition(
    anchor: IntRect,
    windowSize: IntSize,
    direction: LayoutDirection,
    popupSize: IntSize,
    offset: IntOffset,
    gap: Int,
    margin: Int
): IntOffset {
    val right = (windowSize.width - popupSize.width - margin).coerceAtLeast(margin)
    val bottom = (windowSize.height - popupSize.height - margin).coerceAtLeast(margin)
    val start = anchor.left + offset.x
    val end = anchor.right - popupSize.width - offset.x
    val horizontal = if (direction ==
        LayoutDirection.Ltr
    ) {
        listOf(start, end)
    } else {
        listOf(end, start)
    }
    val x =
        horizontal.firstOrNull { it in margin..right } ?: horizontal.first().coerceIn(margin, right)
    val below = anchor.bottom + offset.y + gap
    val above = anchor.top - offset.y - gap - popupSize.height
    val spaceBelow = windowSize.height - margin - below
    val spaceAbove = above + popupSize.height - margin
    val y = when {
        below in margin..bottom -> below
        above in margin..bottom -> above
        spaceBelow >= spaceAbove -> below.coerceIn(margin, bottom)
        else -> above.coerceIn(margin, bottom)
    }
    return IntOffset(x, y)
}

private fun menuScale(progress: Float): Float = 0.91f + 0.09f * progress

private data class MenuPlacement(
    val pivot: Offset,
    val approach: Offset
) {
    fun shift(progress: Float, distance: Float): Offset = approach * (distance * (1f - progress))

    companion object {
        fun resolve(anchor: IntRect, menu: IntRect): MenuPlacement = MenuPlacement(
            pivot = Offset(
                axisPivot(anchor.left, anchor.right, menu.left, menu.right),
                axisPivot(anchor.top, anchor.bottom, menu.top, menu.bottom)
            ),
            approach = when {
                menu.top >= anchor.bottom -> Offset(0f, -1f)
                menu.bottom <= anchor.top -> Offset(0f, 1f)
                menu.left >= anchor.right -> Offset(-1f, 0f)
                menu.right <= anchor.left -> Offset(1f, 0f)
                else -> Offset(0f, -1f)
            }
        )

        private fun axisPivot(
            anchorStart: Int,
            anchorEnd: Int,
            start: Int,
            end: Int
        ): Float = when {
            start == end || start >= anchorEnd -> 0f
            end <= anchorStart -> 1f
            else -> ((maxOf(anchorStart, start) + minOf(anchorEnd, end)) / 2f - start) /
                (end - start)
        }
    }
}
