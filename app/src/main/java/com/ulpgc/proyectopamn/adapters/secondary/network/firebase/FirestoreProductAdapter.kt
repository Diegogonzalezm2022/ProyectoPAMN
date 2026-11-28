package com.ulpgc.proyectopamn.adapters.secondary.network.firebase

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ulpgc.proyectopamn.domain.model.Product
import com.ulpgc.proyectopamn.domain.model.ProductStatus
import com.ulpgc.proyectopamn.domain.ports.output.ProductOutputPort

/**
 * Adaptador Secundario: guarda los productos en la colección `products` de Firestore
 * y sus fotos en Firebase Storage (`product_images/{id}/...`).
 *
 * Cada documento:
 * ```
 * products/{id}
 *   title, description, priceInCents, sellerId,
 *   status:    "AVAILABLE" | "SOLD"
 *   images:    [url portada, url 2, ...]
 *   createdAt: 1764331200000   (milisegundos; para ordenar por "novedades")
 * ```
 *
 * Las fotos que dejan de usarse (quitadas al editar, o de productos borrados) se borran
 * también de Storage: si no, se quedarían ahí para siempre ocupando espacio.
 */
class FirestoreProductAdapter(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
) : ProductOutputPort {

    private val products get() = firestore.collection(COLLECTION)

    override suspend fun save(product: Product): Result<Product> = runCatching {
        val isNew = product.id.isBlank()
        val document = if (isNew) products.document() else products.document(product.id)
        val productId = document.id

        // Fotos que tenía antes (solo al editar), para saber luego cuáles ha quitado.
        val previousImages = if (isNew) emptyList() else findById(productId)?.images.orEmpty()

        val uploadedNow = mutableListOf<String>()
        val finalImages = try {
            product.images.filter { it.isNotBlank() }.mapIndexed { index, image ->
                if (image.isRemoteUrl()) {
                    image
                } else {
                    uploadImage(productId, index, image).also { uploadedNow += it }
                }
            }
        } catch (e: Exception) {
            // Si falla una subida a medias, borramos las que sí se subieron en este intento.
            deleteImagesQuietly(uploadedNow)
            throw e
        }

        val saved = product.copy(id = productId, images = finalImages)
        document.set(saved.toMap()).awaitTask()

        // Solo después de guardar bien el documento: así nunca apunta a una foto borrada.
        deleteImagesQuietly(previousImages - finalImages.toSet())
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

    override suspend fun findAvailable(limit: Int): List<Product> = try {
        // Ordenamos en el móvil y no con `orderBy` en la consulta: filtrar por `status`
        // y ordenar por `createdAt` a la vez obliga a crear un índice compuesto en Firestore.
        // Con pocos productos (proyecto de clase) es más simple así.
        products.whereEqualTo(FIELD_STATUS, ProductStatus.AVAILABLE.name).get().awaitTask()
            .documents.mapNotNull { it.toProduct() }
            .sortedByDescending { it.createdAt }
            .take(limit)
    } catch (_: Exception) {
        emptyList()
    }

    override suspend fun deleteAvailableBySeller(sellerId: String): Result<Unit> = runCatching {
        val available = products
            .whereEqualTo(FIELD_SELLER_ID, sellerId)
            .whereEqualTo(FIELD_STATUS, ProductStatus.AVAILABLE.name)
            .get().awaitTask()

        val imagesToDelete = available.documents.flatMap { it.toProduct()?.images.orEmpty() }

        // Un "batch" borra todos de golpe: o se borran todos o ninguno.
        val batch = firestore.batch()
        available.documents.forEach { batch.delete(it.reference) }
        batch.commit().awaitTask()

        deleteImagesQuietly(imagesToDelete)
    }

    private suspend fun uploadImage(productId: String, index: Int, localUri: String): String {
        val ref = storage.reference.child("product_images/$productId/img_${index}_${System.currentTimeMillis()}.jpg")
        ref.putFile(Uri.parse(localUri)).awaitTask()
        return ref.downloadUrl.awaitTask().toString()
    }

    /**
     * Borra fotos de Storage a partir de su URL. Si alguna falla (ya no existía, sin red...)
     * no rompemos la operación principal: lo importante (el producto) ya está guardado/borrado.
     */
    private suspend fun deleteImagesQuietly(urls: Collection<String>) {
        urls.filter { it.isFirebaseStorageUrl() }.forEach { url ->
            try {
                storage.getReferenceFromUrl(url).delete().awaitTask()
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo borrar la foto $url", e)
            }
        }
    }

    private fun String.isRemoteUrl() = startsWith("http://") || startsWith("https://")

    private fun String.isFirebaseStorageUrl() =
        startsWith("gs://") || startsWith("https://firebasestorage.googleapis.com/")

    private fun Product.toMap(): Map<String, Any> = mapOf(
        FIELD_TITLE to title,
        FIELD_DESCRIPTION to description,
        FIELD_PRICE to priceInCents,
        FIELD_SELLER_ID to sellerId,
        FIELD_STATUS to status.name,
        FIELD_IMAGES to images,
        FIELD_CREATED_AT to createdAt,
    )

    /** Convierte el documento de Firestore en el modelo del dominio (`null` si no existe). */
    private fun DocumentSnapshot.toProduct(): Product? {
        if (!exists()) return null

        // Firestore puede devolver el número como Long o Double según cómo se guardó.
        val price = (get(FIELD_PRICE) as? Number)?.toLong() ?: 0L
        val images = (get(FIELD_IMAGES) as? List<*>)?.mapNotNull { it?.toString() }.orEmpty()

        return Product(
            id = id,
            title = getString(FIELD_TITLE) ?: "",
            description = getString(FIELD_DESCRIPTION) ?: "",
            priceInCents = price,
            sellerId = getString(FIELD_SELLER_ID) ?: "",
            images = images,
            // Si en la base de datos hay un valor raro, se trata como "en venta" en vez de romper la app.
            status = ProductStatus.entries.find { it.name == getString(FIELD_STATUS) } ?: ProductStatus.AVAILABLE,
            // Productos antiguos sin fecha: 0, así salen los últimos en "novedades".
            createdAt = (get(FIELD_CREATED_AT) as? Number)?.toLong() ?: 0L,
        )
    }

    private companion object {
        const val TAG = "FirestoreProductAdapter"
        const val COLLECTION = "products"
        const val FIELD_TITLE = "title"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_PRICE = "priceInCents"
        const val FIELD_SELLER_ID = "sellerId"
        const val FIELD_STATUS = "status"
        const val FIELD_IMAGES = "images"
        const val FIELD_CREATED_AT = "createdAt"
    }
}
