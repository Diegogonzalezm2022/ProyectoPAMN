package com.ulpgc.proyectopamn.adapters.primary.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ulpgc.proyectopamn.domain.model.Product
import com.ulpgc.proyectopamn.ui.theme.ProyectoPAMNTheme

/**
 * Tarjeta de producto para escaparates (Home, y más adelante Favoritos):
 * foto cuadrada, precio destacado y título.
 *
 * @param onClick `null` mientras no haya pantalla de detalle: así la tarjeta no parece
 *   pulsable sin hacer nada (eso confunde y TalkBack anunciaría un botón que no funciona).
 */
@Composable
fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val content: @Composable () -> Unit = {
        Column {
            ProductImage(
                imageUrl = product.mainImage,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            )
            Column(modifier = Modifier.padding(8.dp)) {
                // El precio primero: es lo que más se mira en una app de compraventa.
                Text(
                    text = formatPrice(product.priceInCents),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier) { content() }
    } else {
        // mergeDescendants: TalkBack lee "12,50 €, Bicicleta" de una vez.
        Card(modifier = modifier.semantics(mergeDescendants = true) {}) { content() }
    }
}

@Preview(showBackground = true, widthDp = 160)
@Composable
private fun ProductCardPreview() {
    ProyectoPAMNTheme {
        ProductCard(Product(title = "Bicicleta de montaña con cambios Shimano", priceInCents = 12000))
    }
}
