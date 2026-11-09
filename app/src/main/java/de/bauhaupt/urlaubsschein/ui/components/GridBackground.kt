package de.bauhaupt.urlaubsschein.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GridBackground(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Das Raster im Hintergrund
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 40.dp.toPx()
            val lineColor = Color.Gray.copy(alpha = 0.1f)

            var x = 0f
            while (x < size.width) {
                drawLine(color = lineColor, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1f)
                x += gridSpacing
            }

            var y = 0f
            while (y < size.height) {
                drawLine(color = lineColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1f)
                y += gridSpacing
            }
        }

        // Der Inhalt, ohne künstliche Abstände
        content()
    }
}