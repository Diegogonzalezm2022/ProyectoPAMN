package com.ulpgc.proyectopamn.adapters.primary.ui.user

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ulpgc.proyectopamn.adapters.primary.ui.auth.rememberImageBitmapFromUri
import com.ulpgc.proyectopamn.domain.model.AuthResult
import com.ulpgc.proyectopamn.domain.model.UserProfile
import com.ulpgc.proyectopamn.domain.ports.input.AuthInputPort
import kotlinx.coroutines.launch

/**
 * Pestaña de Usuario - Adaptador Primario UI
 *
 * Muestra la información del usuario incluyendo la imagen de perfil cargada de la galería,
 * permite su edición mediante selector de galería y ofrece opciones para cerrar sesión y borrar cuenta.
 * Sigue los criterios de accesibilidad WCAG 2.1 y Material Design 3.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userProfile: UserProfile,
    authInputPort: AuthInputPort,
    onLogout: () -> Unit,
    onAccountDeleted: () -> Unit,
    onProfileUpdated: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }

    var username by remember(userProfile) { mutableStateOf(userProfile.username) }
    var email by remember(userProfile) { mutableStateOf(userProfile.email) }
    var phone by remember(userProfile) { mutableStateOf(userProfile.phone) }
    var photoUrl by remember(userProfile) { mutableStateOf(userProfile.photoUrl) }
    var description by remember(userProfile) { mutableStateOf(userProfile.description) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Selector de imágenes de la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUrl = it.toString()
        }
    }

    val profileBitmap = rememberImageBitmapFromUri(context, photoUrl)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Usuario") },
                actions = {
                    IconButton(
                        onClick = { isEditing = !isEditing },
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .semantics { contentDescription = if (isEditing) "Cancelar edición" else "Editar perfil" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar de Perfil
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .then(
                        if (isEditing) Modifier.clickable { galleryLauncher.launch("image/*") } else Modifier
                    )
                    .semantics { contentDescription = "Foto de perfil de usuario" },
                contentAlignment = Alignment.Center
            ) {
                if (profileBitmap != null) {
                    Image(
                        bitmap = profileBitmap,
                        contentDescription = "Foto de perfil cargada de la galería",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Foto de perfil por defecto",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (isEditing) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(if (photoUrl.isBlank()) "Seleccionar foto de galería" else "Cambiar foto de galería")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .semantics { contentDescription = "Error de actualización: $error" }
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (!isEditing) {
                // Modo Lectura
                Text(
                    text = username.ifBlank { "Sin nombre" },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (email.isNotBlank()) {
                    Text(
                        text = "Email: $email",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (phone.isNotBlank()) {
                    Text(
                        text = "Teléfono: $phone",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = description.ifBlank { "Sin descripción disponible." },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                // Modo Edición
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nombre de usuario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            val updatedUser = userProfile.copy(
                                username = username,
                                email = email,
                                phone = phone,
                                photoUrl = photoUrl,
                                description = description
                            )
                            val result = authInputPort.updateProfile(updatedUser)
                            isLoading = false
                            when (result) {
                                is AuthResult.Success -> {
                                    isEditing = false
                                    onProfileUpdated(result.data)
                                }
                                is AuthResult.Error -> errorMessage = result.message
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) {
                    Text("Guardar Cambios")
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // Opciones de la parte baja de la pestaña de usuario
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Botón de Cerrar Sesión
            OutlinedButton(
                onClick = {
                    scope.launch {
                        authInputPort.logout()
                        onLogout()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(bottom = 12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Cerrar Sesión")
            }

            // Botón de Borrar Cuenta
            Button(
                onClick = { showDeleteConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Borrar Cuenta")
            }
        }
    }

    // Diálogo de Confirmación para Borrar Cuenta
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Borrar Cuenta") },
            text = { Text("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        isLoading = true
                        scope.launch {
                            val result = authInputPort.deleteAccount()
                            isLoading = false
                            when (result) {
                                is AuthResult.Success -> onAccountDeleted()
                                is AuthResult.Error -> errorMessage = result.message
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
