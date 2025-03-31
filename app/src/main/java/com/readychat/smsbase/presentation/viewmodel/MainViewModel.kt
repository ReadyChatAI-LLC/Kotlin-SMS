package com.readychat.smsbase.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readychat.smsbase.presentation.viewmodel.util.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _shouldShowPermissionScreen = MutableStateFlow(false)
    val shouldShowPermissionScreen: StateFlow<Boolean> = _shouldShowPermissionScreen.asStateFlow()

    init {
        viewModelScope.launch {
            permissionManager.permissionsState.collect { permissionsState ->
                _shouldShowPermissionScreen.value = !permissionsState.allPermissionsGranted
            }
        }
    }

    fun checkPermissions() {
        permissionManager.refreshPermissionsState()
    }
}