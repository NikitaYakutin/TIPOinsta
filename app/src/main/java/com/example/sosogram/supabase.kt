package com.example.sosogram

import android.app.DownloadManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

const val URL = ""
private const val KEY = ""

class AppLifecycleObserver(
    private val onEnterForeground: () -> Unit,
    private val onEnterBackground: () -> Unit
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        onEnterForeground()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        onEnterBackground()
    }
}

val supabase = createSupabaseClient(
    supabaseUrl = URL,
    supabaseKey = KEY
) {
    install(Postgrest)
    install(Storage)
}

suspend fun waitForInternetConnection(context: Context, maxRetries: Int = 8, retryDelay: Long = 2000L): Boolean {
    var attempt = 0
    while (attempt < maxRetries) {
        if (isInternetAvailable(context)) {
            return true
        }
        attempt++
        delay(retryDelay)
    }
    withContext(Dispatchers.Main) {
        Toast.makeText(context, "Нет подключения к интернету. Проверьте соединение.", Toast.LENGTH_LONG).show()
    }
    return false
}

suspend fun <T> safeApiCall(context: Context, apiCall: suspend () -> T?): T? {
    return try {
        apiCall()
    } catch (e: Exception) {
        println("Ошибка API запроса: ${e.message}")
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Ошибка соединения с сервером. Попробуйте позже.", Toast.LENGTH_LONG).show()
        }
        null
    }
}

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}



fun translateTimeToKotlin(supaTime: String): LocalDateTime {
    val formatter = DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
        .optionalStart() // Начало необязательной части
        .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true) // Обработка дробной части секунд
        .optionalEnd() // Конец необязательной части
        .toFormatter()

    return LocalDateTime.parse(supaTime, formatter)
}

fun downloadImage(context: Context, imageUrl: String) {
    val request = DownloadManager.Request(Uri.parse(imageUrl))
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "downloaded_image.jpg")

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    downloadManager.enqueue(request)
}

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun isValidPassword(password: String): Boolean {
    return password.length in 3..15 && password.any(Char::isDigit)
}


