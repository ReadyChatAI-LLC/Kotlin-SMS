package com.readychat.presentation.screens.settings

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.readychat.domain.models.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    //private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _settingsState = mutableStateOf(
        Settings(
            readyChatActive = false,
            readyChatPrompt = "",
            dateActive = false,
            dateRangeStart = "",
            dateRangeEnd = "",
            appStarted = false
        )
    )
    val settingsState: State<Settings> get() = _settingsState

    fun onReadyChatActiveChanged(newValue: Boolean) {
        _settingsState.value = _settingsState.value.copy(readyChatActive = newValue)
    }

    fun onReadyChatPromptChanged(newPrompt: String) {
        _settingsState.value = _settingsState.value.copy(readyChatPrompt = newPrompt)
    }

    fun onDateActiveChanged(newValue: Boolean) {
        _settingsState.value = _settingsState.value.copy(dateActive = newValue)
    }

    fun onDateRangeStartChanged(newStart: String) {
        _settingsState.value = _settingsState.value.copy(dateRangeStart = newStart)
    }

    fun onDateRangeEndChanged(newEnd: String) {
        _settingsState.value = _settingsState.value.copy(dateRangeEnd = newEnd)
    }

    fun saveSettings() {
        Log.d("prueba", "SettingsViewModel -> saveSettings method. Nothing will happen")
    }
}