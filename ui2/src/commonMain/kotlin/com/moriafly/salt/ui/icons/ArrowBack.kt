package com.moriafly.salt.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val SaltIcons.ArrowBack: ImageVector
    get() {
        if (_ArrowBack != null) {
            return _ArrowBack!!
        }
        _ArrowBack = ImageVector.Builder(
            name = "ArrowBack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
            autoMirror = true
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(16.62f, 2.99f)
                curveToRelative(-0.49f, -0.49f, -1.28f, -0.49f, -1.77f, 0f)
                lineTo(6.54f, 11.3f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0f, 1.41f)
                lineToRelative(8.31f, 8.31f)
                curveToRelative(0.49f, 0.49f, 1.28f, 0.49f, 1.77f, 0f)
                reflectiveCurveToRelative(0.49f, -1.28f, 0f, -1.77f)
                lineTo(9.38f, 12f)
                lineToRelative(7.25f, -7.25f)
                curveToRelative(0.48f, -0.48f, 0.48f, -1.28f, -0.01f, -1.76f)
                close()
            }
        }.build()

        return _ArrowBack!!
    }

@Suppress("ObjectPropertyName")
private var _ArrowBack: ImageVector? = null
