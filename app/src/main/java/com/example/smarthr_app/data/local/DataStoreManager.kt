package com.example.smarthr_app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.smarthr_app.data.model.User
import com.example.smarthr_app.data.model.UserRole
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreManager(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_prefs")
        private val USER_KEY = stringPreferencesKey("user")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val PENDING_COMPANY_CODE_KEY = stringPreferencesKey("pending_company_code")
    }

    private val gson = Gson()

    suspend fun saveUser(user: User) {
        context.dataStore.edit { preferences ->
            preferences[USER_KEY] = gson.toJson(user)
            preferences[IS_LOGGED_IN_KEY] = true
        }
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun savePendingCompanyCode(companyCode: String?) {
        context.dataStore.edit { preferences ->
            if (companyCode != null) {
                preferences[PENDING_COMPANY_CODE_KEY] = companyCode
            } else {
                preferences.remove(PENDING_COMPANY_CODE_KEY)
            }
        }
    }

    val user: Flow<User?> = context.dataStore.data.map { preferences ->
        val userJson = preferences[USER_KEY]
        if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else null
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN_KEY] ?: false
    }

    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    val pendingCompanyCode: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PENDING_COMPANY_CODE_KEY]
    }

    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}