package com.ulpgc.proyectopamn.domain.model

/**
 * Reglas de negocio de un producto.
 *
 * Viven en el dominio para que se cumplan siempre, se cree el producto desde donde se cree.
 */
object ProductValidator {

    fun isValidTitle(title: String): Boolean = title.isNotBlank()

    /** Se permite 0 (regalar algo), pero no precios negativos. */
    fun isValidPrice(priceInCents: Long): Boolean = priceInCents >= 0

    /** Devuelve el primer error que encuentre, o `null` si todo está bien. */
    fun validate(title: String, priceInCents: Long): ProductError? = when {
        !isValidTitle(title) -> ProductError.EmptyTitle
        !isValidPrice(priceInCents) -> ProductError.InvalidPrice
        else -> null
    }
}
