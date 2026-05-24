package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aaraksha_settings")

class PreferencesRepository(private val context: Context) {
    companion object {
        val PASSCODE_KEY = stringPreferencesKey("passcode")
        val PASSCODE_ENABLED_KEY = booleanPreferencesKey("passcode_enabled")
        val DARK_MODE_KEY = stringPreferencesKey("dark_mode") // "system", "light", "dark"
        val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")
    }

    val passcodeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PASSCODE_KEY] ?: "1234"
    }

    val passcodeEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PASSCODE_ENABLED_KEY] ?: true
    }

    val darkModeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: "system"
    }

    val biometricEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[BIOMETRIC_ENABLED_KEY] ?: false
    }

    suspend fun savePasscode(passcode: String) {
        context.dataStore.edit { preferences ->
            preferences[PASSCODE_KEY] = passcode
        }
    }

    suspend fun savePasscodeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PASSCODE_ENABLED_KEY] = enabled
        }
    }

    suspend fun saveDarkMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = mode
        }
    }

    suspend fun saveBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED_KEY] = enabled
        }
    }
}
