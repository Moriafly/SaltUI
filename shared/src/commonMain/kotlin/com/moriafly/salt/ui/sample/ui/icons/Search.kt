package com.moriafly.salt.ui.sample.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val SimpleIcons.Search: ImageVector
    get() {
        if (_Search != null) {
            return _Search!!
        }
        _Search = ImageVector.Builder(
            name = "Search",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 48f,
            viewportHeight = 48f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20.5f, 6f)
                curveTo(12.51f, 6f, 6f, 12.51f, 6f, 20.5f)
                curveTo(6f, 28.49f, 12.51f, 35f, 20.5f, 35f)
                curveTo(23.956f, 35f, 27.134f, 33.779f, 29.629f, 31.75f)
                lineTo(39.439f, 41.561f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = true, isPositiveArc = false, 41.561f, 39.439f)
                lineTo(31.75f, 29.629f)
                curveTo(33.779f, 27.134f, 35f, 23.956f, 35f, 20.5f)
                curveTo(35f, 12.51f, 28.49f, 6f, 20.5f, 6f)
                close()
                moveTo(20.5f, 9f)
                curveTo(26.869f, 9f, 32f, 14.131f, 32f, 20.5f)
                curveTo(32f, 23.603f, 30.776f, 26.406f, 28.791f, 28.471f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 28.471f, 28.791f)
                curveTo(26.406f, 30.776f, 23.603f, 32f, 20.5f, 32f)
                curveTo(14.131f, 32f, 9f, 26.869f, 9f, 20.5f)
                curveTo(9f, 14.131f, 14.131f, 9f, 20.5f, 9f)
                close()
            }
        }.build()

        return _Search!!
    }

@Suppress("ObjectPropertyName")
private var _Search: ImageVector? = null
