package com.hypercart.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypercart.data.AddToCartRequest
import com.hypercart.data.Cart
import com.hypercart.data.CartRepository
import com.hypercart.data.Category
import com.hypercart.data.CategoryWithOrder
import com.hypercart.data.CategoryWithCartItems
import com.hypercart.data.CategoryWithProducts
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
    
    private val _categoriesWithOrder = MutableStateFlow<List<CategoryWithOrder>>(emptyList())
    val categoriesWithOrder: StateFlow<List<CategoryWithOrder>> = _categoriesWithOrder.asStateFlow()
    
    private val _categoriesWithProducts = MutableStateFlow<List<CategoryWithProducts>>(emptyList())
    val categoriesWithProducts: StateFlow<List<CategoryWithProducts>> = _categoriesWithProducts.asStateFlow()
    
    private val _categoriesWithCartItems = MutableStateFlow<List<CategoryWithCartItems>>(emptyList())
    val categoriesWithCartItems: StateFlow<List<CategoryWithCartItems>> = _categoriesWithCartItems.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<Long?>(null)
    val selectedCategory: StateFlow<Long?> = _selectedCategory.asStateFlow()
    
    // Nouveau mode d'affichage : groupé par catégorie ou liste simple
    private val _isGroupedByCategory = MutableStateFlow(false)
    val isGroupedByCategory: StateFlow<Boolean> = _isGroupedByCategory.asStateFlow()
    
    // Mode d'affichage groupé pour le panier
    private val _isCartGroupedByCategory = MutableStateFlow(true)
    val isCartGroupedByCategory: StateFlow<Boolean> = _isCartGroupedByCategory.asStateFlow()
    
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
    
    // État pour indiquer si on a besoin de demander la catégorie
    private val _needsCategoryInput = MutableStateFlow<Pair<String, Int>?>(null) // nom produit, quantité
    val needsCategoryInput: StateFlow<Pair<String, Int>?> = _needsCategoryInput.asStateFlow()
    
    // État pour les suggestions d'autocomplétion
    private val _productSuggestions = MutableStateFlow<List<Product>>(emptyList())
    val productSuggestions: StateFlow<List<Product>> = _productSuggestions.asStateFlow()
    
    private val _categorySuggestions = MutableStateFlow<List<Category>>(emptyList())
    val categorySuggestions: StateFlow<List<Category>> = _categorySuggestions.asStateFlow()

    fun loadProductsAndCategories(storeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Charger les catégories
                productRepository.getAllCategories()
                    .onSuccess { categoryList ->
                        _categories.value = categoryList
                        // Si aucune catégorie n'existe, créer une catégorie par défaut
                        if (categoryList.isEmpty()) {
                            Log.w("ItemViewModel", "Aucune catégorie trouvée, création d'une catégorie par défaut")
                        }
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur lors du chargement des catégories"
                    }
                
                // Charger tous les produits du magasin groupés par catégorie par défaut
                loadProductsGroupedByCategory(storeId)
                
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue, veuillez réessayer."
            }
            _isLoading.value = false
        }
    }
    
    fun loadAllProducts(storeId: Long) {
        // Rediriger vers le mode groupé par défaut
        loadProductsGroupedByCategory(storeId)
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
    
    fun addToCart(productId: Long, quantity: Int = 1) {
        viewModelScope.launch {
            val cart = _currentCart.value
            if (cart == null) {
                _error.value = "Panier non initialisé"
                return@launch
            }
            
            _error.value = null
            
            try {
                val request = AddToCartRequest(
                    productId = productId,
                    quantity = quantity,
                )
                
                cartRepository.addItemToCart(cart.id, request)
                    .onSuccess { cartItem ->
                        // Mise à jour locale immédiate avec le nouvel item
                        val updatedItems = cart.items + cartItem
                        val updatedCart = cart.copy(items = updatedItems)
                        _currentCart.value = updatedCart
                        _cartItemCount.value = updatedItems.sumOf { it.quantity }
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Impossible d'ajouter au panier"
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue lors de l'ajout au panier."
            }
        }
    }
    
    fun updateCartItemQuantity(itemId: Long, newQuantity: Int) {
        viewModelScope.launch {
            val cart = _currentCart.value ?: return@launch
            
            if (newQuantity <= 0) {
                // Si quantité <= 0, supprimer directement l'item
                removeFromCart(itemId)
                return@launch
            }
            
            // Mise à jour locale immédiate (optimistic update)
            val updatedItems = cart.items.map { item ->
                if (item.id == itemId) {
                    item.copy(quantity = newQuantity)
                } else {
                    item
                }
            }
            val updatedCart = cart.copy(items = updatedItems)
            _currentCart.value = updatedCart
            _cartItemCount.value = updatedItems.sumOf { it.quantity }
            
            try {
                cartRepository.updateCartItemQuantity(itemId, newQuantity)
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur lors de la mise à jour"
                        // Recharger seulement en cas d'erreur réelle
                        loadCartWithItems(cart.id)
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue lors de la mise à jour."
                // Recharger en cas d'erreur
                loadCartWithItems(cart.id)
            }
        }
    }
    
    fun removeFromCart(itemId: Long) {
        viewModelScope.launch {
            val cart = _currentCart.value ?: return@launch
            
            // Mise à jour locale immédiate (optimistic update)
            val updatedItems = cart.items.filter { item -> item.id != itemId }
            val updatedCart = cart.copy(items = updatedItems)
            _currentCart.value = updatedCart
            _cartItemCount.value = updatedItems.sumOf { it.quantity }

            // Puis sauvegarder en arrière-plan
            try {
                cartRepository.removeItemFromCart(itemId)
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur lors de la suppression"
                        // Recharger en cas d'erreur pour restaurer l'état correct
                        loadCartWithItems(cart.id)
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue lors de la suppression."
                // Recharger en cas d'erreur
                loadCartWithItems(cart.id)
            }
        }
    }
    

    fun clearCart() {
        viewModelScope.launch {
            val cart = _currentCart.value ?: return@launch
            
            // Mise à jour locale immédiate (optimistic update)
            val updatedCart = cart.copy(items = emptyList())
            _currentCart.value = updatedCart
            _cartItemCount.value = 0

            // Puis sauvegarder en arrière-plan
            try {
                cartRepository.clearCart(cart.id)
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur lors du vidage du panier"
                        // Recharger en cas d'erreur pour restaurer l'état correct
                        loadCartWithItems(cart.id)
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue lors du vidage du panier."
                // Recharger en cas d'erreur
                loadCartWithItems(cart.id)
            }
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
    
    fun createProduct(name: String, categoryId: Long = 1, storeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val request = CreateProductRequest(
                    name = name,
                    categoryId = categoryId,
                    storeId = storeId
                )
                
                Log.i("ItemViewModel", "Création produit: name=$name, categoryId=$categoryId, storeId=$storeId")
                Log.i("ItemViewModel", "Request object: $request")
                
                productRepository.createProduct(request)
                    .onSuccess { newProduct ->
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
    
    fun checkAndAddProduct(productName: String, quantity: Int = 1, storeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // 1. Chercher si le produit existe déjà
                productRepository.searchProductByName(productName, storeId)
                    .onSuccess { existingProduct ->
                        if (existingProduct != null) {
                            // Le produit existe, l'ajouter directement au panier
                            Log.i("ItemViewModel", "Produit trouvé: ${existingProduct.name}, ajout au panier")
                            addToCart(existingProduct.id, quantity)
                            _needsCategoryInput.value = null
                        } else {
                            // Le produit n'existe pas, demander la catégorie
                            Log.i("ItemViewModel", "Produit non trouvé: $productName, demande de catégorie")
                            _needsCategoryInput.value = Pair(productName, quantity)
                        }
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur lors de la recherche du produit"
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue lors de l'ajout du produit."
            }
            _isLoading.value = false
        }
    }
    
    fun createProductWithCategory(productName: String, quantity: Int, categoryName: String, storeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                createProductWithNewCategory(productName, categoryName, quantity, storeId)
                _needsCategoryInput.value = null
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue lors de la création du produit."
                _isLoading.value = false
            }
        }
    }
    
    fun clearNeedsCategoryInput() {
        _needsCategoryInput.value = null
    }
    
    // Autocomplétion
    fun searchProductSuggestions(query: String, storeId: Long) {
        if (query.length < 2) {
            _productSuggestions.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            try {
                val allProducts = _products.value
                val suggestions = allProducts.filter { product ->
                    product.name.contains(query, ignoreCase = true)
                }.take(5) // Limiter à 5 suggestions
                
                _productSuggestions.value = suggestions
            } catch (e: Exception) {
                Log.e("ItemViewModel", "Erreur lors de la recherche de suggestions: ${e.message}")
                _productSuggestions.value = emptyList()
            }
        }
    }
    
    fun clearProductSuggestions() {
        _productSuggestions.value = emptyList()
    }
    
    // Autocomplétion pour les catégories
    fun searchCategorySuggestions(query: String) {
        if (query.length < 2) {
            _categorySuggestions.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            try {
                val allCategories = _categories.value
                val suggestions = allCategories.filter { category ->
                    category.name.contains(query, ignoreCase = true)
                }.take(5) // Limiter à 5 suggestions
                
                _categorySuggestions.value = suggestions
            } catch (e: Exception) {
                Log.e("ItemViewModel", "Erreur lors de la recherche de suggestions de catégories: ${e.message}")
                _categorySuggestions.value = emptyList()
            }
        }
    }
    
    fun clearCategorySuggestions() {
        _categorySuggestions.value = emptyList()
    }

    private suspend fun createProductWithNewCategory(productName: String, categoryName: String, quantity: Int, storeId: Long) {
        // 1. Vérifier si la catégorie existe déjà
        val existingCategory = _categories.value.find { it.name.equals(categoryName, ignoreCase = true) }
        
        if (existingCategory != null) {
            // La catégorie existe déjà, utiliser son ID
            Log.i("ItemViewModel", "Catégorie existante trouvée: ${existingCategory.name} (ID: ${existingCategory.id})")
            
            // S'assurer que la catégorie est dans l'ordre du magasin
            productRepository.addCategoryToStoreOrder(existingCategory.id, storeId)
                .onSuccess { 
                    Log.i("ItemViewModel", "Catégorie ajoutée à l'ordre du magasin (si pas déjà présente)")
                }
                .onFailure { exception ->
                    Log.w("ItemViewModel", "Erreur lors de l'ajout à l'ordre: ${exception.message}")
                }
            
            createProductWithExistingCategory(productName, existingCategory.id, quantity, storeId)
        } else {
            // Créer une nouvelle catégorie ET l'ajouter automatiquement à l'ordre du magasin
            productRepository.createCategoryAndAddToStore(categoryName, storeId)
                .onSuccess { (newCategory, categoryOrder) ->
                    Log.i("ItemViewModel", "Nouvelle catégorie créée: ${newCategory.name} (ID: ${newCategory.id}) avec ordre ${categoryOrder.orderId}")
                    
                    // Rafraîchir la liste des catégories
                    loadCategories()
                    
                    // Créer le produit avec cette catégorie
                    createProductWithExistingCategory(productName, newCategory.id, quantity, storeId)
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Impossible de créer la catégorie"
                    _isLoading.value = false
                }
        }
    }
    
    private suspend fun createProductWithExistingCategory(productName: String, categoryId: Long, quantity: Int, storeId: Long) {
        val request = CreateProductRequest(
            name = productName,
            categoryId = categoryId,
            storeId = storeId
        )

        productRepository.createProduct(request)
            .onSuccess { newProduct ->
                _successMessage.value = "Produit '$productName' créé !"
                
                // Recharger la liste des produits selon le mode d'affichage actuel
                refreshProductData(storeId)

                // Ajouter le produit au panier
                addToCart(newProduct.id, quantity)
            }
            .onFailure { exception ->
                _error.value = exception.message ?: "Impossible de créer le produit"
            }
        
        _isLoading.value = false
    }
    
    suspend fun loadCategories() {
        productRepository.getAllCategories()
            .onSuccess { categoryList ->
                _categories.value = categoryList
            }
            .onFailure { exception ->
                Log.e("ItemViewModel", "Erreur lors du rechargement des catégories: ${exception.message}")
            }
    }
    
    fun loadCategoriesWithOrder(storeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                productRepository.getCategoriesWithOrderForStore(storeId)
                    .onSuccess { categoriesList ->
                        _categoriesWithOrder.value = categoriesList
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur lors du chargement des catégories"
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue lors du chargement des catégories."
            }
            _isLoading.value = false
        }
    }
    
    fun saveCategoryOrders(categories: List<CategoryWithOrder>, storeId: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _error.value = null
            
            try {
                Log.i("ItemViewModel", "Début de la sauvegarde de l'ordre des catégories pour le magasin $storeId")
                
                productRepository.saveCategoryOrders(categories, storeId)
                    .onSuccess {
                        Log.i("ItemViewModel", "Sauvegarde réussie - rechargement des données")
                        
                        _successMessage.value = "Ordre des catégories sauvegardé !"
                        
                        // Mettre à jour l'état local avec les nouvelles positions
                        val updatedCategories = categories.mapIndexed { index, category ->
                            category.copy(orderId = index + 1)
                        }
                        _categoriesWithOrder.value = updatedCategories
                        
                        // Debug : Afficher l'état des ordres après sauvegarde
                        debugCategoryOrders(storeId)
                        
                        // Recharger depuis la base pour s'assurer de la cohérence
                        loadCategoriesWithOrder(storeId)
                    }
                    .onFailure { exception ->
                        Log.e("ItemViewModel", "Échec de la sauvegarde: ${exception.message}")
                        _error.value = exception.message ?: "Erreur lors de la sauvegarde"
                    }
            } catch (e: Exception) {
                Log.e("ItemViewModel", "Exception lors de la sauvegarde: ${e.message}")
                _error.value = "Une erreur est survenue lors de la sauvegarde."
            }
            onComplete()
        }
    }
    
    /**
     * Ajoute une catégorie existante à l'ordre d'affichage d'un magasin
     * L'ordre sera automatiquement incrémenté
     */
    fun addCategoryToStoreOrder(categoryId: Long, storeId: Long) {
        viewModelScope.launch {
            _error.value = null
            
            try {
                productRepository.addCategoryToStoreOrder(categoryId, storeId)
                    .onSuccess { 
                        _successMessage.value = "Catégorie ajoutée à l'ordre d'affichage !"
                        // Recharger les catégories avec ordre pour mettre à jour l'affichage
                        loadCategoriesWithOrder(storeId)
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur lors de l'ajout de la catégorie"
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue lors de l'ajout de la catégorie."
            }
        }
    }
    
    /**
     * Récupère le prochain numéro d'ordre pour un magasin (pour information/debug)
     */
    fun getNextOrderIdForStore(storeId: Long, onResult: (Int?) -> Unit) {
        viewModelScope.launch {
            try {
                productRepository.getNextOrderIdForStore(storeId)
                    .onSuccess { nextOrderId ->
                        Log.i("ItemViewModel", "Prochain ordre pour le magasin $storeId: $nextOrderId")
                        onResult(nextOrderId)
                    }
                    .onFailure { exception ->
                        Log.e("ItemViewModel", "Erreur récupération ordre: ${exception.message}")
                        onResult(null)
                    }
            } catch (e: Exception) {
                Log.e("ItemViewModel", "Exception lors de la récupération de l'ordre: ${e.message}")
                onResult(null)
            }
        }
    }
    
    /**
     * Charge les produits groupés par catégorie avec ordre
     */
    fun loadProductsGroupedByCategory(storeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isGroupedByCategory.value = true
            _selectedCategory.value = null
            
            try {
                productRepository.getProductsGroupedByCategoryWithOrder(storeId)
                    .onSuccess { categoriesWithProducts ->
                        Log.i("ItemViewModel", "Produits groupés chargés: ${categoriesWithProducts.size} catégories")
                        _categoriesWithProducts.value = categoriesWithProducts
                        
                        // Aussi mettre à jour la liste simple des produits pour compatibilité
                        val allProducts = categoriesWithProducts.flatMap { it.products }
                        _products.value = allProducts
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur lors du chargement des produits groupés"
                    }
            } catch (e: Exception) {
                _error.value = "Une erreur est survenue lors du chargement."
            }
            _isLoading.value = false
        }
    }
    
    /**
     * Bascule entre l'affichage groupé par catégorie et l'affichage simple
     */
    fun toggleCategoryGrouping(storeId: Long, enableGrouping: Boolean) {
        if (enableGrouping && !_isGroupedByCategory.value) {
            loadProductsGroupedByCategory(storeId)
        } else if (!enableGrouping && _isGroupedByCategory.value) {
            loadAllProductsSimple(storeId)
        }
    }
    
    /**
     * Met à jour le mode d'affichage simple (sans groupement)
     */
    fun loadAllProductsSimple(storeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _selectedCategory.value = null
            _isGroupedByCategory.value = false
            
            try {
                productRepository.getProductsByStore(storeId)
                    .onSuccess { productList ->
                        _products.value = productList
                        // Vider les catégories groupées en mode simple
                        _categoriesWithProducts.value = emptyList()
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
    
    /**
     * Recharge les données après un ajout/modification de produit
     */
    fun refreshProductData(storeId: Long) {
        if (_isGroupedByCategory.value) {
            loadProductsGroupedByCategory(storeId)
        } else if (_selectedCategory.value != null) {
            loadProductsByCategory(_selectedCategory.value!!, storeId)
        } else {
            loadAllProductsSimple(storeId)
        }
    }
    
    /**
     * Méthode de debug pour afficher l'état des ordres en base
     */
    private fun debugCategoryOrders(storeId: Long) {
        viewModelScope.launch {
            try {
                productRepository.debugCategoryOrders(storeId)
                    .onSuccess { 
                        Log.i("ItemViewModel", "Debug des ordres terminé - voir les logs du ProductRepository")
                    }
                    .onFailure { exception ->
                        Log.e("ItemViewModel", "Erreur debug ordres: ${exception.message}")
                    }
            } catch (e: Exception) {
                Log.e("ItemViewModel", "Exception debug ordres: ${e.message}")
            }
        }
    }
    
    /**
     * Groupe les produits du panier par catégorie selon l'ordre défini
     */
    fun groupCartItemsByCategory(storeId: Long) {
        viewModelScope.launch {
            try {
                val cart = _currentCart.value
                if (cart == null || cart.items.isEmpty()) {
                    _categoriesWithCartItems.value = emptyList()
                    return@launch
                }
                
                // Récupérer les catégories avec ordre pour ce magasin
                productRepository.getCategoriesWithOrderForStore(storeId)
                    .onSuccess { categoriesWithOrder ->
                        // Créer une map pour trouver rapidement les catégories
                        val categoryMap = categoriesWithOrder.associateBy { it.id }
                        
                        // Grouper les produits du panier par catégorie
                        val cartItemsGrouped = cart.items.groupBy { cartItem ->
                            // Trouver la catégorie du produit
                            val product = _products.value.find { it.id == cartItem.productId }
                            product?.categoryId ?: 1L // Catégorie par défaut si produit non trouvé
                        }
                        
                        // Créer les CategoryWithCartItems avec ordre
                        val categoriesWithCartItems = cartItemsGrouped.map { (categoryId, cartItems) ->
                            val category = categoryMap[categoryId]
                            CategoryWithCartItems(
                                id = categoryId,
                                name = category?.name ?: "Catégorie inconnue",
                                orderId = category?.orderId,
                                cartItems = cartItems
                            )
                        }.sortedBy { it.orderId ?: Int.MAX_VALUE }
                        
                        _categoriesWithCartItems.value = categoriesWithCartItems
                        
                        Log.i("ItemViewModel", "Panier groupé par catégorie: ${categoriesWithCartItems.size} catégories")
                        categoriesWithCartItems.forEach { category ->
                            Log.i("ItemViewModel", "Catégorie: ${category.name} (${category.cartItems.size} produits)")
                        }
                    }
                    .onFailure { exception ->
                        Log.e("ItemViewModel", "Erreur lors du groupement du panier: ${exception.message}")
                        _categoriesWithCartItems.value = emptyList()
                    }
            } catch (e: Exception) {
                Log.e("ItemViewModel", "Exception lors du groupement du panier: ${e.message}")
                _categoriesWithCartItems.value = emptyList()
            }
        }
    }
    
    /**
     * Bascule l'affichage groupé par catégorie dans le panier
     */
    fun toggleCartCategoryGrouping(storeId: Long, enableGrouping: Boolean) {
        _isCartGroupedByCategory.value = enableGrouping
        if (enableGrouping) {
            groupCartItemsByCategory(storeId)
        } else {
            _categoriesWithCartItems.value = emptyList()
        }
    }
}

