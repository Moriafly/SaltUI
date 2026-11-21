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

package com.moriafly.salt.ui.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.popup.PopupMenu
import com.moriafly.salt.ui.popup.PopupMenuBackdrop
import com.moriafly.salt.ui.popup.PopupMenuDefaults
import com.moriafly.salt.ui.popup.PopupMenuMaterial
import com.moriafly.salt.ui.popup.createMenuPositionProvider
import kotlinx.coroutines.Dispatchers
import org.jetbrains.skia.Bitmap
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalComposeUiApi::class, UnstableSaltUiApi::class)
class SheetPopupBackdropTest {
    @Test
    fun glassAndBlurMenusPreferTheSheetBackdrop() = verifySheetBackdrop(ownerBackdropEnabled = true)

    @Test
    fun glassAndBlurMenusSampleTheSheetWithoutAnOwnerBackdrop() =
        verifySheetBackdrop(ownerBackdropEnabled = false)

    private fun verifySheetBackdrop(ownerBackdropEnabled: Boolean) {
        for (material in listOf(PopupMenuMaterial.Glass, PopupMenuMaterial.Blur)) {
            val onDarkSheet = renderMenu(
                material = material,
                sheetColor = Color.Black,
                ownerBackdropEnabled = ownerBackdropEnabled
            )
            val onLightSheet = renderMenu(
                material = material,
                sheetColor = Color.White,
                ownerBackdropEnabled = ownerBackdropEnabled
            )

            assertTrue(onDarkSheet.red > 0.02f, "$material must draw above the Sheet content")
            assertTrue(
                onLightSheet.red > onDarkSheet.red + 0.05f,
                "$material must reflect the Sheet color: $onDarkSheet -> $onLightSheet"
            )
        }
    }

    private fun renderMenu(
        material: PopupMenuMaterial,
        sheetColor: Color,
        ownerBackdropEnabled: Boolean
    ): Color = ImageComposeScene(
        width = 360,
        height = 420,
        coroutineContext = Dispatchers.Unconfined
    ) {
        SaltTheme {
            PopupMenuBackdrop(
                modifier = Modifier.fillMaxSize(),
                enabled = ownerBackdropEnabled
            ) {
                Box(Modifier.fillMaxSize().background(Color.Green))
                Sheet(
                    visible = true,
                    onDismissRequest = {},
                    properties = SheetProperties(
                        displayMode = SheetDisplayMode.Bottom,
                        detents = listOf(SheetDetent.Large),
                        initialDetent = SheetDetent.Large
                    )
                ) {
                    Box(Modifier.fillMaxWidth().height(260.dp).background(sheetColor)) {
                        PopupMenu(
                            expanded = true,
                            onDismissRequest = {},
                            scrollable = false,
                            popupPositionProvider = createMenuPositionProvider { _, _, _, _ ->
                                IntOffset(80, 130)
                            },
                            style = PopupMenuDefaults.style(material = material)
                        ) {
                            Box(Modifier.size(120.dp, 56.dp))
                        }
                    }
                }
            }
        }
    }.use { scene ->
        repeat(60) { frame -> scene.render((frame + 1) * 16_666_667L).close() }
        scene.render(1_050_000_000L).use { image ->
            Bitmap.makeFromImage(image).use { bitmap ->
                bitmap.asComposeImageBitmap().toPixelMap(140, 155, 1, 1)[0, 0]
            }
        }
    }
}
