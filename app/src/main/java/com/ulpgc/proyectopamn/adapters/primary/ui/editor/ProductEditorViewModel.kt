package com.ulpgc.proyectopamn.adapters.primary.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ulpgc.proyectopamn.R
import com.ulpgc.proyectopamn.domain.model.Product
import com.ulpgc.proyectopamn.domain.model.ProductError
import com.ulpgc.proyectopamn.domain.model.ProductValidator
import com.ulpgc.proyectopamn.domain.ports.input.CreateProductInputPort
import com.ulpgc.proyectopamn.domain.ports.input.UpdateProductInputPort
import kotlinx.coroutines.launch

/**
 * Editor de producto: sirve para crear uno nuevo (`productToEdit == null`)
 * y para editar uno mío. Valida en tiempo real y llama al caso de uso que toque.
 */
class ProductEditorViewModel(
    private val createProduct: CreateProductInputPort,
    private val updateProduct: UpdateProductInputPort,
    private val productToEdit: Product?,
) : ViewModel() {

    // Cómo estaba el formulario al abrirlo, para saber si hay cambios sin guardar.
    private val initial = ProductEditorUiState(
        isEditing = productToEdit != null,
        title = productToEdit?.title.orEmpty(),
        description = productToEdit?.description.orEmpty(),
        priceText = productToEdit?.let { PriceInput.format(it.priceInCents) }.orEmpty(),
        images = productToEdit?.images ?: emptyList(),
    )

    // Hasta que no tocas un campo no te grito que está mal (sería agobiante al abrir).
    private var titleTouched = false
    private var priceTouched = false

    var uiState by mutableStateOf(initial.validated())
        private set

    fun onTitleChange(value: String) {
        titleTouched = true
        update(uiState.copy(title = value))
    }

    fun onDescriptionChange(value: String) {
        update(uiState.copy(description = value))
    }

    fun onPriceChange(value: String) {
        priceTouched = true
        update(uiState.copy(priceText = value))
    }

    fun onImagesChange(newImages: List<String>) {
        update(uiState.copy(images = newImages))
    }

    fun onSave() {
        val state = uiState
        if (!state.canSave || state.isSaving) return
        val priceInCents = PriceInput.parse(state.priceText) ?: return
        uiState = state.copy(isSaving = true, saveErrorRes = null)

        viewModelScope.launch {
            val result = if (productToEdit == null) {
                createProduct.execute(state.title, state.description, priceInCents, state.images)
            } else {
                updateProduct.execute(productToEdit.id, state.title, state.description, priceInCents, state.images)
            }
            uiState = result.fold(
                onSuccess = {
                    uiState.copy(
                        isSaving = false,
                        savedMessageRes = if (productToEdit == null) R.string.editor_saved_created
                        else R.string.editor_saved_updated,
                    )
                },
                onFailure = { error -> uiState.copy(isSaving = false, saveErrorRes = error.toMessageRes()) },
            )
        }
    }

    // Cada cambio vuelve a validar y borra el error de guardado anterior.
    private fun update(newState: ProductEditorUiState) {
        uiState = newState.copy(saveErrorRes = null).validated()
    }

    // Las reglas (título no vacío, precio >= 0) son las del dominio: no las repito aquí.
    private fun ProductEditorUiState.validated(): ProductEditorUiState {
        val titleOk = ProductValidator.isValidTitle(title)
        val priceInCents = PriceInput.parse(priceText)
        val priceOk = priceInCents != null && ProductValidator.isValidPrice(priceInCents)
        return copy(
            titleErrorRes = if (titleTouched && !titleOk) R.string.editor_error_title else null,
            priceErrorRes = if (priceTouched && !priceOk) R.string.editor_error_price else null,
            canSave = titleOk && priceOk,
            hasChanges = title != initial.title ||
                description != initial.description ||
                priceText != initial.priceText ||
                images != initial.images,
        )
    }

    private fun Throwable.toMessageRes(): Int = when (this) {
        ProductError.EmptyTitle -> R.string.editor_error_title
        ProductError.InvalidPrice -> R.string.editor_error_price
        ProductError.NotLoggedIn -> R.string.my_products_error_not_logged_in
        ProductError.NotFound -> R.string.editor_error_not_found
        ProductError.NotOwner -> R.string.editor_error_not_owner
        else -> R.string.editor_error_generic
    }
}
