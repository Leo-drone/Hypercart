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
                
                val productData = CreateProductRequest(
                    name = request.name,
                    categoryId = request.categoryId,
                    storeId = request.storeId
                )

                val createdProduct = supabaseClient
                    .from("product")
                    .insert(productData) {
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
} 