package com.hypercart.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypercart.data.CreateStoreRequest
import com.hypercart.data.Store
import com.hypercart.data.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StoreViewModel : ViewModel() {
    private val storeRepository = StoreRepository()
    
    private val _stores = MutableStateFlow<List<Store>>(emptyList())
    val stores: StateFlow<List<Store>> = _stores.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _selectedStore = MutableStateFlow<Store?>(null)
    val selectedStore: StateFlow<Store?> = _selectedStore.asStateFlow()
    
    init {
        loadStores()
    }
    
    fun loadStores() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                storeRepository.getAllStores()
                    .onSuccess { storeList ->
                        _stores.value = storeList
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur lors du chargement des magasins"
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue, veuillez réessayer."
            }
            _isLoading.value = false
        }
    }
    
    fun refreshStores() {
        // Méthode pour forcer un rafraîchissement complet
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _stores.value = emptyList() // Vider temporairement la liste
            try {
                storeRepository.getAllStores()
                    .onSuccess { storeList ->
                        _stores.value = storeList
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur lors du chargement des magasins"
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue, veuillez réessayer."
            }
            _isLoading.value = false
        }
    }

    fun createStore(name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val request = CreateStoreRequest(name)
            try {
                storeRepository.createStore(request)
                    .onSuccess { newStore ->
                        val currentStores = _stores.value.toMutableList()
                        currentStores.add(newStore)
                        _stores.value = currentStores
                        
                        loadStores()
                        onSuccess()
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Impossible de créer le magasin. Veuillez réessayer."
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue, veuillez réessayer."
            }
            _isLoading.value = false
        }
    }
    
    fun deleteStore(storeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                storeRepository.deleteStore(storeId)
                    .onSuccess {
                        loadStores() // Recharger la liste
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur lors de la suppression du magasin"
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue, veuillez réessayer."
            }
            _isLoading.value = false
        }
    }
    
    fun loadStoreById(storeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                storeRepository.getStoreById(storeId)
                    .onSuccess { store ->
                        _selectedStore.value = store
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur lors du chargement du magasin"
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue, veuillez réessayer."
            }
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun updateStoreName(storeId: Long, newName: String, onSuccess: () -> Unit) {
        if (newName.isBlank()) {
            _error.value = "Le nom du magasin ne peut pas être vide"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                storeRepository.updateStoreName(storeId, newName.trim())
                    .onSuccess { updatedStore ->
                        // Mettre à jour le magasin sélectionné
                        _selectedStore.value = updatedStore
                        
                        // Mettre à jour la liste des magasins
                        val currentStores = _stores.value.toMutableList()
                        val storeIndex = currentStores.indexOfFirst { it.id == storeId }
                        if (storeIndex != -1) {
                            currentStores[storeIndex] = updatedStore
                            _stores.value = currentStores
                        }
                        
                        onSuccess()
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Impossible de modifier le nom du magasin"
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue lors de la modification du nom"
            }
            _isLoading.value = false
        }
    }
} 