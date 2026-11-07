package de.bauhaupt.urlaubsschein.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun GridBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val isDark = backgroundColor.red < 0.5f
    val gridColor = if (isDark) Color(0xFF272B35) else Color(0xFFE2E6ED)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 90f
            val strokeWidth = 1.5f
            val color = gridColor

            // Vertikale Linien
            var x = gridSpacing
            while (x < size.width) {
                drawLine(
                    color = color,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = strokeWidth
                )
                x += gridSpacing
            }

            // Horizontale Linien
            var y = gridSpacing
            while (y < size.height) {
                drawLine(
                    color = color,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
                y += gridSpacing
            }
        }
        content()
    }
}
