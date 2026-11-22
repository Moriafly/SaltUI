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

package com.moriafly.salt.ui.popup

import androidx.compose.ui.graphics.RenderEffect

internal expect fun createMenuBackdropToneEffect(maxLuminance: Float): RenderEffect?

internal const val MENU_BACKDROP_TONE_SHADER = """
    uniform shader menuBackdrop;
    uniform float maxLuminance;

    // Use the sRGB transfer curve explicitly, including on Skia surfaces without a color space.
    float3 decodeSrgb(float3 rgb) {
        return mix(rgb / 12.92, pow((rgb + 0.055) / 1.055, float3(2.4)),
            step(float3(0.04045), rgb));
    }

    float3 encodeSrgb(float3 rgb) {
        return mix(rgb * 12.92, 1.055 * pow(rgb, float3(1.0 / 2.4)) - 0.055,
            step(float3(0.0031308), rgb));
    }

    half4 main(float2 position) {
        float4 pixel = menuBackdrop.eval(position);
        float limit = encodeSrgb(float3(maxLuminance)).r;
        float knee = 0.75 * limit;
        // Estimate the dark window through uncovered pixels. Apply only the resulting RGB change
        // to the captured layer so window coverage and the boundary of translucent artwork survive.
        float missingBackdrop = (1.0 - pixel.a) * (0.97 * knee);
        float3 projected = max(pixel.rgb + missingBackdrop, float3(0.0));
        float3 linear = decodeSrgb(projected);
        float luminance = dot(linear, float3(0.2126, 0.7152, 0.0722));
        float level = encodeSrgb(float3(luminance)).r;
        if (level <= knee) return half4(pixel);

        // Fade the slope from 1 to a small positive value. Matching the slope on either end of
        // the shoulder keeps gradients smooth and leaves visible differences between highlights.
        float brightSlope = 0.22 * limit;
        float width = 2.0 * (limit - knee - brightSlope * (1.0 - knee)) / (1.0 - brightSlope);
        float excess = min(level, 1.0) - knee;
        float t = clamp(excess / width, 0.0, 1.0);
        float mapped = knee + brightSlope * excess + (1.0 - brightSlope) * width * t * (1.0 - 0.5 * t);
        float targetLuminance = decodeSrgb(float3(mapped)).r;
        float3 toned = encodeSrgb(linear * (targetLuminance / luminance));

        // Subpixel dither spreads rounding error across pixels on 8-bit surfaces.
        float noise = fract(52.9829189 * fract(dot(floor(position), float2(0.06711056, 0.00583715))));
        float dither = (noise - 0.5) * smoothstep(0.0, width, excess) / 255.0;
        return half4(clamp(toned - missingBackdrop + dither, float3(0.0), float3(pixel.a)), pixel.a);
    }
"""
