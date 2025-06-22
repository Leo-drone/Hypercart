package com.hypercart.utils

object Validators {
    // Regex plus stricte : exige un point et 2 à 6 lettres pour le TLD
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")

    fun isValidEmail(email: String): Boolean {
        return try {
            val patternsClass = Class.forName("android.util.Patterns")
            val emailAddressField = patternsClass.getField("EMAIL_ADDRESS")
            val emailPattern = emailAddressField.get(null) as java.util.regex.Pattern
            emailPattern.matcher(email).matches()
        } catch (e: Exception) {
            EMAIL_REGEX.matches(email)
        }
    }
    
    fun isValidPassword(password: String): ValidationResult {
        return when {
            password.length < 8 -> ValidationResult.Error("Le mot de passe doit contenir au moins 8 caractères")
            !password.any { it.isLowerCase() } -> ValidationResult.Error("Le mot de passe doit contenir au moins une minuscule")
            !password.any { it.isUpperCase() } -> ValidationResult.Error("Le mot de passe doit contenir au moins une majuscule")
            !password.any { it.isDigit() } -> ValidationResult.Error("Le mot de passe doit contenir au moins un chiffre")
            else -> ValidationResult.Success
        }
    }
    
    fun isValidPasswordConfirmation(password: String, confirmation: String): ValidationResult {
        return if (password == confirmation) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("Les mots de passe ne correspondent pas")
        }
    }
    
    fun isValidToken(token: String): Boolean {
        return token.isNotBlank() && token.length >= 10
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
} 