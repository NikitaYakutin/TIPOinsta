package com.example.sosogram.ui.theme

import android.app.Activity
import android.app.StatusBarManager
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import java.lang.reflect.Modifier


// Темная схема цветов
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1B5E20),  // Темный зеленый
    secondary = Color(0xFF388E3C),  // Зеленый
    tertiary = Color(0xFF8E24AA),  // Розовый
    background = Color(0xFF121212),  // Темный фон
    surface = Color(0xFF1D1D1D),  // Темная поверхность
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

// Светлая схема цветов
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF388E3C),  // Зеленый
    secondary = Color(0xFF81C784),  // Светлый зеленый
    tertiary = Color(0xFFFF4081),  // Розовый
    background = Color(0xFFFFFFFF),  // Белый фон
    surface = Color(0xFFFAFAFA),  // Светлая поверхность
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun SosogramTheme(
    darkTheme: Boolean =true,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,  // Задайте шрифты, если необходимо
        content = content
    )
}



