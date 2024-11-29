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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toolingGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import org.jetbrains.compose.resources.stringResource

/**
 * A icon component that draws [imageVector] using [tint], with a default value of
 * [SaltColors.text]. If [imageVector] has no intrinsic size, this component will use the
 * recommended default size. Icon is an opinionated component designed to be used with single-color
 * icons so that they can be tinted correctly for the component they are placed in. For multicolored
 * icons and icons that should not be tinted, use [Color.Unspecified] for [tint]. For generic images
 * that should not be tinted, and do not follow the recommended icon size, use the generic
 * [androidx.compose.foundation.Image] instead
 *
 * @param imageVector [ImageVector] to draw inside this Icon
 * @param contentDescription text used by accessibility services to describe what this icon
 *   represents. This should always be provided unless this icon is used for decorative purposes,
 *   and does not represent a meaningful action that a user can take. This text should be localized,
 *   such as by using [stringResource] or similar
 * @param modifier optional [Modifier] for this Icon
 * @param tint tint to be applied to [imageVector]. If [Color.Unspecified] is provided, then no tint
 *   is applied
 */
@Composable
@NonRestartableComposable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = SaltTheme.colors.text
) {
    Icon(
        painter = rememberVectorPainter(imageVector),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}

/**
 * A icon component that draws [bitmap] using [tint], with a default value of [SaltColors.text].
 * If [bitmap] has no intrinsic size, this component will use the recommended default size. Icon is
 * an opinionated component designed to be used with single-color icons so that they can be tinted
 * correctly for the component they are placed in. For multicolored icons and icons that should not
 * be tinted, use [Color.Unspecified] for [tint]. For generic images that should not be tinted, and
 * do not follow the recommended icon size, use the generic [androidx.compose.foundation.Image]
 * instead
 *
 * @param bitmap [ImageBitmap] to draw inside this Icon
 * @param contentDescription text used by accessibility services to describe what this icon
 *   represents. This should always be provided unless this icon is used for decorative purposes,
 *   and does not represent a meaningful action that a user can take. This text should be localized,
 *   such as by using [stringResource] or similar
 * @param modifier optional [Modifier] for this Icon
 * @param tint tint to be applied to [bitmap]. If [Color.Unspecified] is provided, then no tint is
 *   applied
 */
@Composable
@NonRestartableComposable
fun Icon(
    bitmap: ImageBitmap,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = SaltTheme.colors.text
) {
    val painter = remember(bitmap) { BitmapPainter(bitmap) }
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}

/**
 * A icon component that draws [painter] using [tint], with a default value of [SaltColors.text].
 * If [painter] has no intrinsic size, this component will use the recommended default size. Icon is
 * an opinionated component designed to be used with single-color icons so that they can be tinted
 * correctly for the component they are placed in. For multicolored icons and icons that should not
 * be tinted, use [Color.Unspecified] for [tint]. For generic images that should not be tinted, and
 * do not follow the recommended icon size, use the generic [androidx.compose.foundation.Image]
 * instead
 *
 * @param painter [Painter] to draw inside this Icon
 * @param contentDescription text used by accessibility services to describe what this icon
 *   represents. This should always be provided unless this icon is used for decorative purposes,
 *   and does not represent a meaningful action that a user can take. This text should be localized,
 *   such as by using [stringResource] or similar
 * @param modifier optional [Modifier] for this Icon
 * @param tint tint to be applied to [painter]. If [Color.Unspecified] is provided, then no tint is
 *   applied
 */
@Composable
fun Icon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = SaltTheme.colors.text
) {
    val colorFilter = remember(tint) {
        if (tint == Color.Unspecified) null else ColorFilter.tint(tint)
    }
    Box(
        modifier = modifier
            .toolingGraphicsLayer()
            .thenIf(
                painter.intrinsicSize == Size.Unspecified
                        || painter.intrinsicSize.isInfinite()
            ) {
                size(SaltTheme.dimens.itemIcon)
            }
            .paint(
                painter,
                colorFilter = colorFilter,
                contentScale = ContentScale.Fit
            )
            .thenIf(contentDescription != null) {
                semantics {
                    this.contentDescription = contentDescription!!
                    this.role = Role.Image
                }
            }
    )
}

private fun Size.isInfinite() = width.isInfinite() && height.isInfinite()