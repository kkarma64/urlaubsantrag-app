package de.bauhaupt.urlaubsschein.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScreenContainer(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = 50.dp,
                bottom = 96.dp
            )
    ) {
        content()
    }
}
