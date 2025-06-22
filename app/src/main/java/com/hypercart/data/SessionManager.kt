package com.hypercart.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {
    
    private val isLoggedInKey = booleanPreferencesKey("is_logged_in")
    private val userEmailKey = stringPreferencesKey("user_email")
    private val userIdKey = stringPreferencesKey("user_id")
    
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[isLoggedInKey] ?: false
    }
    
    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[userEmailKey]
    }
    
    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[userIdKey]
    }
    
    suspend fun saveSession(email: String, userId: String) {
        context.dataStore.edit { preferences ->
            preferences[isLoggedInKey] = true
            preferences[userEmailKey] = email
            preferences[userIdKey] = userId
        }
    }
    
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences[isLoggedInKey] = false
            preferences.remove(userEmailKey)
            preferences.remove(userIdKey)
        }
    }
} 