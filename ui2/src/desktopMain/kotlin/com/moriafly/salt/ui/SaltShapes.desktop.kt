package com.moriafly.salt.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

actual object SaltShapesDefaults {
    actual val small: Shape
        get() = RoundedCornerShape(4.dp)
    actual val medium: Shape
        get() = RoundedCornerShape(6.dp)
    actual val large: Shape
        get() = RoundedCornerShape(10.dp)
}
