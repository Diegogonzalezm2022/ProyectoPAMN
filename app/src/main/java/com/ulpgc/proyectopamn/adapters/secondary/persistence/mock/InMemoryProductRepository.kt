package com.ulpgc.proyectopamn.adapters.secondary.persistence.mock

import com.ulpgc.proyectopamn.domain.model.Product
import com.ulpgc.proyectopamn.domain.model.ProductStatus
import com.ulpgc.proyectopamn.domain.ports.output.ProductOutputPort

/**
 * Productos guardados en memoria, para probar sin Firebase.
 * Se pierden al cerrar la app.
 */
class InMemoryProductRepository : ProductOutputPort {

    private val products = mutableMapOf<String, Product>()
    private var nextId = 1

    override suspend fun save(product: Product): Result<Product> {
        val saved = if (product.id.isBlank()) product.copy(id = "local_${nextId++}") else product
        products[saved.id] = saved
        return Result.success(saved)
    }

    override suspend fun findById(id: String): Product? = products[id]

    override suspend fun findBySeller(sellerId: String): List<Product> =
        products.values.filter { it.sellerId == sellerId }

    override suspend fun deleteAvailableBySeller(sellerId: String): Result<Unit> {
        products.values.removeIf { it.sellerId == sellerId && it.status == ProductStatus.AVAILABLE }
        return Result.success(Unit)
    }
}
