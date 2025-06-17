package com.hypercart

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.io.IOException
import java.security.MessageDigest
import java.util.UUID

sealed interface AuthResponse {
    data object Success : AuthResponse
    data class Error(val message: String?) : AuthResponse
}

class AuthManager(private val context: Context) {

    private val supabase = createSupabaseClient(
        supabaseUrl = "https://egrrgwnbamdezxkhxmrd.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVncnJnd25iYW1kZXp4a2h4bXJkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg3MjU2NzAsImV4cCI6MjA2NDMwMTY3MH0.Btffag13ZrSH6XD1C246HmjqbKcb37xg9BbxUfzd9aA"
    ) {
        install(Auth)
    }

    fun mapSupabaseAuthError(errorCode: String?): String {
        return when (errorCode) {
            "invalid_credentials" -> "Adresse e-mail ou mot de passe incorrect."
            "email_exists", "user_already_exists" -> "Un compte existe déjà avec cette adresse e-mail."
            "email_address_invalid" -> "L'adresse e-mail est invalide."
            "email_not_confirmed" -> "Veuillez confirmer votre adresse e-mail avant de vous connecter."
            "weak_password" -> "Le mot de passe est trop faible. Il doit contenir au moins 8 caractères, une majuscule, une minuscule et un chiffre."
            "signup_disabled", "email_provider_disabled" -> "Les inscriptions par e-mail sont désactivées. Veuillez contacter le support."
            "user_banned" -> "Ce compte est temporairement suspendu."
            "over_request_rate_limit", "too_many_requests", "429" -> "Trop de tentatives. Veuillez réessayer dans quelques instants."
            "server_error", "unexpected_failure", "500" -> "Le serveur rencontre un problème. Merci de réessayer plus tard."
            "user_not_found" -> "Utilisateur introuvable."
            "no_authorization" -> "Vous n'êtes pas autorisé à effectuer cette action."
            "403" -> "Action non autorisée."
            "422" -> "Requête invalide. Veuillez réessayer."
            else -> "Une erreur est survenue. Veuillez réessayer."
        }
    }

    fun resolveAuthError(e: Exception): String {
        return when (e) {
            is io.github.jan.supabase.auth.exception.AuthRestException -> {
                mapSupabaseAuthError(e.error)
            }

            is io.github.jan.supabase.exceptions.HttpRequestException -> {
                when (e.cause) {
                    is java.net.UnknownHostException -> {
                        "Connexion impossible. Vérifiez votre connexion internet."
                    }
                    else -> {
                        "Erreur réseau. Veuillez réessayer."
                    }
                }
            }

            else -> {
                "Une erreur est survenue. Veuillez réessayer."
            }
        }
    }

    fun signUpWithEmail(emailValue: String, passwordValue: String): Flow<AuthResponse> = flow {
        // Validation locale
        if (passwordValue.length < 8) {
            emit(AuthResponse.Error("Le mot de passe doit contenir au moins 8 caractères."))
            return@flow
        }
        if (!passwordValue.any { it.isLowerCase() } ||
            !passwordValue.any { it.isUpperCase() } ||
            !passwordValue.any { it.isDigit() }) {
            emit(AuthResponse.Error("Le mot de passe doit contenir une majuscule, une minuscule et un chiffre."))
            return@flow
        }

        try {
            supabase.auth.signUpWith(Email) {
                email = emailValue
                password = passwordValue
            }
            emit(AuthResponse.Success)
        } catch (e: Exception) {
            Log.e("AuthManager", "Erreur SignUp", e)
            emit(AuthResponse.Error(resolveAuthError(e)))
        }
    }

    fun signInWithEmail(emailValue: String, passwordValue: String): Flow<AuthResponse> = flow {
        try {
            supabase.auth.signInWith(Email) {
                email = emailValue
                password = passwordValue
            }
            emit(AuthResponse.Success)
        } catch (e: Exception) {
            Log.e("AuthManager", "Erreur SignIn", e)
            emit(AuthResponse.Error(resolveAuthError(e)))
        }
    }



    private fun createNonce(rawNonce: String): String {
        val bytes = rawNonce.toByteArray()
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun loginGoogleUser(): Flow<AuthResponse> = flow {
        val rawNonce = UUID.randomUUID().toString()
        val hashedNonce = createNonce(rawNonce)

        Log.i("AuthManager", "Raw Nonce: $rawNonce")
        Log.i("AuthManager", "Hashed Nonce: $hashedNonce")

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId("80305142848-j5j09u3v3lkn4c2q6jaimk4rv761t5ra.apps.googleusercontent.com")
            .setNonce(hashedNonce)
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(context)

        try {
            val result = credentialManager.getCredential(context, request)
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val googleIdToken = googleIdTokenCredential.idToken

            Log.i("AuthManager", "Google ID Token: $googleIdToken")

            supabase.auth.signInWith(IDToken) {
                idToken = googleIdToken
                provider = Google
                nonce = rawNonce
            }

            Log.i("AuthManager", "SignIn with Google ID Token successful")
            emit(AuthResponse.Success)

        } catch (e: NoCredentialException) {
            Log.e("AuthManager", "Aucun compte Google disponible", e)
            emit(AuthResponse.Error("Aucun compte Google n'est configuré. Veuillez en ajouter un dans vos paramètres."))
        } catch (e: IllegalArgumentException) {
            Log.e("AuthManager", "Jeton Google invalide", e)
            emit(AuthResponse.Error("Impossible de vous connecter avec Google. Veuillez réessayer."))
        } catch (e: SecurityException) {
            Log.e("AuthManager", "Erreur de sécurité lors de l'authentification", e)
            emit(AuthResponse.Error("Un problème de sécurité est survenu. Merci de réessayer."))
        } catch (e: IOException) {
            Log.e("AuthManager", "Problème de réseau", e)
            emit(AuthResponse.Error("Connexion impossible. Vérifiez votre connexion internet."))
        } catch (e: Exception) {
            Log.e("AuthManager", "Google Sign In Exception", e)
            emit(AuthResponse.Error("Une erreur est survenue lors de la connexion. Veuillez réessayer."))
        }

    }
}

suspend fun resetPasswordWithSupabase( email: String) {
    val supabase = createSupabaseClient(
        supabaseUrl = "https://egrrgwnbamdezxkhxmrd.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVncnJnd25iYW1kZXp4a2h4bXJkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg3MjU2NzAsImV4cCI6MjA2NDMwMTY3MH0.Btffag13ZrSH6XD1C246HmjqbKcb37xg9BbxUfzd9aA"
    ) {
        install(Auth)
    }

    supabase.auth.resetPasswordForEmail(
        email = email,
        redirectUrl = "https://hypercart.com/reset-password"
    )
    Log.i("AuthManager", "Reset Password envoyé pour $email")
}


suspend fun updatePasswordWithSupabase(newPassword: String, accessToken:String): String? {
    return try {
        val supabase = createSupabaseClient(
            supabaseUrl = "https://egrrgwnbamdezxkhxmrd.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVncnJnd25iYW1kZXp4a2h4bXJkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg3MjU2NzAsImV4cCI6MjA2NDMwMTY3MH0.Btffag13ZrSH6XD1C246HmjqbKcb37xg9BbxUfzd9aA"
        ) {
            install(Auth)
        }

        supabase.auth.retrieveUser(accessToken)

        supabase.auth.updateUser {
            password = newPassword
        }
        return null
    } catch (e: Exception) {
        e.localizedMessage ?: "Erreur inconnue"
    }
}