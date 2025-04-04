package com.readychat.smsbase.presentation.viewmodel.util

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PermissionManager(private val context: Context) {

    private val _permissionsState = MutableStateFlow(checkPermissions())
    val permissionsState: StateFlow<PermissionsState> = _permissionsState.asStateFlow()

    data class PermissionsState(
        val isDefaultSmsApp: Boolean,
        val hasContactPermission: Boolean
    ) {
        val allPermissionsGranted: Boolean
            get() = isDefaultSmsApp && hasContactPermission
    }

    fun checkPermissions(): PermissionsState {
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
        val isDefaultSmsApp = roleManager.isRoleHeld(RoleManager.ROLE_SMS)

        val contactPermissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        return PermissionsState(isDefaultSmsApp, contactPermissionGranted)
    }

    fun refreshPermissionsState() {
        _permissionsState.value = checkPermissions()
    }
}