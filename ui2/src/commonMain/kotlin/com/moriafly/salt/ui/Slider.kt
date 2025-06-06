/**
 * Salt UI
 * Copyright (C) 2024 Moriafly
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

@file:Suppress("unused")

package com.moriafly.salt.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.DragScope
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.GestureCancellationException
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Sliders allow users to make selections from a range of values.
 *
 * @param value current value of the slider. If outside of [valueRange] provided, value will be
 *   coerced to this range.
 * @param onValueChange callback in which value should be updated.
 * @param modifier the [Modifier] to be applied to this slider.
 * @param enabled controls the enabled state of this slider. When `false`, this component will not
 *   respond to user input, and it will appear visually disabled and disabled to accessibility
 *   services.
 * @param valueRange range of values that this slider can take. The passed [value] will be coerced
 *   to this range.
 * @param steps if greater than 0, specifies the amount of discrete allowable values, evenly
 *   distributed across the whole value range. If 0, the slider will behave continuously and allow
 *   any value from the range specified. Must not be negative.
 * @param onValueChangeFinished called when value change has ended. This should not be used to
 *   update the slider value (use [onValueChange] instead), but rather to know when the user has
 *   completed selecting a new value by ending a drag or a click.
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 *   for this slider. You can create and pass in your own `remember`ed instance to observe
 *   [Interaction]s and customize the appearance / behavior of this slider in different states
 */
@UnstableSaltUiApi
@Composable
internal fun Slider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    // @IntRange(from = 0)
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    require(steps >= 0) { "steps should be >= 0" }

    SliderImpl(
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        steps = steps,
        value = value,
        valueRange = valueRange,
        thumb = {
            SliderDefaults.Thumb(
                interactionSource = interactionSource
            )
        },
        track = { sliderPositions ->
            SliderDefaults.Track(
                sliderPositions = sliderPositions
            )
        }
    )
}

/**
 * Range Sliders expand upon [Slider] using the same concepts but allow the user to select 2 values.
 *
 * The two values are still bounded by the value range but they also cannot cross each other.
 *
 * @param value current values of the RangeSlider. If either value is outside of [valueRange]
 *   provided, it will be coerced to this range.
 * @param onValueChange lambda in which values should be updated.
 * @param modifier modifiers for the Range Slider layout.
 * @param enabled whether or not component is enabled and can we interacted with or not.
 * @param valueRange range of values that Range Slider values can take. Passed [value] will be
 *   coerced to this range.
 * @param steps if greater than 0, specifies the amounts of discrete values, evenly distributed
 *   between across the whole value range. If 0, range slider will behave as a continuous slider and
 *   allow to choose any value from the range specified. Must not be negative.
 * @param onValueChangeFinished lambda to be invoked when value change has ended. This callback
 *   shouldn't be used to update the range slider values (use [onValueChange] for that), but rather
 *   to know when the user has completed selecting a new value by ending a drag or a click.
 */
@Composable
internal fun RangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    // @IntRange(from = 0)
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null
) {
    val startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val endInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() }

    require(steps >= 0) { "steps should be >= 0" }

    RangeSliderImpl(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        valueRange = valueRange,
        onValueChangeFinished = onValueChangeFinished,
        startInteractionSource = startInteractionSource,
        endInteractionSource = endInteractionSource,
        startThumb = {
            SliderDefaults.Thumb(
                interactionSource = startInteractionSource
            )
        },
        endThumb = {
            SliderDefaults.Thumb(
                interactionSource = endInteractionSource
            )
        },
        track = { sliderPositions ->
            SliderDefaults.Track(
                sliderPositions = sliderPositions
            )
        },
        modifier = modifier,
        steps = steps
    )
}

@Composable
private fun SliderImpl(
    enabled: Boolean,
    interactionSource: MutableInteractionSource,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)?,
    steps: Int,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    thumb: @Composable (SliderPositions) -> Unit,
    track: @Composable (SliderPositions) -> Unit,
    modifier: Modifier = Modifier
) {
    val onValueChangeState = rememberUpdatedState<(Float) -> Unit> {
        if (it != value) {
            onValueChange(it)
        }
    }

    val tickFractions = remember(steps) {
        stepsToTickFractions(steps)
    }

    val thumbWidth = remember { mutableStateOf(ThumbWidth.value) }
    val totalWidth = remember { mutableIntStateOf(0) }

    fun scaleToUserValue(
        minPx: Float,
        maxPx: Float,
        offset: Float
    ) = scale(minPx, maxPx, offset, valueRange.start, valueRange.endInclusive)

    fun scaleToOffset(
        minPx: Float,
        maxPx: Float,
        userValue: Float
    ) = scale(valueRange.start, valueRange.endInclusive, userValue, minPx, maxPx)

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val rawOffset = remember { mutableStateOf(scaleToOffset(0f, 0f, value)) }
    val pressOffset = remember { mutableFloatStateOf(0f) }
    val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)

    val positionFraction = calcFraction(valueRange.start, valueRange.endInclusive, coerced)
    val sliderPositions = remember {
        SliderPositions(0f..positionFraction, tickFractions)
    }
    sliderPositions.activeRange = 0f..positionFraction
    sliderPositions.tickFractions = tickFractions

    val draggableState = remember(valueRange) {
        SliderDraggableState {
            val maxPx = max(totalWidth.value - thumbWidth.value / 2, 0f)
            val minPx = min(thumbWidth.value / 2, maxPx)
            rawOffset.value += it + pressOffset.value
            pressOffset.value = 0f
            val offsetInTrack = snapValueToTick(rawOffset.value, tickFractions, minPx, maxPx)
            onValueChangeState.value.invoke(scaleToUserValue(minPx, maxPx, offsetInTrack))
        }
    }

    val gestureEndAction = rememberUpdatedState {
        if (!draggableState.isDragging) {
            // check isDragging in case the change is still in progress (touch -> drag case).
            onValueChangeFinished?.invoke()
        }
    }

    val press = Modifier.sliderTapModifier(
        draggableState,
        interactionSource,
        totalWidth.value,
        isRtl,
        rawOffset,
        gestureEndAction,
        pressOffset,
        enabled
    )

    val drag = Modifier.draggable(
        orientation = Orientation.Horizontal,
        reverseDirection = isRtl,
        enabled = enabled,
        interactionSource = interactionSource,
        onDragStopped = { _ -> gestureEndAction.value.invoke() },
        startDragImmediately = draggableState.isDragging,
        state = draggableState
    )

    Layout(
        content = {
            Box(modifier = Modifier.layoutId(SliderComponents.Thumb)) { thumb(sliderPositions) }
            Box(modifier = Modifier.layoutId(SliderComponents.Track)) { track(sliderPositions) }
        },
        modifier = modifier
            .requiredSizeIn(
                minWidth = 20.0.dp,
                minHeight = 20.0.dp,
            )
            .sliderSemantics(
                value,
                enabled,
                onValueChange,
                onValueChangeFinished,
                valueRange,
                steps
            )
            .focusable(enabled, interactionSource)
            .then(press)
            .then(drag)
    ) { measurables, constraints ->
        val thumbPlaceable = measurables.first {
            it.layoutId == SliderComponents.Thumb
        }.measure(constraints)

        val trackPlaceable = measurables.first {
            it.layoutId == SliderComponents.Track
        }.measure(
            constraints.offset(
                horizontal = -thumbPlaceable.width
            ).copy(minHeight = 0)
        )

        val sliderWidth = thumbPlaceable.width + trackPlaceable.width
        val sliderHeight = max(trackPlaceable.height, thumbPlaceable.height)

        thumbWidth.value = thumbPlaceable.width.toFloat()
        totalWidth.value = sliderWidth

        val trackOffsetX = thumbPlaceable.width / 2
        val thumbOffsetX = (trackPlaceable.width * positionFraction).roundToInt()
        val trackOffsetY = (sliderHeight - trackPlaceable.height) / 2
        val thumbOffsetY = (sliderHeight - thumbPlaceable.height) / 2

        layout(
            sliderWidth,
            sliderHeight
        ) {
            trackPlaceable.placeRelative(
                trackOffsetX,
                trackOffsetY
            )
            thumbPlaceable.placeRelative(
                thumbOffsetX,
                thumbOffsetY
            )
        }
    }
}

@Composable
private fun RangeSliderImpl(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    enabled: Boolean,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChangeFinished: (() -> Unit)?,
    startInteractionSource: MutableInteractionSource,
    endInteractionSource: MutableInteractionSource,
    startThumb: @Composable ((SliderPositions) -> Unit),
    endThumb: @Composable ((SliderPositions) -> Unit),
    track: @Composable ((SliderPositions) -> Unit),
    modifier: Modifier = Modifier,
    steps: Int = 0
) {
    val onValueChangeState = rememberUpdatedState<(ClosedFloatingPointRange<Float>) -> Unit> {
        if (it != value) {
            onValueChange(it)
        }
    }

    val tickFractions = remember(steps) {
        stepsToTickFractions(steps)
    }

    var startThumbWidth by remember { mutableStateOf(ThumbWidth.value) }
    var endThumbWidth by remember { mutableStateOf(ThumbWidth.value) }
    var totalWidth by remember { mutableIntStateOf(0) }

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    // scales range offset from within minPx..maxPx to within valueRange.start..valueRange.end.
    fun scaleToUserValue(
        minPx: Float,
        maxPx: Float,
        offset: ClosedFloatingPointRange<Float>
    ) = scale(minPx, maxPx, offset, valueRange.start, valueRange.endInclusive)

    // scales float userValue within valueRange.start..valueRange.end to within minPx..maxPx.
    fun scaleToOffset(
        minPx: Float,
        maxPx: Float,
        userValue: Float
    ) = scale(valueRange.start, valueRange.endInclusive, userValue, minPx, maxPx)

    val obtainedMeasurements = remember { mutableStateOf(false) }
    val rawOffsetStart = remember { mutableFloatStateOf(0f) }
    val rawOffsetEnd = remember { mutableFloatStateOf(0f) }

    val gestureEndAction = rememberUpdatedState<(Boolean) -> Unit> {
        onValueChangeFinished?.invoke()
    }

    val onDrag = rememberUpdatedState<(Boolean, Float) -> Unit> { isStart, offset ->
        val maxPx = max(totalWidth - endThumbWidth / 2, 0f)
        val minPx = min(startThumbWidth / 2, maxPx)
        val offsetRange = if (isStart) {
            rawOffsetStart.value += offset
            rawOffsetEnd.value = scaleToOffset(minPx, maxPx, value.endInclusive)
            val offsetEnd = rawOffsetEnd.value
            var offsetStart = rawOffsetStart.value.coerceIn(minPx, offsetEnd)
            offsetStart = snapValueToTick(offsetStart, tickFractions, minPx, maxPx)
            offsetStart..offsetEnd
        } else {
            rawOffsetEnd.value += offset
            rawOffsetStart.value = scaleToOffset(minPx, maxPx, value.start)
            val offsetStart = rawOffsetStart.value
            var offsetEnd = rawOffsetEnd.value.coerceIn(offsetStart, maxPx)
            offsetEnd = snapValueToTick(offsetEnd, tickFractions, minPx, maxPx)
            offsetStart..offsetEnd
        }

        onValueChangeState.value.invoke(scaleToUserValue(minPx, maxPx, offsetRange))
    }

    val pressDrag = Modifier.rangeSliderPressDragModifier(
        startInteractionSource,
        endInteractionSource,
        rawOffsetStart,
        rawOffsetEnd,
        enabled,
        isRtl,
        totalWidth,
        valueRange,
        gestureEndAction,
        onDrag,
    )

    // The positions of the thumbs are dependant on each other.
    val coercedStart = value.start.coerceIn(valueRange.start, value.endInclusive)
    val coercedEnd = value.endInclusive.coerceIn(value.start, valueRange.endInclusive)
    val positionFractionStart = calcFraction(
        valueRange.start,
        valueRange.endInclusive,
        coercedStart
    )
    val positionFractionEnd = calcFraction(valueRange.start, valueRange.endInclusive, coercedEnd)

    val sliderPositions = remember {
        SliderPositions(
            positionFractionStart..positionFractionEnd,
            tickFractions
        )
    }
    sliderPositions.activeRange = positionFractionStart..positionFractionEnd
    sliderPositions.tickFractions = tickFractions

    val startSteps = floor(steps * positionFractionEnd).toInt()
    val endSteps = floor(steps * (1f - positionFractionStart)).toInt()

    val startThumbSemantics = Modifier.sliderSemantics(
        coercedStart,
        enabled,
        { changedVal -> onValueChangeState.value.invoke(changedVal..coercedEnd) },
        onValueChangeFinished,
        valueRange.start..coercedEnd,
        startSteps
    )
    val endThumbSemantics = Modifier.sliderSemantics(
        coercedEnd,
        enabled,
        { changedVal -> onValueChangeState.value.invoke(coercedStart..changedVal) },
        onValueChangeFinished,
        coercedStart..valueRange.endInclusive,
        endSteps
    )

    val startContentDescription = "" // getString(Strings.SliderRangeStart)
    val endContentDescription = "" // getString(Strings.SliderRangeEnd)

    Layout(
        content = {
            Box(
                modifier = Modifier
                    .layoutId(RangeSliderComponents.StartThumb)
                    .semantics(mergeDescendants = true) {
                        contentDescription = startContentDescription
                    }
                    .focusable(enabled, startInteractionSource)
                    .then(startThumbSemantics)
            ) {
                startThumb(sliderPositions)
            }
            Box(
                modifier = Modifier
                    .layoutId(RangeSliderComponents.EndThumb)
                    .semantics(mergeDescendants = true) {
                        contentDescription = endContentDescription
                    }
                    .focusable(enabled, endInteractionSource)
                    .then(endThumbSemantics)
            ) {
                endThumb(sliderPositions)
            }
            Box(
                modifier = Modifier
                    .layoutId(RangeSliderComponents.Track)
            ) {
                track(sliderPositions)
            }
        },
        modifier = modifier
            .requiredSizeIn(
                minWidth = 20.0.dp,
                minHeight = 20.0.dp,
            )
            .then(pressDrag)
    ) { measurables, constraints ->
        val startThumbPlaceable = measurables.first {
            it.layoutId == RangeSliderComponents.StartThumb
        }.measure(
            constraints
        )

        val endThumbPlaceable = measurables.first {
            it.layoutId == RangeSliderComponents.EndThumb
        }.measure(
            constraints
        )

        val trackPlaceable = measurables.first {
            it.layoutId == RangeSliderComponents.Track
        }.measure(
            constraints.offset(
                horizontal = -(startThumbPlaceable.width + endThumbPlaceable.width) / 2
            ).copy(minHeight = 0)
        )

        val sliderWidth = trackPlaceable.width +
            (startThumbPlaceable.width + endThumbPlaceable.width) / 2
        val sliderHeight = maxOf(
            trackPlaceable.height,
            startThumbPlaceable.height,
            endThumbPlaceable.height
        )

        startThumbWidth = startThumbPlaceable.width.toFloat()
        endThumbWidth = endThumbPlaceable.width.toFloat()
        totalWidth = sliderWidth

        // Updates rawOffsetStart and rawOffsetEnd with the correct min and max pixel. We use this
        // `obtainedMeasurements` boolean so that we only do this update once. Is there a cleaner
        // way to do this?
        if (!obtainedMeasurements.value) {
            val finalizedMaxPx = max(totalWidth - endThumbWidth / 2, 0f)
            val finalizedMinPx = min(startThumbWidth / 2, finalizedMaxPx)
            rawOffsetStart.value = scaleToOffset(
                finalizedMinPx,
                finalizedMaxPx,
                value.start
            )
            rawOffsetEnd.value = scaleToOffset(
                finalizedMinPx,
                finalizedMaxPx,
                value.endInclusive
            )
            obtainedMeasurements.value = true
        }

        val trackOffsetX = startThumbPlaceable.width / 2
        val startThumbOffsetX = (trackPlaceable.width * positionFractionStart).roundToInt()
        // When start thumb and end thumb have different widths,
        // we need to add a correction for the centering of the slider.
        val endCorrection = (startThumbWidth - endThumbWidth) / 2
        val endThumbOffsetX =
            (trackPlaceable.width * positionFractionEnd + endCorrection).roundToInt()
        val trackOffsetY = (sliderHeight - trackPlaceable.height) / 2
        val startThumbOffsetY = (sliderHeight - startThumbPlaceable.height) / 2
        val endThumbOffsetY = (sliderHeight - endThumbPlaceable.height) / 2

        layout(
            sliderWidth,
            sliderHeight
        ) {
            trackPlaceable.placeRelative(
                trackOffsetX,
                trackOffsetY
            )
            startThumbPlaceable.placeRelative(
                startThumbOffsetX,
                startThumbOffsetY
            )
            endThumbPlaceable.placeRelative(
                endThumbOffsetX,
                endThumbOffsetY
            )
        }
    }
}

/**
 * Object to hold defaults used by [Slider].
 */
@Stable
internal object SliderDefaults {
    /**
     * The Default thumb for [Slider] and [RangeSlider].
     *
     * @param interactionSource the [MutableInteractionSource] representing the stream of
     *   [Interaction]s for this thumb. You can create and pass in your own `remember`ed
     *   instance to observe.
     * @param modifier the [Modifier] to be applied to the thumb.
     */
    @Composable
    fun Thumb(
        interactionSource: MutableInteractionSource,
        modifier: Modifier = Modifier,
        thumbSize: DpSize = ThumbSize
    ) {
        val interactions = remember { mutableStateListOf<Interaction>() }
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> interactions.add(interaction)
                    is PressInteraction.Release -> interactions.remove(interaction.press)
                    is PressInteraction.Cancel -> interactions.remove(interaction.press)
                    is DragInteraction.Start -> interactions.add(interaction)
                    is DragInteraction.Stop -> interactions.remove(interaction.start)
                    is DragInteraction.Cancel -> interactions.remove(interaction.start)
                }
            }
        }

        val shape = CircleShape

        Spacer(
            modifier
                .size(thumbSize)
                .hoverable(interactionSource = interactionSource)
                .background(SaltTheme.colors.onHighlight, shape)
                .border(1.dp, SaltTheme.colors.subText.copy(alpha = 0.25f), shape)
        )
    }

    /**
     * The Default track for [Slider] and [RangeSlider].
     *
     * @param sliderPositions [SliderPositions] which is used to obtain the current active track
     *   and the tick positions if the slider is discrete.
     * @param modifier the [Modifier] to be applied to the track.
     */
    @Composable
    fun Track(
        sliderPositions: SliderPositions,
        modifier: Modifier = Modifier
    ) {
        val inactiveTrackColor = rememberUpdatedState(SaltTheme.colors.subText.copy(alpha = 0.1f))
        val activeTrackColor = rememberUpdatedState(SaltTheme.colors.highlight)
        Canvas(
            modifier
                .fillMaxWidth()
                .height(TrackHeight)
        ) {
            val isRtl = layoutDirection == LayoutDirection.Rtl
            val sliderLeft = Offset(0f, center.y)
            val sliderRight = Offset(size.width, center.y)
            val sliderStart = if (isRtl) sliderRight else sliderLeft
            val sliderEnd = if (isRtl) sliderLeft else sliderRight
            val trackStrokeWidth = TrackHeight.toPx()
            drawLine(
                inactiveTrackColor.value,
                sliderStart,
                sliderEnd,
                trackStrokeWidth,
                StrokeCap.Round
            )
            val sliderValueEnd = Offset(
                sliderStart.x +
                    (sliderEnd.x - sliderStart.x) * sliderPositions.activeRange.endInclusive,
                center.y
            )

            val sliderValueStart = Offset(
                sliderStart.x +
                    (sliderEnd.x - sliderStart.x) * sliderPositions.activeRange.start,
                center.y
            )

            drawLine(
                activeTrackColor.value,
                sliderValueStart,
                sliderValueEnd,
                trackStrokeWidth,
                StrokeCap.Round
            )
        }
    }
}

private fun snapValueToTick(
    current: Float,
    tickFractions: FloatArray,
    minPx: Float,
    maxPx: Float
): Float {
    // target is a closest anchor to the `current`, if exists.
    return tickFractions
        .minByOrNull { abs(lerp(minPx, maxPx, it) - current) }
        ?.run { lerp(minPx, maxPx, this) }
        ?: current
}

private suspend fun AwaitPointerEventScope.awaitSlop(
    id: PointerId,
    type: PointerType
): Pair<PointerInputChange, Float>? {
    var initialDelta = 0f
    val postPointerSlop = { pointerInput: PointerInputChange, offset: Float ->
        pointerInput.consume()
        initialDelta = offset
    }
    val afterSlopResult = awaitHorizontalPointerSlopOrCancellation(id, type, postPointerSlop)
    return if (afterSlopResult != null) afterSlopResult to initialDelta else null
}

private fun stepsToTickFractions(steps: Int): FloatArray =
    if (steps == 0) {
        floatArrayOf()
    } else {
        FloatArray(steps + 2) {
            it.toFloat() / (steps + 1)
        }
    }

// Scale x1 from a1..b1 range to a2..b2 range.
private fun scale(
    a1: Float,
    b1: Float,
    x1: Float,
    a2: Float,
    b2: Float
) = lerp(a2, b2, calcFraction(a1, b1, x1))

// Scale x.start, x.endInclusive from a1..b1 range to a2..b2 range.
private fun scale(
    a1: Float,
    b1: Float,
    x: ClosedFloatingPointRange<Float>,
    a2: Float,
    b2: Float
) = scale(a1, b1, x.start, a2, b2)..scale(a1, b1, x.endInclusive, a2, b2)

// Calculate the 0..1 fraction that `pos` value represents between `a` and `b`.
private fun calcFraction(
    a: Float,
    b: Float,
    pos: Float
) = (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)

private fun Modifier.sliderSemantics(
    value: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0
): Modifier {
    val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
    return semantics {
        if (!enabled) disabled()
        setProgress(
            action = { targetValue ->
                var newValue = targetValue.coerceIn(valueRange.start, valueRange.endInclusive)
                val originalVal = newValue
                val resolvedValue = if (steps > 0) {
                    var distance: Float = newValue
                    for (i in 0..steps + 1) {
                        val stepValue = lerp(
                            valueRange.start,
                            valueRange.endInclusive,
                            i.toFloat() / (steps + 1)
                        )
                        if (abs(stepValue - originalVal) <= distance) {
                            distance = abs(stepValue - originalVal)
                            newValue = stepValue
                        }
                    }
                    newValue
                } else {
                    newValue
                }

                // This is to keep it consistent with AbsSeekbar.java: return false if no change
                // from current.
                if (resolvedValue == coerced) {
                    false
                } else {
                    onValueChange(resolvedValue)
                    onValueChangeFinished?.invoke()
                    true
                }
            }
        )
    }.progressSemantics(value, valueRange, steps)
}

@Suppress("ktlint:compose:modifier-composed-check")
private fun Modifier.sliderTapModifier(
    draggableState: DraggableState,
    interactionSource: MutableInteractionSource,
    maxPx: Int,
    isRtl: Boolean,
    rawOffset: State<Float>,
    gestureEndAction: State<() -> Unit>,
    pressOffset: MutableState<Float>,
    enabled: Boolean
) = composed(
    factory = {
        thenIf(enabled) {
            val scope = rememberCoroutineScope()
            pointerInput(draggableState, interactionSource, maxPx, isRtl) {
                detectTapGestures(
                    onPress = { pos ->
                        val to = if (isRtl) maxPx - pos.x else pos.x
                        pressOffset.value = to - rawOffset.value
                        try {
                            awaitRelease()
                        } catch (_: GestureCancellationException) {
                            pressOffset.value = 0f
                        }
                    },
                    onTap = {
                        scope.launch {
                            draggableState.drag(MutatePriority.UserInput) {
                                // just trigger animation, press offset will be applied.
                                dragBy(0f)
                            }
                            gestureEndAction.value.invoke()
                        }
                    }
                )
            }
        }
    },
    inspectorInfo = debugInspectorInfo {
        name = "sliderTapModifier"
        properties["draggableState"] = draggableState
        properties["interactionSource"] = interactionSource
        properties["maxPx"] = maxPx
        properties["isRtl"] = isRtl
        properties["rawOffset"] = rawOffset
        properties["gestureEndAction"] = gestureEndAction
        properties["pressOffset"] = pressOffset
        properties["enabled"] = enabled
    }
)

private suspend fun animateToTarget(
    draggableState: DraggableState,
    current: Float,
    target: Float,
    velocity: Float
) {
    draggableState.drag {
        var latestValue = current
        Animatable(initialValue = current).animateTo(target, SliderToTickAnimation, velocity) {
            dragBy(this.value - latestValue)
            latestValue = this.value
        }
    }
}

private fun Modifier.rangeSliderPressDragModifier(
    startInteractionSource: MutableInteractionSource,
    endInteractionSource: MutableInteractionSource,
    rawOffsetStart: State<Float>,
    rawOffsetEnd: State<Float>,
    enabled: Boolean,
    isRtl: Boolean,
    maxPx: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    gestureEndAction: State<(Boolean) -> Unit>,
    onDrag: State<(Boolean, Float) -> Unit>
): Modifier =
    thenIf(enabled) {
        pointerInput(startInteractionSource, endInteractionSource, maxPx, isRtl, valueRange) {
            val rangeSliderLogic = RangeSliderLogic(
                startInteractionSource,
                endInteractionSource,
                rawOffsetStart,
                rawOffsetEnd,
                onDrag
            )
            coroutineScope {
                awaitEachGesture {
                    val event = awaitFirstDown(requireUnconsumed = false)
                    val interaction = DragInteraction.Start()
                    var posX = if (isRtl) maxPx - event.position.x else event.position.x
                    val compare = rangeSliderLogic.compareOffsets(posX)
                    var draggingStart = if (compare != 0) {
                        compare < 0
                    } else {
                        rawOffsetStart.value > posX
                    }

                    awaitSlop(event.id, event.type)?.let {
                        val slop = viewConfiguration.pointerSlop(event.type)
                        val shouldUpdateCapturedThumb = abs(rawOffsetEnd.value - posX) < slop &&
                            abs(rawOffsetStart.value - posX) < slop
                        if (shouldUpdateCapturedThumb) {
                            val dir = it.second
                            draggingStart = if (isRtl) dir >= 0f else dir < 0f
                            posX += it.first.positionChange().x
                        }
                    }

                    rangeSliderLogic.captureThumb(
                        draggingStart,
                        posX,
                        interaction,
                        this@coroutineScope
                    )

                    val finishInteraction = try {
                        val success = horizontalDrag(pointerId = event.id) {
                            val deltaX = it.positionChange().x
                            onDrag.value.invoke(draggingStart, if (isRtl) -deltaX else deltaX)
                        }
                        if (success) {
                            DragInteraction.Stop(interaction)
                        } else {
                            DragInteraction.Cancel(interaction)
                        }
                    } catch (e: CancellationException) {
                        DragInteraction.Cancel(interaction)
                    }

                    gestureEndAction.value.invoke(draggingStart)
                    launch {
                        rangeSliderLogic
                            .activeInteraction(draggingStart)
                            .emit(finishInteraction)
                    }
                }
            }
        }
    }

private class RangeSliderLogic(
    val startInteractionSource: MutableInteractionSource,
    val endInteractionSource: MutableInteractionSource,
    val rawOffsetStart: State<Float>,
    val rawOffsetEnd: State<Float>,
    val onDrag: State<(Boolean, Float) -> Unit>
) {
    fun activeInteraction(draggingStart: Boolean): MutableInteractionSource =
        if (draggingStart) startInteractionSource else endInteractionSource

    fun compareOffsets(eventX: Float): Int {
        val diffStart = abs(rawOffsetStart.value - eventX)
        val diffEnd = abs(rawOffsetEnd.value - eventX)
        return diffStart.compareTo(diffEnd)
    }

    fun captureThumb(
        draggingStart: Boolean,
        posX: Float,
        interaction: Interaction,
        scope: CoroutineScope
    ) {
        onDrag.value.invoke(
            draggingStart,
            posX - if (draggingStart) rawOffsetStart.value else rawOffsetEnd.value
        )
        scope.launch {
            activeInteraction(draggingStart).emit(interaction)
        }
    }
}

// Internal to be referred to in tests.
internal val ThumbWidth = 24.0.dp
private val ThumbHeight = 24.0.dp
private val ThumbSize = DpSize(ThumbWidth, ThumbHeight)
private val ThumbDefaultElevation = 1.dp
private val ThumbPressedElevation = 6.dp
private val TickSize = 2.0.dp

// Internal to be referred to in tests.
internal val TrackHeight = 23.0.dp
private val SliderHeight = 24.dp
private val SliderMinWidth = 144.dp // TODO: clarify min width
private val DefaultSliderConstraints =
    Modifier
        .widthIn(min = SliderMinWidth)
        .heightIn(max = SliderHeight)

private val SliderToTickAnimation = TweenSpec<Float>(durationMillis = 100)

private class SliderDraggableState(
    val onDelta: (Float) -> Unit
) : DraggableState {
    var isDragging by mutableStateOf(false)
        private set

    private val dragScope: DragScope = object : DragScope {
        override fun dragBy(pixels: Float): Unit = onDelta(pixels)
    }

    private val scrollMutex = MutatorMutex()

    override suspend fun drag(
        dragPriority: MutatePriority,
        block: suspend DragScope.() -> Unit
    ): Unit =
        coroutineScope {
            isDragging = true
            scrollMutex.mutateWith(dragScope, dragPriority, block)
            isDragging = false
        }

    override fun dispatchRawDelta(delta: Float) = onDelta(delta)
}

private enum class SliderComponents {
    Thumb,
    Track
}

private enum class RangeSliderComponents {
    EndThumb,
    StartThumb,
    Track
}

/**
 * Class that holds information about [Slider]'s and [RangeSlider]'s active track and fractional
 * positions where the discrete ticks should be drawn on the track.
 */
@Stable
internal class SliderPositions(
    initialActiveRange: ClosedFloatingPointRange<Float> = 0f..1f,
    initialTickFractions: FloatArray = floatArrayOf()
) {
    /**
     * [ClosedFloatingPointRange] that indicates the current active range for the start to thumb for
     * a [Slider] and start thumb to end thumb for a [RangeSlider].
     */
    var activeRange: ClosedFloatingPointRange<Float> by mutableStateOf(initialActiveRange)
        internal set

    /**
     * The discrete points where a tick should be drawn on the track.
     *
     * Each value of tickFractions should be within the range [0f, 1f]. If the track is continuous,
     * then tickFractions will be an empty [FloatArray].
     */
    var tickFractions: FloatArray by mutableStateOf(initialTickFractions)
        internal set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SliderPositions) return false

        if (activeRange != other.activeRange) return false
        if (!tickFractions.contentEquals(other.tickFractions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = activeRange.hashCode()
        result = 31 * result + tickFractions.contentHashCode()
        return result
    }
}
