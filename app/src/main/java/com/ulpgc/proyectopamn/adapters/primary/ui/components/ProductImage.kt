package com.ulpgc.proyectopamn.adapters.primary.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ulpgc.proyectopamn.adapters.primary.ui.utils.rememberImageBitmapFromUri

/**
 * Foto de portada de un producto (o un icono de "imagen" si no tiene / aún está cargando).
 *
 * Es decorativa para TalkBack (`contentDescription = null`): la tarjeta que la contiene
 * ya lee el título del producto, y repetirlo solo haría ruido.
 * Se usa en "Mis productos" y en el Home: un solo componente = mismo aspecto en toda la app.
 */
@Composable
fun ProductImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bitmap = rememberImageBitmapFromUri(context, imageUrl.orEmpty())

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}
