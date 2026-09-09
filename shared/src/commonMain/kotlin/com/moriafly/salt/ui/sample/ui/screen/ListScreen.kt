/*
 * Salt UI
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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.plus
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.RoundedColumnType
import com.moriafly.salt.ui.SaltDimens
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.innerPadding
import com.moriafly.salt.ui.lazy.LazyColumn
import com.moriafly.salt.ui.sample.ui.screen.basic.BasicScreenBox

@Suppress("ktlint:compose:modifier-missing-check")
@OptIn(UnstableSaltUiApi::class)
@Composable
fun ListScreen() {
    BasicScreenBox(
        title = "List"
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = contentPadding +
                PaddingValues(
                    vertical = SaltDimens.RoundedColumnInListEdgePadding
                )
        ) {
            items(100) {
                RoundedColumn(
                    type = RoundedColumnType.InList
                ) {
                    Text(
                        text = "Item $it",
                        modifier = Modifier
                            .innerPadding()
                    )
                }
            }
        }
    }
}
