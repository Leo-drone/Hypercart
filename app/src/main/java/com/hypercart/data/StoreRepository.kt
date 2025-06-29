package com.hypercart.data

import android.util.Log
import com.hypercart.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class StoreRepository {
    private val supabaseClient = SupabaseConfig.client
    
    suspend fun createStore(request: CreateStoreRequest): Result<Store> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()
                    ?: return@withContext Result.failure(Exception("Vous devez être connecté pour créer un magasin"))
                
                val store = Store(
                    name = request.name
                )

                val createdStore = supabaseClient
                    .from("store")
                    .insert(store) {
                        select(Columns.ALL)
                    }
                    .decodeSingle<Store>()
                
                // Créer automatiquement le membre "owner" pour le créateur du magasin
                try {
                    val ownerMember = CreateStoreMemberRequest(
                        storeId = createdStore.id,
                        userId = currentUser.id,
                        role = "owner"
                    )
                    
                    supabaseClient
                        .from("store_members")
                        .insert(ownerMember)
                        
                    Log.i("StoreRepository", "Membre owner créé pour le magasin ${createdStore.id}")
                } catch (e: Exception) {
                    Log.w("StoreRepository", "Erreur lors de la création du membre owner: ${e.message}")
                    // On continue même si ça échoue, le magasin est créé
                }
                
                // Petit délai pour laisser Supabase se synchroniser
                delay(500)
                
                Result.success(createdStore)

            } catch (e: Exception) {
                Log.i("StoreRepository", "Erreur lors de la création du magasin: ${e.message}")
                Result.failure(Exception("Impossible de créer le magasin. Veuillez réessayer."))
            }
        }
    }
    
suspend fun getAllStores(): Result<List<Store>> {
    return withContext(Dispatchers.IO) {
        try {
            val currentUser = supabaseClient.auth.currentUserOrNull()
                ?: return@withContext Result.failure(Exception("Vous devez être connecté"))

            val stores = supabaseClient
                .from("store")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("created_by", currentUser.id)
                    }
                }
                .decodeList<Store>()
            
            Result.success(stores)
        } catch (e: Exception) {
            Log.e("StoreRepository", "Erreur lors du chargement des magasins: ${e.message}")
            Result.failure(Exception("Impossible de charger les magasins."))
        }
    }
}
    
    suspend fun getStoreById(storeId: Long): Result<Store?> {
        return withContext(Dispatchers.IO) {
            try {
                val store = supabaseClient
                    .from("store")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("id", storeId)
                        }
                    }
                    .decodeSingleOrNull<Store>()
                
                Result.success(store)
            } catch (e: Exception) {
                Result.failure(Exception("Impossible de charger le magasin. Veuillez réessayer."))
            }
        }
    }
    
    suspend fun deleteStore(storeId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()
                    ?: return@withContext Result.failure(Exception("Vous devez être connecté pour supprimer un magasin"))
                
                // Étape 1: Récupérer tous les paniers associés à ce magasin
                val carts = supabaseClient
                    .from("cart")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("store_id", storeId)
                        }
                    }
                    .decodeList<Cart>()
                
                // Étape 2: Supprimer tous les éléments des paniers
                for (cart in carts) {
                    try {
                        supabaseClient
                            .from("cart_items")
                            .delete {
                                filter {
                                    eq("cart_id", cart.id)
                                }
                            }
                        Log.i("StoreRepository", "Éléments du panier ${cart.id} supprimés")
                    } catch (e: Exception) {
                        Log.w("StoreRepository", "Erreur lors de la suppression des éléments du panier ${cart.id}: ${e.message}")
                    }
                }
                
                // Étape 3: Supprimer tous les paniers associés au magasin
                supabaseClient
                    .from("cart")
                    .delete {
                        filter {
                            eq("store_id", storeId)
                        }
                    }
                Log.i("StoreRepository", "Paniers du magasin $storeId supprimés")
                
                // Étape 4: Supprimer tous les produits associés au magasin
                try {
                    supabaseClient
                        .from("products")
                        .delete {
                            filter {
                                eq("store_id", storeId)
                            }
                        }
                    Log.i("StoreRepository", "Produits du magasin $storeId supprimés")
                } catch (e: Exception) {
                    Log.w("StoreRepository", "Erreur lors de la suppression des produits: ${e.message}")
                }
                
                // Étape 5: Supprimer tous les membres du magasin
                try {
                    supabaseClient
                        .from("store_members")
                        .delete {
                            filter {
                                eq("store_id", storeId)
                            }
                        }
                    Log.i("StoreRepository", "Membres du magasin $storeId supprimés")
                } catch (e: Exception) {
                    Log.w("StoreRepository", "Erreur lors de la suppression des membres: ${e.message}")
                }
                
                // Étape 6: Finalement, supprimer le magasin
                supabaseClient
                    .from("store")
                    .delete {
                        filter {
                            eq("id", storeId)
                        }
                    }
                
                Log.i("StoreRepository", "Magasin $storeId supprimé avec succès")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("StoreRepository", "Erreur lors de la suppression du magasin: ${e.message}")
                Result.failure(Exception("Impossible de supprimer le magasin. Veuillez réessayer."))
            }
        }
    }
    
    suspend fun addMemberToStore(storeId: Long, userId: String, role: String = "member"): Result<StoreMember> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()
                    ?: return@withContext Result.failure(Exception("Vous devez être connecté"))
                
                val memberRequest = CreateStoreMemberRequest(
                    storeId = storeId,
                    userId = userId,
                    role = role
                )
                
                val createdMember = supabaseClient
                    .from("store_members")
                    .insert(memberRequest) {
                        select(Columns.ALL)
                    }
                    .decodeSingle<StoreMember>()
                
                Result.success(createdMember)
            } catch (e: Exception) {
                Log.e("StoreRepository", "Erreur lors de l'ajout du membre: ${e.message}")
                Result.failure(Exception("Impossible d'ajouter le membre. Veuillez réessayer."))
            }
        }
    }
    
    suspend fun getStoreMembers(storeId: Long): Result<List<StoreMember>> {
        return withContext(Dispatchers.IO) {
            try {
                val members = supabaseClient
                    .from("store_members")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("store_id", storeId)
                        }
                    }
                    .decodeList<StoreMember>()
                
                Result.success(members)
            } catch (e: Exception) {
                Log.e("StoreRepository", "Erreur lors du chargement des membres: ${e.message}")
                Result.failure(Exception("Impossible de charger les membres. Veuillez réessayer."))
            }
        }
    }
    
    suspend fun removeMemberFromStore(storeId: Long, userId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("store_members")
                    .delete {
                        filter {
                            eq("store_id", storeId)
                            eq("user_id", userId)
                        }
                    }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("StoreRepository", "Erreur lors de la suppression du membre: ${e.message}")
                Result.failure(Exception("Impossible de supprimer le membre. Veuillez réessayer."))
            }
        }
    }
} 