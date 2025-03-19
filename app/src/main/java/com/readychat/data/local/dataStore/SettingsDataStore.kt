package com.readychat.data.local.dataStore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val APP_STARTED = booleanPreferencesKey("app_started")
    }

    val appStartedFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[APP_STARTED] ?: false
    }

    suspend fun setAppStarted(started: Boolean) {
        dataStore.edit { preferences ->
            preferences[APP_STARTED] = started
        }
    }
}
