package com.ulpgc.proyectopamn.domain.ports.input

import com.ulpgc.proyectopamn.domain.model.Product

/** Caso de uso: publicar un producto nuevo. El vendedor es el usuario con la sesión iniciada. */
interface CreateProductInputPort {
    suspend fun execute(title: String, description: String, priceInCents: Long): Result<Product>
}
