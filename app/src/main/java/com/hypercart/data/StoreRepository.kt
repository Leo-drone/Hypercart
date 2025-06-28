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
                val stores = supabaseClient
                    .from("store")
                    .select(columns = Columns.ALL)
                    .decodeList<Store>()
                
                Result.success(stores)
            } catch (e: Exception) {
                Result.failure(Exception("Impossible de charger les magasins. Veuillez réessayer."))
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
                
                supabaseClient
                    .from("store")
                    .delete {
                        filter {
                            eq("id", storeId)
                        }
                    }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Impossible de supprimer le magasin. Veuillez réessayer."))
            }
        }
    }
} 