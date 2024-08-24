/*
 * SaltUI
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

@file:Suppress("UNUSED")

package com.moriafly.salt.ui.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import com.moriafly.salt.ui.UnstableSaltApi

@UnstableSaltApi
object ScreenUtils {

    /**
     * Get the rounded corner radius for the top corners of the screen.
     *
     * The curvature of the rounded corners varies among various devices, and this radius attribute is also an approximate value.
     * In actual implementation, further processing should be considered after obtaining this attribute,
     * such as defaulting to smaller values to cover different curvatures.
     *
     * @param context The context.
     *
     * @return The pixel value of the rounded corner radius for the top corners of the screen.
     *
     * @throws Resources.NotFoundException This attribute needs to be defined by the OEM manufacturer,
     *      and there may be situations where it cannot be obtained.
     */
    @SuppressLint("DiscouragedApi")
    fun getRoundedCornerRadiusTop(context: Context): Int {
        val resourceId = context.resources.getIdentifier("rounded_corner_radius_top", "dimen", "android")
        if (resourceId != 0) {
            return context.resources.getDimensionPixelSize(resourceId)
        } else {
            throw Resources.NotFoundException("Resource id rounded_corner_radius_top not found.")
        }
    }

}