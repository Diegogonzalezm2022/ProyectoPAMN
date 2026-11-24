package com.ulpgc.proyectopamn.domain.ports.input

import com.ulpgc.proyectopamn.domain.model.Product

/** Caso de uso: editar título, descripción y precio de uno de mis productos. */
interface UpdateProductInputPort {
    suspend fun execute(
        productId: String,
        title: String,
        description: String,
        priceInCents: Long,
        images: List<String> = emptyList()
    ): Result<Product>
}
