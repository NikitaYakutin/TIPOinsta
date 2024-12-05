package com.example.sosogram

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sosogram.ui.theme.SosogramTheme
import kotlinx.coroutines.launch

class PostCreationActivity : ComponentActivity() {
    private val baseUI = BaseUI()
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentUserId = getUserIdFromSharedPreferences(this)

        enableEdgeToEdge()
        setContent {
            SosogramTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    CreatePostContent()
                }
            }
        }
    }

    @Composable
    fun CreatePostContent() {
        var text by remember { mutableStateOf("") }
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        val coroutineScope = rememberCoroutineScope()
        var isPosting by remember { mutableStateOf(false) }
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(50.dp))

            // Кнопка "Отмена" с тусклым красным
            baseUI.MyIconButton(
                modik = Modifier,
                text = "Отмена",
                background = Color.Red.copy(alpha = 0.6f),
                contentColor = Color.White,
                defaultIcon = Icons.Default.Close
            ) { finish() }

            Spacer(Modifier.height(25.dp))

            // Текстовое поле для ввода
            baseUI.MyTextField(
                value = text,
                placeholder = "Введите текст",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray, RoundedCornerShape(8.dp)),

            ) { text = it }

            Spacer(modifier = Modifier.height(16.dp))

            // Выбор изображения
            val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
                uri?.let {
                    imageUri = it
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.2f))
                    .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable {
                        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Image preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Image preview",
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Выберите изображение",
                            color = Color.Gray,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка "Добавить пост" с серым фоном
            baseUI.MyButton(
                modik = Modifier.fillMaxWidth(),
                text = "Опубликовать",
                color = if (!isPosting) Color.Gray else Color.DarkGray
            ) {
                if (!isPosting) {
                    isPosting = true

                    coroutineScope.launch {
                        if (text.isBlank() && imageUri == null) {
                            Toast.makeText(context, "Пост должен содержать текст или изображение", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        val uploadedImageUrl = imageUri?.let { uploadPostImage(context, it, currentUserId ?: "") }

                        val success = addNewPost(
                            context,
                            getUserIdFromSharedPreferences(context) ?: "",
                            text,
                            uploadedImageUrl
                        )

                        if (success == true) {
                            Toast.makeText(context, "Пост успешно добавлен!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(context, "Не удалось добавить пост", Toast.LENGTH_SHORT).show()
                            isPosting = false
                        }
                    }
                }
            }
        }
    }
}
