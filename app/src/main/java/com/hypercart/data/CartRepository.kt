package com.hypercart.data

import android.util.Log
import com.hypercart.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CartRepository {
    private val supabaseClient = SupabaseConfig.client
    private val repository = Repository()
    
    suspend fun getOrCreateCart(storeId: Long): Result<Cart> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()
                    ?: return@withContext Result.failure(Exception("Vous devez être connecté"))

                Log.i("CartRepository", "Récupération du panier collaboratif pour le magasin: $storeId")

                // S'assurer que l'utilisateur existe dans la table users
                val userResult = repository.ensureUserExists()
                if (userResult.isFailure) {
                    return@withContext Result.failure(userResult.exceptionOrNull() ?: Exception("Erreur utilisateur"))
                }

                // Vérifier que l'utilisateur est membre du magasin
                val storeRepository = com.hypercart.data.StoreRepository()
                val membershipResult = storeRepository.isUserMemberOfStore(storeId, currentUser.id)
                if (membershipResult.isFailure || membershipResult.getOrNull() != true) {
                    return@withContext Result.failure(Exception("Vous n'êtes pas membre de ce magasin"))
                }

                // Chercher le panier unique du magasin (panier collaboratif)
                val existingCart = supabaseClient
                    .from("cart")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("store_id", storeId)
                        }
                    }
                    .decodeSingleOrNull<Cart>()
                
                if (existingCart != null) {
                    return@withContext Result.success(existingCart)
                }
                
                // Si aucun panier n'existe pour ce magasin, en créer un
                // Le premier membre à accéder au magasin crée le panier collaboratif
                val newCartData = InsertCartRequest(
                    storeId = storeId,
                    ownedBy = currentUser.id // Créateur initial, mais accessible à tous les membres
                )
                
                val createdCart = supabaseClient
                    .from("cart")
                    .insert(newCartData) {
                        select(Columns.ALL)
                    }
                    .decodeSingle<Cart>()
                
                Log.i("CartRepository", "Panier collaboratif créé pour le magasin $storeId")
                Result.success(createdCart)
                
            } catch (e: Exception) {
                Log.e("CartRepository", "Erreur lors de la récupération/création du panier: ${e.message}")
                Result.failure(Exception("Erreur avec le panier. Veuillez réessayer."))
            }
        }
    }
    
    suspend fun addItemToCart(cartId: Long, request: AddToCartRequest): Result<CartItem> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()
                    ?: return@withContext Result.failure(Exception("Vous devez être connecté"))
                
                // Vérifier si l'item existe déjà dans le panier
                val existingItem = supabaseClient
                    .from("cart_items")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("cart_id", cartId)
                            eq("product_id", request.productId)
                        }
                    }
                    .decodeSingleOrNull<CartItem>()
                
                if (existingItem != null) {
                    
                    // throw error
                    Result.failure(Exception("Ce produit est déjà dans le panier."))
                } else {
                    // Créer un nouvel item
                    val newItemData = InsertCartItemRequest(
                        productId = request.productId,
                        quantity = request.quantity,
                        cartId = cartId,
                    )
                    
                    val createdItem = supabaseClient
                        .from("cart_items")
                        .insert(newItemData) {
                            select(Columns.ALL)
                        }
                        .decodeSingle<CartItem>()
                    
                    Result.success(createdItem)
                }
                
            } catch (e: Exception) {
                Log.e("CartRepository", "Erreur lors de l'ajout au panier: ${e.message}")
                Result.failure(Exception("Impossible d'ajouter au panier. Veuillez réessayer."))
            }
        }
    }
    
    suspend fun getCartWithItems(cartId: Long): Result<Cart> {
        return withContext(Dispatchers.IO) {
            try {
                // Récupérer le panier
                val cart = supabaseClient
                    .from("cart")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("id", cartId)
                        }
                    }
                    .decodeSingle<Cart>()
                
                // Récupérer les items du panier
                val items = supabaseClient
                    .from("cart_items")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("cart_id", cartId)
                        }
                    }
                    .decodeList<CartItem>()
                
                val cartWithItems = cart.copy(items = items)
                Result.success(cartWithItems)
                
            } catch (e: Exception) {
                Log.e("CartRepository", "Erreur lors de la récupération du panier: ${e.message}")
                Result.failure(Exception("Impossible de charger le panier. Veuillez réessayer."))
            }
        }
    }
    
    suspend fun updateCartItemQuantity(itemId: Long, newQuantity: Int): Result<CartItem> {
        return withContext(Dispatchers.IO) {
            try {
                if (newQuantity <= 0) {
                    // Supprimer l'item si la quantité est 0 ou négative
                    supabaseClient
                        .from("cart_items")
                        .delete {
                            filter {
                                eq("id", itemId)
                            }
                        }
                    
                    return@withContext Result.failure(Exception("Item supprimé du panier"))
                }
                
                val updatedItem = supabaseClient
                    .from("cart_items")
                    .update(UpdateQuantityRequest(quantity = newQuantity)) {
                        filter {
                            eq("id", itemId)
                        }
                        select(Columns.ALL)
                    }
                    .decodeSingle<CartItem>()
                
                Result.success(updatedItem)
                
            } catch (e: Exception) {
                Log.e("CartRepository", "Erreur lors de la mise à jour de la quantité: ${e.message}")
                Result.failure(Exception("Impossible de mettre à jour la quantité. Veuillez réessayer."))
            }
        }
    }
    
    suspend fun removeItemFromCart(itemId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("cart_items")
                    .delete {
                        filter {
                            eq("id", itemId)
                        }
                    }
                
                Result.success(Unit)
                
            } catch (e: Exception) {
                Log.e("CartRepository", "Erreur lors de la suppression de l'item: ${e.message}")
                Result.failure(Exception("Impossible de supprimer l'item. Veuillez réessayer."))
            }
        }
    }
    
    suspend fun clearCart(cartId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("cart_items")
                    .delete {
                        filter {
                            eq("cart_id", cartId)
                        }
                    }
                
                Result.success(Unit)
                
            } catch (e: Exception) {
                Log.e("CartRepository", "Erreur lors du vidage du panier: ${e.message}")
                Result.failure(Exception("Impossible de vider le panier. Veuillez réessayer."))
            }
        }
    }
} 