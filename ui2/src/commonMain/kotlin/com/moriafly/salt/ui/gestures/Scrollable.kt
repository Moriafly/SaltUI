/*
 * Salt UI
 * Copyright (C) 2025 Moriafly
 * Copyright 2020 The Android Open Source Project
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

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "unused")

package com.moriafly.salt.ui.gestures

import androidx.compose.animation.core.animate
import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.FocusedBoundsObserverNode
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.CanDragCalculation
import androidx.compose.foundation.gestures.DragEvent
import androidx.compose.foundation.gestures.DragGestureNode
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.FlingCancellationException
import androidx.compose.foundation.gestures.MouseWheelScrollingLogic
import androidx.compose.foundation.gestures.NestedScrollScope
import androidx.compose.foundation.gestures.OnScrollChangedDispatcher
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.gestures.ScrollLogic
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableContainerNode
import androidx.compose.foundation.gestures.ScrollableDefaultFlingBehavior
import androidx.compose.foundation.gestures.ScrollableNestedScrollConnection
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.VerticalAxisThresholdAngle
import androidx.compose.foundation.gestures.platformScrollConfig
import androidx.compose.foundation.gestures.platformScrollableDefaultFlingBehavior
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.relocation.BringIntoViewResponderNode
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusTargetModifierNode
import androidx.compose.ui.focus.Focusability
import androidx.compose.ui.focus.getFocusedRect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.KeyInputModifierNode
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.NestedScrollSource.Companion.SideEffect
import androidx.compose.ui.input.nestedscroll.NestedScrollSource.Companion.UserInput
import androidx.compose.ui.input.nestedscroll.nestedScrollModifierNode
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.node.SemanticsModifierNode
import androidx.compose.ui.node.dispatchOnScrollChanged
import androidx.compose.ui.node.invalidateSemantics
import androidx.compose.ui.node.requireDensity
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.scrollBy
import androidx.compose.ui.semantics.scrollByOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.util.fastAny
import com.moriafly.salt.ui.SaltUiFlags
import com.moriafly.salt.ui.UnstableSaltUiApi
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.atan2

internal class ScrollableNode(
    state: ScrollableState,
    private var overscrollEffect: OverscrollEffect?,
    private var flingBehavior: FlingBehavior?,
    orientation: Orientation,
    enabled: Boolean,
    reverseDirection: Boolean,
    interactionSource: MutableInteractionSource?,
    bringIntoViewSpec: BringIntoViewSpec?,
) : DragGestureNode(
        canDrag = CanDragCalculation,
        enabled = enabled,
        interactionSource = interactionSource,
        orientationLock = orientation,
    ),
    KeyInputModifierNode,
    SemanticsModifierNode,
    OnScrollChangedDispatcher {
    override val shouldAutoInvalidate: Boolean = false

    private val nestedScrollDispatcher = NestedScrollDispatcher()

    private val scrollableContainerNode = delegate(ScrollableContainerNode(enabled))

    // Place holder fling behavior, we'll initialize it when the density is available.
    private val defaultFlingBehavior = platformScrollableDefaultFlingBehavior()

    private val scrollingLogic =
        ScrollingLogic(
            scrollableState = state,
            orientation = orientation,
            overscrollEffect = overscrollEffect,
            reverseDirection = reverseDirection,
            flingBehavior = flingBehavior ?: defaultFlingBehavior,
            nestedScrollDispatcher = nestedScrollDispatcher,
            onScrollChangedDispatcher = this,
            isScrollableNodeAttached = { isAttached },
        )

    private val nestedScrollConnection =
        ScrollableNestedScrollConnection(enabled = enabled, scrollingLogic = scrollingLogic)

    private val focusTargetModifierNode =
        delegate(FocusTargetModifierNode(focusability = Focusability.Never))

    private val contentInViewNode =
        delegate(
            ContentInViewNode(
                orientation = orientation,
                scrollingLogic = scrollingLogic,
                reverseDirection = reverseDirection,
                bringIntoViewSpec = bringIntoViewSpec,
                getFocusedRect = { focusTargetModifierNode.getFocusedRect() },
            )
        )

    private var scrollByAction: ((x: Float, y: Float) -> Boolean)? = null
    private var scrollByOffsetAction: (suspend (Offset) -> Offset)? = null

    private var mouseWheelScrollingLogic: MouseWheelScrollingLogic? = null

    init {
        /** Nested scrolling */
        delegate(nestedScrollModifierNode(nestedScrollConnection, nestedScrollDispatcher))

        /** Focus scrolling */
        delegate(BringIntoViewResponderNode(contentInViewNode))
        if (
            @OptIn(ExperimentalFoundationApi::class)
            !ComposeFoundationFlags.isKeepInViewFocusObservationChangeEnabled
        ) {
            delegate(FocusedBoundsObserverNode { contentInViewNode.onFocusBoundsChanged(it) })
        }
    }

    override fun dispatchScrollDeltaInfo(delta: Offset) {
        if (!isAttached) return
        dispatchOnScrollChanged(delta)
    }

    override suspend fun drag(
        forEachDelta: suspend ((dragDelta: DragEvent.DragDelta) -> Unit) -> Unit
    ) {
        with(scrollingLogic) {
            scroll(scrollPriority = MutatePriority.UserInput) {
                forEachDelta {
                    // Indirect pointer Events should be reverted to account for the reverse we
                    // do in Scrollable. Regular touchscreen events are inverted in scrollable, but
                    // that shouldn't happen for indirect pointer events, so we cancel the reverse
                    // here.
                    val invertIndirectPointer = if (it.isIndirectPointerEvent) -1f else 1f
                    scrollByWithOverscroll(
                        it.delta.singleAxisOffset() * invertIndirectPointer,
                        source = UserInput,
                    )
                }
            }
        }
    }

    override fun onDragStarted(startedPosition: Offset) {}

    override fun onDragStopped(event: DragEvent.DragStopped) {
        nestedScrollDispatcher.coroutineScope.launch {
            // Indirect pointer Events should be reverted to account for the reverse we
            // do in Scrollable. Regular touchscreen events are inverted in scrollable, but
            // that shouldn't happen for indirect pointer events, so we cancel the reverse
            // here.
            val invertIndirectPointer = if (event.isIndirectPointerEvent) -1f else 1f
            scrollingLogic.onScrollStopped(
                event.velocity * invertIndirectPointer,
                isMouseWheel = false,
            )
        }
    }

    private fun onWheelScrollStopped(velocity: Velocity) {
        nestedScrollDispatcher.coroutineScope.launch {
            scrollingLogic.onScrollStopped(velocity, isMouseWheel = true)
        }
    }

    override fun startDragImmediately(): Boolean = scrollingLogic.shouldScrollImmediately()

    private fun ensureMouseWheelScrollNodeInitialized() {
        if (mouseWheelScrollingLogic == null) {
            mouseWheelScrollingLogic =
                MouseWheelScrollingLogic(
                    scrollingLogic = scrollingLogic,
                    mouseWheelScrollConfig = platformScrollConfig(),
                    onScrollStopped = ::onWheelScrollStopped,
                    density = requireDensity(),
                )
        }

        mouseWheelScrollingLogic?.startReceivingMouseWheelEvents(coroutineScope)
    }

    fun update(
        state: ScrollableState,
        orientation: Orientation,
        overscrollEffect: OverscrollEffect?,
        enabled: Boolean,
        reverseDirection: Boolean,
        flingBehavior: FlingBehavior?,
        interactionSource: MutableInteractionSource?,
        bringIntoViewSpec: BringIntoViewSpec?,
    ) {
        var shouldInvalidateSemantics = false
        if (this.enabled != enabled) { // enabled changed
            nestedScrollConnection.enabled = enabled
            scrollableContainerNode.update(enabled)
            shouldInvalidateSemantics = true
        }
        // a new fling behavior was set, change the resolved one.
        val resolvedFlingBehavior = flingBehavior ?: defaultFlingBehavior

        val resetPointerInputHandling =
            scrollingLogic.update(
                scrollableState = state,
                orientation = orientation,
                overscrollEffect = overscrollEffect,
                reverseDirection = reverseDirection,
                flingBehavior = resolvedFlingBehavior,
                nestedScrollDispatcher = nestedScrollDispatcher,
            )
        contentInViewNode.update(orientation, reverseDirection, bringIntoViewSpec)

        this.overscrollEffect = overscrollEffect
        this.flingBehavior = flingBehavior

        // update DragGestureNode
        update(
            canDrag = CanDragCalculation,
            enabled = enabled,
            interactionSource = interactionSource,
            orientationLock = if (scrollingLogic.isVertical()) Vertical else Horizontal,
            shouldResetPointerInputHandling = resetPointerInputHandling,
        )

        if (shouldInvalidateSemantics) {
            clearScrollSemanticsActions()
            invalidateSemantics()
        }
    }

    override fun onAttach() {
        updateDefaultFlingBehavior()
        mouseWheelScrollingLogic?.updateDensity(requireDensity())
    }

    private fun updateDefaultFlingBehavior() {
        if (!isAttached) return
        val density = requireDensity()
        defaultFlingBehavior.updateDensity(density)
    }

    override fun onDensityChange() {
        onCancelPointerInput()
        updateDefaultFlingBehavior()
        mouseWheelScrollingLogic?.updateDensity(requireDensity())
    }

    // Key handler for Page up/down scrolling behavior.
    override fun onKeyEvent(event: KeyEvent): Boolean = if (
        enabled &&
        (event.key == Key.PageDown || event.key == Key.PageUp) &&
        (event.type == KeyEventType.KeyDown) &&
        (!event.isCtrlPressed)
    ) {
        val scrollAmount: Offset =
            if (scrollingLogic.isVertical()) {
                val viewportHeight = contentInViewNode.viewportSize.height

                val yAmount =
                    if (event.key == Key.PageUp) {
                        viewportHeight.toFloat()
                    } else {
                        -viewportHeight.toFloat()
                    }

                Offset(0f, yAmount)
            } else {
                val viewportWidth = contentInViewNode.viewportSize.width

                val xAmount =
                    if (event.key == Key.PageUp) {
                        viewportWidth.toFloat()
                    } else {
                        -viewportWidth.toFloat()
                    }

                Offset(xAmount, 0f)
            }

        // A coroutine is launched for every individual scroll event in the
        // larger scroll gesture. If we see degradation in the future (that is,
        // a fast scroll gesture on a slow device causes UI jank [not seen up to
        // this point), we can switch to a more efficient solution where we
        // lazily launch one coroutine (with the first event) and use a Channel
        // to communicate the scroll amount to the UI thread.
        coroutineScope.launch {
            scrollingLogic.scroll(scrollPriority = MutatePriority.UserInput) {
                scrollBy(offset = scrollAmount, source = UserInput)
            }
        }
        true
    } else {
        false
    }

    override fun onPreKeyEvent(event: KeyEvent) = false

    // Forward all PointerInputModifierNode method calls to `mmouseWheelScrollNode.pointerInputNode`
    // See explanation in `MouseWheelScrollNode.pointerInputNode`

    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize,
    ) {
        if (pointerEvent.changes.fastAny { canDrag.invoke(it.type) }) {
            super.onPointerEvent(pointerEvent, pass, bounds)
        }
        if (enabled) {
            if (pass == PointerEventPass.Initial && pointerEvent.type == PointerEventType.Scroll) {
                ensureMouseWheelScrollNodeInitialized()
            }
            mouseWheelScrollingLogic?.onPointerEvent(pointerEvent, pass, bounds)
        }
    }

    override fun SemanticsPropertyReceiver.applySemantics() {
        if (enabled && (scrollByAction == null || scrollByOffsetAction == null)) {
            setScrollSemanticsActions()
        }

        scrollByAction?.let { scrollBy(action = it) }

        scrollByOffsetAction?.let { scrollByOffset(action = it) }
    }

    private fun setScrollSemanticsActions() {
        scrollByAction = { x, y ->
            coroutineScope.launch { scrollingLogic.semanticsScrollBy(Offset(x, y)) }
            true
        }

        scrollByOffsetAction = { offset -> scrollingLogic.semanticsScrollBy(offset) }
    }

    private fun clearScrollSemanticsActions() {
        scrollByAction = null
        scrollByOffsetAction = null
    }
}

/** Contains the default values used by [scrollable] */
object ScrollableDefaults {
    /**
     * Create and remember default [FlingBehavior] that will represent natural fling curve.
     */
    @Composable
    fun flingBehavior(): FlingBehavior = rememberPlatformDefaultFlingBehavior()
}

/**
 * Create and remember default [FlingBehavior] that will represent natural platform fling decay
 * behavior.
 */
@Composable internal expect fun rememberPlatformDefaultFlingBehavior(): FlingBehavior

/**
 * Holds all scrolling related logic: controls nested scrolling, flinging, overscroll and delta
 * dispatching.
 */
internal class ScrollingLogic(
    var scrollableState: ScrollableState,
    private var overscrollEffect: OverscrollEffect?,
    private var flingBehavior: FlingBehavior,
    private var orientation: Orientation,
    private var reverseDirection: Boolean,
    private var nestedScrollDispatcher: NestedScrollDispatcher,
    private var onScrollChangedDispatcher: OnScrollChangedDispatcher,
    private val isScrollableNodeAttached: () -> Boolean,
) : ScrollLogic {
    // specifies if this scrollable node is currently flinging
    override var isFlinging = false
        private set

    fun Float.toOffset(): Offset =
        when {
            this == 0f -> Offset.Zero
            orientation == Horizontal -> Offset(this, 0f)
            else -> Offset(0f, this)
        }

    fun Offset.singleAxisOffset(): Offset =
        if (orientation == Horizontal) copy(y = 0f) else copy(x = 0f)

    fun Offset.toFloat(): Float = if (orientation == Horizontal) this.x else this.y

    /**
     * Converts this offset to a single axis delta based on the derived angle from the x and y
     * deltas.
     *
     * @return Returns a single axis delta based on the angle. If the angle is mostly horizontal,
     *   and we are in a horizontal scrollable, this will return the x component. If the angle is
     *   mostly vertical, and we are in a vertical scrollable, this will return the y component.
     *   Otherwise, this will return 0. Mostly horizontal means angles smaller than
     *   [VerticalAxisThresholdAngle].
     */
    fun Offset.toSingleAxisDeltaFromAngle(): Float {
        val angle = atan2(this.y.absoluteValue, this.x.absoluteValue)
        return if (angle >= VerticalAxisThresholdAngle) {
            if (orientation == Vertical) this.y else 0f
        } else {
            if (orientation == Horizontal) this.x else 0f
        }
    }

    fun Float.toVelocity(): Velocity =
        when {
            this == 0f -> Velocity.Zero
            orientation == Horizontal -> Velocity(this, 0f)
            else -> Velocity(0f, this)
        }

    private fun Velocity.toFloat(): Float = if (orientation == Horizontal) this.x else this.y

    private fun Velocity.singleAxisVelocity(): Velocity =
        if (orientation == Horizontal) copy(y = 0f) else copy(x = 0f)

    private fun Velocity.update(newValue: Float): Velocity =
        if (orientation == Horizontal) copy(x = newValue) else copy(y = newValue)

    fun Float.reverseIfNeeded(): Float = if (reverseDirection) this * -1 else this

    fun Offset.reverseIfNeeded(): Offset = if (reverseDirection) this * -1f else this

    private var latestScrollSource = UserInput
    private var outerStateScope = NoOpScrollScope

    private val nestedScrollScope =
        object : NestedScrollScope {
            override fun scrollBy(
                offset: Offset,
                source: NestedScrollSource
            ): Offset = with(outerStateScope) { performScroll(offset, source) }

            override fun scrollByWithOverscroll(
                offset: Offset,
                source: NestedScrollSource,
            ): Offset {
                latestScrollSource = source
                val overscroll = overscrollEffect
                return if (overscroll != null && shouldDispatchOverscroll) {
                    overscroll.applyToScroll(offset, latestScrollSource, performScrollForOverscroll)
                } else {
                    with(outerStateScope) { performScroll(offset, source) }
                }
            }
        }

    private val performScrollForOverscroll: (Offset) -> Offset = { delta ->
        with(outerStateScope) { performScroll(delta, latestScrollSource) }
    }

    @OptIn(ExperimentalFoundationApi::class)
    private fun ScrollScope.performScroll(delta: Offset, source: NestedScrollSource): Offset {
        val consumedByPreScroll = nestedScrollDispatcher.dispatchPreScroll(delta, source)

        val scrollAvailableAfterPreScroll = delta - consumedByPreScroll

        val singleAxisDeltaForSelfScroll =
            scrollAvailableAfterPreScroll.singleAxisOffset().reverseIfNeeded().toFloat()

        // Consume on a single axis.
        val consumedBySelfScroll =
            scrollBy(singleAxisDeltaForSelfScroll).toOffset().reverseIfNeeded()

        // Trigger on scroll changed callback
        onScrollChangedDispatcher.dispatchScrollDeltaInfo(consumedBySelfScroll)

        val deltaAvailableAfterScroll = scrollAvailableAfterPreScroll - consumedBySelfScroll
        val consumedByPostScroll =
            nestedScrollDispatcher.dispatchPostScroll(
                consumedBySelfScroll,
                deltaAvailableAfterScroll,
                source,
            )
        return consumedByPreScroll + consumedBySelfScroll + consumedByPostScroll
    }

    @OptIn(UnstableSaltUiApi::class)
    private val shouldDispatchOverscroll
        get() = SaltUiFlags.isAlwaysShouldDispatchOverscrollEnabled ||
            scrollableState.canScrollForward ||
            scrollableState.canScrollBackward

    override fun performRawScroll(scroll: Offset): Offset =
        if (scrollableState.isScrollInProgress) {
            Offset.Zero
        } else {
            dispatchRawDelta(scroll)
        }

    private fun dispatchRawDelta(scroll: Offset): Offset = scrollableState
        .dispatchRawDelta(scroll.toFloat().reverseIfNeeded())
        .reverseIfNeeded()
        .toOffset()

    suspend fun onScrollStopped(initialVelocity: Velocity, isMouseWheel: Boolean) {
        if (isMouseWheel && !flingBehavior.shouldBeTriggeredByMouseWheel) {
            return
        }
        val availableVelocity = initialVelocity.singleAxisVelocity()

        val performFling: suspend (Velocity) -> Velocity = { velocity ->
            val preConsumedByParent = nestedScrollDispatcher.dispatchPreFling(velocity)
            val available = velocity - preConsumedByParent

            val velocityLeft = doFlingAnimation(available)

            val consumedPost =
                nestedScrollDispatcher.dispatchPostFling((available - velocityLeft), velocityLeft)
            val totalLeft = velocityLeft - consumedPost
            velocity - totalLeft
        }

        val overscroll = overscrollEffect
        if (overscroll != null && shouldDispatchOverscroll) {
            overscroll.applyToFling(availableVelocity, performFling)
        } else {
            performFling(availableVelocity)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    override suspend fun doFlingAnimation(available: Velocity): Velocity {
        var result: Velocity = available
        isFlinging = true
        try {
            scroll(scrollPriority = MutatePriority.Default) {
                val nestedScrollScope = this
                val reverseScope =
                    object : ScrollScope {
                        override fun scrollBy(pixels: Float): Float {
                            // Fling has hit the bounds or node left composition,
                            // cancel it to allow continuation. This will conclude this node's
                            // fling,
                            // allowing the onPostFling signal to be called
                            // with the leftover velocity from the fling animation. Any nested
                            // scroll
                            // node above will be able to pick up the left over velocity and
                            // continue
                            // the fling.
                            if (
                                pixels.absoluteValue != 0.0f && !isScrollableNodeAttached.invoke()
                            ) {
                                throw FlingCancellationException()
                            }

                            return nestedScrollScope
                                .scrollByWithOverscroll(
                                    offset = pixels.toOffset().reverseIfNeeded(),
                                    source = SideEffect,
                                )
                                .toFloat()
                                .reverseIfNeeded()
                        }
                    }
                with(reverseScope) {
                    with(flingBehavior) {
                        result =
                            result.update(
                                performFling(available.toFloat().reverseIfNeeded())
                                    .reverseIfNeeded()
                            )
                    }
                }
            }
        } finally {
            isFlinging = false
        }

        return result
    }

    fun shouldScrollImmediately(): Boolean =
        scrollableState.isScrollInProgress || overscrollEffect?.isInProgress ?: false

    /** Opens a scrolling session with nested scrolling and overscroll support. */
    suspend fun scroll(
        scrollPriority: MutatePriority = MutatePriority.Default,
        block: suspend NestedScrollScope.() -> Unit,
    ) {
        scrollableState.scroll(scrollPriority) {
            outerStateScope = this
            block.invoke(nestedScrollScope)
        }
    }

    /** @return true if the pointer input should be reset */
    fun update(
        scrollableState: ScrollableState,
        orientation: Orientation,
        overscrollEffect: OverscrollEffect?,
        reverseDirection: Boolean,
        flingBehavior: FlingBehavior,
        nestedScrollDispatcher: NestedScrollDispatcher,
    ): Boolean {
        var resetPointerInputHandling = false
        if (this.scrollableState != scrollableState) {
            this.scrollableState = scrollableState
            resetPointerInputHandling = true
        }
        this.overscrollEffect = overscrollEffect
        if (this.orientation != orientation) {
            this.orientation = orientation
            resetPointerInputHandling = true
        }
        if (this.reverseDirection != reverseDirection) {
            this.reverseDirection = reverseDirection
            resetPointerInputHandling = true
        }
        this.flingBehavior = flingBehavior
        this.nestedScrollDispatcher = nestedScrollDispatcher
        return resetPointerInputHandling
    }

    fun isVertical(): Boolean = orientation == Vertical
}

/**
 * TODO: Move it to public interface Currently, default [FlingBehavior] is not triggered at all to
 *   avoid unexpected effects during regular scrolling. However, custom one must be triggered
 *   because it's used not only for "inertia", but also for snapping in
 *   [androidx.compose.foundation.pager.Pager] or
 *   [androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior].
 */
private val FlingBehavior.shouldBeTriggeredByMouseWheel
    get() = this !is ScrollableDefaultFlingBehavior

private val NoOpScrollScope: ScrollScope =
    object : ScrollScope {
        override fun scrollBy(pixels: Float): Float = pixels
    }

/**
 * Scroll deltas originating from the semantics system. Should be dispatched as an animation driven
 * event.
 */
private suspend fun ScrollingLogic.semanticsScrollBy(offset: Offset): Offset {
    var previousValue = 0f
    scroll(scrollPriority = MutatePriority.Default) {
        animate(0f, offset.toFloat()) { currentValue, _ ->
            val delta = currentValue - previousValue
            val consumed =
                scrollBy(offset = delta.reverseIfNeeded().toOffset(), source = UserInput)
                    .toFloat()
                    .reverseIfNeeded()
            previousValue += consumed
        }
    }
    return previousValue.toOffset()
}

private const val VerticalAxisThresholdAngle = PI / 4
