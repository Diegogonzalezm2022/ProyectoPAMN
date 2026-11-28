package com.ulpgc.proyectopamn.domain.ports.input

import com.ulpgc.proyectopamn.domain.model.Product

/** Caso de uso: productos en venta de **otros** usuarios para el Home, de más nuevo a más viejo. */
interface GetHomeProductsInputPort {
    suspend fun execute(): Result<List<Product>>
}
