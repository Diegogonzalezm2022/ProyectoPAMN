package com.ulpgc.proyectopamn.domain.ports.input

import com.ulpgc.proyectopamn.domain.model.Product

/** Caso de uso: cambiar título, descripción o precio de un producto propio. */
interface UpdateProductInputPort {
    suspend fun execute(productId: String, title: String, description: String, priceInCents: Long): Result<Product>
}
