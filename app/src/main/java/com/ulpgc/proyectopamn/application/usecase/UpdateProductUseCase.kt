package com.ulpgc.proyectopamn.application.usecase

import com.ulpgc.proyectopamn.domain.model.Product
import com.ulpgc.proyectopamn.domain.model.ProductError
import com.ulpgc.proyectopamn.domain.model.ProductValidator
import com.ulpgc.proyectopamn.domain.ports.input.UpdateProductInputPort
import com.ulpgc.proyectopamn.domain.ports.output.AuthOutputPort
import com.ulpgc.proyectopamn.domain.ports.output.ProductOutputPort

class UpdateProductUseCase(
    private val authOutputPort: AuthOutputPort,
    private val productOutputPort: ProductOutputPort,
) : UpdateProductInputPort {

    override suspend fun execute(
        productId: String,
        title: String,
        description: String,
        priceInCents: Long,
        images: List<String>
    ): Result<Product> {
        val cleanTitle = title.trim()
        ProductValidator.validate(cleanTitle, priceInCents)?.let { return Result.failure(it) }

        val user = authOutputPort.getCurrentUser()
            ?: return Result.failure(ProductError.NotLoggedIn)
        val existing = productOutputPort.findById(productId)
            ?: return Result.failure(ProductError.NotFound)
        if (existing.sellerId != user.id) {
            return Result.failure(ProductError.NotOwner)
        }

        // Partimos del producto guardado y solo cambiamos lo editable:
        // así el vendedor y el estado no se pueden modificar desde aquí.
        val updated = existing.copy(
            title = cleanTitle,
            description = description.trim(),
            priceInCents = priceInCents,
            images = images,
        )
        return productOutputPort.save(updated)
    }
}
