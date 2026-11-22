package com.ulpgc.proyectopamn.adapters.secondary.network.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.ulpgc.proyectopamn.domain.model.Product
import com.ulpgc.proyectopamn.domain.model.ProductStatus
import com.ulpgc.proyectopamn.domain.ports.output.ProductOutputPort

/**
 * Adaptador Secundario: guarda los productos en la colección `products` de Firestore.
 *
 * Cada documento:
 * ```
 * products/{id}
 *   title:        "Bicicleta"
 *   description:  "Casi nueva"
 *   priceInCents: 12000
 *   sellerId:     "<uid del vendedor en users/>"
 *   status:       "AVAILABLE" | "SOLD"
 * ```
 */
class FirestoreProductAdapter(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : ProductOutputPort {

    private val products get() = firestore.collection(COLLECTION)

    override suspend fun save(product: Product): Result<Product> = runCatching {
        // Sin id → Firestore genera uno nuevo; con id → se sobrescribe ese documento.
        val document = if (product.id.isBlank()) products.document() else products.document(product.id)
        val saved = product.copy(id = document.id)
        document.set(saved.toMap()).awaitTask()
        saved
    }

    override suspend fun findById(id: String): Product? = try {
        products.document(id).get().awaitTask().toProduct()
    } catch (_: Exception) {
        null
    }

    override suspend fun findBySeller(sellerId: String): List<Product> = try {
        products.whereEqualTo(FIELD_SELLER_ID, sellerId).get().awaitTask()
            .documents.mapNotNull { it.toProduct() }
    } catch (_: Exception) {
        emptyList()
    }

    override suspend fun deleteAvailableBySeller(sellerId: String): Result<Unit> = runCatching {
        val available = products
            .whereEqualTo(FIELD_SELLER_ID, sellerId)
            .whereEqualTo(FIELD_STATUS, ProductStatus.AVAILABLE.name)
            .get().awaitTask()

        // Un "batch" borra todos de golpe: o se borran todos o ninguno.
        val batch = firestore.batch()
        available.documents.forEach { batch.delete(it.reference) }
        batch.commit().awaitTask()
    }

    private fun Product.toMap(): Map<String, Any> = mapOf(
        FIELD_TITLE to title,
        FIELD_DESCRIPTION to description,
        FIELD_PRICE to priceInCents,
        FIELD_SELLER_ID to sellerId,
        FIELD_STATUS to status.name,
    )

    /** Convierte el documento de Firestore en el modelo del dominio (`null` si no existe). */
    private fun DocumentSnapshot.toProduct(): Product? {
        if (!exists()) return null
        return Product(
            id = id,
            title = getString(FIELD_TITLE) ?: "",
            description = getString(FIELD_DESCRIPTION) ?: "",
            priceInCents = getLong(FIELD_PRICE) ?: 0,
            sellerId = getString(FIELD_SELLER_ID) ?: "",
            // Si en la base de datos hay un valor raro, se trata como "en venta" en vez de romper la app.
            status = ProductStatus.entries.find { it.name == getString(FIELD_STATUS) } ?: ProductStatus.AVAILABLE,
        )
    }

    private companion object {
        const val COLLECTION = "products"
        const val FIELD_TITLE = "title"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_PRICE = "priceInCents"
        const val FIELD_SELLER_ID = "sellerId"
        const val FIELD_STATUS = "status"
    }
}
