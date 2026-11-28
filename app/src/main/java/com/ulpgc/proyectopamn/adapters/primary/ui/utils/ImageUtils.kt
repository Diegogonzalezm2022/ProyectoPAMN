package com.ulpgc.proyectopamn.adapters.primary.ui.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Carga de forma asíncrona y segura un [ImageBitmap] desde una URI local (`content://`, `file://`)
 * o una URL remota de red (`https://...` de Firebase Storage).
 * Evita bloqueos del hilo principal de UI e incluye persistencia de permisos en URIs locales.
 */
@Composable
fun rememberImageBitmapFromUri(context: Context, uriString: String): ImageBitmap? {
    var imageBitmap by remember(uriString) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(uriString) {
        if (uriString.isBlank()) {
            imageBitmap = null
            return@LaunchedEffect
        }

        withContext(Dispatchers.IO) {
            try {
                val bitmap: Bitmap? = if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
                    val connection = URL(uriString).openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000
                    connection.connect()
                    connection.inputStream.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                } else {
                    val uri = Uri.parse(uriString)
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (_: Exception) {}

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                            decoder.isMutableRequired = true
                        }
                    } else {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    }
                }
                imageBitmap = bitmap?.asImageBitmap()
            } catch (e: Exception) {
                Log.e("ImageLoader", "Error al cargar imagen desde URI/URL '$uriString': ${e.message}", e)
                imageBitmap = null
            }
        }
    }

    return imageBitmap
}
