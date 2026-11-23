package com.ulpgc.proyectopamn.adapters.primary.ui.myproducts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ulpgc.proyectopamn.R
import com.ulpgc.proyectopamn.domain.model.ProductError
import com.ulpgc.proyectopamn.domain.ports.input.GetMyProductsInputPort
import kotlinx.coroutines.launch

/**
 * Hace de puente entre la pantalla "Mis productos" y el caso de uso.
 * No sabe nada de Firebase: solo conoce el puerto de entrada.
 */
class MyProductsViewModel(
    private val getMyProducts: GetMyProductsInputPort,
) : ViewModel() {

    var uiState by mutableStateOf<MyProductsUiState>(MyProductsUiState.Loading)
        private set

    /**
     * Pide los productos. Se llama cada vez que se abre la pestaña y desde "Reintentar".
     * No se llama en `init` porque el ViewModel sobrevive mientras viva la Activity:
     * si cierras sesión y entra otra persona, vería la lista del anterior.
     */
    fun loadProducts() {
        uiState = MyProductsUiState.Loading
        viewModelScope.launch {
            uiState = getMyProducts.execute().fold(
                onSuccess = { products ->
                    if (products.isEmpty()) MyProductsUiState.Empty
                    else MyProductsUiState.Success(products)
                },
                onFailure = { error -> MyProductsUiState.Error(error.toMessageRes()) },
            )
        }
    }

    // Traduce el error del dominio a un texto de strings.xml.
    private fun Throwable.toMessageRes(): Int = when (this) {
        ProductError.NotLoggedIn -> R.string.my_products_error_not_logged_in
        else -> R.string.my_products_error_generic
    }
}
