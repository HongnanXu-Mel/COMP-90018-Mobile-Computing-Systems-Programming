package com.example.food

import android.content.Context
import android.net.Uri
import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class SupabaseStorageService(private val context: Context) {
    
    init {
        Config.initialize(context)
    }
    
    private val supabase = createSupabaseClient(
        supabaseUrl = Config.getSupabaseUrl(),
        supabaseKey = Config.getSupabaseKey()
    ) {
        install(Storage)
        install(Auth)
    }
    
    
    suspend fun uploadProfilePicture(uid: String, imageUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            Log.d("SupabaseStorage", "Starting upload for user")
            
            val inputStream: InputStream = context.contentResolver.openInputStream(imageUri) ?: run {
                Log.e("SupabaseStorage", "Failed to open input stream")
                return@withContext null
            }
            
            val bytes = inputStream.readBytes()
            inputStream.close()
            Log.d("SupabaseStorage", "Read ${bytes.size} bytes from image")
            
            val path = "pfp/$uid.jpg"
            Log.d("SupabaseStorage", "Uploading to storage")

            supabase.storage.from("palate").upload(path, bytes, upsert = true)
            Log.d("SupabaseStorage", "Upload successful, creating signed URL")

            val signedUrlPath = supabase.storage.from("palate").createSignedUrl(path, kotlin.time.Duration.parse("P365D"))
            val fullSignedUrl = "${Config.getSupabaseUrl()}/storage/v1/$signedUrlPath"
            Log.d("SupabaseStorage", "Signed URL created successfully")

            fullSignedUrl
        } catch (e: Exception) {
            Log.e("SupabaseStorage", "Error uploading profile picture", e)
            e.printStackTrace()

            Log.e("SupabaseStorage", "Upload failed, returning null")
            null
        }
    }
    
    suspend fun deleteProfilePicture(uid: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val path = "pfp/$uid.jpg"
            supabase.storage.from("palate").delete(path)
            true
        } catch (e: Exception) {
            Log.e("SupabaseStorage", "Error deleting profile picture", e)
            false
        }
    }
}
