package com.ulpgc.proyectopamn.adapters.primary.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ulpgc.proyectopamn.R
import com.ulpgc.proyectopamn.domain.model.Product
import com.ulpgc.proyectopamn.domain.model.ProductError
import com.ulpgc.proyectopamn.domain.ports.input.GetHomeProductsInputPort
import kotlinx.coroutines.launch
import java.text.Normalizer

/**
 * Pide al caso de uso los productos de otros usuarios y los reparte en tiras.
 * La búsqueda filtra en memoria lo ya cargado: es instantánea y no gasta lecturas de Firestore.
 */
class HomeViewModel(
    private val getHomeProducts: GetHomeProductsInputPort,
) : ViewModel() {

    var uiState by mutableStateOf(HomeUiState())
        private set

    private var allProducts: List<Product> = emptyList()

    /** Se llama cada vez que se abre la pestaña y desde "Reintentar". */
    fun loadProducts() {
        uiState = uiState.copy(isLoading = true, errorRes = null)
        viewModelScope.launch {
            getHomeProducts.execute().fold(
                onSuccess = { products ->
                    allProducts = products
                    uiState = uiState.copy(
                        isLoading = false,
                        sections = buildSections(products),
                        searchResults = search(uiState.query),
                    )
                },
                onFailure = { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        errorRes = if (error == ProductError.NotLoggedIn) R.string.my_products_error_not_logged_in
                        else R.string.home_error_generic,
                    )
                },
            )
        }
    }

    fun onQueryChange(query: String) {
        uiState = uiState.copy(query = query, searchResults = search(query))
    }

    /** Las tiras solo aparecen si tienen algo: una tira vacía no aporta nada. */
    private fun buildSections(products: List<Product>): List<HomeSection> = listOf(
        HomeSection(R.string.home_section_newest, products.take(SECTION_SIZE)),
        HomeSection(
            R.string.home_section_cheap,
            products.filter { it.priceInCents <= CHEAP_LIMIT_CENTS }
                .sortedBy { it.priceInCents }
                .take(SECTION_SIZE),
        ),
    ).filter { it.products.isNotEmpty() }

    // "bici" encuentra "Bicicleta" y "camara" encuentra "Cámara": sin mayúsculas ni tildes.
    private fun search(query: String): List<Product> {
        val words = query.normalized().split(" ").filter { it.isNotBlank() }
        if (words.isEmpty()) return emptyList()
        return allProducts.filter { product ->
            val text = "${product.title} ${product.description}".normalized()
            words.all { it in text }
        }
    }

    private fun String.normalized(): String =
        Normalizer.normalize(lowercase().trim(), Normalizer.Form.NFD)
            .replace(DIACRITICS, "")

    private companion object {
        const val SECTION_SIZE = 20
        const val CHEAP_LIMIT_CENTS = 10_00L
        val DIACRITICS = Regex("\\p{Mn}+")
    }
}
