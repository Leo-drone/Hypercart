package com.hypercart.data

import android.util.Log
import com.hypercart.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepository {
    private val supabaseClient = SupabaseConfig.client
    
    suspend fun getAllProducts(): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                val products = supabaseClient
                    .from("product")
                    .select(columns = Columns.ALL)
                    .decodeList<Product>()
                
                Result.success(products)
            } catch (e: Exception) {
                Log.e("ProductRepository", "Erreur lors du chargement des produits: ${e.message}")
                Result.failure(Exception("Impossible de charger les produits. Veuillez réessayer."))
            }
        }
    }
    
    suspend fun getProductsByStore(storeId: Long): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                val products = supabaseClient
                    .from("product")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("store_id", storeId)
                        }
                    }
                    .decodeList<Product>()
                
                Result.success(products)
            } catch (e: Exception) {
                Log.e("ProductRepository", "Erreur lors du chargement des produits du magasin: ${e.message}")
                Result.failure(Exception("Impossible de charger les produits. Veuillez réessayer."))
            }
        }
    }
    
    suspend fun getProductById(productId: Long): Result<Product?> {
        return withContext(Dispatchers.IO) {
            try {
                val product = supabaseClient
                    .from("product")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("id", productId)
                        }
                    }
                    .decodeSingleOrNull<Product>()
                
                Result.success(product)
            } catch (e: Exception) {
                Log.e("ProductRepository", "Erreur lors du chargement du produit: ${e.message}")
                Result.failure(Exception("Impossible de charger le produit. Veuillez réessayer."))
            }
        }
    }
    
    suspend fun getProductsByCategory(categoryId: Long, storeId: Long): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                val products = supabaseClient
                    .from("product")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("category_id", categoryId)
                            eq("store_id", storeId)
                        }
                    }
                    .decodeList<Product>()
                
                Result.success(products)
            } catch (e: Exception) {
                Log.e("ProductRepository", "Erreur lors du chargement des produits par catégorie: ${e.message}")
                Result.failure(Exception("Impossible de charger les produits. Veuillez réessayer."))
            }
        }
    }
    
    suspend fun createProduct(request: CreateProductRequest): Result<Product> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()
                    ?: return@withContext Result.failure(Exception("Vous devez être connecté pour créer un produit"))
                
                Log.i("ProductRepository", "Création produit - name: ${request.name}, categoryId: ${request.categoryId}, storeId: ${request.storeId}")

                val createdProduct = supabaseClient
                    .from("product")
                    .insert(request) {
                        select(Columns.ALL)
                    }
                    .decodeSingle<Product>()
                
                Result.success(createdProduct)

            } catch (e: Exception) {
                Log.e("ProductRepository", "Erreur lors de la création du produit: ${e.message}")
                Result.failure(Exception("Impossible de créer le produit. Veuillez réessayer."))
            }
        }
    }
    
    suspend fun searchProductByName(productName: String, storeId: Long): Result<Product?> {
        return withContext(Dispatchers.IO) {
            try {
                val product = supabaseClient
                    .from("product")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("name", productName)
                            eq("store_id", storeId)
                        }
                    }
                    .decodeSingleOrNull<Product>()
                
                Result.success(product)
            } catch (e: Exception) {
                Log.e("ProductRepository", "Erreur lors de la recherche du produit: ${e.message}")
                Result.failure(Exception("Impossible de rechercher le produit. Veuillez réessayer."))
            }
        }
    }
    
    suspend fun createCategory(categoryName: String): Result<Category> {
        return withContext(Dispatchers.IO) {
            try {
                val category = Category(name = categoryName)
                
                val createdCategory = supabaseClient
                    .from("categories")
                    .insert(category) {
                        select(Columns.ALL)
                    }
                    .decodeSingle<Category>()
                
                Result.success(createdCategory)
            } catch (e: Exception) {
                Log.e("ProductRepository", "Erreur lors de la création de la catégorie: ${e.message}")
                Result.failure(Exception("Impossible de créer la catégorie. Veuillez réessayer."))
            }
        }
    }
    
    suspend fun getAllCategories(): Result<List<Category>> {
        return withContext(Dispatchers.IO) {
            try {
                val categories = supabaseClient
                    .from("categories")
                    .select(columns = Columns.ALL)
                    .decodeList<Category>()
                
                Result.success(categories)
            } catch (e: Exception) {
                Log.e("ProductRepository", "Erreur lors du chargement des catégories: ${e.message}")
                Result.failure(Exception("Impossible de charger les catégories. Veuillez réessayer."))
            }
        }
    }
    
    suspend fun getCategoriesWithOrderForStore(storeId: Long): Result<List<CategoryWithOrder>> {
        return withContext(Dispatchers.IO) {
            try {
                // D'abord récupérer toutes les catégories
                val allCategories = supabaseClient
                    .from("categories")
                    .select(columns = Columns.ALL)
                    .decodeList<Category>()
                
                // Puis récupérer les ordres pour ce magasin
                val categoryOrders = supabaseClient
                    .from("store_category_order")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("store_id", storeId)
                        }
                    }
                    .decodeList<StoreCategoryOrder>()
                
                // Combiner les données
                val categoriesWithOrder = allCategories.map { category ->
                    val order = categoryOrders.find { it.categoryId == category.id }
                    CategoryWithOrder(
                        id = category.id,
                        name = category.name,
                        orderId = order?.orderId
                    )
                }
                
                // Trier par ordre (null en dernier)
                val sorted = categoriesWithOrder.sortedWith(
                    compareBy<CategoryWithOrder> { it.orderId ?: Int.MAX_VALUE }
                        .thenBy { it.name }
                )
                
                Result.success(sorted)
            } catch (e: Exception) {
                Log.e("ProductRepository", "Erreur lors du chargement des catégories avec ordre: ${e.message}")
                Result.failure(Exception("Impossible de charger les catégories. Veuillez réessayer."))
            }
        }
    }
    
    suspend fun updateCategoryOrder(categoryId: Long, storeId: Long, newOrder: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Vérifier si l'ordre existe déjà
                val existingOrder = supabaseClient
                    .from("store_category_order")
                    .select() {
                        filter {
                            eq("category_id", categoryId)
                            eq("store_id", storeId)
                        }
                    }
                    .decodeSingleOrNull<StoreCategoryOrder>()
                
                if (existingOrder != null) {
                    // Mettre à jour l'ordre existant
                    supabaseClient
                        .from("store_category_order")
                        .update(UpdateCategoryOrderRequest(orderId = newOrder)) {
                            filter {
                                eq("category_id", categoryId)
                                eq("store_id", storeId)
                            }
                        }
                } else {
                    // Créer un nouvel ordre
                    supabaseClient
                        .from("store_category_order")
                        .insert(CreateCategoryOrderRequest(
                            categoryId = categoryId,
                            storeId = storeId,
                            orderId = newOrder
                        ))
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("ProductRepository", "Erreur lors de la mise à jour de l'ordre: ${e.message}")
                Result.failure(Exception("Impossible de mettre à jour l'ordre. Veuillez réessayer."))
            }
        }
    }
    
    suspend fun saveCategoryOrders(categories: List<CategoryWithOrder>, storeId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.i("ProductRepository", "Sauvegarde de l'ordre de ${categories.size} catégories pour le magasin $storeId")
                
                categories.forEachIndexed { index, category ->
                    // Utiliser index + 1 pour commencer à 1 au lieu de 0 (cohérent avec le système d'incrémentation)
                    val newOrderId = index + 1
                    Log.i("ProductRepository", "Catégorie '${category.name}' (ID: ${category.id}) → Nouvel ordre: $newOrderId")
                    
                    val result = updateCategoryOrder(category.id, storeId, newOrderId)
                    if (result.isFailure) {
                        Log.e("ProductRepository", "Échec de la mise à jour pour la catégorie ${category.id}: ${result.exceptionOrNull()?.message}")
                        return@withContext result
                    }
                }
                
                Log.i("ProductRepository", "Sauvegarde des ordres terminée avec succès")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("ProductRepository", "Erreur lors de la sauvegarde des ordres: ${e.message}")
                Result.failure(Exception("Impossible de sauvegarder les ordres. Veuillez réessayer."))
            }
        }
    }
    
    /**
     * Récupère le prochain numéro d'ordre disponible pour un magasin
     * (plus grande valeur existante + 1)
     */
    suspend fun getNextOrderIdForStore(storeId: Long): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val categoryOrders = supabaseClient
                    .from("store_category_order")
                    .select() {
                        filter {
                            eq("store_id", storeId)
                        }
                    }
                    .decodeList<StoreCategoryOrder>()
                
                val maxOrderId = categoryOrders.maxOfOrNull { it.orderId } ?: 0
                val nextOrderId = maxOrderId + 1
                
                Log.i("ProductRepository", "Prochain ordre pour le magasin $storeId: $nextOrderId (max actuel: $maxOrderId)")
                Result.success(nextOrderId)
                
            } catch (e: Exception) {
                Log.e("ProductRepository", "Erreur lors de la récupération du prochain ordre: ${e.message}")
                Result.failure(Exception("Impossible de déterminer l'ordre. Veuillez réessayer."))
            }
        }
    }
    
    /**
     * Ajoute automatiquement une catégorie à l'ordre d'affichage d'un magasin
     * L'ordre sera automatiquement défini comme la plus grande valeur + 1
     */
    suspend fun addCategoryToStoreOrder(categoryId: Long, storeId: Long): Result<StoreCategoryOrder> {
        return withContext(Dispatchers.IO) {
            try {
                // Vérifier si la catégorie est déjà dans l'ordre de ce magasin
                val existingOrder = supabaseClient
                    .from("store_category_order")
                    .select() {
                        filter {
                            eq("category_id", categoryId)
                            eq("store_id", storeId)
                        }
                    }
                    .decodeSingleOrNull<StoreCategoryOrder>()
                
                if (existingOrder != null) {
                    Log.i("ProductRepository", "La catégorie $categoryId est déjà dans l'ordre du magasin $storeId")
                    return@withContext Result.success(existingOrder)
                }
                
                // Récupérer le prochain ordre disponible
                val nextOrderResult = getNextOrderIdForStore(storeId)
                if (nextOrderResult.isFailure) {
                    return@withContext Result.failure(nextOrderResult.exceptionOrNull() ?: Exception("Erreur ordre"))
                }
                
                val nextOrderId = nextOrderResult.getOrNull() ?: 1
                
                // Créer le nouvel ordre
                val newOrderRequest = CreateCategoryOrderRequest(
                    categoryId = categoryId,
                    storeId = storeId,
                    orderId = nextOrderId
                )
                
                val createdOrder = supabaseClient
                    .from("store_category_order")
                    .insert(newOrderRequest) {
                        select(Columns.ALL)
                    }
                    .decodeSingle<StoreCategoryOrder>()
                
                Log.i("ProductRepository", "Catégorie $categoryId ajoutée à l'ordre du magasin $storeId avec l'ordre $nextOrderId")
                Result.success(createdOrder)
                
            } catch (e: Exception) {
                Log.e("ProductRepository", "Erreur lors de l'ajout de la catégorie à l'ordre: ${e.message}")
                Result.failure(Exception("Impossible d'ajouter la catégorie à l'ordre. Veuillez réessayer."))
            }
        }
    }
    
    /**
     * Crée une catégorie et l'ajoute automatiquement à l'ordre d'un magasin
     */
    suspend fun createCategoryAndAddToStore(categoryName: String, storeId: Long): Result<Pair<Category, StoreCategoryOrder>> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Créer la catégorie
                val categoryResult = createCategory(categoryName)
                if (categoryResult.isFailure) {
                    return@withContext Result.failure(categoryResult.exceptionOrNull() ?: Exception("Erreur création catégorie"))
                }
                
                val createdCategory = categoryResult.getOrNull()!!
                
                // 2. L'ajouter à l'ordre du magasin
                val orderResult = addCategoryToStoreOrder(createdCategory.id, storeId)
                if (orderResult.isFailure) {
                    return@withContext Result.failure(orderResult.exceptionOrNull() ?: Exception("Erreur ajout ordre"))
                }
                
                val createdOrder = orderResult.getOrNull()!!
                
                Log.i("ProductRepository", "Catégorie '${createdCategory.name}' créée et ajoutée au magasin $storeId avec l'ordre ${createdOrder.orderId}")
                Result.success(Pair(createdCategory, createdOrder))
                
            } catch (e: Exception) {
                Log.e("ProductRepository", "Erreur lors de la création et ajout de catégorie: ${e.message}")
                Result.failure(Exception("Impossible de créer et ajouter la catégorie. Veuillez réessayer."))
            }
        }
    }
    
    /**
     * Récupère les produits d'un magasin groupés par catégorie, triés par ordre d'affichage
     */
    suspend fun getProductsGroupedByCategoryWithOrder(storeId: Long): Result<List<CategoryWithProducts>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.i("ProductRepository", "Chargement des produits groupés par catégorie pour le magasin $storeId")
                
                // 1. Récupérer toutes les catégories
                val allCategories = supabaseClient
                    .from("categories")
                    .select(columns = Columns.ALL)
                    .decodeList<Category>()
                
                // 2. Récupérer les ordres pour ce magasin
                val categoryOrders = supabaseClient
                    .from("store_category_order")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("store_id", storeId)
                        }
                    }
                    .decodeList<StoreCategoryOrder>()
                
                // 3. Récupérer tous les produits du magasin
                val allProducts = supabaseClient
                    .from("product")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("store_id", storeId)
                        }
                    }
                    .decodeList<Product>()
                
                // 4. Créer la map des catégories avec leurs ordres
                val categoryOrderMap = categoryOrders.associate { it.categoryId to it.orderId }
                
                // 5. Grouper les produits par catégorie
                val productsByCategory = allProducts.groupBy { it.categoryId }
                
                // 6. Créer les CategoryWithProducts seulement pour les catégories qui ont des produits
                val categoriesWithProducts = allCategories
                    .filter { category -> productsByCategory.containsKey(category.id) }
                    .map { category ->
                        val products = productsByCategory[category.id] ?: emptyList()
                        val orderId = categoryOrderMap[category.id]
                        
                        CategoryWithProducts(
                            id = category.id,
                            name = category.name,
                            orderId = orderId,
                            products = products.sortedBy { it.name } // Trier les produits par nom dans chaque catégorie
                        )
                    }
                
                // 7. Trier par ordre (catégories avec ordre en premier, puis par nom)
                val sortedCategories = categoriesWithProducts.sortedWith(
                    compareBy<CategoryWithProducts> { it.orderId ?: Int.MAX_VALUE }
                        .thenBy { it.name }
                )
                
                Log.i("ProductRepository", "Produits groupés: ${sortedCategories.size} catégories avec produits")
                sortedCategories.forEach { category ->
                    Log.i("ProductRepository", "Catégorie '${category.name}' (ordre: ${category.orderId}) → ${category.products.size} produits")
                }
                
                Result.success(sortedCategories)
                
            } catch (e: Exception) {
                Log.e("ProductRepository", "Erreur lors du chargement des produits groupés: ${e.message}")
                Result.failure(Exception("Impossible de charger les produits groupés. Veuillez réessayer."))
            }
        }
    }
    
    /**
     * Méthode de debug pour afficher l'état actuel des ordres en base
     */
    suspend fun debugCategoryOrders(storeId: Long): Result<List<StoreCategoryOrder>> {
        return withContext(Dispatchers.IO) {
            try {
                val categoryOrders = supabaseClient
                    .from("store_category_order")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("store_id", storeId)
                        }
                    }
                    .decodeList<StoreCategoryOrder>()
                
                Log.i("ProductRepository", "=== ÉTAT ACTUEL DES ORDRES POUR LE MAGASIN $storeId ===")
                categoryOrders.sortedBy { it.orderId }.forEach { order ->
                    Log.i("ProductRepository", "Catégorie ID ${order.categoryId} → Ordre ${order.orderId}")
                }
                Log.i("ProductRepository", "=== FIN DEBUG ORDRES ===")
                
                Result.success(categoryOrders)
                
            } catch (e: Exception) {
                Log.e("ProductRepository", "Erreur lors du debug des ordres: ${e.message}")
                Result.failure(Exception("Erreur debug"))
            }
        }
    }
} 