package com.example.sosogram

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ProcessLifecycleOwner
import coil.compose.AsyncImage
import com.example.sosogram.ui.theme.SosogramTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    val baseUI = BaseUI()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val localContext = this@MainActivity
        val userId = getUserIdFromSharedPreferences(this@MainActivity).toString()



       // enableEdgeToEdge()
        setContent {
            SosogramTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var state by remember { mutableStateOf<PanelState>(PanelState.PROFILE) }

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when (state) {
                            PanelState.PROFILE -> {
                                MainScreen(
                                    modik = Modifier
                                        .fillMaxSize()
                                        .weight(1f)
                                )
                            }

                            PanelState.MAIN -> {
                                PostsFeed(
                                    modik = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            }

                        }

                        Box(
                            Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.background,
                                    shape = RoundedCornerShape(0.dp)
                                )
                                .fillMaxWidth()

                                .height(45.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.profile),
                                    tint = if (state == PanelState.PROFILE) MaterialTheme.colorScheme.primary else Color.Black,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clickable {
                                            state = PanelState.PROFILE
                                        }
                                )
                                Icon(
                                    painter = painterResource(R.drawable.feed),
                                    tint = if (state == PanelState.MAIN) MaterialTheme.colorScheme.primary else Color.Black,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clickable {
                                            state = PanelState.MAIN
                                        }
                                )

                            }
                        }
                    }
                }
            }
        }
    }

    sealed class Screen {
        object Profile : Screen()
        object MyPosts : Screen()
        object LikedPosts : Screen()
    }

    enum class PostFilter(val displayName: String) {
        NEWEST("Все"),

    }

    enum class PanelState() {
        PROFILE,
        MAIN,

    }

    fun sortPosts(posts: List<Post>, filter: PostFilter): List<Post> {
        return when (filter) {
            PostFilter.NEWEST -> posts.sortedByDescending { it.createdAt }
        }
    }

    @Composable
    fun MainScreen(modik: Modifier) {
        val context = LocalContext.current
        val userId = getUserIdFromSharedPreferences(context)
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Profile) }

        Column(
            modifier = modik,
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            // Хедер с кнопками
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 35.dp)
                ) {
                    // Кнопка "Профиль"
                    Button(
                        onClick = { currentScreen = Screen.Profile },
                        modifier = Modifier,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        )
                    ) {
                        Text("Профиль", textAlign = TextAlign.Center, color = Color.White)
                    }





                    // Кнопка "Понравилось"
                    Button(
                        onClick = { currentScreen = Screen.LikedPosts },
                        modifier = Modifier,
                                colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF6F61) // Приятный любовный красный
                                )
                    ) {
                        Text("Понравившиеся", textAlign = TextAlign.Center, color = Color.White)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Контент экрана
            when (currentScreen) {
                Screen.Profile -> UserProfileScreen(
                    userId = userId.toString(),
                    modik = Modifier.fillMaxSize()
                )

                Screen.MyPosts -> UserPostsScreen(
                    userId = userId.toString(),
                    modik = Modifier.fillMaxSize()
                )

                Screen.LikedPosts -> LikedPostsScreen(
                    userId = userId.toString(),
                    modik = Modifier.fillMaxSize()
                )
            }
        }
    }

    @Composable
    fun UserProfileScreen(userId: String, modik: Modifier) {
        val coroutineScope = rememberCoroutineScope()
        var user by remember { mutableStateOf<User?>(null) }
        var isEditing by remember { mutableStateOf(false) }
        var nameInput by remember { mutableStateOf("") }
        var emailInput by remember { mutableStateOf("") }
        var passwordInput by remember { mutableStateOf("") }
        var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }
        val context = LocalContext.current

        // Загружаем данные пользователя при старте
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                user = getUserById(context, userId)
                user?.let {
                    nameInput = it.name
                    emailInput = it.email
                }
            }
        }

        // Обработчик выбора изображения
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri: Uri? ->
                selectedAvatarUri = uri
            }
        )

        Column(
            modifier = modik
                .padding(16.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Аватар пользователя с возможностью изменения
            if (user?.avatarUrl != null && user?.avatarUrl != "") {
                AsyncImage(
                    model = user?.avatarUrl,
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                        .clickable {
                            launcher.launch("image/*") // Запускаем выбор изображения
                        }
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            launcher.launch("image/*") // Запускаем выбор изображения
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.profile), // Замените на свой ресурс иконки
                        contentDescription = "Profile Icon",
                        modifier = Modifier.fillMaxSize(),  // Размер иконки
                        tint = Color.LightGray // Белый цвет для иконки
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp)) // Немного отступа между аватаром и именем

            // Имя пользователя под аватаром
            Text(
                text = nameInput,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White, // Белый текст

            )

        // Отступ перед кнопкой "Выйти"

            // Кнопка "Выйти" сбоку от имени пользователя
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End // Располагаем кнопку справа от имени
            ) {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            finish()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.6f)) // Тусклый красный для кнопки выхода
                ) {
                    Text(text = "Выйти", color = Color.White) // Белый текст
                }
            }



        // Кнопка "Редактировать"
            Button(
                onClick = { isEditing = !isEditing },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B0000) // Тусклый зеленый (PaleGreen) // Серые кнопки
            ) ){
                Text(text = if (isEditing) "Отмена" else "Редактировать", color = Color.White) // Белый текст
            }

            // Режим редактирования
            if (isEditing) {
                Spacer(modifier = Modifier.height(16.dp))

                // Поля для редактирования
                baseUI.MyTextField(
                    value = nameInput,
                    placeholder = "Имя",
                    modifier = Modifier.fillMaxWidth(),

                ) { nameInput = it }

                Spacer(modifier = Modifier.height(16.dp))

                baseUI.MyTextField(
                    value = emailInput,
                    placeholder = "Email",
                    modifier = Modifier.fillMaxWidth(),
                    visualTransform = VisualTransformation.None,
                    input = KeyboardOptions(keyboardType = KeyboardType.Email),

                ) { emailInput = it }

                Spacer(modifier = Modifier.height(16.dp))


                baseUI.MyTextField(
                    value = passwordInput,
                    placeholder = "Пароль",
                    modifier = Modifier.fillMaxWidth(),
                    visualTransform = PasswordVisualTransformation(),
                    input = KeyboardOptions(keyboardType = KeyboardType.Password),


                ) {
                    passwordInput = it }

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопка "Подтвердить"
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (selectedAvatarUri != null) {
                                val newAvatarUrl =
                                    uploadAvatar(context, selectedAvatarUri!!, userId)
                                if (newAvatarUrl != null) {
                                    user = user?.copy(avatarUrl = newAvatarUrl)
                                    Toast.makeText(context, "Аватар обновлен", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }

                            if (nameInput.length > 12) {
                                Toast.makeText(
                                    context,
                                    "Имя не должно превышать 12 символов",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@launch
                            }

                            if (!isValidEmail(emailInput)) {
                                Toast.makeText(context, "Некорректный email", Toast.LENGTH_SHORT)
                                    .show()
                                return@launch
                            }

                            if (passwordInput.length > 0) {
                                if (!isValidPassword(passwordInput)) {
                                    Toast.makeText(
                                        context,
                                        "Некорректный пароль",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@launch
                                }
                            }

                            val isUpdated = updateUserInfo(
                                context,
                                user = user ?: return@launch,
                                name = nameInput,
                                email = emailInput,
                                password = passwordInput.takeIf { it.isNotBlank() }
                            )
                            if (isUpdated == true) {
                                Toast.makeText(context, "Данные обновлены", Toast.LENGTH_SHORT)
                                    .show()
                                isEditing = false
                            } else {
                                Toast.makeText(context, "Ошибка обновления", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF006400) // Тусклый зеленый (PaleGreen)
                )) {
                    Text(text = "Подтвердить", color = Color.White) // Белый текст
                }
            }
        }
    }

    @Composable
    fun UserPostsScreen(userId: String, modik: Modifier) {
        val coroutineScope = rememberCoroutineScope()
        var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            coroutineScope.launch {
                posts = getUserPosts(context, userId) ?: emptyList()
            }
        }

        if (posts.isNotEmpty()) {
            LazyColumn(modifier = modik.padding(horizontal = 15.dp)) {
                items(posts.sortedByDescending { it.createdAt }) { post ->
                    PostItemWithDelete(post = post) {
                        coroutineScope.launch {
                            deletePost(context, post.id ?: "")
                            posts = posts.filter { it.id != post.id }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = modik,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Ничего нет", color = Color.Gray)
            }
        }
    }

    @Composable
    fun PostItemWithDelete(post: Post, onDelete: () -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(12.dp)
                ),
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                Text(post.text.toString(), color = Color.White) // Белый текст
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (post.imageUrl != null) {
                        AsyncImage(
                            model = post.imageUrl,
                            contentDescription = "Post image",
                            modifier = Modifier.height(200.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                    Text("Удалить", color = Color.White) // Белый текст
                }
            }
        }
    }

    @Composable
    fun LikedPostsScreen(userId: String, modik: Modifier) {
        val coroutineScope = rememberCoroutineScope()
        var likedPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
        var fullscreenImageUrl by remember { mutableStateOf<String?>(null) }
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            coroutineScope.launch {
                likedPosts = getLikedPosts(context, userId) ?: emptyList()
            }
        }
        fullscreenImageUrl?.let { imageUrl ->
            baseUI.FullscreenImageViewer(
                imageUrl = imageUrl,
                onClose = { fullscreenImageUrl = null }
            )
        }

        if (likedPosts.isNotEmpty()) {
            LazyColumn(modifier = modik) {
                items(likedPosts) { post ->
                    baseUI.PostItem(post = post) { imageUrl ->
                        fullscreenImageUrl = imageUrl
                    }
                }
            }
        } else {

            Column(
                modifier = modik,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Ничего нет", color = Color.Gray)
            }
        }
    }

    @Composable
    fun PostsFeed(modik: Modifier) {
        val compScope = rememberCoroutineScope()
        var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
        var sortedPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
        var selectedFilter by remember { mutableStateOf(PostFilter.NEWEST) }
        val listState = rememberLazyListState()
        val key = remember { mutableStateOf(System.currentTimeMillis()) }
        val refreshDebounce = remember { mutableStateOf(false) }
        val context = LocalContext.current

        val isAtTop =
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0

        val refreshPosts = {
            compScope.launch {
                posts = getAllPosts(context) ?: emptyList()
                sortedPosts = sortPosts(posts, selectedFilter)
                key.value = System.currentTimeMillis() // Обновляем ключ
            }
        }

        LaunchedEffect(isAtTop) {
            if (isAtTop && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                refreshDebounce.value = true
                delay(250)
                if (isAtTop && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                    refreshPosts()
                }
                refreshDebounce.value = false
            }
        }

        LaunchedEffect(selectedFilter) {
            sortedPosts = sortPosts(posts, selectedFilter)
            key.value = System.currentTimeMillis() // Обновляем ключ для LazyColumn
        }

        Box(modifier = modik.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(color = Color.Black),  // Черный фон
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(35.dp))
                    Text(
                        text = "Лента",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White  // Белый текст
                    )
                    Spacer(Modifier.height(10.dp))

                    // LazyRow для выбора фильтров


                }

                // LazyColumn для отображения постов
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(
                        items = sortedPosts,
                        key = { post -> post.id + key.value } // Обновляем список при изменении ключа
                    ) { post ->
                        baseUI.PostItem(
                            post,
                            onImageClick = { imageUrl ->
                                println("SENDING IMAGE URL: $imageUrl")
                                val context = this@MainActivity
                                val intent = Intent(context, ImageViewerActivity::class.java)
                                intent.putExtra("imageUrl", imageUrl)
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }

            // FloatingActionButton для добавления поста
            FloatingActionButton(
                onClick = {
                    startActivity(Intent(this@MainActivity, PostCreationActivity::class.java))
                },
                containerColor = Color.Gray,  // Серый цвет кнопки
                contentColor = Color.White,   // Белый цвет контента
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Создать новый пост")
            }
        }
    }
}