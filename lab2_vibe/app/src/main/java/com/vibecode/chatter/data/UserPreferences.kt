package com.vibecode.chatter.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    private val usernameKey = stringPreferencesKey("last_username")

    val lastUsername: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[usernameKey].orEmpty()
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { prefs ->
            prefs[usernameKey] = username
        }
    }
}
