/*
 * SaltUI
 * Copyright (C) 2025 Moriafly
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

@file:Suppress("unused", "ktlint:standard:property-naming")

package com.moriafly.salt.ui.material

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import dev.chrisbanes.haze.blur.HazeBlurStyle

/**
 * Material effect applied by [Modifier.material] and [Modifier.subMaterial].
 */
@UnstableSaltUiApi
enum class MaterialType {
    /**
     * Without material.
     */
    None,

    /**
     * Blurred glass effect with theme-aware tinting and subtle noise.
     */
    BlurryGlass,

    /**
     * Fluent acrylic effect using base and default styles for the material layers.
     */
    Acrylic,

    /**
     * Fluent mica effect using alternate and regular styles for the material layers.
     */
    Mica,

    /**
     * Strongly blurred effect with layered color blending and subtle noise on the sub-background.
     */
    Premium
}

/**
 * Background layer used to select the styling of a [MaterialType].
 */
@UnstableSaltUiApi
enum class MaterialLayer {
    /**
     * Primary background layer used by [Modifier.material].
     */
    Background,

    /**
     * Secondary background layer used by [Modifier.subMaterial].
     */
    SubBackground
}

/**
 * Describes how the content of a [MaterialSource] changes over time.
 */
@UnstableSaltUiApi
@Immutable
sealed interface MaterialSourceMode {
    /**
     * The source content may change at any time.
     */
    data object Dynamic : MaterialSourceMode

    /**
     * Declares visually unchanged content for the same [key], layout, and rendering environment,
     * allowing material pixels to be cached and reused by consumers.
     *
     * Represent every content change, including asynchronous image loading, with a new [key]. Keys
     * are compared using [Any.equals]; use an immutable value with stable equality. [Unit] can be
     * used for content without external changes.
     *
     * Prefer wallpaper images or content drawn with fixed colors. Static source content must not
     * depend on changing theme colors such as [SaltTheme.colors] without including their resolved
     * values in [key]. The same rule applies to custom CompositionLocals, text styles, shapes, and
     * other visual inputs: their reads inside content are not observed by the pixel cache. A
     * Compose [Color] with a fixed value is safe; a theme color can be used as the content key.
     *
     * Source size, density, layout direction, dark/light mode, and consumer material styles
     * invalidate caches automatically. Palette changes within the same dark/light mode require a
     * new content key. Results belong to this source instance and may be rebuilt. Source content
     * continues to compose normally; rendering paths without cache support remain dynamic.
     */
    data class Static(
        val key: Any
    ) : MaterialSourceMode
}

/**
 * Creates a Material background layer for implementing [Modifier.material] and
 * [Modifier.subMaterial].
 *
 * Note: Do not use [Modifier.material] or [Modifier.subMaterial] inside [content], as [content] is
 * typically the wallpaper image.
 *
 * Additionally, the hierarchy of MaterialSource needs to be placed before elements using
 * [Modifier.material] or [Modifier.subMaterial] to achieve the correct effect.
 *
 * Using a Composable instead of a Modifier here is intentional - it encourages developers to
 * explicitly specify the hierarchy of MaterialSource and use it only once.
 *
 * @param mode Describes whether the source content changes dynamically or is static for a content
 * key. Static sources allow material pixels to be baked and shared by consumers. Multiple sources
 * in the same theme, consumers extending outside the source or using a different transform, and
 * inherited custom Haze styles currently use the dynamic pipeline to preserve their appearance.
 */
@UnstableSaltUiApi
@Composable
fun MaterialSource(
    modifier: Modifier = Modifier,
    materialSelf: Boolean = true,
    mode: MaterialSourceMode = MaterialSourceMode.Dynamic,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
    ) {
        MaterialSourceContent(
            mode = mode,
            content = content
        )

        if (materialSelf) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .material()
            )
        }
    }
}

@Deprecated("Maintained for binary compatibility", level = DeprecationLevel.HIDDEN)
@UnstableSaltUiApi
@Composable
fun MaterialSource(
    modifier: Modifier = Modifier,
    materialSelf: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    MaterialSource(
        modifier = modifier,
        materialSelf = materialSelf,
        mode = MaterialSourceMode.Dynamic,
        content = content
    )
}

/**
 * Disables material effects for child components, commonly used in Dialogs.
 */
@UnstableSaltUiApi
@Composable
fun DisableMaterial(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalMaterialState provides null
    ) {
        content()
    }
}

@UnstableSaltUiApi
@Composable
fun Modifier.material(
    isDarkTheme: Boolean = SaltTheme.configs.isDarkTheme,
    fallback: Color = Color.Unspecified
): Modifier = basicMaterial(
    type = SaltTheme.material.type,
    layer = MaterialLayer.Background,
    isDarkTheme = isDarkTheme,
    fallback = fallback
)

@UnstableSaltUiApi
@Composable
fun Modifier.subMaterial(
    isDarkTheme: Boolean = SaltTheme.configs.isDarkTheme,
    fallback: Color = Color.Unspecified
): Modifier = basicMaterial(
    type = SaltTheme.material.type,
    layer = MaterialLayer.SubBackground,
    isDarkTheme = isDarkTheme,
    fallback = fallback
)

@UnstableSaltUiApi
@Composable
internal fun Modifier.basicMaterial(
    type: MaterialType,
    layer: MaterialLayer,
    isDarkTheme: Boolean,
    fallback: Color
): Modifier {
    val state = LocalMaterialState.current

    return if (state != null) {
        val blurStyle = when (type) {
            MaterialType.None -> HazeBlurStyle
            MaterialType.BlurryGlass -> SaltHazeStyles.blurryGlass(layer, isDarkTheme)
            MaterialType.Acrylic -> SaltHazeStyles.acrylic(layer, isDarkTheme)
            MaterialType.Mica -> SaltHazeStyles.mica(layer, isDarkTheme)
            MaterialType.Premium -> SaltHazeStyles.premium(layer, isDarkTheme)
        }
        materialEffect(state, blurStyle)
    } else {
        background(fallback)
    }
}

internal val LocalMaterialState = compositionLocalOf<MaterialState?> { null }
