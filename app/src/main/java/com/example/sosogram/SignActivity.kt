package com.example.sosogram

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.example.sosogram.ui.theme.SosogramTheme

class SignActivity : ComponentActivity() {

    val baseUI = BaseUI()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userExist = getUserIdFromSharedPreferences(this@SignActivity) != null
        println("USER EXIST: $userExist == ${getUserIdFromSharedPreferences(this@SignActivity)}")

        if (userExist) {
            startActivity(Intent(this@SignActivity, MainActivity::class.java))
            finish()
        } else {
            enableEdgeToEdge()
            setContent {
                SosogramTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Sign()
                    }
                }
            }
        }
    }

    @Composable
    fun Sign() {
        val compScope = rememberCoroutineScope()
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLogin by remember { mutableStateOf(false) }
        val context = LocalContext.current

        LaunchedEffect(isLogin == true) {
            if (isLogin) {
                startActivity(Intent(this@SignActivity, MainActivity::class.java))
                finish()
            }
        }
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(color = Color.DarkGray, shape = RoundedCornerShape(12.dp)),
            )
        }
        Column(Modifier
            .fillMaxSize()
            .padding(start = 40.dp, end = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            baseUI.MyTextField(
                value = email,
                placeholder = "Почта",
                visualTransform = VisualTransformation.None,
                input = KeyboardOptions(keyboardType = KeyboardType.Email)

            ) { email = it }
            Spacer(Modifier.height(10.dp))
            baseUI.MyTextField(
                value = password,
                placeholder = "Пароль",
                visualTransform = PasswordVisualTransformation(),
                input = KeyboardOptions(keyboardType = KeyboardType.Password)
            ) { password = it }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth()) {
                baseUI.MyButton(
                    text = "Вход",

                    color = Color(0xFF32CD32).copy(alpha = 0.5f), // Полупрозрачный лайм

                            modik = Modifier.fillMaxWidth(0.5f).height(40.dp)
                ) {
                    compScope.launch {
                        if (email.isNotEmpty() && password.isNotEmpty()) {

                            val loggedInUserId = loginUser(context, email, password)
                            if (loggedInUserId != null) {
                                saveUserIdToSharedPreferences(this@SignActivity, loggedInUserId)
                                println("Успешный вход. ID пользователя: $loggedInUserId")
                                Toast.makeText(this@SignActivity, "Добро пожаловать!", Toast.LENGTH_SHORT).show()
                                isLogin = true
                            } else {
                                println("Ошибка входа: неверный логин или пароль")
                                Toast.makeText(this@SignActivity, "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
                            }

                        } else {
                            Toast.makeText(this@SignActivity, "Email или пароль не введены!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                Spacer(Modifier.width(5.dp))
                baseUI.MyButton(
                    text = "Регистрация",
                    color = Color.Gray,
                    modik = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    val name = "user_${Random.nextInt(100000, 999999)}"
                    compScope.launch {
                        if (email.isNotEmpty() && password.isNotEmpty()) {

                            if (!isValidEmail(email)) {
                                Toast.makeText(this@SignActivity, "Некорректный Email!", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            if (!isValidPassword(password)) {
                                Toast.makeText(this@SignActivity, "Некорректный пароль!", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            if (isUserExists(context, email) == false) {

                                val newUser: User = User(
                                    name = name,
                                    email = email,
                                    password = password,

                                )

                                val userId = addUser(context, newUser)
                                if (userId != null) {
                                    saveUserIdToSharedPreferences(this@SignActivity, userId)
                                    println("Пользователь добавлен с ID: $userId")
                                    Toast.makeText(this@SignActivity, "Велком ту раша!", Toast.LENGTH_SHORT).show()
                                    isLogin = true
                                } else {
                                    println("Ошибка при добавлении пользователя")
                                    Toast.makeText(this@SignActivity, "Ошибка", Toast.LENGTH_SHORT).show()
                                }

                            } else {
                                Toast.makeText(this@SignActivity, "Такой Email уже зарегистрирован!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@SignActivity, "Email или пароль не введены!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}
