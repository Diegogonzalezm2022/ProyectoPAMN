package com.ulpgc.proyectopamn.adapters.primary.ui.myproducts

import androidx.annotation.StringRes
import com.ulpgc.proyectopamn.domain.model.Product

/**
 * Lo que puede estar enseñando la pantalla "Mis productos" en cada momento.
 * Solo puede estar en UNO de estos estados a la vez, así nunca se ve
 * a la vez la rueda de carga y un mensaje de error.
 */
sealed interface MyProductsUiState {

    /** Pidiendo los productos: se enseña un indicador de carga. */
    data object Loading : MyProductsUiState

    /** No tengo ningún producto: se invita a publicar el primero. */
    data object Empty : MyProductsUiState

    /** Tengo productos: se enseña la lista. */
    data class Success(val products: List<Product>) : MyProductsUiState

    /** Algo ha fallado: el texto viene de strings.xml para poder traducirlo. */
    data class Error(@StringRes val messageRes: Int) : MyProductsUiState
}
