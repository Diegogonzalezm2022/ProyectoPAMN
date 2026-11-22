package com.ulpgc.proyectopamn.domain.model

/**
 * Entidad de Dominio: Producto en venta.
 *
 * El precio se guarda en céntimos (`Long`) y no en euros con decimales (`Double`):
 * con `Double`, 0.1 + 0.2 da 0.30000000000000004, y con dinero no se puede fallar.
 * Ejemplo: 15,99 € → `priceInCents = 1599`.
 */
data class Product(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val priceInCents: Long = 0,
    val sellerId: String = "",
    val status: ProductStatus = ProductStatus.AVAILABLE,
)
