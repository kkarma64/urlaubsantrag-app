package de.bauhaupt.urlaubsschein.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appDataStore by preferencesDataStore(name = "app_preferences")

class AppPreferencesRepository(private val context: Context) {
    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val PASSWORD_HASH = stringPreferencesKey("password_hash")
        val EMPFAENGER_EMAIL = stringPreferencesKey("empfaenger_email")
        val BETREFF_FORMAT = stringPreferencesKey("betreff_format")
        val DATEINAME_FORMAT = stringPreferencesKey("dateiname_format")
        val LOCK_TIMEOUT = longPreferencesKey("lock_timeout")
    }

    val onboardingAbgeschlossen: Flow<Boolean> = context.appDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }

    val passwortHash: Flow<String?> = context.appDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PASSWORD_HASH]
        }

    val empfaengerEmail: Flow<String?> = context.appDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.EMPFAENGER_EMAIL] ?: "lohnbuero@nameunternehmen.de"
        }

    val betreffFormat: Flow<String> = context.appDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.BETREFF_FORMAT] ?: "Urlaubsschein {Name} {Zeitraum}"
        }

    val dateinameFormat: Flow<String> = context.appDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DATEINAME_FORMAT] ?: "Urlaubsschein_{Name}_{Datum}.pdf"
        }

    val lockTimeout: Flow<Long> = context.appDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LOCK_TIMEOUT] ?: 60000L
        }

    suspend fun setOnboardingAbgeschlossen(abgeschlossen: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = abgeschlossen
        }
    }

    suspend fun setPasswortHash(hash: String) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.PASSWORD_HASH] = hash
        }
    }

    suspend fun setEmpfaengerEmail(email: String) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.EMPFAENGER_EMAIL] = email
        }
    }

    suspend fun setBetreffFormat(format: String) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.BETREFF_FORMAT] = format
        }
    }

    suspend fun setDateinameFormat(format: String) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.DATEINAME_FORMAT] = format
        }
    }

    suspend fun setLockTimeout(timeoutMs: Long) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCK_TIMEOUT] = timeoutMs
        }
    }
}
