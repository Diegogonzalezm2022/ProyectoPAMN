package com.ulpgc.proyectopamn.adapters.primary.ui.editor

import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulpgc.proyectopamn.R
import com.ulpgc.proyectopamn.adapters.primary.ui.utils.rememberImageBitmapFromUri
import com.ulpgc.proyectopamn.config.AppDependencyContainer
import com.ulpgc.proyectopamn.domain.model.Product
import com.ulpgc.proyectopamn.ui.theme.ProyectoPAMNTheme
import java.io.File

/**
 * Pantalla del editor con su ViewModel.
 *
 * @param product el producto a editar, o `null` para crear uno nuevo.
 * @param viewModelKey distinto cada vez que se abre el editor, para estrenar ViewModel
 *   (si no, al abrirlo otra vez saldría lo que escribiste la vez anterior).
 * @param onClose salir sin guardar.
 * @param onSaved se ha guardado bien; recibe el mensaje a enseñar ("Producto publicado").
 */
@Composable
fun ProductEditorScreen(
    product: Product?,
    viewModelKey: String,
    onClose: () -> Unit,
    onSaved: (messageRes: Int) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: ProductEditorViewModel = viewModel(
        key = viewModelKey,
        factory = AppDependencyContainer.getProductEditorViewModelFactory(context, product),
    )
    val state = viewModel.uiState

    // Cuando el ViewModel dice "guardado", la pantalla avisa hacia fuera y se cierra.
    LaunchedEffect(state.savedMessageRes) {
        state.savedMessageRes?.let(onSaved)
    }

    ProductEditorContent(
        state = state,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onPriceChange = viewModel::onPriceChange,
        onImagesChange = viewModel::onImagesChange,
        onSave = viewModel::onSave,
        onClose = onClose,
    )
}

/** Solo pinta el formulario. No conoce el ViewModel: así se puede previsualizar. */
@Composable
fun ProductEditorContent(
    state: ProductEditorUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onImagesChange: (List<String>) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Selector de Múltiples Imágenes desde Galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onImagesChange(state.images + uris.map { it.toString() })
        }
    }

    // Captura de Foto con Cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            onImagesChange(state.images + listOf(tempCameraUri.toString()))
        }
    }

    fun launchCamera() {
        try {
            val photoFile = File(context.cacheDir, "product_cam_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (_: Exception) {}
    }

    // Lanzador de petición de permiso de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        }
    }

    // Salir (flecha o botón atrás del móvil): si hay cambios, se pregunta antes.
    val requestClose = {
        if (state.hasChanges && !state.isSaving) showDiscardDialog = true else onClose()
    }
    BackHandler(onBack = requestClose)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = requestClose) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.editor_back),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(if (state.isEditing) R.string.editor_title_edit else R.string.editor_title_new),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() },
            )
        }

        EditorField(
            value = state.title,
            onValueChange = onTitleChange,
            labelRes = R.string.editor_field_title,
            hintRes = R.string.editor_field_title_hint,
            errorRes = state.titleErrorRes,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
            ),
            singleLine = true,
        )

        EditorField(
            value = state.priceText,
            onValueChange = onPriceChange,
            labelRes = R.string.editor_field_price,
            hintRes = R.string.editor_field_price_hint,
            errorRes = state.priceErrorRes,
            // Teclado numérico con coma: menos posibilidades de escribir algo inválido.
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            singleLine = true,
            suffix = { Text(stringResource(R.string.editor_price_suffix)) },
        )

        EditorField(
            value = state.description,
            onValueChange = onDescriptionChange,
            labelRes = R.string.editor_field_description,
            hintRes = R.string.editor_field_description_hint,
            errorRes = null,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            singleLine = false,
            minLines = 4,
        )

        // --- Sección de Gestión de Imágenes Ordenadas ---
        Text(
            text = "Imágenes del producto (Lista ordenada)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = "La primera imagen (#1) será la Imagen Principal (Portada) en el catálogo.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Galería")
            }

            OutlinedButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        launchCamera()
                    } else {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
            ) {
                Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Cámara")
            }
        }

        if (state.images.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(state.images) { index, imgUri ->
                    val isMain = (index == 0)
                    val bitmap = rememberImageBitmapFromUri(context, imgUri)

                    Card(
                        modifier = Modifier
                            .width(140.dp)
                            .then(
                                if (isMain) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)) else Modifier
                            ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Surface(
                                color = if (isMain) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = if (isMain) "⭐ Principal" else "#${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isMain) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(2.dp),
                                    maxLines = 1
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = "Foto ${index + 1}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(
                                    onClick = {
                                        if (index > 0) {
                                            val mutable = state.images.toMutableList()
                                            val temp = mutable[index]
                                            mutable[index] = mutable[index - 1]
                                            mutable[index - 1] = temp
                                            onImagesChange(mutable)
                                        }
                                    },
                                    enabled = index > 0,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mover a la izquierda")
                                }

                                IconButton(
                                    onClick = {
                                        val mutable = state.images.toMutableList()
                                        mutable.removeAt(index)
                                        onImagesChange(mutable)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }

                                IconButton(
                                    onClick = {
                                        if (index < state.images.size - 1) {
                                            val mutable = state.images.toMutableList()
                                            val temp = mutable[index]
                                            mutable[index] = mutable[index + 1]
                                            mutable[index + 1] = temp
                                            onImagesChange(mutable)
                                        }
                                    },
                                    enabled = index < state.images.size - 1,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mover a la derecha")
                                }
                            }
                        }
                    }
                }
            }
        }

        state.saveErrorRes?.let { errorRes ->
            Text(
                text = stringResource(errorRes),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }

        // Desactivado mientras el formulario no es válido o se está guardando
        // (evita errores y que se publique dos veces por pulsar rápido).
        Button(
            onClick = onSave,
            enabled = state.canSave && !state.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.editor_saving))
            } else {
                Text(stringResource(if (state.isEditing) R.string.editor_save_edit else R.string.editor_save_new))
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.editor_discard_title)) },
            text = { Text(stringResource(R.string.editor_discard_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onClose()
                }) { Text(stringResource(R.string.editor_discard_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.editor_discard_dismiss))
                }
            },
        )
    }
}

/**
 * Un campo del formulario: etiqueta siempre visible, pista debajo y,
 * si hay error, el error sustituye a la pista (en rojo y anunciado por TalkBack).
 */
@Composable
private fun EditorField(
    value: String,
    onValueChange: (String) -> Unit,
    labelRes: Int,
    hintRes: Int,
    errorRes: Int?,
    keyboardOptions: KeyboardOptions,
    singleLine: Boolean,
    minLines: Int = 1,
    suffix: (@Composable () -> Unit)? = null,
) {
    val errorText = errorRes?.let { stringResource(it) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelRes)) },
        supportingText = { Text(errorText ?: stringResource(hintRes)) },
        isError = errorText != null,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        minLines = minLines,
        suffix = suffix,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { if (errorText != null) error(errorText) },
    )
}

@Preview(showBackground = true)
@Composable
private fun ProductEditorNewPreview() {
    ProyectoPAMNTheme {
        ProductEditorContent(
            state = ProductEditorUiState(),
            onTitleChange = {}, onDescriptionChange = {}, onPriceChange = {}, onImagesChange = {}, onSave = {}, onClose = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductEditorErrorsPreview() {
    ProyectoPAMNTheme {
        ProductEditorContent(
            state = ProductEditorUiState(
                isEditing = true,
                title = "",
                priceText = "12,555",
                titleErrorRes = R.string.editor_error_title,
                priceErrorRes = R.string.editor_error_price,
            ),
            onTitleChange = {}, onDescriptionChange = {}, onPriceChange = {}, onImagesChange = {}, onSave = {}, onClose = {},
        )
    }
}
