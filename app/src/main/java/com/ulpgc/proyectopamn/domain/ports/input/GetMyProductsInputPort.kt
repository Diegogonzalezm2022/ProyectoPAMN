package com.ulpgc.proyectopamn.domain.ports.input

import com.ulpgc.proyectopamn.domain.model.Product

/** Caso de uso: listar los productos del usuario con la sesión iniciada (en venta y vendidos). */
interface GetMyProductsInputPort {
    suspend fun execute(): Result<List<Product>>
}
