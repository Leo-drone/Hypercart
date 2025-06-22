package com.hypercart.config

import android.content.Context
import java.io.IOException
import java.util.Properties

object SecureConfig {
    private var properties: Properties? = null
    
    fun initialize(context: Context) {
        if (properties == null) {
            properties = Properties()
            try {
                context.assets.open("config.properties").use { inputStream ->
                    properties?.load(inputStream)
                }
            } catch (e: IOException) {
                // Fallback to default values for development
                properties = Properties().apply {
                    setProperty("SUPABASE_URL", "https://egrrgwnbamdezxkhxmrd.supabase.co")
                    setProperty("SUPABASE_KEY", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVncnJnd25iYW1kZXp4a2h4bXJkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg3MjU2NzAsImV4cCI6MjA2NDMwMTY3MH0.Btffag13ZrSH6XD1C246HmjqbKcb37xg9BbxUfzd9aA")
                    setProperty("GOOGLE_CLIENT_ID", "80305142848-j5j09u3v3lkn4c2q6jaimk4rv761t5ra.apps.googleusercontent.com")
                    setProperty("RESET_PASSWORD_REDIRECT_URL", "https://hypercart.com/reset-password")
                }
            }
        }
    }
    
    val SUPABASE_URL: String
        get() = properties?.getProperty("SUPABASE_URL") ?: ""
    
    val SUPABASE_KEY: String
        get() = properties?.getProperty("SUPABASE_KEY") ?: ""
    
    val GOOGLE_CLIENT_ID: String
        get() = properties?.getProperty("GOOGLE_CLIENT_ID") ?: ""
    
    val RESET_PASSWORD_REDIRECT_URL: String
        get() = properties?.getProperty("RESET_PASSWORD_REDIRECT_URL") ?: ""
} 