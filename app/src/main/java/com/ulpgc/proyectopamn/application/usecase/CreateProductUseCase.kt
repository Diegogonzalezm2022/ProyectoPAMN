package com.ulpgc.proyectopamn.application.usecase

import com.ulpgc.proyectopamn.domain.model.Product
import com.ulpgc.proyectopamn.domain.model.ProductError
import com.ulpgc.proyectopamn.domain.model.ProductStatus
import com.ulpgc.proyectopamn.domain.model.ProductValidator
import com.ulpgc.proyectopamn.domain.ports.input.CreateProductInputPort
import com.ulpgc.proyectopamn.domain.ports.output.AuthOutputPort
import com.ulpgc.proyectopamn.domain.ports.output.ProductOutputPort

class CreateProductUseCase(
    private val authOutputPort: AuthOutputPort,
    private val productOutputPort: ProductOutputPort,
) : CreateProductInputPort {

    override suspend fun execute(title: String, description: String, priceInCents: Long): Result<Product> {
        val cleanTitle = title.trim()
        ProductValidator.validate(cleanTitle, priceInCents)?.let { return Result.failure(it) }

        // El vendedor no lo elige la pantalla: siempre es quien tiene la sesión iniciada.
        val seller = authOutputPort.getCurrentUser()
            ?: return Result.failure(ProductError.NotLoggedIn)

        val product = Product(
            title = cleanTitle,
            description = description.trim(),
            priceInCents = priceInCents,
            sellerId = seller.id,
            status = ProductStatus.AVAILABLE,
        )
        return productOutputPort.save(product)
    }
}
