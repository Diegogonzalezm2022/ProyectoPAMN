package com.ulpgc.proyectopamn.adapters.primary.ui.editor

/**
 * Convierte el precio que escribe el usuario ("12,50") a céntimos (1250) y al revés.
 *
 * Está en la capa de UI y no en el dominio: el dominio solo trabaja con céntimos,
 * cómo lo teclea la persona (coma, punto...) es cosa de la pantalla.
 */
object PriceInput {

    // Hasta 7 cifras de euros y, opcionalmente, coma o punto con 1 o 2 decimales.
    private val PRICE_REGEX = Regex("""^\d{1,7}([.,]\d{1,2})?$""")

    /** "12,50" → 1250, "12.5" → 1250, "7" → 700. Si no es un precio válido, `null`. */
    fun parse(text: String): Long? {
        val clean = text.trim()
        if (!PRICE_REGEX.matches(clean)) return null
        val parts = clean.replace(',', '.').split('.')
        val euros = parts[0].toLong()
        val cents = parts.getOrNull(1)?.padEnd(2, '0')?.toLong() ?: 0L
        return euros * 100 + cents
    }

    /** 1250 → "12,50", 700 → "7". Para rellenar el campo al editar. */
    fun format(priceInCents: Long): String {
        val euros = priceInCents / 100
        val cents = priceInCents % 100
        return if (cents == 0L) "$euros" else "$euros," + cents.toString().padStart(2, '0')
    }
}
