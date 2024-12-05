package com.example.sosogram

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import java.time.LocalDateTime

open class BaseUI {

    @Composable
    fun DarkThemeUI() {
        // Устанавливаем черный фон
        val backgroundColor = Color.Black
        val primaryColor = Color.Gray // Серый для кнопок
        val accentColor = Color(0xFFB53C3C) // Тусклый красный для критичных кнопок

        // Фон всего экрана
        Box(
            modifier = Modifier.fillMaxSize().background(backgroundColor)
        ) {
            // Пример кнопки
            MyButton(
                modik = Modifier.padding(16.dp),
                text = "Обычная кнопка",
                color = primaryColor,
                onCLick = { /* действие */ }
            )

            // Пример красной кнопки (например, для выхода)
            MyButton(
                modik = Modifier.padding(16.dp).align(Alignment.BottomCenter),
                text = "Выход",
                color = accentColor,
                onCLick = { /* выход */ }
            )
        }
    }

    @Composable
    fun MyButton(
        modik: Modifier,
        text: String,
        color: Color = MaterialTheme.colorScheme.primary,
        shape: Shape = RoundedCornerShape(6.dp),
        onCLick: () -> Unit
    ) {
        Button(
            modifier = modik,
            onClick = { onCLick() },
            colors = ButtonDefaults.buttonColors(
                containerColor = color,
                contentColor = Color.White // Белый текст
            ),
            shape = shape
        ) {
            Text(
                text = text,
                color = Color.White // Белый текст на темном фоне
            )
        }
    }

    @Composable
    fun MyIconButton(
        modik: Modifier,
        text: String,
        background: Color = MaterialTheme.colorScheme.surface,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        shape: Shape = RoundedCornerShape(6.dp),
        icon: Int? = null,
        defaultIcon: ImageVector = Icons.Default.Close,
        description: String? = null,
        onCLick: () -> Unit
    ) {
        Box(
            modifier = modik
                .clickable { onCLick() }
                .background(color = background, shape = shape),
            contentAlignment = Alignment.Center
        ) {
            Row(Modifier.padding(5.dp)) {
                Icon(
                    imageVector = (if (icon != null) ImageVector.vectorResource(icon) else defaultIcon),
                    tint = contentColor,
                    contentDescription = description,
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = text,
                    color = contentColor // белый текст на темном фоне
                )
            }
        }
    }

    @Composable
    fun MyTextField(
        modifier: Modifier = Modifier,
        value: String,
        placeholder: String = "",
        visualTransform: VisualTransformation = VisualTransformation.None,
        input: KeyboardOptions = KeyboardOptions.Default,
        onChange: (String) -> Unit
    ) {
        OutlinedTextField(

            value = value,
            onValueChange = onChange,
            placeholder = { Text(text = placeholder) },
            keyboardOptions = input,
            textStyle = TextStyle(color = Color.White),
            visualTransformation = visualTransform,
            modifier = modifier.fillMaxWidth()
        )
    }

    @Composable
    fun PostItem(post: Post, onImageClick: (String) -> Unit) {
        val compScope = rememberCoroutineScope()
        var user by remember { mutableStateOf<User?>(null) }
        var localPost by remember { mutableStateOf(post) }
        var liked by remember { mutableStateOf<Boolean?>(null) }
        var isUpdating by remember { mutableStateOf(false) }
        var localTime by remember { mutableStateOf<LocalDateTime?>(null) }
        val context = LocalContext.current

        val userId = getUserIdFromSharedPreferences(context)

        LaunchedEffect(userId) {
            user = getUserById(context, post.userId)
            liked = isPostLikedByUser(context, userId ?: "", post.id ?: "")
            if (user != null) {
                localTime = translateTimeToKotlin(post.createdAt.toString())
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface, // фоновый цвет под постом
                    shape = RoundedCornerShape(12.dp),
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                UserItem(user = user)
                if (localPost.text != "") {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = localPost.text ?: "",
                        color = MaterialTheme.colorScheme.onSurface // текст поста будет светлым
                    )
                }
                if (localPost.imageUrl != null || localPost.imageUrl != "") {
                    Spacer(Modifier.height(12.dp))
                    localPost.imageUrl?.let { imageUrl ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Post Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 300.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onImageClick(imageUrl)
                                }
                        )
                    }
                } else {
                    Placeholder(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (liked != null) {
                        MyIconButton(
                            modik = Modifier,
                            text = "${localPost.reactionCount}",
                            background = if (liked == true) Color.Red else Color.Gray.copy(alpha = 0.7f),
                            defaultIcon = Icons.Default.Favorite
                        ) {


                            compScope.launch {
                                if (liked == false) {
                                    likePost(context, localPost, userId ?: "")
                                    localPost = localPost.copy(reactionCount = localPost.reactionCount + 1)
                                } else {
                                    unlikePost(context, localPost, userId ?: "")
                                    localPost = localPost.copy(reactionCount = localPost.reactionCount - 1)
                                }
                                liked = liked == false
                                isUpdating = true
                            }
                        }
                    } else {
                        Placeholder(
                            modifier = Modifier.width(55.dp).height(40.dp)
                        )
                    }
                    Spacer(Modifier.width(15.dp))
                    if (user != null && localTime != null) {
                        Text(
                            text = "${localTime?.dayOfMonth}.${localTime?.monthValue}.${localTime?.year} в ${localTime?.hour}:${localTime?.minute}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    } else {
                        Placeholder(
                            modifier = Modifier
                                .width(100.dp)
                                .height(14.dp)
                        )
                    }
                }
            }
        }
    }


    @Composable
    fun UserItem(
        user: User?,
        avatarModifier: Modifier = Modifier,
        haveBackground: Boolean = false,
        onItemClick: () -> Unit = {},
        onAvatarClick: () -> Unit = {}
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = if (haveBackground) 15.dp else 0.dp, vertical = if (haveBackground) 8.dp else 0.dp)
                .background(
                    color = if (haveBackground) Color.White else Color.Transparent,
                    shape = if (haveBackground) RoundedCornerShape(8.dp) else RectangleShape
                )
                .clickable { onItemClick },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(if (haveBackground) 5.dp else 0.dp)
            ) {
                if (user != null) {
                    if (user.avatarUrl == null || user.avatarUrl == "") {
                        Box(
                            modifier = avatarModifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.Gray, CircleShape)
                                .clickable { onAvatarClick }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(user.avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "User Avatar",
                            modifier = avatarModifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.Gray, CircleShape)
                                .clickable { onAvatarClick }
                        )
                    }
                } else {
                    Placeholder(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.Gray, CircleShape)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(Modifier.fillMaxHeight()) {
                        if (user != null) {
                            Text(
                                text = user.name,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 22.sp
                            )
                            Spacer(Modifier.width(5.dp))

                        } else {
                            Placeholder(
                                modifier = Modifier
                                    .width(150.dp)
                                    .height(16.dp)
                            )
                        }
                    }


                }
            }
        }
    }

    @Composable
    fun FullscreenImageViewer(imageUrl: String, onClose: () -> Unit) {
        val context = LocalContext.current
        val scaleState = remember { mutableStateOf(1f) } // Масштаб
        val offsetX = remember { mutableStateOf(0f) }    // Смещение по X
        val offsetY = remember { mutableStateOf(0f) }    // Смещение по Y

        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scaleState.value =
                            (scaleState.value * zoom).coerceIn(1f, 4f) // Ограничение масштаба
                        if (scaleState.value > 1f) { // Панорамирование только при увеличении
                            offsetX.value += pan.x
                            offsetY.value += pan.y
                        } else {
                            offsetX.value = 0f
                            offsetY.value = 0f
                        }
                    }
                }
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(Modifier.height(30.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Закрыть",
                        color = Color.White,
                        modifier = Modifier.clickable { onClose() }
                    )
                    Text(
                        text = "Скачать",
                        color = Color.White,
                        modifier = Modifier.clickable {
                            downloadImage(context, imageUrl)
                        }
                    )
                }
                Spacer(Modifier.height(16.dp))

                // Масштабируемое и перемещаемое изображение
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scaleState.value = (scaleState.value * zoom).coerceIn(1f, 4f)
                                if (scaleState.value > 1f) {
                                    offsetX.value += pan.x
                                    offsetY.value += pan.y
                                } else {
                                    offsetX.value = 0f
                                    offsetY.value = 0f
                                }
                            }
                        }
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Full Screen Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scaleState.value,
                                scaleY = scaleState.value,
                                translationX = offsetX.value,
                                translationY = offsetY.value
                            ),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }

    @Composable
    fun Placeholder(
        modifier: Modifier = Modifier,
        shape: Shape = RoundedCornerShape(2.dp),
        color: Color = Color.LightGray.copy(alpha = 0.5f),
        animationDuration: Int = 1500
    ) {
        val animation = rememberInfiniteTransition(label = "LoadingAnimation")
        val waveOffset by animation.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(animationDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "WaveOffset"
        )

        Box(
            modifier = modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        color,
                        color.copy(alpha = 0.2f),
                        color
                    ),
                    start = Offset(waveOffset * 300f, 0f), // Условное смещение
                    end = Offset((waveOffset * 300f) + 300f, 0f)
                ),
                shape = shape
            )
        )
    }
}