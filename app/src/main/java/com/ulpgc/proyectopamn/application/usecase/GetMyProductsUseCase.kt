package com.ulpgc.proyectopamn.application.usecase

import com.ulpgc.proyectopamn.domain.model.Product
import com.ulpgc.proyectopamn.domain.model.ProductError
import com.ulpgc.proyectopamn.domain.ports.input.GetMyProductsInputPort
import com.ulpgc.proyectopamn.domain.ports.output.AuthOutputPort
import com.ulpgc.proyectopamn.domain.ports.output.ProductOutputPort

class GetMyProductsUseCase(
    private val authOutputPort: AuthOutputPort,
    private val productOutputPort: ProductOutputPort,
) : GetMyProductsInputPort {

    override suspend fun execute(): Result<List<Product>> {
        val user = authOutputPort.getCurrentUser()
            ?: return Result.failure(ProductError.NotLoggedIn)
        return Result.success(productOutputPort.findBySeller(user.id))
    }
}
