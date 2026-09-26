package com.plazaorbita.app.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "plaza_orbita_session")

class SessionManager(private val context: Context) {

    companion object {
        val TOKEN_KEY = stringPreferencesKey("token")
        val ROLE_KEY = stringPreferencesKey("role")
        val NAME_KEY = stringPreferencesKey("name")
        val USER_ID_KEY = longPreferencesKey("userId")
    }

    suspend fun saveSession(token: String, userId: Long, name: String, role: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
            prefs[NAME_KEY] = name
            prefs[ROLE_KEY] = role
        }
    }

    suspend fun getRole(): String? = context.dataStore.data.first()[ROLE_KEY]

    suspend fun getName(): String? = context.dataStore.data.first()[NAME_KEY]
    suspend fun getUserId(): Long? = context.dataStore.data.first()[USER_ID_KEY]
    suspend fun getToken(): String? = context.dataStore.data.first()[TOKEN_KEY]

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
