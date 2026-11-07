package de.bauhaupt.urlaubsschein.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- Helles Farbschema (Seriös, klar, passend zur Bau-App) ---
val LightBackground = Color(0xFFF4F6F8)       // Warmes, sehr helles Grau/Weiß
val LightSurface = Color(0xFFFFFFFF)          // Reines Weiß für Karten & Formulare
val LightAccentYellow = Color(0xFFB38600)     // Satteres, dunkleres Gelb/Gold für optimalen Kontrast auf hell
val LightTextDark = Color(0xFF1A1D24)         // Anthrazit/Dunkelblau für exzellente Lesbarkeit
val LightTextGray = Color(0xFF5A6072)         // Mittleres Grau für Untertitel
val LightBorderColor = Color(0xFFD1D5DB)      // Dezentere Rahmenfarbe im Hellmodus

private val DarkColorScheme = darkColorScheme(
    primary = AccentYellow,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkBackground,
    onBackground = TextWhite,
    onSurface = TextWhite
)

private val LightColorScheme = lightColorScheme(
    primary = LightAccentYellow,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onBackground = LightTextDark,
    onSurface = LightTextDark
)

@Composable
fun UrlaubsscheinBauTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
