package com.ulpgc.proyectopamn.adapters.secondary.network.firebase

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ulpgc.proyectopamn.domain.model.Product
import com.ulpgc.proyectopamn.domain.model.ProductStatus
import com.ulpgc.proyectopamn.domain.ports.output.ProductOutputPort
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Adaptador Secundario: guarda los productos en la colección `products` de Firestore
 * y sube la lista ordenada de imágenes a Firebase Storage.
 */
class FirestoreProductAdapter(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
) : ProductOutputPort {

    private val products get() = firestore.collection(COLLECTION)

    override suspend fun save(product: Product): Result<Product> = runCatching {
        val document = if (product.id.isBlank()) products.document() else products.document(product.id)
        val productId = document.id

        // Subir imágenes locales de la lista ordenada a Firebase Storage
        val uploadedImages = mutableListOf<String>()
        for ((index, imgUri) in product.images.withIndex()) {
            val uploadedUrl = uploadProductImageIfNeeded(productId, index, imgUri)
            if (uploadedUrl.isNotBlank()) {
                uploadedImages.add(uploadedUrl)
            }
        }

        val saved = product.copy(id = productId, images = uploadedImages)
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

        val batch = firestore.batch()
        available.documents.forEach { batch.delete(it.reference) }
        batch.commit().awaitTask()
    }

    private suspend fun uploadProductImageIfNeeded(productId: String, index: Int, imageUri: String): String {
        if (imageUri.isBlank()) return ""
        if (imageUri.startsWith("http://") || imageUri.startsWith("https://")) {
            return imageUri
        }
        return try {
            val uri = Uri.parse(imageUri)
            val ref = storage.reference.child("product_images/$productId/img_${index}_${System.currentTimeMillis()}.jpg")
            ref.putFile(uri).awaitTask()
            ref.downloadUrl.awaitTask().toString()
        } catch (e: Exception) {
            Log.e("FirestoreProductAdapter", "Error al subir imagen a Firebase Storage: ${e.message}", e)
            throw Exception("Error al subir la imagen a Firebase Storage: ${e.localizedMessage}")
        }
    }

    private fun Product.toMap(): Map<String, Any> = mapOf(
        FIELD_TITLE to title,
        FIELD_DESCRIPTION to description,
        FIELD_PRICE to priceInCents,
        FIELD_SELLER_ID to sellerId,
        FIELD_STATUS to status.name,
        FIELD_IMAGES to images
    )

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toProduct(): Product? {
        if (!exists()) return null

        val priceLong = when (val p = get(FIELD_PRICE)) {
            is Long -> p
            is Int -> p.toLong()
            is Double -> p.toLong()
            else -> getLong(FIELD_PRICE) ?: 0L
        }

        val rawImages = get(FIELD_IMAGES)
        val imagesList = when (rawImages) {
            is List<*> -> rawImages.mapNotNull { it?.toString() }
            else -> emptyList()
        }

        return Product(
            id = id,
            title = getString(FIELD_TITLE) ?: "",
            description = getString(FIELD_DESCRIPTION) ?: "",
            priceInCents = priceLong,
            sellerId = getString(FIELD_SELLER_ID) ?: "",
            images = imagesList,
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
        const val FIELD_IMAGES = "images"
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { exception -> continuation.resumeWithException(exception) }
    }
}
