package com.hypercart.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypercart.AuthManager
import com.hypercart.AuthResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import android.content.Context

class UserSettingsViewModel : ViewModel() {
    private var authManager: AuthManager? = null
    
    private val _userName = MutableStateFlow("Chargement...")
    val userName: StateFlow<String> = _userName.asStateFlow()
    
    private val _userEmail = MutableStateFlow("Chargement...")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    private val _isEditingName = MutableStateFlow(false)
    val isEditingName: StateFlow<Boolean> = _isEditingName.asStateFlow()
    
    private val _tempName = MutableStateFlow("")
    val tempName: StateFlow<String> = _tempName.asStateFlow()
    
    // État pour indiquer si l'utilisateur s'est déconnecté
    private val _isSignedOut = MutableStateFlow(false)
    val isSignedOut: StateFlow<Boolean> = _isSignedOut.asStateFlow()
    
    fun initializeAuthManager(context: Context) {
        authManager = AuthManager(context)
        
        // Essayer un chargement immédiat
        loadUserInfoImmediately()
        
        // Programmer des tentatives supplémentaires avec délais croissants
        viewModelScope.launch {
            repeat(3) { attempt ->
                val delay = (attempt + 1) * 500L // 500ms, 1s, 1.5s
                kotlinx.coroutines.delay(delay)
                
                android.util.Log.i("UserSettingsViewModel", "Tentative de rechargement #${attempt + 1} après ${delay}ms")
                
                // Vérifier si on a déjà les infos
                if (_userEmail.value.isNotEmpty() && _userEmail.value != "Email non disponible" && 
                    _userEmail.value != "Utilisateur non connecté" && _userEmail.value != "Chargement...") {
                    android.util.Log.i("UserSettingsViewModel", "Informations déjà chargées, arrêt des tentatives")
                    return@launch
                }
                
                loadUserInfo()
            }
        }
    }
    
    private fun loadUserInfoImmediately() {
        authManager?.let { auth ->
            // Debug complet de l'état utilisateur
            android.util.Log.i("UserSettingsViewModel", "=== DEBUG ÉTAT UTILISATEUR ===")
            auth.debugUserState()
            
            // Vérifier si l'utilisateur est connecté
            if (!auth.isUserLoggedIn()) {
                android.util.Log.w("UserSettingsViewModel", "Utilisateur non connecté, impossible de charger les infos")
                _userEmail.value = "Utilisateur non connecté"
                _userName.value = ""
                _tempName.value = ""
                return
            }
            
            val email = auth.getUserEmail()
            val name = auth.getUserName()
            
            android.util.Log.i("UserSettingsViewModel", "Email chargé immédiatement: '$email'")
            android.util.Log.i("UserSettingsViewModel", "Nom chargé immédiatement: '$name'")
            
            _userEmail.value = email ?: "Email non disponible"
            _userName.value = name ?: ""
            _tempName.value = name ?: ""
            
            // Si les infos sont vides, programmer un rechargement asynchrone
            if (email.isNullOrEmpty() || (name.isNullOrEmpty() && email != "Email non disponible")) {
                android.util.Log.i("UserSettingsViewModel", "Infos incomplètes, programmation d'un rechargement...")
                viewModelScope.launch {
                    kotlinx.coroutines.delay(1000) // Attendre 1 seconde
                    refreshUserInfo()
                }
            }
        }
    }
    
    private fun loadUserInfo() {
        authManager?.let { auth ->
            val email = auth.getUserEmail()
            val name = auth.getUserName()
            
            android.util.Log.i("UserSettingsViewModel", "Email chargé: $email")
            android.util.Log.i("UserSettingsViewModel", "Nom chargé: $name")
            
            _userEmail.value = email ?: "Email non disponible"
            _userName.value = name ?: ""
            _tempName.value = name ?: ""
        }
    }
    
    fun refreshUserInfo() {
        viewModelScope.launch {
            try {
                // Rafraîchir la session d'abord si possible
                authManager?.refreshUserSession()?.let { result ->
                    if (result.isSuccess) {
                        android.util.Log.i("UserSettingsViewModel", "Session rafraîchie avec succès")
                    } else {
                        android.util.Log.w("UserSettingsViewModel", "Échec du rafraîchissement de session")
                    }
                }
                
                // Charger les informations dans tous les cas
                loadUserInfo()
            } catch (e: Exception) {
                android.util.Log.e("UserSettingsViewModel", "Erreur lors du rafraîchissement: ${e.message}")
                // Même en cas d'erreur, essayer de charger les infos locales
                loadUserInfo()
            }
        }
    }
    
    fun startEditingName() {
        _isEditingName.value = true
        _tempName.value = _userName.value
        android.util.Log.i("UserSettingsViewModel", "Édition du nom commencée - nom actuel: ${_userName.value}")
    }
    
    fun cancelEditingName() {
        _isEditingName.value = false
        _tempName.value = _userName.value
    }
    
    fun updateTempName(newName: String) {
        _tempName.value = newName
    }
    
    fun saveUserName() {
        val newName = _tempName.value.trim()
        if (newName.isEmpty()) {
            _error.value = "Le nom ne peut pas être vide"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            authManager?.updateUserName(newName)?.collect { response ->
                when (response) {
                    is AuthResponse.Success -> {
                        _userName.value = newName
                        _isEditingName.value = false
                        _successMessage.value = "Nom mis à jour avec succès !"
                        
                        // Rafraîchir les informations utilisateur après la mise à jour
                        kotlinx.coroutines.delay(500) // Petite pause pour laisser le temps à Supabase
                        refreshUserInfo()
                    }
                    is AuthResponse.Error -> {
                        _error.value = response.message ?: "Erreur lors de la mise à jour"
                    }
                }
            }
            
            _isLoading.value = false
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            authManager?.signOut()?.collect { response ->
                when (response) {
                    is AuthResponse.Success -> {
                        _isSignedOut.value = true
                        _successMessage.value = "Déconnexion réussie"
                    }
                    is AuthResponse.Error -> {
                        _error.value = response.message ?: "Erreur lors de la déconnexion"
                    }
                }
            }
            
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearSuccessMessage() {
        _successMessage.value = null
    }
} 