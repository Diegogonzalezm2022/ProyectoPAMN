package com.ulpgc.proyectopamn.adapters.primary.ui.home

import androidx.annotation.StringRes
import com.ulpgc.proyectopamn.domain.model.Product

/**
 * Lo que pinta el Home. Es una data class (no sealed) porque el buscador
 * está siempre visible, cargue o no la lista.
 */
data class HomeUiState(
    val query: String = "",
    val isLoading: Boolean = true,
    @StringRes val errorRes: Int? = null,
    /** Tiras horizontales cuando no se está buscando ("Novedades", "Por menos de 10 €"...). */
    val sections: List<HomeSection> = emptyList(),
    /** Resultados cuando hay algo escrito en el buscador. */
    val searchResults: List<Product> = emptyList(),
) {
    val isSearching: Boolean get() = query.isNotBlank()

    /** No hay ningún producto de otros usuarios (no es lo mismo que "la búsqueda no encuentra nada"). */
    val isEmpty: Boolean get() = !isLoading && errorRes == null && sections.isEmpty()
}

/** Una tira horizontal del Home: un título y sus productos. */
data class HomeSection(
    @StringRes val titleRes: Int,
    val products: List<Product>,
)
