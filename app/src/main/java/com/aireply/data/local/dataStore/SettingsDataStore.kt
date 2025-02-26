package com.aireply.data.local.dataStore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.aireply.domain.models.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val READYCHAT_ACTIVE = booleanPreferencesKey("readychat_active")
        val READYCHAT_PROMPT = stringPreferencesKey("readychat_prompt")
        val DATE_ACTIVE = booleanPreferencesKey("date_active")
        val DATE_RANGE_START = stringPreferencesKey("date_range_start")
        val DATE_RANGE_END = stringPreferencesKey("date_range_end")
    }

    val settingsFlow: Flow<Settings> = dataStore.data.map { preferences ->
        Settings(
            readyChatActive = preferences[READYCHAT_ACTIVE] ?: false,
            readyChatPrompt = preferences[READYCHAT_PROMPT] ?: "",
            dateActive = preferences[DATE_ACTIVE] ?: false,
            dateRangeStart = preferences[DATE_RANGE_START] ?: "",
            dateRangeEnd = preferences[DATE_RANGE_END] ?: ""
        )
    }

    suspend fun updateReadyChatActive(isActive: Boolean) {
        dataStore.edit { preferences ->
            preferences[READYCHAT_ACTIVE] = isActive
        }
    }

    suspend fun updateReadyChatPrompt(prompt: String) {
        dataStore.edit { preferences ->
            preferences[READYCHAT_PROMPT] = prompt
        }
    }

    suspend fun updateDateActive(isActive: Boolean) {
        dataStore.edit { preferences ->
            preferences[DATE_ACTIVE] = isActive
        }
    }

    suspend fun updateDateRange(start: String, end: String) {
        dataStore.edit { preferences ->
            preferences[DATE_RANGE_START] = start
            preferences[DATE_RANGE_END] = end
        }
    }
}
