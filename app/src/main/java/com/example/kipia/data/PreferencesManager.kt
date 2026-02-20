package com.example.kipia.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val MUTE_SPLASH = booleanPreferencesKey("mute_splash")
        val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
        val DEVICE_ID = stringPreferencesKey("device_id")
        val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
        val AUTO_SYNC = booleanPreferencesKey("auto_sync")
    }

    // Существующие настройки
    val muteSplashFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.MUTE_SPLASH] ?: false
        }

    suspend fun setMuteSplash(mute: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MUTE_SPLASH] = mute
        }
    }

    // Новые настройки синхронизации
    val lastSyncTimestamp: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIMESTAMP] ?: 0L
        }

    val deviceId: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEVICE_ID] ?: generateDeviceId()
        }

    val syncEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SYNC_ENABLED] ?: true
        }

    val autoSync: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.AUTO_SYNC] ?: false
        }

    suspend fun setLastSyncTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIMESTAMP] = timestamp
        }
    }

    suspend fun setSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SYNC_ENABLED] = enabled
        }
    }

    suspend fun setAutoSync(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SYNC] = enabled
        }
    }

    private suspend fun generateDeviceId(): String {
        val newId = "DEV_${System.currentTimeMillis()}_${(1000..9999).random()}"
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEVICE_ID] = newId
        }
        return newId
    }
}