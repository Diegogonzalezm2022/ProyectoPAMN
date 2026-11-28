package com.ulpgc.proyectopamn.domain.model

/**
 * Entidad de Dominio: Producto
 *
 * Representa un producto en la aplicación. La propiedad [images] contiene una lista ordenada
 * de URIs/URLs de imágenes donde la primera posición (`images[0]`) es la imagen principal (portada).
 *
 * El precio se guarda en céntimos (`Long`) y no en euros con decimales (`Double`):
 * con `Double`, 0.1 + 0.2 da 0.30000000000000004, y con dinero no se puede fallar.
 * Ejemplo: 15,99 € → `priceInCents = 1599`.
 */
data class Product(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val priceInCents: Long = 0L,
    val sellerId: String = "",
    val sellerName: String = "",
    val images: List<String> = emptyList(),
    val status: ProductStatus = ProductStatus.AVAILABLE,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** Imagen principal (portada) que corresponde al primer elemento de la lista ordenada */
    val mainImage: String?
        get() = images.firstOrNull { it.isNotBlank() }
}
