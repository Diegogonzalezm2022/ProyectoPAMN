package com.ulpgc.proyectopamn.application.usecase

import com.ulpgc.proyectopamn.domain.model.Product
import com.ulpgc.proyectopamn.domain.model.ProductError
import com.ulpgc.proyectopamn.domain.ports.input.GetHomeProductsInputPort
import com.ulpgc.proyectopamn.domain.ports.output.AuthOutputPort
import com.ulpgc.proyectopamn.domain.ports.output.ProductOutputPort

class GetHomeProductsUseCase(
    private val authOutputPort: AuthOutputPort,
    private val productOutputPort: ProductOutputPort,
) : GetHomeProductsInputPort {

    override suspend fun execute(): Result<List<Product>> {
        val user = authOutputPort.getCurrentUser()
            ?: return Result.failure(ProductError.NotLoggedIn)

        // Regla de negocio: en el Home no salen tus propios productos (no te los vas a comprar).
        // Se filtra aquí y no en Firestore porque "distinto de" + ordenar por fecha
        // necesitaría un índice especial; y así la regla vale para cualquier adaptador.
        val products = productOutputPort.findAvailable(MAX_PRODUCTS)
            .filter { it.sellerId != user.id }
        return Result.success(products)
    }

    private companion object {
        const val MAX_PRODUCTS = 100
    }
}
