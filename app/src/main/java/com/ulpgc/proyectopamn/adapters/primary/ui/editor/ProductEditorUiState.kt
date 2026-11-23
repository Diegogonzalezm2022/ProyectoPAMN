package com.ulpgc.proyectopamn.adapters.primary.ui.editor

import androidx.annotation.StringRes

/**
 * Todo lo que necesita pintar el editor de producto.
 * Aquí sí uso una data class (y no una sealed) porque el formulario
 * siempre está en pantalla: lo que cambia son sus campos.
 */
data class ProductEditorUiState(
    /** `true` si estoy editando uno existente, `false` si es nuevo. Cambia títulos y botón. */
    val isEditing: Boolean = false,
    val title: String = "",
    val description: String = "",
    /** El precio tal cual lo escribe el usuario ("12,50"); se pasa a céntimos al guardar. */
    val priceText: String = "",
    /** Error de cada campo. Solo se rellena cuando el usuario ya ha tocado ese campo. */
    @StringRes val titleErrorRes: Int? = null,
    @StringRes val priceErrorRes: Int? = null,
    /** El formulario es válido: si no, el botón de guardar está desactivado. */
    val canSave: Boolean = false,
    /** Hay algo escrito distinto a lo inicial: al salir se pregunta si descartar. */
    val hasChanges: Boolean = false,
    val isSaving: Boolean = false,
    /** Fallo al guardar (sin sesión, sin conexión...). */
    @StringRes val saveErrorRes: Int? = null,
    /** Se rellena cuando se ha guardado bien: la pantalla se cierra y enseña este mensaje. */
    @StringRes val savedMessageRes: Int? = null,
)
