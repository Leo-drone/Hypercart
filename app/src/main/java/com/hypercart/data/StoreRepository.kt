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
                val ownerMember = CreateStoreMemberRequest(
                    storeId = createdStore.id,
                    userId = currentUser.id,
                    role = "owner"
                )
                
                try {
                    supabaseClient
                        .from("store_members")
                        .insert(ownerMember)
                        
                    Log.i("StoreRepository", "Membre owner créé pour le magasin ${createdStore.id}")
                } catch (e: Exception) {
                    Log.e("StoreRepository", "ERREUR CRITIQUE: Impossible de créer le membre owner: ${e.message}")
                    
                    // Si la création du membre échoue, supprimer le magasin créé
                    try {
                        supabaseClient
                            .from("store")
                            .delete {
                                filter {
                                    eq("id", createdStore.id)
                                }
                            }
                        Log.i("StoreRepository", "Magasin ${createdStore.id} supprimé suite à l'échec de création du membre")
                    } catch (cleanupException: Exception) {
                        Log.e("StoreRepository", "Erreur lors du nettoyage: ${cleanupException.message}")
                    }
                    
                    return@withContext Result.failure(Exception("Erreur lors de la configuration du magasin. Veuillez réessayer."))
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
                        .from("product")
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
    
    suspend fun isUserMemberOfStore(storeId: Long, userId: String? = null): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()
                    ?: return@withContext Result.failure(Exception("Vous devez être connecté"))
                
                val userIdToCheck = userId ?: currentUser.id
                
                val member = supabaseClient
                    .from("store_members")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("store_id", storeId)
                            eq("user_id", userIdToCheck)
                        }
                    }
                    .decodeSingleOrNull<StoreMember>()
                
                Log.i("StoreRepository", "Vérification membre: utilisateur $userIdToCheck dans magasin $storeId = ${member != null}")
                Result.success(member != null)
            } catch (e: Exception) {
                Log.e("StoreRepository", "Erreur lors de la vérification du membre: ${e.message}")
                Result.failure(Exception("Impossible de vérifier les permissions. Veuillez réessayer."))
            }
        }
    }
    
    suspend fun updateStoreName(storeId: Long, newName: String): Result<Store> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()
                    ?: return@withContext Result.failure(Exception("Vous devez être connecté pour modifier le magasin"))
                
                // Vérifier si l'utilisateur est propriétaire ou admin du magasin
                val member = supabaseClient
                    .from("store_members")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("store_id", storeId)
                            eq("user_id", currentUser.id)
                        }
                    }
                    .decodeSingleOrNull<StoreMember>()
                
                if (member == null || (member.role != "owner" && member.role != "admin")) {
                    return@withContext Result.failure(Exception("Vous n'avez pas les permissions pour modifier ce magasin"))
                }
                
                val updatedStore = supabaseClient
                    .from("store")
                    .update(mapOf("name" to newName)) {
                        filter {
                            eq("id", storeId)
                        }
                        select(Columns.ALL)
                    }
                    .decodeSingle<Store>()
                
                Log.i("StoreRepository", "Nom du magasin $storeId mis à jour: $newName")
                Result.success(updatedStore)
            } catch (e: Exception) {
                Log.e("StoreRepository", "Erreur lors de la mise à jour du nom: ${e.message}")
                Result.failure(Exception("Impossible de modifier le nom du magasin. Veuillez réessayer."))
            }
        }
    }
} 