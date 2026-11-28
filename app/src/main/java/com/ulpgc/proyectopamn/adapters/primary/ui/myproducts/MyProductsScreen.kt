package com.ulpgc.proyectopamn.adapters.primary.ui.myproducts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulpgc.proyectopamn.R
import com.ulpgc.proyectopamn.adapters.primary.ui.utils.rememberImageBitmapFromUri
import com.ulpgc.proyectopamn.config.AppDependencyContainer
import com.ulpgc.proyectopamn.domain.model.Product
import com.ulpgc.proyectopamn.domain.model.ProductStatus
import com.ulpgc.proyectopamn.ui.theme.ProyectoPAMNTheme
import java.text.NumberFormat
import java.util.Locale

/**
 * Pantalla "Mis productos" con su ViewModel.
 * Solo conecta: pide el estado al ViewModel y se lo pasa a [MyProductsContent].
 */
@Composable
fun MyProductsScreen(
    onAddClick: () -> Unit,
    onProductClick: (Product) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: MyProductsViewModel = viewModel(
        factory = AppDependencyContainer.getMyProductsViewModelFactory(context)
    )
    // Se ejecuta cada vez que la pestaña entra en pantalla: la lista siempre está al día.
    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }
    MyProductsContent(
        uiState = viewModel.uiState,
        onRetry = viewModel::loadProducts,
        onAddClick = onAddClick,
        onProductClick = onProductClick,
    )
}

/**
 * Lo que se ve. No conoce el ViewModel: recibe el estado y avisa con lambdas.
 * Así se puede previsualizar y reutilizar sin Firebase.
 */
@Composable
fun MyProductsContent(
    uiState: MyProductsUiState,
    onRetry: () -> Unit,
    onAddClick: () -> Unit,
    onProductClick: (Product) -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.my_products_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(16.dp)
                    .semantics { heading() },
            )
            when (uiState) {
                MyProductsUiState.Loading -> LoadingState()
                MyProductsUiState.Empty -> EmptyState()
                is MyProductsUiState.Success -> ProductList(uiState.products, onProductClick)
                is MyProductsUiState.Error -> ErrorState(uiState.messageRes, onRetry)
            }
        }

        // El + está siempre visible: publicar es la acción principal de esta pantalla.
        val addDescription = stringResource(R.string.my_products_add)
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = addDescription)
        }
    }
}

@Composable
private fun LoadingState() {
    val description = stringResource(R.string.my_products_loading)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.semantics { contentDescription = description })
    }
}

@Composable
private fun EmptyState() {
    CenteredMessage {
        Text(
            text = stringResource(R.string.my_products_empty_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.my_products_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ErrorState(messageRes: Int, onRetry: () -> Unit) {
    CenteredMessage {
        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            // TalkBack lo lee en cuanto aparece, sin que el usuario tenga que buscarlo.
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.my_products_retry))
        }
    }
}

@Composable
private fun CenteredMessage(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content()
    }
}

@Composable
private fun ProductList(products: List<Product>, onProductClick: (Product) -> Unit) {
    LazyColumn(
        // Margen de abajo grande para que el + no tape el último producto.
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(products, key = { it.id }) { product ->
            MyProductItem(product, onClick = { onProductClick(product) })
        }
    }
}

@Composable
private fun MyProductItem(product: Product, onClick: () -> Unit) {
    val context = LocalContext.current
    val mainBitmap = rememberImageBitmapFromUri(context, product.mainImage ?: "")

    // Al ser pulsable, la tarjeta agrupa su contenido: TalkBack la lee de una vez
    // ("Bici, 50 €, En venta") y avisa de que se puede pulsar.
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (mainBitmap != null) {
                    Image(
                        bitmap = mainBitmap,
                        contentDescription = "Miniatura principal de ${product.title}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Sin imagen principal",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = formatPrice(product.priceInCents),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    // El estado va en texto, no solo con un color (WCAG: no depender del color).
                    Text(
                        text = stringResource(
                            if (product.status == ProductStatus.SOLD) R.string.my_products_status_sold
                            else R.string.my_products_status_available
                        ),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                if (product.images.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🖼️ ${product.images.size} foto(s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

/** 1250 céntimos → "12,50 €"; 0 → "Gratis". */
@Composable
private fun formatPrice(priceInCents: Long): String {
    if (priceInCents == 0L) return stringResource(R.string.my_products_free)
    val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-ES"))
    return format.format(priceInCents / 100.0)
}

@Preview(showBackground = true)
@Composable
private fun MyProductsListPreview() {
    ProyectoPAMNTheme {
        MyProductsContent(
            uiState = MyProductsUiState.Success(
                listOf(
                    Product(id = "1", title = "Bicicleta de montaña", priceInCents = 12000),
                    Product(id = "2", title = "Libros de 1º", priceInCents = 0),
                    Product(id = "3", title = "Silla de escritorio", priceInCents = 2550, status = ProductStatus.SOLD),
                )
            ),
            onRetry = {},
            onAddClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MyProductsEmptyPreview() {
    ProyectoPAMNTheme {
        MyProductsContent(uiState = MyProductsUiState.Empty, onRetry = {}, onAddClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun MyProductsErrorPreview() {
    ProyectoPAMNTheme {
        MyProductsContent(
            uiState = MyProductsUiState.Error(R.string.my_products_error_generic),
            onRetry = {},
            onAddClick = {},
        )
    }
}
