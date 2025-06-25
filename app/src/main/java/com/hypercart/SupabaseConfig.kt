package com.hypercart

import com.hypercart.config.SecureConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseConfig {
    val SUPABASE_URL: String get() = SecureConfig.SUPABASE_URL
    val SUPABASE_KEY: String get() = SecureConfig.SUPABASE_KEY
    val GOOGLE_CLIENT_ID: String get() = SecureConfig.GOOGLE_CLIENT_ID
    val RESET_PASSWORD_REDIRECT_URL: String get() = SecureConfig.RESET_PASSWORD_REDIRECT_URL
    
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }
}
