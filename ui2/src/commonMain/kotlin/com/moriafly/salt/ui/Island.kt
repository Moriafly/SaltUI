package com.moriafly.salt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@UnstableSaltUiApi
@Composable
fun Island(
    modifier: Modifier = Modifier,
    shape: Shape = SaltTheme.shapes.large,
    contentPadding: PaddingValues = PaddingValues(IslandDefaults.IslandPadding),
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .padding(contentPadding)
            .clip(shape)
            .border(
                width = 1.dp,
                color = SaltTheme.colors.stroke,
                shape = shape
            )
            .background(SaltTheme.colors.background),
        content = content
    )
}

@UnstableSaltUiApi
@Composable
fun IslandGroup(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(IslandDefaults.IslandGroupPadding),
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .padding(contentPadding),
        content = content
    )
}

object IslandDefaults {
    val IslandPadding = 1.75f.dp
    val IslandGroupPadding = 2.25f.dp
}
