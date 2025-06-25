package com.hypercart.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypercart.AuthResponse
import com.hypercart.data.AuthRepository
import com.hypercart.utils.Validators
import com.hypercart.utils.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }
    
    fun signUpWithEmail() {
        val currentState = _uiState.value
        
        // Validation
        when {
            currentState.email.isBlank() -> {
                _uiState.value = currentState.copy(errorMessage = "Email requis")
                return
            }
            !Validators.isValidEmail(currentState.email) -> {
                _uiState.value = currentState.copy(errorMessage = "Email invalide")
                return
            }
            currentState.password.isBlank() -> {
                _uiState.value = currentState.copy(errorMessage = "Mot de passe requis")
                return
            }
        }
        
        // Password validation
        when (val passwordValidation = Validators.isValidPassword(currentState.password)) {
            is ValidationResult.Error -> {
                _uiState.value = currentState.copy(errorMessage = passwordValidation.message)
                return
            }
            is ValidationResult.Success -> {
                // Continue with registration
            }
        }
        
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            try {
                authRepository.signUpWithEmail(currentState.email, currentState.password)
                    .collect { result ->
                        when (result) {
                            is AuthResponse.Success -> {
                                _uiState.value = currentState.copy(
                                    isLoading = false,
                                    isSuccess = true,
                                    email = "",
                                    password = ""
                                )
                            }
                            is AuthResponse.Error -> {
                                _uiState.value = currentState.copy(
                                    isLoading = false,
                                    errorMessage = result.message ?: "Erreur inconnue",
                                    password = ""
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Une erreur est survenue, veuillez réessayer.",
                    password = ""
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }
} 