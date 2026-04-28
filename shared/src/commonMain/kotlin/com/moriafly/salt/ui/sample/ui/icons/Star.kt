package com.moriafly.salt.ui.sample.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val SimpleIcons.Star: ImageVector
    get() {
        if (_Star != null) {
            return _Star!!
        }
        _Star = ImageVector.Builder(
            name = "Star",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 48f,
            viewportHeight = 48f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(24.01f, 5f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 22.658f, 5.83f)
                lineTo(17.506f, 16.135f)
                lineTo(5.271f, 18.018f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4.439f, 20.561f)
                lineTo(12.902f, 29.023f)
                lineTo(11.018f, 41.271f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 13.193f, 42.83f)
                lineTo(24f, 37.191f)
                lineTo(34.807f, 42.83f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 36.982f, 41.271f)
                lineTo(35.098f, 29.023f)
                lineTo(43.561f, 20.561f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 42.729f, 18.018f)
                lineTo(30.494f, 16.135f)
                lineTo(25.342f, 5.83f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 24.01f, 5f)
                close()
                moveTo(24f, 9.854f)
                lineTo(28.158f, 18.17f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 29.271f, 18.982f)
                lineTo(39.346f, 20.533f)
                lineTo(32.439f, 27.439f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32.018f, 28.729f)
                lineTo(33.566f, 38.799f)
                lineTo(24.693f, 34.17f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 23.307f, 34.17f)
                lineTo(14.434f, 38.799f)
                lineTo(15.982f, 28.729f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 15.561f, 27.439f)
                lineTo(8.654f, 20.533f)
                lineTo(18.729f, 18.982f)
                arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 19.842f, 18.17f)
                lineTo(24f, 9.854f)
                close()
            }
        }.build()

        return _Star!!
    }

@Suppress("ObjectPropertyName")
private var _Star: ImageVector? = null
