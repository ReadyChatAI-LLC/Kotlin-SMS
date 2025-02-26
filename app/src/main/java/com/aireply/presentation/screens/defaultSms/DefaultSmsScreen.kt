package com.aireply.presentation.screens.defaultSms

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.aireply.presentation.screens.chatList.SmsUiState

@Composable
fun DefaultSmsScreen(
    viewModel: DefaultSmsViewModel = hiltViewModel(),
    onSetDefaultApp: () -> Unit
) {

    val isDefaultApp by viewModel.isDefaultApp
    val contactPermissionGranted by viewModel.contactPermissionGranted

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
        if (roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
            Toast.makeText(context, "App predeterminada", Toast.LENGTH_SHORT).show()
            Log.d("prueba", "Usuario otorgo permisos")
            viewModel.updateIsDefaultApp(true)
        } else {
            Toast.makeText(context, "Configuración omitida", Toast.LENGTH_SHORT).show()
            Log.d("prueba", "Usuario rechazo permisos")
        }
    }

    val launcherContacts = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.d("prueba", "Acceso a contactos: $isGranted")
        viewModel.updateContactPermissionGranted(isGranted)
    }

    LaunchedEffect(Unit) {
        val packageName = context.packageName
        val isDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context) == packageName
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

    if(isDefaultApp && contactPermissionGranted){
        Log.d("prueba", "Permisos concedidos ($isDefaultApp - $contactPermissionGranted), a navegar")
        onSetDefaultApp()
        viewModel.updateIsDefaultApp(false)
        viewModel.updateContactPermissionGranted(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Sms,
            contentDescription = "SMS Icon",
            tint = Color(0xFF007AFF),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Para usar todas las funciones, selecciona esta aplicación como tu app de SMS predeterminada y acepta el permiso de acceso a tus contactos.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.onRequestRoleClicked() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
            enabled = !isDefaultApp
        ) {
            Text(text = "Establecer como predeterminada", color = Color.White)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = { launcherContacts.launch(Manifest.permission.READ_CONTACTS) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
            enabled = !contactPermissionGranted
        ) {
            Text(text = "Acceso a contactos", color = Color.White)
        }
    }
}
