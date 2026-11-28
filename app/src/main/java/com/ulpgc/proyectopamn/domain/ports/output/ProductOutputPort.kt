package com.ulpgc.proyectopamn.domain.ports.output

import com.ulpgc.proyectopamn.domain.model.Product

/**
 * Puerto de Salida para guardar y leer productos.
 *
 * El dominio no sabe si detrás hay Firestore, Room o una lista en memoria.
 */
interface ProductOutputPort {

    /** Crea el producto si no tiene id, o lo actualiza si ya lo tiene. Devuelve el producto guardado (con id). */
    suspend fun save(product: Product): Result<Product>

    suspend fun findById(id: String): Product?

    suspend fun findBySeller(sellerId: String): List<Product>

    /** Productos **en venta** de todos los vendedores, de más nuevo a más viejo (como mucho [limit]). */
    suspend fun findAvailable(limit: Int): List<Product>

    /**
     * Borra los productos **en venta** de un vendedor. Los vendidos se conservan.
     * Se usa al borrar una cuenta.
     */
    suspend fun deleteAvailableBySeller(sellerId: String): Result<Unit>
}
