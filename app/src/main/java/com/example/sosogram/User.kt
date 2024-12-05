package com.example.sosogram

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Serializable
data class User(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,




    @SerialName("avatar_url") val avatarUrl: String? = null,



    @SerialName("created_at") val createdAt: String? = null,





    @SerialName("liked_posts") val likedPosts: List<String>? = null
)

fun newUserConst(
    user: User,
    id: String? = null,
    name: String? = null,
    email: String? = null,
    password: String? = null,

    avatarUrl: String? = null,

    createdAt: String? = null,

    likedPosts: List<String>? = null
): User {
    return User(
        id = id ?: user.id,
        name = name ?: user.name,
        email = email ?: user.email,
        password = password ?: user.password,

        avatarUrl = avatarUrl ?: user.avatarUrl,

        createdAt = createdAt ?: user.createdAt,
        likedPosts = likedPosts ?: user.likedPosts,

    )
}

fun emptyUser(): User {
    return User(
        id = null,
        name = "",
        email = "",
        password = "",

        avatarUrl = null,

        createdAt = null,

        likedPosts = null
    )
}

fun saveUserIdToSharedPreferences(context: Context, id: String) {
    val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("userId", id).apply()
}

fun getUserIdFromSharedPreferences(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("userId", null)
}







suspend fun updateUserInfo(
    context: Context,
    user: User,
    name: String? = null,

    email: String? = null,
    password: String? = null,
    avatarUrl: String? = null,

): Boolean? {
    if (!waitForInternetConnection(context)) return null

    return safeApiCall(context) {
        val newUser = newUserConst(
            user,
            name = name ?: user.name,

            avatarUrl = avatarUrl ?: user.avatarUrl,
            email = email ?: user.email,
            password = password ?: user.password,

        )

        val response = supabase.from("users").update(newUser) {
            filter { eq("id", newUser.id.toString()) }
            select()
        }.decodeSingleOrNull<User>()
        response != null
    }
}

suspend fun getUserById(context: Context, id: String): User? {
    val hasInternet = waitForInternetConnection(context)
    if (!hasInternet) {
        return null
    }
    return safeApiCall(context) {
        val response = supabase.from("users").select {
            filter { eq("id", id) }
        }.decodeList<User>()

        response[0]
    }
}

suspend fun searchUsersByName(context: Context, name: String): List<User>? {
    if (!waitForInternetConnection(context)) return null

    return safeApiCall(context) {
        supabase.from("users").select {
            filter { ilike("name", "%$name%") }
        }.decodeList<User>()
    }
}

suspend fun searchUsersByTag(context: Context, tag: String): List<User>? {
    if (!waitForInternetConnection(context)) return null

    return safeApiCall(context) {
        supabase.from("users").select {
            filter { ilike("tag", "%$tag%") }
        }.decodeList<User>()
    }
}

suspend fun isUserExists(context: Context, email: String): Boolean? {
    if (!waitForInternetConnection(context)) return null

    return safeApiCall(context) {
        val existingUser = supabase.from("users").select(Columns.raw("email")) {
            filter { eq("email", email) }
        }.decodeSingleOrNull<User>()
        existingUser != null
    }
}

suspend fun addUser(context: Context, user: User): String? {
    if (!waitForInternetConnection(context)) return null

    return safeApiCall(context) {
        val response = supabase.from("users").insert(user) { select() }.decodeSingleOrNull<User>()
        response?.id
    }
}

suspend fun loginUser(context: Context, email: String, password: String): String? {
    if (!waitForInternetConnection(context)) return null

    return safeApiCall(context) {
        val user = supabase.from("users").select {
            filter { eq("email", email) }
        }.decodeList<User>().firstOrNull()

        if (user != null && user.password == password) {
            user.id
        } else null
    }
}





suspend fun uploadAvatar(context: Context, uri: Uri, userId: String): String? {
    if (!waitForInternetConnection(context)) return null

    return safeApiCall(context) {
        val contentResolver = context.contentResolver
        val fileName = "$userId-avatar.jpg"
        val mimeType = contentResolver.getType(uri)
        val inputStream = contentResolver.openInputStream(uri) ?: return@safeApiCall null
        val byteArray = inputStream.readBytes()

        supabase.storage.from("avatars").upload(fileName, byteArray) {
            contentType = mimeType?.let { ContentType.parse(it) } ?: ContentType.Image.JPEG
        }
        supabase.storage.from("avatars").publicUrl(fileName)
    }
}

suspend fun isPostLikedByUser(context: Context, userId: String, postId: String): Boolean? {
    if (!waitForInternetConnection(context)) return null

    return safeApiCall(context) {
        val user = supabase.from("users").select {
            filter { eq("id", userId) }
        }.decodeSingle<User>()

        user.likedPosts?.contains(postId) == true
    }
}
