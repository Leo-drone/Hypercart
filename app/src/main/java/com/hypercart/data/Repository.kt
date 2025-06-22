package com.hypercart.data

import com.hypercart.AuthManager
import com.hypercart.AuthResponse
import kotlinx.coroutines.flow.Flow

class AuthRepository(private val authManager: AuthManager) {
    
    fun signUpWithEmail(email: String, password: String): Flow<AuthResponse> {
        return authManager.signUpWithEmail(email, password)
    }
    
    fun signInWithEmail(email: String, password: String): Flow<AuthResponse> {
        return authManager.signInWithEmail(email, password)
    }
    
    fun loginGoogleUser(): Flow<AuthResponse> {
        return authManager.loginGoogleUser()
    }
    
    suspend fun resetPassword(email: String) {
        com.hypercart.resetPasswordWithSupabase(email)
    }
    
    suspend fun updatePassword(newPassword: String, accessToken: String, email: String): String? {
        return com.hypercart.updatePasswordWithSupabase(newPassword, accessToken, email)
    }
} 