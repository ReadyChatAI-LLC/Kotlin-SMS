package com.readychat.smsbase.presentation.screens.defaultSms

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DefaultSmsScreen(
    viewModel: DefaultSmsViewModel = hiltViewModel(),
    onSetDefaultApp: () -> Unit
) {

    val isDefaultApp by viewModel.isDefaultApp
    val contactPermissionGranted by viewModel.contactPermissionGranted
    val isEnabledToNavigate by viewModel.isEnabledToNavigate

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
        if (roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
            viewModel.updateIsDefaultApp(true)
        }
    }

    val launcherContacts = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        viewModel.updateContactPermissionGranted(isGranted)
    }

    LaunchedEffect(Unit) {
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
        val isDefaultSmsApp = roleManager.isRoleHeld(RoleManager.ROLE_SMS)

        val contactPermissionGrantedApp = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        viewModel.updateIsDefaultApp(isDefaultSmsApp)
        viewModel.updateContactPermissionGranted(contactPermissionGrantedApp)
    }

    LaunchedEffect(Unit) {
        viewModel.requestRoleEvent.collect {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (roleManager.isRoleAvailable(RoleManager.ROLE_SMS)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                launcher.launch(intent)
            }
        }
    }

    if (isEnabledToNavigate) {
        Toast.makeText(context, "Permissions Granted", Toast.LENGTH_SHORT).show()
        onSetDefaultApp()
        viewModel.navigationWasExecuted()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Sms,
            contentDescription = "SMS Icon",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(90.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "To use all features, set this app as your default SMS app and grant access to your contacts.",
            style = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                letterSpacing = 0.2.sp,
                fontSize = 14.sp
            ),
            textAlign = TextAlign.Center,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        DefaultSmsButtons(
            textButton = "Default SMS app",
            isEnabledButton = !isDefaultApp
        ) {
            viewModel.onRequestRoleClicked()
        }

        Spacer(modifier = Modifier.height(14.dp))

        DefaultSmsButtons(
            textButton = "Access to Contacts",
            isEnabledButton = !contactPermissionGranted
        ) {
            launcherContacts.launch(Manifest.permission.READ_CONTACTS)
        }
    }
}

@Composable
fun DefaultSmsButtons(textButton: String, isEnabledButton: Boolean, onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface),
        enabled = isEnabledButton
    ) {
        Text(text = textButton, color = MaterialTheme.colorScheme.surface, fontSize = 18.sp)
    }
}