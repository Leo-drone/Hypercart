package com.hypercart.data

import android.util.Log
import com.hypercart.AuthManager
import com.hypercart.AuthResponse
import com.hypercart.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String? = null
)

@Serializable
data class CreateUserRequest(
    val id: String,
    val name: String? = null
)

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

class Repository {
    private val supabaseClient = SupabaseConfig.client
    
    suspend fun ensureUserExists(): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()
                    ?: return@withContext Result.failure(Exception("Vous devez être connecté"))
                
                Log.i("Repository", "Vérification de l'utilisateur: ${currentUser.id}")
                
                // Vérifier si l'utilisateur existe déjà dans la table users
                val existingUser = supabaseClient
                    .from("users")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("id", currentUser.id)
                        }
                    }
                    .decodeSingleOrNull<User>()
                
                if (existingUser != null) {
                    Log.i("Repository", "Utilisateur trouvé: ${existingUser.id}")
                    return@withContext Result.success(existingUser)
                }
                
                // Si l'utilisateur n'existe pas, le créer
                val newUserData = CreateUserRequest(
                    id = currentUser.id,
                    name = ""
                )
                
                Log.i("Repository", "Création de l'utilisateur: ${newUserData.id}")
                
                val createdUser = supabaseClient
                    .from("users")
                    .insert(newUserData) {
                        select(Columns.ALL)
                    }
                    .decodeSingle<User>()
                
                Log.i("Repository", "Utilisateur créé avec succès: ${createdUser.id}")
                Result.success(createdUser)
                
            } catch (e: Exception) {
                Log.e("Repository", "Erreur lors de la gestion de l'utilisateur: ${e.message}")
                Result.failure(Exception("Erreur de configuration utilisateur. Veuillez réessayer."))
            }
        }
    }
} 