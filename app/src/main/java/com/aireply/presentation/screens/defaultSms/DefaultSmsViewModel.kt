package com.aireply.presentation.screens.defaultSms

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DefaultSmsViewModel @Inject constructor() : ViewModel() {
    private val _requestRoleEvent = MutableSharedFlow<Unit>()
    val requestRoleEvent = _requestRoleEvent.asSharedFlow()

    private val _isDefaultApp = mutableStateOf(false)
    val isDefaultApp: State<Boolean> get() = _isDefaultApp

    private val _contactPermissionGranted = mutableStateOf(false)
    val contactPermissionGranted: State<Boolean> get() = _contactPermissionGranted

    private val _isEnabledToNavigate = mutableStateOf(false)
    val isEnabledToNavigate: State<Boolean> get() = _isEnabledToNavigate

    fun onRequestRoleClicked() {
        viewModelScope.launch {
            _requestRoleEvent.emit(Unit)
        }
    }

    fun updateIsDefaultApp(isDefaultApp: Boolean){
        Log.d("prueba", "Uptading IsDefaultApp: $isDefaultApp")
        _isDefaultApp.value = isDefaultApp
        checkIsEnabledToNavigate()
    }

    fun updateContactPermissionGranted(contactPermissionGranted: Boolean){
        Log.d("prueba", "Uptading contactPermissionGranted: $contactPermissionGranted")
        _contactPermissionGranted.value = contactPermissionGranted
        checkIsEnabledToNavigate()
    }

    private fun checkIsEnabledToNavigate(){
        _isEnabledToNavigate.value = _isDefaultApp.value && _contactPermissionGranted.value
    }

    fun navigationWasExecuted(){
        _isEnabledToNavigate.value = false
    }
}