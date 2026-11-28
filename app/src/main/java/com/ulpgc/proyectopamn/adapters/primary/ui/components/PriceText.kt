package com.ulpgc.proyectopamn.adapters.primary.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ulpgc.proyectopamn.R
import java.text.NumberFormat
import java.util.Locale

/**
 * Precio para enseñar en pantalla: 1250 céntimos → "12,50 €"; 0 → "Gratis".
 * Guardamos céntimos (sin errores de decimales) y solo convertimos al pintar.
 */
@Composable
fun formatPrice(priceInCents: Long): String {
    if (priceInCents == 0L) return stringResource(R.string.price_free)
    val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-ES"))
    return format.format(priceInCents / 100.0)
}
