package com.aireply.presentation.screens.settings

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aireply.data.local.dataStore.SettingsDataStore
import com.aireply.domain.models.Settings
import com.aireply.util.TimeRangeValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    //private val settingsDataStore: SettingsDataStore
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _settingsState = mutableStateOf(
        Settings(
            readyChatActive = false,
            readyChatPrompt = "",
            dateActive = false,
            dateRangeStart = "",
            dateRangeEnd = ""
        )
    )
    val settingsState: State<Settings> get() = _settingsState

    init {
        viewModelScope.launch {
            settingsDataStore.settingsFlow.collect { settings ->
                _settingsState.value = settings
            }
        }
    }

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
        viewModelScope.launch {
            if (_settingsState.value.dateActive) {
                val validTimeRange = TimeRangeValidator.isValidTimeRange(
                    _settingsState.value.dateRangeStart,
                    _settingsState.value.dateRangeEnd
                )
                if (!validTimeRange) {
                    Log.e("prueba", "El rango de horas es inválido: la hora de inicio debe ser anterior a la hora de fin.")
                    return@launch
                }
            }
            settingsDataStore.updateReadyChatActive(_settingsState.value.readyChatActive)
            settingsDataStore.updateReadyChatPrompt(_settingsState.value.readyChatPrompt)
            settingsDataStore.updateDateActive(_settingsState.value.dateActive)
            settingsDataStore.updateDateRange(
                _settingsState.value.dateRangeStart,
                _settingsState.value.dateRangeEnd
            )
        }
    }
}