package com.example.food

import android.content.Context
import java.util.Properties

object Config {
    private var properties: Properties? = null
    
    fun initialize(context: Context) {
        if (properties == null) {
            properties = Properties()
            try {
                val inputStream = context.assets.open("config.properties")
                properties?.load(inputStream)
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun getSupabaseUrl(): String {
        return properties?.getProperty("SUPABASE_URL") ?: ""
    }
    
    fun getSupabaseKey(): String {
        return properties?.getProperty("SUPABASE_KEY") ?: ""
    }
}
