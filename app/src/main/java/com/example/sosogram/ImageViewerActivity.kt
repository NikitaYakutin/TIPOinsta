package com.example.sosogram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.sosogram.ui.theme.SosogramTheme

class ImageViewerActivity : ComponentActivity() {
    val baseUI = BaseUI()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageUrl = intent.getStringExtra("imageUrl")

        enableEdgeToEdge()
        setContent {
            // Выбор темы на основе системной темной темы
            SosogramTheme(darkTheme = isSystemInDarkTheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Main(url = imageUrl ?: "")
                }
            }
        }
    }

    @Composable
    fun Main(url: String) {
        var fullscreenImageUrl by remember { mutableStateOf<String?>(url) }

        fullscreenImageUrl?.let { imageUrl ->
            baseUI.FullscreenImageViewer(
                imageUrl = imageUrl,
                onClose = { finish() }
            )
        }
    }
}
