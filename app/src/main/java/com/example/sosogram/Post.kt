package com.example.sosogram

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random
import kotlin.to

@Serializable
data class Post(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("text") val text: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("reaction_count") val reactionCount: Int,
    @SerialName("created_at") val createdAt: String? = null
)

suspend fun getAllPosts(context: Context): List<Post>? {
    if (!waitForInternetConnection(context)) return null

    return safeApiCall(context) {
        supabase.from("posts").select().decodeList<Post>()
    }
}

suspend fun getUserPosts(context: Context, userId: String): List<Post>? {
    if (!waitForInternetConnection(context)) return null

    return safeApiCall(context) {
        supabase.from("posts").select {
            filter { eq("user_id", userId) }
        }.decodeList<Post>()
    }
}

suspend fun getLikedPosts(context: Context, userId: String): List<Post>? {
    if (!waitForInternetConnection(context)) return null

    return safeApiCall(context) {
        val user = supabase.from("users").select {
            filter { eq("id", userId) }
        }.decodeSingleOrNull<User>()

        val likedPostIds = user?.likedPosts ?: return@safeApiCall emptyList()
        if (likedPostIds.isEmpty()) return@safeApiCall emptyList()

        likedPostIds.mapNotNull { postId ->
            supabase.from("posts").select {
                filter { eq("id", postId) }
            }.decodeSingleOrNull<Post>()
        }
    }
}

suspend fun deletePost(context: Context, postId: String): Boolean? {
    if (!waitForInternetConnection(context)) return null

    return safeApiCall(context) {
        val response = supabase.from("posts").delete {
            filter { eq("id", postId) }
            select()
        }.decodeSingleOrNull<Post>()
        response != null
    }
}



suspend fun uploadPostImage(context: Context, uri: Uri, userId: String): String? {
    if (!waitForInternetConnection(context)) return null

    return safeApiCall(context) {
        val contentResolver = context.contentResolver
        val fileName = "$userId${Random.nextInt(100000, 999999)}-post-image.jpg"
        val mimeType = contentResolver.getType(uri)
        val inputStream = contentResolver.openInputStream(uri) ?: return@safeApiCall null
        val byteArray = inputStream.readBytes()

        supabase.storage.from("post_images").upload(fileName, byteArray) {
            contentType = mimeType?.let { ContentType.parse(it) } ?: ContentType.Image.JPEG
        }
        supabase.storage.from("post_images").publicUrl(fileName)
    }
}

suspend fun addNewPost(context: Context, userId: String, text: String? = "", imageUrl: String? = null): Boolean? {
    if (!waitForInternetConnection(context)) return null

    return safeApiCall(context) {
        val newPost = Post(
            userId = userId,
            text = text,
            imageUrl = imageUrl,
            reactionCount = 0,
        )
        val response = supabase.from("posts").insert(newPost) { select() }.decodeSingleOrNull<Post>()
        response != null
    }
}

suspend fun incrementReactionCount(context: Context, postId: String): Boolean? {
    if (!waitForInternetConnection(context)) return null

    return safeApiCall(context) {
        val post = supabase.from("posts").select {
            filter { eq("id", postId) }
        }.decodeSingleOrNull<Post>() ?: return@safeApiCall false

        supabase.from("posts").update(
            mapOf("reaction_count" to post.reactionCount + 1)
        ) {
            filter { eq("id", postId) }
            select()
        }.decodeSingleOrNull<Post>() != null
    }
}

suspend fun decrementReactionCount(context: Context, postId: String): Boolean? {
    if (!waitForInternetConnection(context)) return null

    return safeApiCall(context) {
        val post = supabase.from("posts").select {
            filter { eq("id", postId) }
        }.decodeSingleOrNull<Post>() ?: return@safeApiCall false

        supabase.from("posts").update(
            mapOf("reaction_count" to post.reactionCount - 1)
        ) {
            filter { eq("id", postId) }
            select()
        }.decodeSingleOrNull<Post>() != null
    }
}

suspend fun likePost(context: Context, thisPost: Post, userId: String): Boolean? {
    if (!waitForInternetConnection(context)) return null

    return safeApiCall(context) {
        val user = supabase.from("users").select {
            filter { eq("id", userId) }
        }.decodeSingleOrNull<User>() ?: return@safeApiCall false

        val updatedLikedPosts = (user.likedPosts ?: emptyList()).toMutableList()

        if (!updatedLikedPosts.contains(thisPost.id.toString())) {
            updatedLikedPosts.add(thisPost.id.toString())
        }

        supabase.from("users").update(mapOf(
            "liked_posts" to updatedLikedPosts
        )) {
            filter { eq("id", userId) }
        }
        incrementReactionCount(context, thisPost.id ?: "")
        true
    }
}

suspend fun unlikePost(context: Context, thisPost: Post, userId: String): Boolean? {
    if (!waitForInternetConnection(context)) return null

    return safeApiCall(context) {
        val user = supabase.from("users").select {
            filter { eq("id", userId) }
        }.decodeSingleOrNull<User>() ?: return@safeApiCall false

        val updatedLikedPosts = (user.likedPosts ?: emptyList()).toMutableList()

        if (updatedLikedPosts.contains(thisPost.id.toString())) {
            updatedLikedPosts.remove(thisPost.id.toString())
        } else {
            return@safeApiCall false
        }

        supabase.from("users").update(mapOf(
            "liked_posts" to updatedLikedPosts
        )) {
            filter { eq("id", userId) }
        }
        decrementReactionCount(context, thisPost.id ?: "")
        true
    }
}
