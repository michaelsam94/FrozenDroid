package com.michael.frozendroid.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "frozendroid_settings")

@Singleton
class UserPreferencesManager @Inject constructor(
    private val context: Context
) {
    companion object {
        val ACTIVE_PROFILE_ID = stringPreferencesKey("active_profile_id")
        val PRIVILEGE_MODE = stringPreferencesKey("privilege_mode") // "SHIZUKU", "ADB", "DEGRADED"
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val MONITORING_INTERVAL_MINUTES = intPreferencesKey("monitoring_interval_minutes")
        val THEME = stringPreferencesKey("theme") // "DARK", "LIGHT", "SYSTEM"
    }

    val activeProfileIdFlow: Flow<String?> = context.dataStore.data.map { it[ACTIVE_PROFILE_ID] }
    val privilegeModeFlow: Flow<String> = context.dataStore.data.map { it[PRIVILEGE_MODE] ?: "DEGRADED" }
    val onboardingCompleteFlow: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_COMPLETE] ?: false }
    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: true }
    val monitoringIntervalMinutesFlow: Flow<Int> = context.dataStore.data.map { it[MONITORING_INTERVAL_MINUTES] ?: 15 }
    val themeFlow: Flow<String> = context.dataStore.data.map { it[THEME] ?: "SYSTEM" }

    suspend fun setActiveProfileId(id: String?) {
        context.dataStore.edit { prefs ->
            if (id == null) {
                prefs.remove(ACTIVE_PROFILE_ID)
            } else {
                prefs[ACTIVE_PROFILE_ID] = id
            }
        }
    }

    suspend fun setPrivilegeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[PRIVILEGE_MODE] = mode
        }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETE] = complete
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setMonitoringIntervalMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[MONITORING_INTERVAL_MINUTES] = minutes
        }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME] = theme
        }
    }
}
