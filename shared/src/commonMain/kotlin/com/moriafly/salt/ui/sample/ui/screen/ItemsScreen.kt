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

package com.moriafly.salt.ui.sample.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.moriafly.salt.ui.ItemButton
import com.moriafly.salt.ui.ItemCheck
import com.moriafly.salt.ui.ItemDivider
import com.moriafly.salt.ui.ItemDropdown
import com.moriafly.salt.ui.ItemEdit
import com.moriafly.salt.ui.ItemEditPassword
import com.moriafly.salt.ui.ItemInfo
import com.moriafly.salt.ui.ItemInfoType
import com.moriafly.salt.ui.ItemLabelValueContainer
import com.moriafly.salt.ui.ItemSlider
import com.moriafly.salt.ui.ItemValue
import com.moriafly.salt.ui.LabelValue
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.popup.PopupMenuItem
import com.moriafly.salt.ui.sample.ui.screen.basic.BasicScreenColumn
import com.moriafly.salt.ui.screen.ScreenCard

@OptIn(UnstableSaltUiApi::class)
@Composable
fun ItemsScreen() {
    var name by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var checked by rememberSaveable { mutableStateOf(false) }
    var selectedQuality by rememberSaveable { mutableStateOf("High") }
    var volume by rememberSaveable { mutableFloatStateOf(0.5f) }
    var actionCount by rememberSaveable { mutableIntStateOf(0) }

    BasicScreenColumn(title = "Items") {
        ScreenCard(header = "Edit") {
            ItemEdit(
                text = name,
                onChange = { name = it },
                hint = "Enter a name"
            )
            ItemDivider()
            ItemEditPassword(
                text = password,
                onChange = { password = it },
                hint = "Enter a password"
            )
        }

        ScreenCard(header = "Selection") {
            ItemCheck(
                state = checked,
                onChange = { checked = it },
                text = "Enable notifications",
                sub = "Tap to change the checked state"
            )
            ItemCheck(
                state = true,
                onChange = {},
                text = "Disabled selection",
                enabled = false
            )
            ItemDropdown(
                text = "Quality",
                value = selectedQuality
            ) {
                listOf("Low", "Medium", "High").forEach { quality ->
                    PopupMenuItem(
                        onClick = {
                            selectedQuality = quality
                            state.dismiss()
                        },
                        selected = selectedQuality == quality,
                        text = quality
                    )
                }
            }
            ItemSlider(
                value = volume,
                onValueChange = { volume = it },
                text = "Volume",
                sub = "${(volume * 100).toInt()}%"
            )
        }

        ScreenCard(header = "Values and actions") {
            ItemValue(
                text = "Name",
                sub = name.ifBlank { "No name entered" }
            )
            ItemLabelValueContainer {
                LabelValue(label = "Quality", value = selectedQuality)
                LabelValue(label = "Notifications", value = if (checked) "On" else "Off")
                LabelValue(label = "Actions", value = actionCount.toString())
            }
            ItemButton(
                onClick = { actionCount++ },
                text = "Run action ($actionCount)"
            )
            ItemButton(
                onClick = { actionCount = 0 },
                text = "Reset action count",
                primary = false
            )
        }

        ItemInfoType.entries.forEach { type ->
            ScreenCard {
                ItemInfo(
                    text = when (type) {
                        ItemInfoType.Success -> "Changes saved"
                        ItemInfoType.Warning -> "Review this setting"
                        ItemInfoType.Error -> "Something went wrong"
                    },
                    infoType = type
                )
            }
        }
    }
}
