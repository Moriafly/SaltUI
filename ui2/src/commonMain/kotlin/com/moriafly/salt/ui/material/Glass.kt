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

package com.moriafly.salt.ui.material

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isFinite
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.TraversableNode
import androidx.compose.ui.node.UnplacedAwareModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.traverseAncestors
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.moriafly.salt.core.os.OS
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.thenIf
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeSourceRetention.ClearWhenUnavailable
import dev.chrisbanes.haze.HazeSourceSelection.All
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.glass.GlassStyle
import dev.chrisbanes.haze.glass.hazeGlass
import dev.chrisbanes.haze.hazeSource
import kotlin.jvm.JvmSynthetic
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Appearance used by [glass].
 */
@UnstableSaltUiApi
@Immutable
sealed interface GlassEffect {
    /**
     * Draws the color for the current theme, including its alpha, without processing the source.
     */
    data class Solid(
        val lightColor: Color = Color(0x99FFFFFF),
        val darkColor: Color = Color(0x99181818)
    ) : GlassEffect

    /**
     * Blurs the source with theme-aware premium material tinting, without glass optics.
     */
    data object Blur : GlassEffect

    /**
     * Draws glass with a background and tint selected for the current theme.
     *
     * [lightColor] and [darkColor] fill behind the captured content with their own alpha.
     * The default fills give the underlying background a similar tone whether it is captured
     * or shows through a translucent part of the source.
     * [lightTint] and [darkTint] color the refracted content, including opaque pixels.
     * Use [Color.Transparent] as a tint to disable that theme's coloring.
     */
    data class Regular(
        val lightColor: Color = DefaultLightColor,
        val darkColor: Color = DefaultDarkColor,
        val lightTint: Color = DefaultLightTint,
        val darkTint: Color = DefaultDarkTint
    ) : GlassEffect {
        private companion object {
            val DefaultLightTint = Color(0x4DFFFFFF)
            val DefaultDarkTint = Color(0x3D666666)
            val DefaultLightColor = GlassStyles.regularBackdropColor(
                tint = DefaultLightTint,
                isDarkTheme = false
            )
            val DefaultDarkColor = GlassStyles.regularBackdropColor(
                tint = DefaultDarkTint,
                isDarkTheme = true
            )
        }
    }

    /**
     * Draws glass without an added background color, tint, or color grading. Refraction, blur, and
     * highlights can still change the apparent color of the content.
     */
    data object Transparent : GlassEffect
}

/**
 * The default glass appearance for the current composition.
 */
@UnstableSaltUiApi
val LocalGlassEffect = staticCompositionLocalOf<GlassEffect> { GlassEffect.Regular() }

/**
 * Connects explicitly marked source content to glass surfaces.
 * Multiple composables can share the same source.
 */
@UnstableSaltUiApi
@Stable
class GlassSource internal constructor() {
    @get:JvmSynthetic
    internal val hazeState = HazeState()

    @get:JvmSynthetic
    internal val hazeInput = HazeInput.Sources(hazeState)
    private val surfaces = mutableStateListOf<GlassSurface>()
    private val fadingLayers by derivedStateOf(structuralEqualityPolicy()) {
        val opacity = glassLayerOpacity ?: return@derivedStateOf emptyMap()
        if (surfaces.none { it.bounds != null && !it.properties.useWindowCoordinates }) {
            return@derivedStateOf emptyMap()
        }

        opacity.revision.intValue
        var layersBySurface: MutableMap<GlassSurface, List<FadingGlassLayer>>? = null
        for (surface in surfaces) {
            val bounds = surface.bounds ?: continue
            if (surface.properties.useWindowCoordinates) continue

            val layers = opacity.fadingLayers(bounds.coordinates)
            if (layers.isNotEmpty()) {
                if (layersBySurface == null) {
                    layersBySurface = mutableMapOf()
                }
                layersBySurface[surface] = layers
            }
        }
        layersBySurface.orEmpty()
    }

    @Composable
    internal fun cutout(
        shape: Shape,
        useWindowCoordinates: Boolean = false
    ): Modifier = cutout(
        shape = shape,
        useWindowCoordinates = useWindowCoordinates,
        selection = null
    )

    @Composable
    private fun cutout(
        shape: Shape,
        useWindowCoordinates: Boolean,
        selection: Selection?
    ): Modifier = SurfaceElement(
        source = this,
        shape = shape,
        density = LocalDensity.current,
        layoutDirection = LocalLayoutDirection.current,
        useWindowCoordinates = useWindowCoordinates,
        selection = selection
    )

    internal class Selection(
        private val source: GlassSource,
        private val sourceKeys: Set<Any>?
    ) {
        private var ancestors by mutableStateOf(emptyList<SourceNode>())

        val input = if (sourceKeys != null) {
            source.hazeInput.copy(
                selection = All.where { includes(it.key) },
                retention = ClearWhenUnavailable
            )
        } else {
            source.hazeInput
        }

        @Composable
        fun cutout(shape: Shape): Modifier {
            if (sourceKeys?.isEmpty() == true) {
                return Modifier
            }
            return source.cutout(
                shape = shape,
                useWindowCoordinates = false,
                selection = takeIf { sourceKeys != null }
            )
        }

        fun trackAncestors(): Modifier =
            if (sourceKeys.isNullOrEmpty()) Modifier else SelectionElement(this)

        private fun updateAncestors(node: Modifier.Node) {
            ancestors = buildList {
                node.traverseAncestors(SourceNodeKey) { ancestor ->
                    add(ancestor as SourceNode)
                    true
                }
            }
        }

        fun includes(key: Any?): Boolean = key != null && sourceKeys?.contains(key) == true &&
            ancestors.none {
                it.registration.source === source && it.registration.key == key
            }

        private data class SelectionElement(
            val selection: Selection
        ) : ModifierNodeElement<SelectionNode>() {
            override fun create() = SelectionNode(selection)

            override fun update(node: SelectionNode) = node.update(selection)
        }

        private class SelectionNode(
            private var selection: Selection
        ) : Modifier.Node(),
            LayoutAwareModifierNode {
            override fun onAttach() = selection.updateAncestors(this)

            override fun onPlaced(coordinates: LayoutCoordinates) = selection.updateAncestors(this)

            override fun onDetach() {
                selection.ancestors = emptyList()
            }

            fun update(selection: Selection) {
                this.selection = selection
                if (isAttached) selection.updateAncestors(this)
            }
        }
    }

    private object SourceNodeKey

    private class GlassSurface(
        properties: SurfaceElement
    ) {
        var properties by mutableStateOf(properties)
        var bounds by mutableStateOf<SurfaceBounds?>(null)

        val opaqueCutout = Cutout(this, 1f)
    }

    private data class Cutout(
        val surface: GlassSurface,
        val coverage: Float
    )

    private data class SurfaceBounds(
        val coordinates: LayoutCoordinates,
        val root: LayoutCoordinates,
        val size: IntSize,
        val origin: Offset,
        val topRight: Offset,
        val bottomLeft: Offset,
        val bottomRight: Offset
    ) {
        val widthInRoot = (topRight - origin).getDistance()
        val heightInRoot = (bottomLeft - origin).getDistance()

        // LayoutCoordinates can mutate without changing identity, so retain transformed corners
        // to detect position and transform changes.
        constructor(coordinates: LayoutCoordinates) : this(
            coordinates = coordinates,
            root = coordinates.findRootCoordinates(),
            size = coordinates.size,
            origin = coordinates.localToRoot(Offset.Zero),
            topRight = coordinates.localToRoot(Offset(coordinates.size.width.toFloat(), 0f)),
            bottomLeft = coordinates.localToRoot(Offset(0f, coordinates.size.height.toFloat())),
            bottomRight = coordinates.localToRoot(
                Offset(coordinates.size.width.toFloat(), coordinates.size.height.toFloat())
            )
        )
    }

    internal data class SourceElement(
        val source: GlassSource,
        val key: Any?
    ) : ModifierNodeElement<SourceNode>() {
        override fun create() = SourceNode(this)

        override fun update(node: SourceNode) {
            node.registration = this
        }

        override fun InspectorInfo.inspectableProperties() {
            name = "glassSource"
        }
    }

    internal class SourceNode(
        registration: SourceElement
    ) : Modifier.Node(),
        DrawModifierNode,
        LayoutAwareModifierNode,
        GlobalPositionAwareModifierNode,
        TraversableNode,
        UnplacedAwareModifierNode {
        override val traverseKey: Any = SourceNodeKey
        var registration by mutableStateOf(registration)
        private var coordinates by mutableStateOf<LayoutCoordinates?>(null)
        private val cutouts by derivedStateOf(structuralEqualityPolicy()) {
            val sourceCoordinates = coordinates ?: return@derivedStateOf emptyList()
            val sourceKey = registration.key
            val source = registration.source
            val fadingLayers = source.fadingLayers
            source.surfaces.mapNotNull { surface ->
                val selection = surface.properties.selection
                if (surface.bounds == null || selection?.includes(sourceKey) == false) {
                    return@mapNotNull null
                }

                val coverage = fadingLayers[surface]?.cutoutCoverageOver(sourceCoordinates) ?: 1f
                when (coverage) {
                    1f -> surface.opaqueCutout
                    0f -> null
                    else -> Cutout(surface, coverage)
                }
            }
        }
        private val transform = Matrix()
        private val shapePath = Path()
        private val visiblePath = Path()
        private val fadingCutouts = mutableListOf<FadingCutout>()
        private val sourcePaint = Paint()
        private val cutoutPaint = Paint().apply {
            blendMode = BlendMode.DstOut
        }

        override fun onPlaced(coordinates: LayoutCoordinates) {
            this.coordinates = coordinates
            invalidateDraw()
        }

        override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
            this.coordinates = coordinates
            invalidateDraw()
        }

        override fun onUnplaced() {
            coordinates = null
        }

        override fun onDetach() {
            coordinates = null
        }

        override fun ContentDrawScope.draw() {
            val sourceCoordinates = coordinates
            val activeCutouts = cutouts
            if (sourceCoordinates == null || !sourceCoordinates.isAttached ||
                activeCutouts.isEmpty()
            ) {
                drawContent()
                return
            }

            val root = sourceCoordinates.findRootCoordinates()
            val canvas = drawContext.canvas
            canvas.withSave {
                var fadingCount = 0
                var fadingBounds = Rect.Zero
                for (cutout in activeCutouts) {
                    val fadingCutout = if (cutout.coverage < 1f) {
                        fadingCutouts.getOrNull(fadingCount)
                            ?: FadingCutout().also(fadingCutouts::add)
                    } else {
                        null
                    }
                    val path = fadingCutout?.path ?: shapePath
                    val bounds = buildCutoutPath(
                        surface = cutout.surface,
                        sourceCoordinates = sourceCoordinates,
                        sourceRoot = root,
                        sourceSize = size,
                        path = path
                    ) ?: continue

                    if (fadingCutout == null) {
                        canvas.clipPath(path, ClipOp.Difference)
                        continue
                    }

                    fadingCutout.coverage = cutout.coverage
                    fadingCount++
                    fadingBounds = if (fadingBounds.isEmpty) {
                        bounds
                    } else {
                        Rect(
                            left = minOf(fadingBounds.left, bounds.left),
                            top = minOf(fadingBounds.top, bounds.top),
                            right = maxOf(fadingBounds.right, bounds.right),
                            bottom = maxOf(fadingBounds.bottom, bounds.bottom)
                        )
                    }
                }

                if (fadingBounds.isEmpty) {
                    drawContent()
                } else {
                    drawFadingCutouts(bounds = fadingBounds, count = fadingCount)
                }
            }
        }

        private fun ContentDrawScope.drawFadingCutouts(
            bounds: Rect,
            count: Int
        ) {
            val canvas = drawContext.canvas
            // Align the two draws at integer bounds to keep the split clear of the cutout.
            val layerBounds = Rect(
                left = floor(bounds.left),
                top = floor(bounds.top),
                right = ceil(bounds.right),
                bottom = ceil(bounds.bottom)
            )
            canvas.withSave {
                canvas.clipRect(layerBounds, ClipOp.Difference)
                drawContent()
            }
            canvas.withSaveLayer(layerBounds, sourcePaint) {
                canvas.clipRect(layerBounds)
                drawContent()
                for (index in 0 until count) {
                    val cutout = fadingCutouts[index]
                    cutoutPaint.alpha = cutout.coverage
                    canvas.drawPath(cutout.path, cutoutPaint)
                }
            }
        }

        private fun buildCutoutPath(
            surface: GlassSurface,
            sourceCoordinates: LayoutCoordinates,
            sourceRoot: LayoutCoordinates,
            sourceSize: Size,
            path: Path
        ): Rect? {
            val properties = surface.properties

            val bounds = surface.bounds ?: return null
            val surfaceCoordinates = bounds.coordinates
            val surfaceSize = bounds.size
            if (!surfaceCoordinates.isAttached || surfaceSize.width <= 0 ||
                surfaceSize.height <= 0
            ) {
                return null
            }

            val sameRoot = bounds.root === sourceRoot
            val visibleBounds = when {
                sameRoot -> sourceCoordinates.localBoundingBoxOf(
                    sourceCoordinates = surfaceCoordinates,
                    clipBounds = true
                )
                properties.useWindowCoordinates -> Rect(Offset.Zero, sourceSize)
                else -> return null
            }
            if (visibleBounds.isEmpty) {
                return null
            }

            val rootWidth = bounds.widthInRoot
            val rootHeight = bounds.heightInRoot
            if (!rootWidth.isFinite() || !rootHeight.isFinite() ||
                rootWidth <= CutoutEdgeOverlapPx * 2f || rootHeight <= CutoutEdgeOverlapPx * 2f
            ) {
                return null
            }
            val insetX = CutoutEdgeOverlapPx / rootWidth
            val insetY = CutoutEdgeOverlapPx / rootHeight

            transform.reset()
            if (sameRoot) {
                sourceCoordinates.transformFrom(surfaceCoordinates, transform)
            } else if (!sourceCoordinates.transformFromWindow(surfaceCoordinates)) {
                return null
            }
            transform.translate(insetX * surfaceSize.width, insetY * surfaceSize.height)
            transform.scale(x = 1f - 2f * insetX, y = 1f - 2f * insetY)

            val outline = properties.shape.createOutline(
                size = surfaceSize.toSize(),
                layoutDirection = properties.layoutDirection,
                density = properties.density
            )
            path.reset()
            path.fillType = PathFillType.NonZero
            path.addOutline(outline)
            path.transform(transform)

            val shapeBounds = path.getBounds()
            if (shapeBounds.left >= visibleBounds.left &&
                shapeBounds.top >= visibleBounds.top &&
                shapeBounds.right <= visibleBounds.right &&
                shapeBounds.bottom <= visibleBounds.bottom
            ) {
                return shapeBounds
            }

            visiblePath.reset()
            visiblePath.addRect(visibleBounds)
            return if (path.op(path, visiblePath, PathOperation.Intersect)) {
                path.getBounds()
            } else {
                null
            }
        }

        // Same-canvas popups have their own layout roots but use the same window coordinates.
        private fun LayoutCoordinates.transformFromWindow(coordinates: LayoutCoordinates): Boolean {
            val origin = windowToLocal(coordinates.localToWindow(Offset.Zero))
            val horizontal = windowToLocal(coordinates.localToWindow(Offset(1f, 0f))) - origin
            val vertical = windowToLocal(coordinates.localToWindow(Offset(0f, 1f))) - origin
            if (!origin.isFinite || !horizontal.isFinite || !vertical.isFinite) {
                return false
            }

            transform[0, 0] = horizontal.x
            transform[0, 1] = horizontal.y
            transform[1, 0] = vertical.x
            transform[1, 1] = vertical.y
            transform[3, 0] = origin.x
            transform[3, 1] = origin.y
            return true
        }

        private class FadingCutout {
            val path = Path()
            var coverage = 0f
        }

        private companion object {
            // Keep source pixels under the antialiased edge, measured in root pixels.
            @Suppress("ConstPropertyName")
            private const val CutoutEdgeOverlapPx = 2.5f
        }
    }

    private data class SurfaceElement(
        val source: GlassSource,
        val shape: Shape,
        val density: Density,
        val layoutDirection: LayoutDirection,
        val useWindowCoordinates: Boolean,
        val selection: Selection?
    ) : ModifierNodeElement<SurfaceNode>() {
        override fun create() = SurfaceNode(this)

        override fun update(node: SurfaceNode) {
            node.update(this)
        }

        override fun InspectorInfo.inspectableProperties() {
            name = "glass"
        }
    }

    private class SurfaceNode(
        properties: SurfaceElement
    ) : Modifier.Node(),
        LayoutAwareModifierNode,
        GlobalPositionAwareModifierNode,
        UnplacedAwareModifierNode {
        private val surface = GlassSurface(properties)
        private var layerObserver: AutoCloseable? = null

        override fun onAttach() {
            surface.properties.source.surfaces.add(surface)
        }

        override fun onPlaced(coordinates: LayoutCoordinates) {
            if (layerObserver == null && !surface.properties.useWindowCoordinates) {
                layerObserver = glassLayerOpacity?.observe()
            }
            // Global positioning refreshes the transformed bounds after placement.
            if (surface.bounds?.coordinates !== coordinates) {
                surface.bounds = SurfaceBounds(coordinates)
            }
        }

        override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
            surface.bounds = SurfaceBounds(coordinates)
        }

        override fun onUnplaced() {
            layerObserver?.close()
            layerObserver = null
            surface.bounds = null
        }

        override fun onDetach() {
            onUnplaced()
            surface.properties.source.surfaces.remove(surface)
        }

        fun update(properties: SurfaceElement) {
            val previousSource = surface.properties.source
            if (isAttached && previousSource !== properties.source) {
                previousSource.surfaces.remove(surface)
                properties.source.surfaces.add(surface)
            }
            surface.properties = properties
        }
    }
}

/**
 * Remembers a source that can be shared between source content and glass surfaces.
 */
@UnstableSaltUiApi
@Composable
fun rememberGlassSource(): GlassSource = remember { GlassSource() }

/**
 * Marks this content as input for glass surfaces using the same [source].
 *
 * One [source] can mark multiple composables, each captured separately. Place the source content
 * behind its glass surfaces so automatic clipping matches the intended stacking order.
 *
 * @param key An optional layer identifier for explicit selection by [glass]. Non-null keys must be
 * stable and unique among active layers of this [source]. A null key leaves the layer unnamed.
 */
@UnstableSaltUiApi
fun Modifier.glassSource(
    source: GlassSource,
    key: Any? = null
): Modifier = this
    // Keep the clip outside hazeSource so Haze records the complete content.
    .then(
        GlassSource.SourceElement(
            source = source,
            key = key
        )
    )
    // Isolate clip-only redraws from unchanged source content.
    .graphicsLayer()
    .hazeSource(
        state = source.hazeState,
        key = key
    )

@Deprecated("Binary compatibility with the unkeyed overload", level = DeprecationLevel.HIDDEN)
@UnstableSaltUiApi
fun Modifier.glassSource(source: GlassSource): Modifier = glassSource(
    source = source,
    key = null
)

/**
 * Draws glass using content explicitly marked with [glassSource] and the same [source].
 * Unmarked content behind translucent source pixels can remain visible, but is not blurred.
 * [effect] selects a solid color, blur, regular glass or glass without an added color. It defaults
 * to [SaltTheme.glassEffect]. Its colors and glass appearance follow the light or dark
 * [SaltTheme.configs]. [interactionState] can be shared with other content to follow the surface.
 * Add click handling separately.
 *
 * Source content in the same Compose hierarchy is clipped beneath glass surfaces while remaining
 * available as input to the effect. Solid surfaces do not capture or clip source content.
 * Sources should be drawn before their glass surfaces.
 * On Android below 13, transparent glass leaves the original source visible during fallback.
 * On desktop, the cutout fades with graphics layer opacity applied to the glass.
 * Ancestor clipping is tracked using rectangular layout bounds.
 * The content and source cutout follow the pressed material as it scales and moves, while the
 * content's hit region remains at its original layout position. The press ends when another
 * gesture, such as scrolling, claims the pointer movement.
 *
 * @param sourceKeys Keys of the layers to sample and clip. Selection ignores key order and skips
 * layers that contain this surface. An empty set selects no layers. Null uses automatic selection.
 */
@UnstableSaltUiApi
@Composable
fun Modifier.glass(
    source: GlassSource,
    shape: RoundedCornerShape = CircleShape,
    effect: GlassEffect = SaltTheme.glassEffect,
    interactionState: GlassInteractionState = rememberGlassInteractionState(),
    sourceKeys: Set<Any>? = null
): Modifier = this
    .glassPointerInput(interactionState)
    // Move the material and its automatically tracked source cutout together.
    .graphicsLayer {
        scaleX = interactionState.scale
        scaleY = interactionState.scale
        transformOrigin = interactionState.transformOrigin(size)
        translationX = interactionState.translation.x
        translationY = interactionState.translation.y
    }
    // The glass cutout, when needed, observes the material's transform here.
    .glassMaterial(
        source = source,
        sourceKeys = sourceKeys,
        shape = shape,
        effect = effect,
        interactionState = interactionState
    )
    // Undo the material transform for the content's hit testing.
    .graphicsLayer {
        val inverseScale = 1f / interactionState.scale
        scaleX = inverseScale
        scaleY = inverseScale
        transformOrigin = interactionState.transformOrigin(size)
        translationX = -interactionState.translation.x * inverseScale
        translationY = -interactionState.translation.y * inverseScale
    }
    // Reapply the transform to content drawing without changing its hit region.
    .drawGlassInteraction(interactionState)

@Deprecated("Binary compatibility with the unkeyed overload", level = DeprecationLevel.HIDDEN)
@UnstableSaltUiApi
@Composable
fun Modifier.glass(
    source: GlassSource,
    shape: RoundedCornerShape = CircleShape,
    effect: GlassEffect = SaltTheme.glassEffect,
    interactionState: GlassInteractionState = rememberGlassInteractionState()
): Modifier = glass(
    source = source,
    shape = shape,
    effect = effect,
    interactionState = interactionState,
    sourceKeys = null
)

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun Modifier.glassMaterial(
    source: GlassSource,
    sourceKeys: Set<Any>?,
    shape: RoundedCornerShape,
    effect: GlassEffect,
    interactionState: GlassInteractionState
): Modifier {
    val isDarkTheme = SaltTheme.configs.isDarkTheme
    val keys = sourceKeys?.toSet()
    return composed {
        val selection = remember(source, keys) {
            GlassSource.Selection(
                source = source,
                sourceKeys = keys
            )
        }
        when (effect) {
            is GlassEffect.Solid -> background(
                color = if (isDarkTheme) effect.darkColor else effect.lightColor,
                shape = shape
            )
            GlassEffect.Blur -> {
                val os = OS.current
                if (os is OS.Android && os.versionSdk < OS.Android.ANDROID_12) {
                    background(
                        color = SaltTheme.colors.subBackground,
                        shape = shape
                    )
                } else {
                    val style = remember(isDarkTheme) { GlassStyles.blur(isDarkTheme) }
                    then(selection.trackAncestors())
                        .then(selection.cutout(shape))
                        .clip(shape)
                        .hazeBlur(
                            input = selection.input,
                            style = style
                        )
                }
            }
            is GlassEffect.Regular -> {
                val backgroundColor = if (isDarkTheme) effect.darkColor else effect.lightColor
                val tintColor = if (isDarkTheme) effect.darkTint else effect.lightTint
                val glassStyle = remember(shape, backgroundColor, tintColor, isDarkTheme) {
                    GlassStyles.regularSurface(isDarkTheme).then {
                        shape(shape)
                        backgroundColor(backgroundColor)
                        tint(tintColor)
                    }
                }
                refractiveGlass(
                    selection = selection,
                    shape = shape,
                    glassStyle = glassStyle,
                    isBackgroundFullyTransparent = backgroundColor.alpha == 0f,
                    interactionState = interactionState,
                    isDarkTheme = isDarkTheme
                )
                    .glassRimHighlight(
                        shape = shape,
                        isDarkTheme = isDarkTheme
                    )
            }
            GlassEffect.Transparent -> {
                val glassStyle = remember(shape, isDarkTheme) {
                    GlassStyles.transparent(isDarkTheme).then {
                        shape(shape)
                        backgroundColor(Color.Transparent)
                    }
                }
                refractiveGlass(
                    selection = selection,
                    shape = shape,
                    glassStyle = glassStyle,
                    isBackgroundFullyTransparent = true,
                    interactionState = interactionState,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

// Top and bottom rim bands from Haze's Apache-2.0 fallback renderer.
// Copyright 2025, Christopher Banes and the Haze project contributors.
// https://github.com/chrisbanes/haze/pull/1746
private fun Modifier.glassRimHighlight(
    shape: RoundedCornerShape,
    isDarkTheme: Boolean
): Modifier = drawWithCache {
    val widthPx = 1.dp.toPx()
    // Haze fades over the last two physical pixels. Use the equivalent solid width while
    // retaining one visible pixel when the band is wide enough.
    val bandWidthPx = (widthPx - 1f).coerceAtLeast(minOf(widthPx, 1f))
    val outline = shape.createOutline(size, layoutDirection, this)
    val silhouette = Path().apply { addOutline(outline) }

    fun edge(dy: Float): Path {
        val translated = Path().apply { addPath(silhouette, Offset(0f, dy)) }
        return Path.combine(PathOperation.Difference, silhouette, translated)
    }

    val rim = Path.combine(PathOperation.Union, edge(bandWidthPx), edge(-bandWidthPx))
    // Calibrated against the rendered Haze Regular rim at 100–200% display scale.
    // https://github.com/chrisbanes/haze/pull/1822
    val intensity = if (isDarkTheme) 0.25f else 0.7f
    onDrawBehind {
        drawPath(rim, Color.White, alpha = intensity * 0.8f)
    }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun Modifier.refractiveGlass(
    selection: GlassSource.Selection,
    shape: RoundedCornerShape,
    glassStyle: GlassStyle,
    isBackgroundFullyTransparent: Boolean,
    interactionState: GlassInteractionState,
    isDarkTheme: Boolean
): Modifier {
    val os = OS.current
    // Haze 2.1's transparent fallback does not replay its source below Android 13.
    val keepSourceVisible = isBackgroundFullyTransparent &&
        os is OS.Android &&
        os.versionSdk < OS.Android.ANDROID_13
    return this
        .then(selection.trackAncestors())
        .thenIf(!keepSourceVisible) {
            then(selection.cutout(shape))
        }
        // Inherit Haze's performance mode; full resolution can cost more to render.
        .hazeGlass(input = selection.input, style = glassStyle)
        .glassInteractionLight(
            interaction = interactionState,
            shape = shape,
            isDarkTheme = isDarkTheme
        )
}
