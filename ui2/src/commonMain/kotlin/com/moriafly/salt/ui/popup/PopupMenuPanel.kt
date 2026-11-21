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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathHitTester
import androidx.compose.ui.graphics.Shape
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
import com.moriafly.salt.ui.material.GlassInteractionState
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
 *
 * The outer popup layout owns the shadow gutter. The caller's modifier belongs to the panel.
 */
@Suppress("ktlint:compose:modifier-not-used-at-root")
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
    val pointerPosition = rememberMenuOpeningPosition(anchor, expanded)
    visible.targetState = expanded
    if (expanded || (!desktop && (!visible.isIdle || visible.currentState))) {
        val shadowGutter = if (OS.isAndroid()) MenuShadowGutter else 0.dp
        val inset = with(LocalDensity.current) { shadowGutter.roundToPx() }
        var placement by remember { mutableStateOf<MenuPlacement?>(null) }
        val positionProvider = rememberMenuPositionProvider(
            pointerPosition = pointerPosition,
            offset = offset,
            popupPositionProvider = popupPositionProvider,
            properties = properties,
            inset = inset,
            desktop = desktop,
            onPlacement = { placement = it }
        )

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
            val dismissOutside = onDismissRequest.takeIf {
                expanded && properties.dismissOnClickOutside
            }
            Box {
                if (inset > 0 && dismissOutside != null) {
                    MenuShadowDismissRegion(dismissOutside)
                }

                Box(Modifier.padding(shadowGutter)) {
                    MenuSurface(
                        modifier = modifier,
                        visible = visible,
                        placement = { placement },
                        desktop = desktop,
                        style = style,
                        scrollable = scrollable,
                        useIntrinsicWidth = useIntrinsicWidth,
                        dropdownSlideSelection = dropdownSlideSelection,
                        isSlideSelecting = isSlideSelecting,
                        onDismissOutside = dismissOutside,
                        content = content
                    )
                }
            }
        }
    }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun rememberMenuOpeningPosition(anchor: PopupMenuAnchor?, expanded: Boolean): Offset? {
    if (!expanded || anchor == null) {
        return null
    }

    val position = remember(anchor) { anchor.pendingPositionInWindow }
    DisposableEffect(anchor) {
        anchor.onMenuOpened()
        onDispose { anchor.onMenuClosed() }
    }
    return position
}

@Composable
private fun rememberMenuPositionProvider(
    pointerPosition: Offset?,
    offset: DpOffset,
    popupPositionProvider: PopupPositionProvider?,
    properties: PopupProperties,
    inset: Int,
    desktop: Boolean,
    onPlacement: (MenuPlacement) -> Unit
): PopupPositionProvider {
    val density = LocalDensity.current
    val offsetPx = with(density) { IntOffset(offset.x.roundToPx(), offset.y.roundToPx()) }
    val clippingEnabled = properties.clippingEnabled
    val positionOrigin = properties.menuPositionOrigin()
    val updatePlacement by rememberUpdatedState(onPlacement)
    return remember(
        pointerPosition,
        offsetPx,
        popupPositionProvider,
        density,
        desktop,
        clippingEnabled,
        positionOrigin,
        inset
    ) {
        val pointerAnchor = pointerPosition?.let { position ->
            val point = IntOffset(position.x.roundToInt(), position.y.roundToInt()) - positionOrigin
            IntRect(point, IntSize.Zero)
        }
        val gap = if (desktop || pointerAnchor == null) 0 else with(density) { 12.dp.roundToPx() }
        val margin = maxOf(inset, with(density) { (if (desktop) 8.dp else 12.dp).roundToPx() })
        createMenuPositionProvider { bounds, windowSize, direction, popupSize ->
            // The provider places the painted panel; Compose positions the larger window.
            val panelSize = IntSize(
                width = (popupSize.width - 2 * inset).coerceAtLeast(0),
                height = (popupSize.height - 2 * inset).coerceAtLeast(0)
            )
            val placementAnchor = pointerAnchor ?: bounds
            val requestedPosition = popupPositionProvider?.calculatePosition(
                bounds,
                windowSize,
                direction,
                panelSize
            ) ?: menuPosition(
                anchor = placementAnchor,
                windowSize = windowSize,
                direction = direction,
                popupSize = panelSize,
                offset = offsetPx,
                gap = gap,
                margin = margin
            )
            val position = if (inset > 0 && clippingEnabled) {
                clampMenuPositionToGutter(
                    position = requestedPosition,
                    windowSize = windowSize,
                    panelSize = panelSize,
                    inset = inset
                )
            } else {
                requestedPosition
            }
            if (!desktop) {
                val panelBounds = IntRect(position, panelSize)
                updatePlacement(MenuPlacement.resolve(anchor = placementAnchor, menu = panelBounds))
            }
            position - IntOffset(inset, inset)
        }
    }
}

private fun clampMenuPositionToGutter(
    position: IntOffset,
    windowSize: IntSize,
    panelSize: IntSize,
    inset: Int
): IntOffset {
    val maxX = (windowSize.width - panelSize.width - inset).coerceAtLeast(inset)
    val maxY = (windowSize.height - panelSize.height - inset).coerceAtLeast(inset)
    return IntOffset(position.x.coerceIn(inset, maxX), position.y.coerceIn(inset, maxY))
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
    val motion = if (desktop) null else rememberMenuPanelMotion(visible, placement)
    val backdrop = if (style.material == PopupMenuMaterial.Solid) {
        null
    } else {
        LocalPopupBackdrop.current
    }
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
    val dropdownDrag = if (dropdownSlideSelection && !desktop) {
        remember { MenuDragSelection<DropdownMenuItem>() }
    } else {
        null
    }
    Box(
        modifier = modifier
            .menuSurfaceSize(useIntrinsicWidth)
            .thenIf(reveal) { revealPopupWindowBackdrop(shape) }
            .menuSurfaceInput(
                expanded = visible.targetState,
                shape = shape,
                motion = motion,
                glassInteraction = glassInteraction,
                dropdownDrag = dropdownDrag,
                isSlideSelecting = isSlideSelecting,
                onDismissOutside = onDismissOutside
            ),
        propagateMinConstraints = true
    ) {
        Box(
            modifier = Modifier.menuSurfaceFrame(
                desktop = desktop,
                dark = dark,
                shape = shape,
                motion = motion,
                glassInteraction = glassInteraction
            ),
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
            MenuSurfaceContent(
                desktop = desktop,
                expanded = visible.targetState,
                scrollable = scrollable,
                dropdownDrag = dropdownDrag,
                content = content
            )
        }
    }
}

@Composable
private fun Modifier.menuSurfaceSize(useIntrinsicWidth: Boolean): Modifier {
    val windowSize = LocalWindowInfo.current.containerDpSize
    val maxWidth = (windowSize.width - 24.dp).coerceAtLeast(1.dp)
    val maxHeight = (windowSize.height - 24.dp).coerceAtLeast(1.dp)
    return widthIn(max = if (windowSize.width > 0.dp) maxWidth else Dp.Unspecified)
        .heightIn(max = if (windowSize.height > 0.dp) maxHeight else Dp.Unspecified)
        .thenIf(useIntrinsicWidth) { width(IntrinsicSize.Max) }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun Modifier.menuSurfaceInput(
    expanded: Boolean,
    shape: Shape,
    motion: MenuPanelMotion?,
    glassInteraction: GlassInteractionState?,
    dropdownDrag: MenuDragSelection<DropdownMenuItem>?,
    isSlideSelecting: (() -> Boolean)?,
    onDismissOutside: (() -> Unit)?
): Modifier {
    val direction = LocalLayoutDirection.current
    val currentDismiss by rememberUpdatedState(onDismissOutside)
    val currentSlideSelecting by rememberUpdatedState(isSlideSelecting)
    val followsConsumedMovement = remember(dropdownDrag) {
        {
            dropdownDrag?.isSlideSelecting == true || currentSlideSelecting?.invoke() == true
        }
    }
    return pointerInput(shape, direction, motion) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            val panelSize = size.toSize()
            val point = if (motion == null) {
                down.position
            } else {
                motion.hitPoint(down.position, panelSize, MobileMenuTravel.toPx())
            }
            val inside = point?.let {
                val outline = shape.createOutline(panelSize, direction, this)
                val path = Path().apply { addOutline(outline) }
                it in PathHitTester(path)
            } == true
            if (!inside) {
                down.consume()
                currentDismiss?.invoke()
            }
        }
    }
        .thenIf(expanded && glassInteraction != null) {
            glassPointerInput(requireNotNull(glassInteraction), followsConsumedMovement)
        }
        .thenIf(!expanded) {
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
        }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun Modifier.menuSurfaceFrame(
    desktop: Boolean,
    dark: Boolean,
    shape: Shape,
    motion: MenuPanelMotion?,
    glassInteraction: GlassInteractionState?
): Modifier = this
    .thenIf(motion != null) { graphicsLayer { requireNotNull(motion).applyLayer(this) } }
    .thenIf(!desktop && glassInteraction != null) {
        drawGlassInteraction(requireNotNull(glassInteraction), maxTranslation = 3.dp)
    }
    .thenIf(desktop || OS.isAndroid()) {
        menuShadow(
            shape = shape,
            dark = dark,
            desktop = desktop,
            shadowContext = LocalGraphicsContext.current.shadowContext
        )
    }
    .clip(shape)

@Composable
private fun MenuSurfaceContent(
    desktop: Boolean,
    expanded: Boolean,
    scrollable: Boolean,
    dropdownDrag: MenuDragSelection<DropdownMenuItem>?,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val padding = if (desktop) DesktopMenuContentPadding else MobileMenuContentPadding
    val dragModifier = if (dropdownDrag != null) {
        Modifier
            .onGloballyPositioned { dropdownDrag.coordinates = it }
            .menuDragSelection(
                selection = dropdownDrag,
                enabled = expanded,
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
                verticalScroll(scrollState, enabled = expanded && scrollState.maxValue > 0)
            }
            .then(dragModifier)
    ) {
        CompositionLocalProvider(LocalDropdownDragSelection provides dropdownDrag) {
            content()
        }
    }
}

@Composable
private fun rememberMenuPanelMotion(
    visible: MutableTransitionState<Boolean>,
    placement: () -> MenuPlacement?
): MenuPanelMotion {
    val transition = rememberTransition(visible, label = "Popup menu")
    val progress = transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                spring(dampingRatio = 0.72f, stiffness = 700f, visibilityThreshold = 0.008f)
            } else {
                tween(105, easing = FastOutLinearInEasing)
            }
        },
        label = "Panel motion"
    ) { if (it) 1f else 0f }
    val opacity = transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                tween(120, easing = LinearOutSlowInEasing)
            } else {
                tween(90, easing = FastOutLinearInEasing)
            }
        },
        label = "Panel opacity"
    ) { if (it) 1f else 0f }
    val currentPlacement by rememberUpdatedState(placement)
    return remember(progress, opacity) {
        MenuPanelMotion(progress, opacity) { currentPlacement() }
    }
}

private class MenuPanelMotion(
    private val progress: State<Float>,
    private val opacity: State<Float>,
    private val placement: () -> MenuPlacement?
) {
    fun hitPoint(point: Offset, panelSize: Size, travel: Float): Offset? {
        val position = placement() ?: return null
        if (opacity.value <= 0f) {
            return null
        }
        val fraction = progress.value
        val pivot = Offset(panelSize.width * position.pivot.x, panelSize.height * position.pivot.y)
        val shift = position.shift(fraction, travel)
        return pivot + (point - shift - pivot) / menuScale(fraction)
    }

    fun applyLayer(layer: GraphicsLayerScope) = with(layer) {
        val position = placement()
        if (position == null) {
            alpha = 0f
        } else {
            val fraction = progress.value
            val scale = menuScale(fraction)
            val shift = position.shift(fraction, MobileMenuTravel.toPx())
            transformOrigin = TransformOrigin(position.pivot.x, position.pivot.y)
            scaleX = scale
            scaleY = scale
            translationX = shift.x
            translationY = shift.y
            alpha = opacity.value
        }
    }
}

internal val DesktopMenuContentPadding = 4.dp
private val MobileMenuTravel = 4.dp
private val MenuShadowGutter = 12.dp

@Composable
private fun BoxScope.MenuShadowDismissRegion(onDismissRequest: () -> Unit) {
    val dismiss by rememberUpdatedState(onDismissRequest)
    Spacer(
        Modifier
            .matchParentSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown().consume()
                    dismiss()
                }
            }
    )
}

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
