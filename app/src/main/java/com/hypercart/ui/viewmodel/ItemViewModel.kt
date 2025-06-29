package com.hypercart.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypercart.data.AddToCartRequest
import com.hypercart.data.Cart
import com.hypercart.data.CartRepository
import com.hypercart.data.Category
import com.hypercart.data.CreateProductRequest
import com.hypercart.data.Product
import com.hypercart.data.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ItemViewModel : ViewModel() {
    private val productRepository = ProductRepository()
    private val cartRepository = CartRepository()
    
    // États pour les produits
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()
    
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<Long?>(null)
    val selectedCategory: StateFlow<Long?> = _selectedCategory.asStateFlow()
    
    // États pour le panier
    private val _currentCart = MutableStateFlow<Cart?>(null)
    val currentCart: StateFlow<Cart?> = _currentCart.asStateFlow()
    
    private val _cartItemCount = MutableStateFlow(0)
    val cartItemCount: StateFlow<Int> = _cartItemCount.asStateFlow()
    
    // États globaux
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun loadProductsAndCategories(storeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Charger les catégories
                productRepository.getAllCategories()
                    .onSuccess { categoryList ->
                        _categories.value = categoryList
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur lors du chargement des catégories"
                    }
                
                // Charger tous les produits du magasin par défaut
                loadAllProducts(storeId)
                
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue, veuillez réessayer."
            }
            _isLoading.value = false
        }
    }
    
    fun loadAllProducts(storeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _selectedCategory.value = null
            
            try {
                productRepository.getProductsByStore(storeId)
                    .onSuccess { productList ->
                        _products.value = productList
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur lors du chargement des produits"
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue, veuillez réessayer."
            }
            _isLoading.value = false
        }
    }
    
    fun loadProductsByCategory(categoryId: Long, storeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _selectedCategory.value = categoryId
            
            try {
                productRepository.getProductsByCategory(categoryId, storeId)
                    .onSuccess { productList ->
                        _products.value = productList
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur lors du chargement des produits"
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue, veuillez réessayer."
            }
            _isLoading.value = false
        }
    }
    
    fun initializeCartForStore(storeId: Long) {
        viewModelScope.launch {
            try {
                cartRepository.getOrCreateCart(storeId)
                    .onSuccess { cart ->
                        _currentCart.value = cart
                        loadCartWithItems(cart.id)
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur lors de l'initialisation du panier"
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue avec le panier."
            }
        }
    }
    
    fun addToCart(productId: Long, quantity: Int = 1, description: String = "") {
        viewModelScope.launch {
            val cart = _currentCart.value
            if (cart == null) {
                _error.value = "Panier non initialisé"
                return@launch
            }
            
            _isLoading.value = true
            _error.value = null
            
            try {
                val request = AddToCartRequest(
                    productId = productId,
                    quantity = quantity,
                    description = description
                )
                
                cartRepository.addItemToCart(cart.id, request)
                    .onSuccess { cartItem ->
                        _successMessage.value = "Produit ajouté au panier !"
                        loadCartWithItems(cart.id) // Recharger le panier
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Impossible d'ajouter au panier"
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue lors de l'ajout au panier."
            }
            _isLoading.value = false
        }
    }
    
    fun updateCartItemQuantity(itemId: Long, newQuantity: Int) {
        viewModelScope.launch {
            val cart = _currentCart.value ?: return@launch
            
            _isLoading.value = true
            _error.value = null
            
            try {
                cartRepository.updateCartItemQuantity(itemId, newQuantity)
                    .onSuccess {
                        loadCartWithItems(cart.id) // Recharger le panier
                    }
                    .onFailure { exception ->
                        if (exception.message?.contains("supprimé") == true) {
                            _successMessage.value = "Article supprimé du panier"
                            loadCartWithItems(cart.id)
                        } else {
                            _error.value = exception.message ?: "Erreur lors de la mise à jour"
                        }
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue lors de la mise à jour."
            }
            _isLoading.value = false
        }
    }
    
    fun removeFromCart(itemId: Long) {
        viewModelScope.launch {
            val cart = _currentCart.value ?: return@launch
            
            _isLoading.value = true
            _error.value = null
            
            try {
                cartRepository.removeItemFromCart(itemId)
                    .onSuccess {
                        _successMessage.value = "Article supprimé du panier"
                        loadCartWithItems(cart.id) // Recharger le panier
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur lors de la suppression"
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue lors de la suppression."
            }
            _isLoading.value = false
        }
    }
    
    fun clearCart() {
        viewModelScope.launch {
            val cart = _currentCart.value ?: return@launch
            
            _isLoading.value = true
            _error.value = null
            
            try {
                cartRepository.clearCart(cart.id)
                    .onSuccess {
                        _successMessage.value = "Panier vidé"
                        loadCartWithItems(cart.id) // Recharger le panier
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur lors du vidage du panier"
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue lors du vidage du panier."
            }
            _isLoading.value = false
        }
    }
    
    private fun loadCartWithItems(cartId: Long) {
        viewModelScope.launch {
            try {
                cartRepository.getCartWithItems(cartId)
                    .onSuccess { cartWithItems ->
                        _currentCart.value = cartWithItems
                        _cartItemCount.value = cartWithItems.items.sumOf { it.quantity }
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur lors du chargement du panier"
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue lors du chargement du panier."
            }
        }
    }
    
    fun createProduct(name: String, description: String = "", categoryId: Long = 1, storeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val request = CreateProductRequest(
                    name = name,
                    categoryId = categoryId,
                    storeId = storeId
                )
                
                productRepository.createProduct(request)
                    .onSuccess { newProduct ->
                        _successMessage.value = "Produit '$name' créé avec succès !"
                        // Recharger la liste des produits
                        if (_selectedCategory.value != null) {
                            loadProductsByCategory(_selectedCategory.value!!, storeId)
                        } else {
                            loadAllProducts(storeId)
                        }
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Impossible de créer le produit"
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue lors de la création du produit."
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

