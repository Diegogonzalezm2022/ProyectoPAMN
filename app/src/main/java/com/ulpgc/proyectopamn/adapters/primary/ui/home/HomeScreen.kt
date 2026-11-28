package com.ulpgc.proyectopamn.adapters.primary.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulpgc.proyectopamn.R
import com.ulpgc.proyectopamn.adapters.primary.ui.components.ProductCard
import com.ulpgc.proyectopamn.config.AppDependencyContainer
import com.ulpgc.proyectopamn.domain.model.Product
import com.ulpgc.proyectopamn.ui.theme.ProyectoPAMNTheme

/** Home con su ViewModel. Recarga cada vez que se abre la pestaña. */
@Composable
fun HomeScreen(username: String) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(
        factory = AppDependencyContainer.getHomeViewModelFactory(context)
    )
    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }
    HomeContent(
        username = username,
        state = viewModel.uiState,
        onQueryChange = viewModel::onQueryChange,
        onRetry = viewModel::loadProducts,
    )
}

/** Solo pinta. No conoce el ViewModel: así hay previews sin Firebase. */
@Composable
fun HomeContent(
    username: String,
    state: HomeUiState,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.home_greeting, username),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .semantics { heading() },
            )
        }
        item { SearchField(query = state.query, onQueryChange = onQueryChange) }

        when {
            state.isLoading && state.sections.isEmpty() -> item { LoadingState() }
            state.errorRes != null -> item { ErrorState(state.errorRes, onRetry) }
            state.isEmpty -> item {
                MessageState(
                    title = stringResource(R.string.home_empty_title),
                    message = stringResource(R.string.home_empty_message),
                )
            }
            state.isSearching -> searchResults(state.query, state.searchResults)
            else -> state.sections.forEach { section ->
                item(key = section.titleRes) { ProductStrip(section) }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(stringResource(R.string.home_search_label)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            // La X solo aparece si hay algo que borrar.
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.home_search_clear))
                }
            }
        },
        singleLine = true,
        // La lupa del teclado cierra el teclado: los resultados ya se ven mientras escribes.
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    )
}

/** Una tira horizontal: título + productos que se deslizan de lado. */
@Composable
private fun ProductStrip(section: HomeSection) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = stringResource(section.titleRes),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .semantics { heading() },
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(section.products, key = { it.id }) { product ->
                ProductCard(product = product, modifier = Modifier.width(CARD_WIDTH))
            }
        }
    }
}

/** Resultados de búsqueda en dos columnas (cuadrícula hecha con filas de 2). */
private fun androidx.compose.foundation.lazy.LazyListScope.searchResults(
    query: String,
    results: List<Product>,
) {
    item {
        // liveRegion: TalkBack anuncia "3 resultados" mientras escribes, sin tener que buscarlo.
        Text(
            text = if (results.isEmpty()) stringResource(R.string.home_search_empty, query.trim())
            else pluralStringResource(R.plurals.home_search_count, results.size, results.size),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
    items(results.chunked(2)) { row ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            row.forEach { product ->
                ProductCard(product = product, modifier = Modifier.weight(1f))
            }
            // Si la última fila tiene uno solo, un hueco para que no se estire a todo el ancho.
            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun LoadingState() {
    val description = stringResource(R.string.home_loading)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.semantics { contentDescription = description })
    }
}

@Composable
private fun ErrorState(messageRes: Int, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(messageRes),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}

@Composable
private fun MessageState(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
    }
}

private val CARD_WIDTH = 150.dp

private val previewProducts = listOf(
    Product(id = "1", title = "Bicicleta de montaña", priceInCents = 12000),
    Product(id = "2", title = "Libros de 1º de carrera", priceInCents = 0),
    Product(id = "3", title = "Lámpara de escritorio", priceInCents = 850),
)

@Preview(showBackground = true)
@Composable
private fun HomeStripsPreview() {
    ProyectoPAMNTheme {
        HomeContent(
            username = "Ana",
            state = HomeUiState(
                isLoading = false,
                sections = listOf(
                    HomeSection(R.string.home_section_newest, previewProducts),
                    HomeSection(R.string.home_section_cheap, previewProducts.drop(1)),
                ),
            ),
            onQueryChange = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeSearchPreview() {
    ProyectoPAMNTheme {
        HomeContent(
            username = "Ana",
            state = HomeUiState(
                query = "bici",
                isLoading = false,
                sections = listOf(HomeSection(R.string.home_section_newest, previewProducts)),
                searchResults = previewProducts.take(1),
            ),
            onQueryChange = {},
            onRetry = {},
        )
    }
}
